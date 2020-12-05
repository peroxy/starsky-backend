package com.starsky.api.models

import com.google.gson.annotations.SerializedName

data class NewUserModel(
    val name: String,
    val email: String,
    val password: String,
    @SerializedName("job_title") val jobTitle: String,
)