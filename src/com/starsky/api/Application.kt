package com.starsky.api

import com.starsky.api.routes.getAuthRoutes
import com.starsky.api.security.JwtConfig
import com.starsky.api.security.JwtUser
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.request.*
import org.slf4j.event.Level

fun main(args: Array<String>): Unit = io.ktor.server.jetty.EngineMain.main(args)

//val ApplicationCall.user get() = authentication.principal<UserJwt>()

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    install(Authentication) {
        jwt {
            //TODO: move to separate file and use bcrypt for comparing with db..
            verifier(JwtConfig.verifier)
            realm = "com.starsky"
            validate {
                val email = it.payload.getClaim("email").asString()
                val password = it.payload.getClaim("password").asString()
                if(email != null && password != null){
                    JwtUser(email, password )
                }else{
                    null
                }
            }
        }
    }

    install(ContentNegotiation) {
        gson {
            setPrettyPrinting() //todo: set for development only
        }
    }

    getAuthRoutes()
//    routing {
//        getRoutes()
//        contact()
//        get("/"){
//            call.respond(UserGateway().getById(1)?: "null")
//        }
//
//
//
//        authenticate{
//            get("/authenticate"){
//                call.respond("get authenticated value from token " +
//                        "email = ${call.user?.email}, password= ${call.user?.password}")
//            }
//        }
//    }

}
