package com.t2pellet.boycottisraelapi.controller

import com.t2pellet.boycottisraelapi.model.NameEntry
import com.t2pellet.boycottisraelapi.service.BoycottService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/api/names")
class NamesController(val boycottService: BoycottService) {

    @GetMapping("")
    fun get(@RequestParam name: Optional<String>): List<NameEntry> {
        return if (name.isPresent) {
            boycottService.getNames(name.get())
        } else boycottService.getNames()
    }

    @GetMapping("{id}")
    fun getNames(@PathVariable id: Number): NameEntry {
        return boycottService.getName(id)
    }
}