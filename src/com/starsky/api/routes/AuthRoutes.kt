package com.starsky.api.routes

import com.starsky.api.security.JwtConfig
import com.starsky.api.security.JwtUser
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Application.getAuthRoutes(){
    routing {
        getTokenRoute()
    }
}

fun Route.getTokenRoute() {
    post("/auth/token"){
        val user = call.receive<JwtUser>()
        val token = JwtConfig.generateToken(user)
        call.respond(token)
    }
}