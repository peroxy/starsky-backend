package com.starsky.api.routes

import com.starsky.api.models.GetTokenModel
import com.starsky.api.responses.TokenResponse
import com.starsky.api.security.JwtConfig
import com.starsky.api.validations.UserValidation
import com.starsky.database.gateways.UserGateway
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import java.time.Duration

fun Application.getAuthRoutes() {
    routing {
        getTokenRoute()
    }
}

private fun Route.getTokenRoute() {
    post("/auth/token") {
        val user = call.receive<GetTokenModel>()
        var error = UserValidation.validateCredentials(user.email, user.password)
        if (error != null) {
            call.respond(error.errorCode, error)
        } else {
            val userDto = UserGateway.getByEmail(user.email)
            error = UserValidation.tryAuthenticate(userDto, user.password)
            if (error != null) {
                call.respond(error.errorCode, error)
            } else {
                val token = JwtConfig.generateToken(userDto!!)
                call.respond(HttpStatusCode.OK, TokenResponse(token, "bearer", Duration.ofDays(1).seconds))
            }
        }
    }

    authenticate {
        get("/auth/token/validate") {
            call.respond(HttpStatusCode.OK);
        }
    }


}

