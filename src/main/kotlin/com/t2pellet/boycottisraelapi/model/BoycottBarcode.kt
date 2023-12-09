package com.t2pellet.boycottisraelapi.model

data class BoycottBarcode(
    val product: String,
    val company: String,
    val boycott: Boolean,
    val reason: String? = null,
    val logo: String? = null
)