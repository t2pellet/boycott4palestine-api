package com.t2pellet.boycottisraelapi.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.t2pellet.boycottisraelapi.model.BoycottEntry
import com.t2pellet.boycottisraelapi.model.BoycottName
import io.netty.handler.codec.json.JsonObjectDecoder
import me.xdrop.fuzzywuzzy.FuzzySearch
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec
import java.util.*
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
        val response = client.get().uri("boycotts?fields[0]=name").retrieve()
        return parse(response, this::parseName)
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

    private fun <T> parse(response: ResponseSpec, parseFn: Function<JsonNode, T>): List<T> {
        val responseData = response.bodyToMono(String::class.java).block()
        val responseJson = mapper.reader().readTree(responseData)
        val data = responseJson.get("data").toList()
        return data.map { parseFn.apply(it) }
    }

    private fun parseBoycottEntry(it: JsonNode): BoycottEntry {
        val attributes = it.get("attributes") as ObjectNode
        val logo = attributes.get("logo").get("data").get("attributes").get("url")
        attributes.set<JsonNode>("logo", logo)
        attributes.set<JsonNode>("id", it.get("id"))
        attributes.remove("createdAt")
        attributes.remove("updatedAt")
        attributes.remove("publishedAt")
        return mapper.treeToValue(attributes, BoycottEntry::class.java)
    }

    private fun parseName(it: JsonNode): BoycottName {
        val attributes = it.get("attributes")
        val node = mapper.createObjectNode()
        node.set<JsonNode>("id", it.get("id"))
        node.set<JsonNode>("name", attributes.get("name"))
        return mapper.treeToValue(node, BoycottName::class.java)
    }
}