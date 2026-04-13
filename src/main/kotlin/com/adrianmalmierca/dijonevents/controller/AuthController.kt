package com.adrianmalmierca.dijonevents.controller

import com.adrianmalmierca.dijonevents.dto.AuthResponse
import com.adrianmalmierca.dijonevents.dto.LoginRequest
import com.adrianmalmierca.dijonevents.dto.RegisterRequest
import com.adrianmalmierca.dijonevents.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController //to serialize to JSON
@RequestMapping("/api/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<AuthResponse> {
        return ResponseEntity.ok(authService.register(request))
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
        return ResponseEntity.ok(authService.login(request))
    }
}
