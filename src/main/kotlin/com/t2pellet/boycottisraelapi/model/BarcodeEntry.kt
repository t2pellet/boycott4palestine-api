package com.t2pellet.boycottisraelapi.model

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@Table(name = "barcodes")
data class BarcodeEntry (
    @Id
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
)