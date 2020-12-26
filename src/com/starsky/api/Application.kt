package com.starsky.api

import com.starsky.api.routes.getAuthRoutes
import com.starsky.api.routes.getInviteRoutes
import com.starsky.api.routes.getTeamRoutes
import com.starsky.api.routes.getUserRoutes
import com.starsky.api.security.JwtConfig
import com.starsky.api.security.UserPrincipal
import com.starsky.env.Environment
import com.starsky.env.EnvironmentVars
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.request.*
import org.slf4j.event.Level

fun main(args: Array<String>): Unit = io.ktor.server.jetty.EngineMain.main(args)

@Suppress("unused")
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    install(CallLogging) {
        level = if (EnvironmentVars.environment == Environment.DEV) Level.DEBUG else Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    install(CORS) {
        // these is the minimum required configuration for cross origin to work
        // NOTE: if you change these settings and restart backend - frontend (browser) must also be restarted,
        // since OPTIONS API call stays in cache (firefox 24h, chrome 2h), see https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Access-Control-Max-Age
        method(HttpMethod.Options)
        method(HttpMethod.Get)
        method(HttpMethod.Post)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
        method(HttpMethod.Patch)
        header(HttpHeaders.AccessControlAllowHeaders)
        header(HttpHeaders.ContentType)
        header(HttpHeaders.AccessControlAllowOrigin)
        header(HttpHeaders.Authorization)
        host(EnvironmentVars.frontendHost)
    }

    install(Authentication) {
        jwt {
            verifier(JwtConfig.verifier)
            realm = "com.starsky"
            validate {
                val id = it.payload.getClaim("id")?.asInt()
                val roleId = it.payload.getClaim("roleId")?.asInt()
                if (id == null || roleId == null) {
                    null
                } else {
                    UserPrincipal(id, roleId)
                }
            }
        }
    }

    install(ContentNegotiation) {
        gson {
            if (EnvironmentVars.environment == Environment.DEV){
                setPrettyPrinting()
            }

        }
    }
    getAuthRoutes()
    getUserRoutes()
    getTeamRoutes()
    getInviteRoutes()
}
