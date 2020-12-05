package com.starsky.api.security

import io.ktor.auth.*

data class UserPrincipal(val id: Int, val roleId: Int) : Principal