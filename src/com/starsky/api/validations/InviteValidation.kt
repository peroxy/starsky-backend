package com.starsky.api.validations

import com.starsky.api.models.NewInviteModel
import com.starsky.api.responses.ErrorResponse
import io.ktor.http.*

object InviteValidation {

    fun validateNewInvite(invite: NewInviteModel): ErrorResponse? {
        if (invite.email.isBlank() || !invite.email.contains("@")) {
            return ErrorResponse(HttpStatusCode.BadRequest, "Invalid Body", "Email in body has invalid format.")
        }

        if (invite.jobTitle.isBlank()) {
            return ErrorResponse(HttpStatusCode.BadRequest, "Invalid Body", "Job title in body has invalid format.")
        }

        if (invite.name.isBlank()) {
            return ErrorResponse(HttpStatusCode.BadRequest, "Invalid Body", "Employee name in body has invalid format.")
        }

        return null
    }
}