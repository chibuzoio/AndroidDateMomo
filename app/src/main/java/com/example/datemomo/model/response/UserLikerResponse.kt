package com.example.datemomo.model.response

import kotlinx.serialization.Serializable

@Serializable
data class UserLikerResponse(var memberId: Int,
                             var age: Int,
                             var sex: String,
                             var state: String,
                             var country: String,
                             var fullName: String,
                             var userName: String,
                             var phoneNumber: String,
                             var emailAddress: String,
                             var profilePicture: String,
                             var registrationDate: String)


