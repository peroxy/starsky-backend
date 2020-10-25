package com.starsky.api.routes

import com.starsky.api.responses.ErrorResponse
import com.starsky.api.security.UserPrincipal
import com.starsky.database.gateways.UserGateway
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*

fun Application.getUserRoutes() {
    routing {
        getUserRoute()
    }
}

fun Route.getUserRoute() {
    authenticate {
        route("/user") {
            get {
                val principal = call.principal<UserPrincipal>()!!
                val user = UserGateway.getById(principal.id)
                if (user == null){
                    call.respond(HttpStatusCode.NotFound, ErrorResponse(HttpStatusCode.NotFound, "Not Found", "User does not exist in database."))
                } else {
                    call.respond(user.toResponse())
                }
            }
            patch {
            }
            post {
            }
        }
    }


}

