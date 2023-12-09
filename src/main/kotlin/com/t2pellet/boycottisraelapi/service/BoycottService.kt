package com.t2pellet.boycottisraelapi.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.t2pellet.boycottisraelapi.model.BoycottBarcode
import com.t2pellet.boycottisraelapi.model.BoycottEntry
import com.t2pellet.boycottisraelapi.model.BoycottName
import com.t2pellet.boycottisraelapi.model.BarcodeData
import me.xdrop.fuzzywuzzy.FuzzySearch
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec
import java.util.function.Function

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
        return parse(response, this::parseBoycottEntry)
    }

    // TODO : This is really inefficient
    @Cacheable("boycott")
    fun get(name: String): List<BoycottEntry> {
        val results = getAll()
        val names = results.map { it.name }
        val matches = FuzzySearch.extractSorted(name, names, { s1, s2 -> FuzzySearch.weightedRatio(s1, s2.take(s1.length)) }, 75).take(5)
        return matches.map { match -> results.find { it.name == match.string } as BoycottEntry }
    }

    @Cacheable("boycott")
    fun get(id: Number): BoycottEntry {
        val response = client.get().uri("boycotts/$id?populate=*").retrieve()
        val responseData = response.bodyToMono(String::class.java).block()
        val responseJson = mapper.reader().readTree(responseData).get("data")
        return parseBoycottEntry(responseJson)
    }


    @Cacheable("names")
    fun getNames(): List<BoycottName> {
        val response = client.get().uri("boycotts?populate=Subsidiary&fields[0]=name").retrieve()
        val subsidiaries: List<BoycottName> = flatParse(response, this::parseSubsidiaryNames)
        val names: List<BoycottName> = parse(response, this::parseName)
        return names + subsidiaries
    }

    // TODO : This is really inefficient
    @Cacheable("names")
    fun getNames(name: String): List<BoycottName> {
        val names = getNames()
        val namesStr = names.map { it.name }
        val matches = FuzzySearch.extractSorted(name, namesStr, { s1, s2 -> FuzzySearch.weightedRatio(s1, s2.take(s1.length)) }, 75).take(5)
        return matches.map { match -> names.find { it.name == match.string } as BoycottName }
    }

    @Cacheable("name")
    fun getName(id: Number): BoycottName {
        val entry = get(id)
        return BoycottName(entry.id, entry.name)
    }

    @Cacheable("barcode")
    fun getForBarcode(barcode: BarcodeData): BoycottBarcode {
        val entries = getNames()
        val namesStr = entries.map { it.name }
        val match = FuzzySearch.extractOne(barcode.company, namesStr) { s1, s2 ->
            FuzzySearch.ratio(
                s1,
                s2.take(s1.length)
            )
        }
        if (match.score >= 90) {
            val idx = namesStr.indexOf(match.string)
            val entry = entries[idx]
            val product = get(entry.id)
            return BoycottBarcode(barcode.product, product.name, true, product.reason)
        }
        return BoycottBarcode(barcode.product, barcode.company, false)
    }

    private fun <T> parse(response: ResponseSpec, parseFn: Function<JsonNode, T>): List<T> {
        val responseData = response.bodyToMono(String::class.java).block()
        val responseJson = mapper.reader().readTree(responseData)
        val data = responseJson.get("data").toList()
        return data.map { parseFn.apply(it) }
    }

    private fun <T> flatParse(response: ResponseSpec, parseFn: Function<JsonNode, List<T>>): List<T> {
        val responseData = response.bodyToMono(String::class.java).block()
        val responseJson = mapper.reader().readTree(responseData)
        val data: List<JsonNode> = responseJson.get("data").toList()
        return data.flatMap { parseFn.apply(it) }
    }

    private fun parseBoycottEntry(it: JsonNode): BoycottEntry {
        val attributes = it.get("attributes") as ObjectNode
        val logo = attributes.get("logo").get("data").get("attributes").get("url")
        attributes.set<JsonNode>("logo", logo)
        attributes.set<JsonNode>("id", it.get("id"))
        attributes.remove("createdAt")
        attributes.remove("updatedAt")
        attributes.remove("publishedAt")
        attributes.remove("Subsidiary")
        return mapper.treeToValue(attributes, BoycottEntry::class.java)
    }

    private fun parseName(it: JsonNode): BoycottName {
        val attributes = it.get("attributes")
        val node = mapper.createObjectNode()
        node.set<JsonNode>("id", it.get("id"))
        node.set<JsonNode>("name", attributes.get("name"))
        return mapper.treeToValue(node, BoycottName::class.java)
    }

    private fun parseSubsidiaryNames(it: JsonNode): List<BoycottName> {
        val id = it.get("id")
        val subsidiaries: JsonNode = it.get("attributes").get("Subsidiary")
        return subsidiaries.map {
            BoycottName(id.asInt(), it.get("name").asText())
        }
    }

}