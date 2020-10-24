package com.starsky.api.routes

import com.google.gson.annotations.SerializedName
import com.starsky.api.security.JwtConfig
import com.starsky.api.security.JwtUser
import com.starsky.database.gateways.UserGateway
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.mindrot.jbcrypt.BCrypt
import java.time.Duration

fun Application.getAuthRoutes() {
    routing {
        getTokenRoute()
    }
}

fun Route.getTokenRoute() {
    post("/auth/token") {
        val user = call.receive<JwtUser>()
        if (user.email.isBlank()) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Invalid Body", "Email in body can't be empty or blank.")
            )
        } else if (user.password.isEmpty()) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Invalid Body", "Password in body can't be empty.")
            )
        } else if (user.password.length < 8 || user.password.length > 72) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Invalid Body", "Password length must be higher than 8 and lower than 72 characters.")
            )
        } else {
            val userDto = UserGateway().getByEmail(user.email)
            if (userDto == null || !BCrypt.checkpw(user.password, userDto.password)) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Invalid User", "User not found / invalid password.")
                )
            } else {
                val token = JwtConfig.generateToken(userDto)
                call.respond(HttpStatusCode.OK, TokenResponse(token, "bearer", Duration.ofDays(1).toSeconds()))
            }
        }
    }
}

data class TokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("expires_in") val expiresIn: Long
)

data class ErrorResponse(
    @SerializedName("error_title") val errorTitle: String,
    @SerializedName("error_detail") val errorDetail: String
)