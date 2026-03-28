package com.adrianmalmierca.dijonevents.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

//Auth
data class RegisterRequest(
    @field:NotBlank val name: String,
    @field:Email @field:NotBlank val email: String,
    @field:Size(min = 6) val password: String
)

data class LoginRequest(
    @field:Email @field:NotBlank val email: String,
    @field:NotBlank val password: String
)

data class AuthResponse(
    val token: String,
    val email: String,
    val name: String
)

//Events (response structure of OpenAgenda)
data class OpenAgendaResponse(
    val total: Int,
    val events: List<OpenAgendaEvent>
)

//What we receive from the API. Extern model
data class OpenAgendaEvent(
    val uid: Long,
    val title: Map<String, String>,
    val description: Map<String, String>?,
    val image: OpenAgendaImage?,
    val location: OpenAgendaLocation?,
    val firstTiming: OpenAgendaTiming?,
    val lastTiming: OpenAgendaTiming?,
    val keywords: Map<String, List<String>>?
)

data class OpenAgendaImage(
    val base: String?
)

data class OpenAgendaLocation(
    val name: String?,
    val address: String?,
    val city: String?,
    val latitude: Double?,
    val longitude: Double?
)

data class OpenAgendaTiming(
    val begin: String?,
    val end: String?
)

//Simple event for the app. Intern model
data class EventDto(
    val uid: String,
    val title: String,
    val description: String?,
    val imageUrl: String?,
    val locationName: String?,
    val address: String?,
    val city: String?,
    val latitude: Double?,
    val longitude: Double?,
    val dateStart: String?,
    val dateEnd: String?,
    val categories: List<String>
)

//Favourites
data class FavoriteRequest(
    val uid: String,
    val title: String,
    val imageUrl: String?,
    val dateStart: String?,
    val latitude: Double?,
    val longitude: Double?
)
