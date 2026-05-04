package com.adrianmalmierca.dijonevents.config

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

@Component
class RateLimitFilter : OncePerRequestFilter() {

    private val buckets = ConcurrentHashMap<String, Bucket>()

    private fun resolveBucket(ip: String): Bucket {
        return buckets.computeIfAbsent(ip) { //creates if it doesnt exist
            Bucket.builder()
                .addLimit(
                    Bandwidth.builder()
                        .capacity(60) //max 60 requests
                        .refillGreedy(60, Duration.ofMinutes(1)) //new 60 tokens every minute
                        .build()
                )
                .build()
        }
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val ip = request.remoteAddr //get the clients ip
        val bucket = resolveBucket(ip)

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response) //let pass the request
        } else {
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.writer.write("Too many requests — réessayez dans une minute.")
        }
    }
}