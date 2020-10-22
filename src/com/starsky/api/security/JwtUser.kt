package com.starsky.api.security

import io.ktor.auth.*

data class JwtUser(val email: String, val password: String): Principal