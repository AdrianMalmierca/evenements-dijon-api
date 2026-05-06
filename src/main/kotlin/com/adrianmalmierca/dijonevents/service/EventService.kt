package com.adrianmalmierca.dijonevents.service

import com.adrianmalmierca.dijonevents.client.OpenAgendaClient
import com.adrianmalmierca.dijonevents.dto.EventDto
import com.adrianmalmierca.dijonevents.dto.FavoriteRequest
import com.adrianmalmierca.dijonevents.dto.PagedEventsResponse
import com.adrianmalmierca.dijonevents.model.FavoriteEvent
import com.adrianmalmierca.dijonevents.repository.FavoriteEventRepository
import com.adrianmalmierca.dijonevents.repository.UserRepository
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.CacheEvict

@Service
class EventService(
    private val openAgendaClient: OpenAgendaClient,
    private val userRepository: UserRepository,
    private val favoriteEventRepository: FavoriteEventRepository,
    private val notificationService: NotificationService
) {

    //value is the caché name, key is the unique identifier for the cache entry, for example, 0-20-null, 1-20-music...
    @Cacheable(value = ["events"], key = "#page + '-' + #size + '-' + (#keyword ?: 'null')")
    fun getEvents(page: Int = 0, size: Int = 20, keyword: String? = null): PagedEventsResponse {
        val from = page * size
        val (events, total) = openAgendaClient.getEvents(size, from, keyword)
        return PagedEventsResponse(
            events = events,
            total = total,
            page = page,
            size = size,
            hasMore = from + events.size < total
        )
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

        notificationService.sendNotificationToUser(
            email = email,
            title = "Favori ajouté 🍷",
            body = "«${request.title}» a été ajouté à vos favoris"
        )

        favoriteEventRepository.save(favorite)
        user.favorites.add(favorite)
        userRepository.save(user)
    }

    fun removeFavorite(email: String, eventUid: String) {
        val user = userRepository.findByEmail(email)
            .orElseThrow { UsernameNotFoundException("User not found") }

        user.favorites.removeIf { it.openAgendaUid == eventUid }
        userRepository.save(user)
    }
}
