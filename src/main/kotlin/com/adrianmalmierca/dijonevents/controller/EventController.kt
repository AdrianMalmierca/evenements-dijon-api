package com.adrianmalmierca.dijonevents.controller

import com.adrianmalmierca.dijonevents.dto.EventDto
import com.adrianmalmierca.dijonevents.dto.FavoriteRequest
import com.adrianmalmierca.dijonevents.service.EventService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/events")
class EventController(private val eventService: EventService) {

    @GetMapping
    fun getEvents(
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "0") from: Int,
        @RequestParam(required = false) keyword: String?
    ): ResponseEntity<List<EventDto>> {
        return ResponseEntity.ok(eventService.getEvents(size, from, keyword))
    }

    @GetMapping("/{uid}")
    fun getEventById(@PathVariable uid: String): ResponseEntity<EventDto> {
        val event = eventService.getEventById(uid)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(event)
    }

    @GetMapping("/favorites")
    fun getFavorites(@AuthenticationPrincipal user: UserDetails): ResponseEntity<List<EventDto>> {
        return ResponseEntity.ok(eventService.getFavorites(user.username))
    }

    @PostMapping("/favorites")
    fun addFavorite(
        @AuthenticationPrincipal user: UserDetails,
        @RequestBody request: FavoriteRequest
    ): ResponseEntity<Void> {
        eventService.addFavorite(user.username, request)
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/favorites/{eventUid}")
    fun removeFavorite(
        @AuthenticationPrincipal user: UserDetails,
        @PathVariable eventUid: String
    ): ResponseEntity<Void> {
        eventService.removeFavorite(user.username, eventUid)
        return ResponseEntity.noContent().build()
    }
}
