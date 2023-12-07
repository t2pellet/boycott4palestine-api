package com.t2pellet.boycottisraelapi.model

import com.fasterxml.jackson.annotation.JsonProperty

data class BoycottName(
    @JsonProperty("id")
    val id: Number,
    @JsonProperty("name")
    val name: String
)