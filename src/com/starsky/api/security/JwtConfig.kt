package com.starsky.api.security

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.starsky.env.EnvironmentVars
import com.starsky.models.User
import java.time.Duration
import java.util.*

object JwtConfig {


    private const val issuer = "com.starsky"
    private val tokenDuration = Duration.ofDays(1)
    private val algorithm = Algorithm.HMAC512(EnvironmentVars.jwtSecret)

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
        .withClaim("roleId", user.userRole.id.value)
        .withIssuedAt(Date(System.currentTimeMillis()))
        .withExpiresAt(getExpiration())
        .sign(algorithm)

    /**
     * Calculate the expiration Date based on current time + the given duration
     */
    private fun getExpiration() = Date(System.currentTimeMillis() + tokenDuration.toMillis())

}



