package fr.racomach.zigweelo.data

import com.squareup.moshi.Json

data class UserRequest(
    @Json(name = "name")
    val name: String?
)

data class UserResponse(
    @Json(name = "authToken")
    val authenticationToken: String
)
