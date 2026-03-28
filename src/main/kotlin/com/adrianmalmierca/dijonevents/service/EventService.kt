package com.adrianmalmierca.dijonevents.service

import com.adrianmalmierca.dijonevents.client.OpenAgendaClient
import com.adrianmalmierca.dijonevents.dto.EventDto
import com.adrianmalmierca.dijonevents.dto.FavoriteRequest
import com.adrianmalmierca.dijonevents.model.FavoriteEvent
import com.adrianmalmierca.dijonevents.repository.FavoriteEventRepository
import com.adrianmalmierca.dijonevents.repository.UserRepository
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class EventService(
    private val openAgendaClient: OpenAgendaClient,
    private val userRepository: UserRepository,
    private val favoriteEventRepository: FavoriteEventRepository
) {

    fun getEvents(size: Int = 20, from: Int = 0, keyword: String? = null): List<EventDto> {
        return openAgendaClient.getEvents(size, from, keyword)
    }

    fun getEventById(uid: String): EventDto? {
        return openAgendaClient.getEventById(uid)
    }

    fun getFavorites(email: String): List<EventDto> {
        val user = userRepository.findByEmail(email)
            .orElseThrow { UsernameNotFoundException("User not found") }

        return user.favorites.map { fav ->
            EventDto(
                uid = fav.openAgendaUid,
                title = fav.title,
                description = null,
                imageUrl = fav.imageUrl,
                locationName = null,
                address = null,
                city = null,
                latitude = fav.latitude,
                longitude = fav.longitude,
                dateStart = fav.dateStart,
                dateEnd = null,
                categories = emptyList()
            )
        }
    }

    fun addFavorite(email: String, request: FavoriteRequest) {
        val user = userRepository.findByEmail(email)
            .orElseThrow { UsernameNotFoundException("User not found") }

        val favorite = favoriteEventRepository.findById(request.uid).orElse(
            FavoriteEvent(
                openAgendaUid = request.uid,
                title = request.title,
                imageUrl = request.imageUrl,
                dateStart = request.dateStart,
                latitude = request.latitude,
                longitude = request.longitude
            )
        )

        favoriteEventRepository.save(favorite)
        user.favorites.add(favorite)
        userRepository.save(user)
    }

    fun removeFavorite(email: String, eventUid: String) {
        val user = userRepository.findByEmail(email)
            .orElseThrow { UsernameNotFoundException("Usuario no encontrado") }

        user.favorites.removeIf { it.openAgendaUid == eventUid }
        userRepository.save(user)
    }
}
