package com.t2pellet.boycottisraelapi.service

import com.t2pellet.boycottisraelapi.model.BoycottEntry
import com.t2pellet.boycottisraelapi.repository.BoycottRepo
import me.xdrop.fuzzywuzzy.FuzzySearch
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
@CacheConfig(cacheNames = ["boycottServiceCache"])
class BoycottService(private val boycottRepo: BoycottRepo) {

    @Cacheable("boycotts")
    fun getAll(): List<BoycottEntry> {
        return boycottRepo.findAll()
    }

    @Cacheable("boycott")
    fun get(name: String): List<BoycottEntry> {
        val names = getNames()
        val matches = FuzzySearch.extractSorted(name, names, { s1, s2 -> FuzzySearch.weightedRatio(s1, s2.take(s1.length)) }, 75).take(5)
        return boycottRepo.findAllById(matches.map{ it.string })
    }


    @Cacheable("names")
    fun getNames(): List<String> {
        return getAll().map { it.name }
    }

    @Cacheable("name")
    fun getNames(name: String): List<String> {
        val names = getNames()
        val matches = FuzzySearch.extractSorted(name, names, { s1, s2 -> FuzzySearch.weightedRatio(s1, s2.take(s1.length)) }, 75).take(5)
        return matches.map { it.string }
    }
}