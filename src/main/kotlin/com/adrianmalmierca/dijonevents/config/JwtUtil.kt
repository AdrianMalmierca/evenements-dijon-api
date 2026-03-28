package com.adrianmalmierca.dijonevents.config

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtUtil {

    @Value("\${jwt.secret}")
    private lateinit var secret: String

    @Value("\${jwt.expiration}")
    private var expiration: Long = 0

    private val key: SecretKey by lazy {
        //transform the secret string into a SecretKey object
        Keys.hmacShaKeyFor(secret.toByteArray())
    }

    fun generateToken(email: String): String {
        return Jwts.builder()
            .subject(email) //id of the token
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + expiration))
            .signWith(key)
            .compact() //transform into a string
    }

    fun extractEmail(token: String): String {
        return extractClaims(token).subject
    }

    fun isTokenValid(token: String): Boolean {
        return try {
            extractClaims(token).expiration.after(Date())
        } catch (e: Exception) {
            false
        }
    }

    private fun extractClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token) //parse the token and verify the signature
            .payload //get the claims (payload) of the token
    }
}
