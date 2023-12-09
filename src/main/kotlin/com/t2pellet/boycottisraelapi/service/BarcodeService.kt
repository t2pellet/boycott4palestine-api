package com.t2pellet.boycottisraelapi.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.t2pellet.boycottisraelapi.model.BarcodeData
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
@CacheConfig(cacheNames = ["barcodeServiceCache"])
class BarcodeService {

    val client = WebClient.builder()
        .baseUrl("https://api.upcdatabase.org")
        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer ${System.getenv("BARCODE_KEY")}")
        .build()
    val mapper: ObjectMapper = ObjectMapper()

    @Cacheable("barcode")
    fun getBarcodeData(barcode: String): BarcodeData? {
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
            return mapper.convertValue(jsonData, BarcodeData::class.java)
        }
        return null
    }

}