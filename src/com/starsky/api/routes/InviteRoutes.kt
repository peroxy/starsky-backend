package com.starsky.api.routes

import com.starsky.api.models.NewInviteModel
import com.starsky.api.security.UserPrincipal
import com.starsky.api.validations.InviteValidation
import com.starsky.api.validations.RoleValidation
import com.starsky.models.UserRoleEnum
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Application.getInviteRoutes() {
    routing {
        getInviteRoutes()
    }
}

private fun Route.getInviteRoutes() {
    authenticate {
        post("/invites/") {
            val principal = call.principal<UserPrincipal>()!!
            when {
                RoleValidation.hasAnyRole(principal, setOf(UserRoleEnum.Manager)) -> {
                    val model = call.receive<NewInviteModel>()
                    val error = InviteValidation.validateNewInvite(model)
                    if (error != null) {
                        call.respond(error.errorCode, error)
                    } else {
                        //todo: insert into db and call starsky-email /api/add-to-email-queue processing..
                        throw NotImplementedError()
                    }
                }
                else -> {
                    call.respond(HttpStatusCode.Forbidden)
                }
            }
        }
    }
}