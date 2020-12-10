package com.starsky.api.routes

import com.starsky.api.models.NewUserModel
import com.starsky.api.responses.ErrorResponse
import com.starsky.api.security.UserPrincipal
import com.starsky.api.validations.UserValidation
import com.starsky.database.gateways.UserGateway
import com.starsky.models.NotificationTypeEnum
import com.starsky.models.UserRoleEnum
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.mindrot.jbcrypt.BCrypt

fun Application.getUserRoutes() {
    routing {
        getUserRoute()
    }
}

private fun Route.getUserRoute() {
    authenticate {
        route("/user") {
            get {
                val principal = call.principal<UserPrincipal>()!!

                // example for how to do role validation:
                //RoleValidation.hasAnyRole(principal, setOf(UserRoleEnum.Manager, UserRoleEnum.Admin))

                val user = UserGateway.getById(principal.id)
                if (user == null) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse(HttpStatusCode.NotFound, "Not Found", "User does not exist in database.")
                    )
                } else {
                    call.respond(user.toResponse())
                }
            }
            patch {
                //TODO:
                throw NotImplementedError()
            }
        }


    }

    /*
        Register a new user with manager role.
     */
    post("/users/") {
        val model = call.receive<NewUserModel>()
        val error = UserValidation.validateNewUser(model)
        if (error != null) {
            call.respond(error.errorCode, error)
        } else {
            val hashedPassword = BCrypt.hashpw(model.password, BCrypt.gensalt())
            val user = UserGateway.insert(
                model.email,
                model.name,
                hashedPassword,
                model.jobTitle,
                NotificationTypeEnum.Email.id,
                null,
                UserRoleEnum.Manager.id,
                null
            )
            call.respond(HttpStatusCode.OK, user.toResponse())
        }


        //TODO: add email verification?
        // - 1. insert (random) unique hash for verification into user table
        // - 2. create rest api method for verification that accepts this unique hash, and sets verifiedMail to true
        // - 3. send verification URL GET, then make a POST on the website, do not include token in get https://stackoverflow.com/questions/39690159/whats-the-rest-way-to-verify-an-email
    }
}

