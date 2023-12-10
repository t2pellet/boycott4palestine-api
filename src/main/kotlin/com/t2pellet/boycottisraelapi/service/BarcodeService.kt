package com.t2pellet.boycottisraelapi.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.t2pellet.boycottisraelapi.repository.BarcodeRepository
import com.t2pellet.boycottisraelapi.model.BarcodeData
import com.t2pellet.boycottisraelapi.model.BarcodeEntity
import com.t2pellet.boycottisraelapi.model.BoycottBarcode
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.util.*

@Service
@CacheConfig(cacheNames = ["barcodeServiceCache"])
class BarcodeService(
  val barcodeRepository: BarcodeRepository
) {

    val client = WebClient.builder()
        .baseUrl("https://api.upcdatabase.org")
        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer ${System.getenv("BARCODE_KEY")}")
        .build()
    val mapper: ObjectMapper = ObjectMapper()

    @Cacheable("barcode")
    fun getBarcodeData(barcode: String): BarcodeData? {
        // First try with DB
        val dbResult: Optional<BarcodeEntity> = barcodeRepository.findById(barcode)
        if (dbResult.isPresent) {
            val dbData = dbResult.get()
            return BarcodeData(dbData)
        }
        // If not in DB we fetch from API
        val response = client.get().uri("/product/${barcode}")
            .exchangeToMono {
                if (it.statusCode() != HttpStatus.OK) {
                    return@exchangeToMono Mono.empty()
                } else {
                    return@exchangeToMono it.bodyToMono(String::class.java)
                }
            }
            .block()
        if (response != null) {
            val jsonData = mapper.reader().readTree(response)
            if (jsonData.get("success").asBoolean()) {
                return mapper.convertValue(jsonData, BarcodeData::class.java)
            }
        }
        return null
    }

    fun saveBarcode(barcodeEntity: BarcodeEntity) {
        barcodeRepository.saveAndFlush(barcodeEntity)
    }

    fun saveBarcode(barcodeData: BarcodeData, strapiId: Int) {
        saveBarcode(BarcodeEntity(barcodeData, strapiId))
    }

    fun saveBarcode(barcode: String, barcodeData: BoycottBarcode) {
        saveBarcode(BarcodeEntity(
            barcode,
            barcodeData.product,
            barcodeData.company,
            barcodeData.id,
        ))
    }

}