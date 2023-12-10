package com.t2pellet.boycottisraelapi.model

data class BoycottBarcode(
    val product: String,
    val company: String,
    val boycott: Boolean,
    val reason: String? = null,
    val logo: String? = null,
    val proof: String? = null,
    val id: Int? = null,
) {
    constructor(barcode: BarcodeEntry, parent: BoycottEntry): this(
        barcode.product,
        parent.name,
        true,
        parent.reason,
        parent.logo,
        parent.proof,
        parent.id
    )
}