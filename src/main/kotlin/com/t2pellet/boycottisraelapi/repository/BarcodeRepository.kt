package com.t2pellet.boycottisraelapi.repository
import com.t2pellet.boycottisraelapi.model.BarcodeEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BarcodeRepository: JpaRepository<BarcodeEntity, String> {
}