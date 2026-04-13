package com.adrianmalmierca.dijonevents.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter


@Component //so spring creates the object, is allowed dependency injection
class JwtAuthFilter(
    private val jwtUtil: JwtUtil,
    private val userDetailsService: UserDetailsService
) : OncePerRequestFilter() { //is executed once per request

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            //If there's no token or it doesn't start with "Bearer ",
            //just continue the filter chain without setting authentication
            filterChain.doFilter(request, response)
            return
        }

        //Remove "Bearer " prefix to get the actual token
        val token = authHeader.substring(7)

        if (jwtUtil.isTokenValid(token)) {
            val email = jwtUtil.extractEmail(token)
            val userDetails = userDetailsService.loadUserByUsername(email)
            val auth = UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.authorities
            ) //Create an authentication object with the user details and authorities

            //Set additional details from the request (like IP address)
            auth.details = WebAuthenticationDetailsSource().buildDetails(request)

            //Set the authentication in the security context, so that Spring Security knows the user is authenticated
            SecurityContextHolder.getContext().authentication = auth
        }

        //Continue the filter chain, whether the token was valid or not.
        //If it was valid, the user will be authenticated in the context
        filterChain.doFilter(request, response)
    }
}
