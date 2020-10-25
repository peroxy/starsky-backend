package com.starsky.api.validations

import com.starsky.api.responses.ErrorResponse
import com.starsky.api.security.JwtUser
import com.starsky.models.User
import io.ktor.http.*
import org.mindrot.jbcrypt.BCrypt

object AuthValidation {
    fun validateJwtUser(user: JwtUser): ErrorResponse? {
        if (user.email.isBlank() || !user.email.contains("@")) {
            return ErrorResponse(HttpStatusCode.BadRequest, "Invalid Body", "Email in body has invalid format.")
        }
        if (user.password.length < 8 || user.password.length > 72) {
            return ErrorResponse(
                HttpStatusCode.BadRequest,
                "Invalid Body",
                "Password length must be higher than 8 and lower than 72 characters."
            )
        }
        return null
    }

    fun validateCredentials(user: User?, password: String) : ErrorResponse? {
        return if (user == null || !BCrypt.checkpw(password, user.password)) {
            ErrorResponse(HttpStatusCode.NotFound, "Invalid User", "User not found / invalid password.")
        } else {
            null
        }
    }
}