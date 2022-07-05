package com.example.datemomo.model.response

import kotlinx.serialization.Serializable

@Serializable
data class AllLikersResponse(var age: Int,
                             var memberId: Int,
                             var fullName: String,
                             var userName: String,
                             var profilePicture: String,
                             var currentLocation: String)


