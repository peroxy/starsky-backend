package com.starsky.api.responses

import com.google.gson.annotations.SerializedName
import io.ktor.http.*

data class ErrorResponse(
    @SerializedName("error_code") val errorCode: HttpStatusCode,
    @SerializedName("error_title") val errorTitle: String,
    @SerializedName("error_detail") val errorDetail: String
)