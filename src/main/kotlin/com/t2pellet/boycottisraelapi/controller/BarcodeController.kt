package com.t2pellet.boycottisraelapi.controller

import com.t2pellet.boycottisraelapi.model.BoycottBarcode
import com.t2pellet.boycottisraelapi.service.BarcodeService
import com.t2pellet.boycottisraelapi.service.BoycottService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/barcode")
class BarcodeController(
    val barcodeService: BarcodeService,
    val boycottService: BoycottService
) {

    @GetMapping("/{barcode}")
    fun getBarcode(@PathVariable barcode: String): BoycottBarcode {
        val barcodeData = barcodeService.getBarcodeData(barcode)
        if (barcodeData != null) {
            return boycottService.getForBarcode(barcodeData)
        }
        throw ResponseStatusException(HttpStatus.NOT_FOUND, "Barcode not found")
    }
}