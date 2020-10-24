package com.starsky.api.security

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.starsky.models.User
import io.ktor.auth.*
import java.time.Duration
import java.util.*

object JwtConfig {

    private val secret: String = System.getenv("STARSKY_JWT_SECRET")
        ?: throw java.lang.IllegalStateException("STARSKY_JWT_SECRET environment variable is missing, API authentication will not work!")
    private const val issuer = "com.starsky"
    private val tokenDuration = Duration.ofDays(1)
    private val algorithm = Algorithm.HMAC512(secret)

    val verifier: JWTVerifier = JWT.require(algorithm)
        .withIssuer(issuer)
        .build()

    /**
     * Produce a token for this combination of name and password
     */
    fun generateToken(user: User): String = JWT.create()
        .withSubject("Authentication")
        .withIssuer(issuer)
        .withClaim("id", user.id.value)
        .withExpiresAt(getExpiration())
        .sign(algorithm)

    /**
     * Calculate the expiration Date based on current time + the given duration
     */
    private fun getExpiration() = Date(System.currentTimeMillis() + tokenDuration.toMillis())

}



