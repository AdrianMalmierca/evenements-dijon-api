package com.adrianmalmierca.dijonevents

import com.adrianmalmierca.dijonevents.dto.LoginRequest
import com.adrianmalmierca.dijonevents.dto.RegisterRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest //run the full springboot context, as it was real
@AutoConfigureMockMvc //to make http without run the server
@Testcontainers //docker containers in tests
class AuthControllerIntegrationTest {

    companion object {
        @Container //container managed by Testcontainers
        @ServiceConnection //spring boot detects this container and configure the connexion
        val postgres = PostgreSQLContainer("postgres:16-alpine") //runs postgresql in docker
    }

    @Autowired //for dependecy inyection
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `register should return token`() {
        val request = RegisterRequest(
            name = "Test User",
            email = "test@dijon.fr",
            password = "password123"
        )

        mockMvc.post("/api/auth/register") { //simulates http request
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request) //transform JSON to kotlin object
        }.andExpect {
            status { isOk() }
            jsonPath("$.token") { exists() }
            jsonPath("$.email") { value("test@dijon.fr") }
            jsonPath("$.name") { value("Test User") }
        }
    }

    @Test
    fun `login with wrong password should return 403`() {
        val request = LoginRequest(
            email = "noexiste@dijon.fr",
            password = "wrongpassword"
        )

        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isForbidden() }
        }
    }

    @Test
    fun `register then login should return valid token`() {
        val email = "login@dijon.fr"
        val password = "securepass"

        //Register
        mockMvc.post("/api/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                RegisterRequest("Login User", email, password)
            )
        }.andExpect { status { isOk() } }

        //Login
        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                LoginRequest(email, password)
            )
        }.andExpect {
            status { isOk() }
            jsonPath("$.token") { exists() }
        }
    }
}