package com.adrianmalmierca.dijonevents.service

import com.adrianmalmierca.dijonevents.config.JwtUtil
import com.adrianmalmierca.dijonevents.dto.AuthResponse
import com.adrianmalmierca.dijonevents.dto.LoginRequest
import com.adrianmalmierca.dijonevents.dto.RegisterRequest
import com.adrianmalmierca.dijonevents.model.User
import com.adrianmalmierca.dijonevents.repository.UserRepository
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil
){

    fun register(request: RegisterRequest): AuthResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("The email is already registered")
        }

        val user = User(
            name = request.name,
            email = request.email,
            password = passwordEncoder.encode(request.password)
        )

        userRepository.save(user)

        return AuthResponse(
            token = jwtUtil.generateToken(user.email),
            email = user.email,
            name = user.name
        )
    }

    fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findByEmail(request.email)
            .orElseThrow { BadCredentialsException("Wrong credentials") }

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw BadCredentialsException("Wrong credentials")
        }

        return AuthResponse(
            token = jwtUtil.generateToken(user.email),
            email = user.email,
            name = user.name
        )
    }
}
