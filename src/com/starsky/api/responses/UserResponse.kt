package com.starsky.api.responses

import com.google.gson.annotations.SerializedName

data class UserResponse(val id : Int, val name: String, val email: String, @SerializedName("date_created") val dateCreated: String)