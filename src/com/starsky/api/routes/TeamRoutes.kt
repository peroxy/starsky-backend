package com.starsky.api.routes

import com.starsky.api.security.UserPrincipal
import com.starsky.api.validations.RoleValidation
import com.starsky.database.gateways.TeamGateway
import com.starsky.models.UserRoleEnum
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*

fun Application.getTeamRoutes() {
    routing {
        getTeamRoute()
    }
}

private fun Route.getTeamRoute() {
    authenticate {
        route("/team") {
            get {
                val principal = call.principal<UserPrincipal>()!!
                when {
                    RoleValidation.hasAnyRole(principal, setOf(UserRoleEnum.Manager)) -> {
                        val teams = TeamGateway.getByOwnerId(principal.id)
                        call.respond(teams.map { it.toResponse() })
                    }
                    RoleValidation.hasAnyRole(principal, setOf(UserRoleEnum.Employee)) -> {
                        val teams = TeamGateway.getByEmployeeId(principal.id)
                        call.respond(teams.map { it.toResponse() })
                    }
                    else -> {
                        call.respond(HttpStatusCode.Forbidden)
                    }
                }
            }
        }
    }
}