package com.t2pellet.boycottisraelapi.model

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "barcodes")
data class BarcodeEntity(
    @Id
    val barcode: String,
    val product: String,
    val company: String?,
    val strapiId: Int,
) {
    constructor(barcodeData: BarcodeData, strapiId: Int): this(
        barcodeData.barcode,
        barcodeData.product,
        barcodeData.company,
        strapiId
    )
}
