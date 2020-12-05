package com.starsky.api.validations

import com.starsky.api.models.NewUserModel
import com.starsky.api.responses.ErrorResponse
import com.starsky.database.gateways.UserGateway
import com.starsky.models.User
import io.ktor.http.*
import org.mindrot.jbcrypt.BCrypt

object UserValidation {

    fun validateNewUser(user: NewUserModel): ErrorResponse? {
        val response = validateCredentials(user.email, user.password)
        if (response != null) {
            return response
        }
        if (user.name.isBlank()) {
            return ErrorResponse(HttpStatusCode.BadRequest, "Invalid Body", "Name in body can't be blank.")
        }
        if (user.jobTitle.isBlank()) {
            return ErrorResponse(HttpStatusCode.BadRequest, "Invalid Body", "Job title in body can't be blank.")
        }
        if (UserGateway.getByEmail(user.email) != null) {
            return ErrorResponse(
                HttpStatusCode.Conflict,
                "Already Exists",
                "User with email ${user.email} already exists."
            )
        }
        return null
    }

    fun validateCredentials(email: String, password: String): ErrorResponse? {
        if (email.isBlank() || !email.contains("@")) {
            return ErrorResponse(HttpStatusCode.BadRequest, "Invalid Body", "Email in body has invalid format.")
        }
        if (password.length < 8 || password.length > 72) {
            return ErrorResponse(
                HttpStatusCode.BadRequest,
                "Invalid Body",
                "Password length must be higher than 8 and lower than 72 characters."
            )
        }
        return null
    }

    fun tryAuthenticate(user: User?, password: String): ErrorResponse? {
        return if (user == null || !BCrypt.checkpw(password, user.password)) {
            ErrorResponse(HttpStatusCode.NotFound, "Invalid User", "User not found / invalid password.")
        } else {
            null
        }
    }
}