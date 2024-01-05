package com.t2pellet.boycottisraelapi.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.t2pellet.boycottisraelapi.model.BarcodeEntry
import com.t2pellet.boycottisraelapi.model.BoycottBarcode
import com.t2pellet.boycottisraelapi.repository.BarcodeRepository
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.util.*

@Service
class BarcodeService(
  val barcodeRepository: BarcodeRepository
) {

    val client = WebClient.builder()
        .baseUrl("https://api.upcdatabase.org")
        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer ${System.getenv("BARCODE_KEY")}")
        .build()
    val mapper: ObjectMapper = ObjectMapper()

    fun getBarcodeEntry(barcode: String): BarcodeEntry? {
        // First try with DB
        val dbResult: Optional<BarcodeEntry> = barcodeRepository.findById(barcode)
        if (dbResult.isPresent) {
            return dbResult.get()
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
        if (!response.isNullOrEmpty()) {
            val jsonData = mapper.reader().readTree(response)
            if (jsonData.get("success").asBoolean()) {
                return mapper.convertValue(jsonData, BarcodeEntry::class.java)
            }
        }
        return null
    }

    fun isCachedBarcode(barcode: String): Boolean {
        return barcodeRepository.existsById(barcode)
    }

    fun saveBarcode(barcodeEntity: BarcodeEntry) {
        if (!barcodeRepository.existsById(barcodeEntity.barcode)) {
            barcodeRepository.saveAndFlush(barcodeEntity)
        }
    }

    fun saveBarcode(barcode: String, barcodeData: BoycottBarcode) {
        saveBarcode(BarcodeEntry(
            barcode,
            barcodeData.company,
            barcodeData.product,
            barcodeData.id,
        ))
    }

    fun saveBarcodeCompany(barcode: String, company: String, strapiId: Int? = null): BarcodeEntry? {
        val entry = barcodeRepository.findById(barcode)
        if (entry.isPresent) {
            val data = entry.get()
            if (data.company.isEmpty()) {
                val newEntry = BarcodeEntry(data.barcode, company, data.product, strapiId)
                barcodeRepository.saveAndFlush(newEntry)
                return newEntry
            }
        }
        return null
    }
}