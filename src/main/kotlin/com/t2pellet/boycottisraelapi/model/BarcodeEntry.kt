package com.t2pellet.boycottisraelapi.model

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table(name = "barcodes")
data class BarcodeEntry (
    @Id
    @JsonProperty("barcode")
    val barcode: String,
    @JsonProperty("company")
    @JsonAlias("brand")
    val company: String,
    @JsonProperty("product")
    @JsonAlias("title")
    val product: String,
    @JsonIgnore
    @JsonProperty("strapiId")
    val strapiId: Int? = null
)