package com.t2pellet.boycottisraelapi.controller

import com.t2pellet.boycottisraelapi.model.BoycottBarcode
import com.t2pellet.boycottisraelapi.service.BarcodeService
import com.t2pellet.boycottisraelapi.service.BoycottService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import com.t2pellet.boycottisraelapi.model.BarcodeData
import com.t2pellet.boycottisraelapi.model.BarcodeEntry
import com.t2pellet.boycottisraelapi.model.BoycottEntry

@RestController
@RequestMapping("/api/barcode")
class BarcodeController(
    val barcodeService: BarcodeService,
    val boycottService: BoycottService
) {

    @GetMapping("/{barcode}")
    fun getBarcode(@PathVariable barcode: String): BoycottBarcode {
        val barcodeData = barcodeService.getBarcodeEntry(barcode)
        if (barcodeData != null) {
            val barcodeResult = boycottService.getForBarcode(barcodeData)
            barcodeService.saveBarcode(barcodeData.barcode, barcodeResult)
            return barcodeResult
        }
        throw ResponseStatusException(HttpStatus.NOT_FOUND, "Barcode not found")
    }

    @PostMapping("/")
    fun addBarcode(@RequestBody barcode: BarcodeData) {
        val match: BoycottEntry? = boycottService.getBest(barcode.company) ?: boycottService.getBest(barcode.product)
        if (match != null) {
            barcodeService.saveBarcode(BarcodeEntry(barcode.barcode, barcode.company, barcode.product, match.id))
        } else {
            barcodeService.saveBarcode(BarcodeEntry(barcode.barcode, barcode.company, barcode.product))
        }
    }
}