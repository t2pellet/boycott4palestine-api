package com.t2pellet.boycottisraelapi.model

import jakarta.validation.constraints.NotEmpty

data class BarcodeData(
    @NotEmpty
    val barcode: String,
    @NotEmpty
    val product: String,
    @NotEmpty
    val company: String
)