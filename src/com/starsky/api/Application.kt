package com.starsky.api

import com.starsky.api.routes.getAuthRoutes
import com.starsky.api.security.JwtConfig
import com.starsky.api.security.JwtUser
import com.starsky.api.security.UserPrincipal
import com.starsky.models.User
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.slf4j.event.Level

fun main(args: Array<String>): Unit = io.ktor.server.jetty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    install(Authentication) {
        jwt {
            verifier(JwtConfig.verifier)
            realm = "com.starsky"
            validate {
                val id = it.payload.getClaim("id").toString().toInt()
                UserPrincipal(id)
            }
        }
    }

    install(ContentNegotiation) {
        gson {
            setPrettyPrinting() //todo: set for development only
        }
    }

    getAuthRoutes()
    routing {

        authenticate{
            get("/"){
                call.respond("id = ${call.principal<UserPrincipal>()?.id}")
            }
        }
    }

}
