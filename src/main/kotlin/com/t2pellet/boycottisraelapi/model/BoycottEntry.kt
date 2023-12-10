package com.t2pellet.boycottisraelapi.model

import com.fasterxml.jackson.annotation.JsonProperty

data class BoycottEntry(
    @JsonProperty("id")
    val id: Int,
    @JsonProperty("name")
    val name: String,
    @JsonProperty("description")
    val description: String?,
    @JsonProperty("reason")
    val reason: String,
    @JsonProperty("proof")
    val proof: String,
    @JsonProperty("logo")
    val logo: String,
)