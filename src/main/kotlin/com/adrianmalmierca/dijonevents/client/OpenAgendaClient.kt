package com.adrianmalmierca.dijonevents.client

import com.adrianmalmierca.dijonevents.dto.EventDto
import com.adrianmalmierca.dijonevents.dto.OpenAgendaResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component //to crate an instance (bean), ready for dependency injection
class OpenAgendaClient(
    private val openAgendaWebClient: WebClient
) {

    //to inject properties
   @Value("\${openagenda.api-key}")
    private lateinit var apiKey: String

    @Value("\${openagenda.dijon-uid}")
    private lateinit var dijonUid: String

    fun getEvents(size: Int = 20, from: Int = 0, keyword: String? = null): List<EventDto> {
        val response = openAgendaWebClient.get()
            .uri { builder ->
                builder
                    .path("/agendas/{uid}/events")
                    .queryParam("key", apiKey)
                    .queryParam("size", size)
                    .queryParam("from", from)
                    //optional
                    .apply { keyword?.let { queryParam("search", it) } }
                    .build(dijonUid)
            }
            .retrieve() //execute request
            .bodyToMono(OpenAgendaResponse::class.java) //transform JSON into OpenAgendaResponse
            .block() ?: return emptyList() //wait until have response, else, return emptyList

        return response.events.map { event ->
            EventDto(
                uid = event.uid.toString(),
                title = event.title["fr"] ?: event.title.values.firstOrNull() ?: "Sans titre",
                description = event.description?.get("fr"),
                imageUrl = event.image?.filename?.let {
                    "https://cdn.openagenda.com/main/$it"
                },
                locationName = event.location?.name,
                address = event.location?.address,
                city = event.location?.city,
                latitude = event.location?.latitude,
                longitude = event.location?.longitude,
                dateStart = event.firstTiming?.begin,
                dateEnd = event.lastTiming?.end,
                categories = event.keywords?.get("fr") ?: emptyList()
            )
        }
    }

    fun getEventById(eventUid: String): EventDto? {
        return getEvents(size = 100).find { it.uid == eventUid }
    }
}
