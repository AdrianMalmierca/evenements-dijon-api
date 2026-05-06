package com.adrianmalmierca.dijonevents

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import com.adrianmalmierca.dijonevents.client.OpenAgendaClient
import com.adrianmalmierca.dijonevents.dto.EventDto
import org.mockito.Mockito.`when`
import org.springframework.boot.test.mock.mockito.MockBean

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class EventControllerIntegrationTest {

    companion object {
        @Container
        @ServiceConnection
        val postgres = PostgreSQLContainer("postgres:16-alpine")
    }

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var openAgendaClient: OpenAgendaClient

    private fun mockEvent() = EventDto(
        uid = "12345",
        title = "Concert Test à Dijon",
        description = "Un super concert",
        imageUrl = null,
        locationName = "La Vapeur",
        address = "42 avenue de Stalingrad 21000 Dijon",
        city = "Dijon",
        latitude = 47.345685,
        longitude = 5.059641,
        dateStart = "2026-06-01T20:00:00.000+02:00",
        dateEnd = "2026-06-01T23:59:00.000+02:00",
        categories = listOf("Concert", "Dijon")
    )

    @Test
    fun `get events should return list without auth`() {
        `when`(openAgendaClient.getEvents(20, 0, null))
            .thenReturn(Pair(listOf(mockEvent()), 1))

        mockMvc.get("/api/events").andExpect {
            status { isOk() }
            jsonPath("$.events") { isArray() }
            jsonPath("$.events[0].title") { value("Concert Test à Dijon") }
            jsonPath("$.total") { value(1) }
        }
    }

    @Test
    fun `get favorites without auth should return 403`() {
        mockMvc.get("/api/events/favorites").andExpect {
            status { isForbidden() }
        }
    }

    @Test
    fun `get events with keyword should call client with keyword`() {
        `when`(openAgendaClient.getEvents(20, 0, "jazz"))
            .thenReturn(Pair(emptyList(), 0)) //no results for jazz

        mockMvc.get("/api/events?keyword=jazz").andExpect {
            status { isOk() }
            jsonPath("$.events") { isArray() }
        }
    }
}