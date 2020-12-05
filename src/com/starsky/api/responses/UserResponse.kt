package com.starsky.api.responses

import com.google.gson.annotations.SerializedName

data class UserResponse(
    val id: Int,
    val name: String,
    val email: String,
    @SerializedName("job_title") val jobTitle: String,
    @SerializedName("phone_number") val phoneNumber: String?,
    @SerializedName("notification_type") val notificationType: String,
    val role: String,
    @SerializedName("date_created") val dateCreated: String
)