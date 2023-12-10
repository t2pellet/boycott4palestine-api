package com.t2pellet.boycottisraelapi.model

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class BarcodeData (
    @JsonProperty("barcode")
    val barcode: String,
    @JsonProperty("brand")
    @JsonAlias("company")
    val company: String,
    @JsonProperty("title")
    @JsonAlias("product")
    val product: String,
    @JsonIgnore
    @JsonProperty("strapiId")
    val strapiId: Int? = null
) {

    constructor(entity: BarcodeEntity) : this(entity.barcode, entity.company ?: "", entity.product)
}