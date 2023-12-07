package com.t2pellet.boycottisraelapi.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.t2pellet.boycottisraelapi.model.BoycottEntry
import io.netty.handler.codec.json.JsonObjectDecoder
import me.xdrop.fuzzywuzzy.FuzzySearch
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec
import java.util.*

@Service
@CacheConfig(cacheNames = ["boycottServiceCache"])
class BoycottService {

    val client: WebClient = WebClient.builder()
        .baseUrl("https://strapi-production-0fb3.up.railway.app/api/")
        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + System.getenv("STRAPI_TOKEN"))
        .build()
    val mapper: ObjectMapper = ObjectMapper()

    @Cacheable("boycotts")
    fun getAll(): List<BoycottEntry> {
        val response = client.get().uri("boycotts?populate=*").retrieve()
        return responseToBoycotts(response)
    }

    // TODO : This is really inefficient
    @Cacheable("boycott")
    fun get(name: String): List<BoycottEntry> {
        val results = getAll()
        val names = results.map { it.name }
        val matches = FuzzySearch.extractSorted(name, names, { s1, s2 -> FuzzySearch.weightedRatio(s1, s2.take(s1.length)) }, 75).take(5)
        return matches.map { match -> results.find { it.name == match.string } as BoycottEntry }
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

    private fun responseToBoycotts(response: ResponseSpec): List<BoycottEntry> {
        val responseData = response.bodyToMono(String::class.java).block()
        val responseJson = mapper.reader().readTree(responseData)
        val data = responseJson.get("data").toList()
        return data.map { parseBoycottEntry(it) }
    }

    private fun parseBoycottEntry(it: JsonNode): BoycottEntry {
        val attributes = it.get("attributes") as ObjectNode
        val logo = attributes.get("logo").get("data").get("attributes").get("url")
        attributes.set<JsonNode>("logo", logo)
        attributes.remove("createdAt")
        attributes.remove("updatedAt")
        attributes.remove("publishedAt")
        return mapper.treeToValue(attributes, BoycottEntry::class.java)
    }
}