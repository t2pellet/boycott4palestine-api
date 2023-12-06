package com.t2pellet.boycottisraelapi.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "boycott")
data class BoycottEntry(
    @Id
    @Column
    val name: String,
    @Column
    val reason: String,
    @Column
    val description: String,
    @Column
    val proof: String,
    @Column
    val how: String,
    @Column
    val logo: String,
    @Column
    val alternatives: String
)