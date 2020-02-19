package fr.racomach.zigweelo.data.network

import com.squareup.moshi.Json

data class UserRequest(
    @Json(name = "name")
    val name: String? = null
)

data class UserResponse(
    @Json(name = "authToken")
    val authenticationToken: String
)
