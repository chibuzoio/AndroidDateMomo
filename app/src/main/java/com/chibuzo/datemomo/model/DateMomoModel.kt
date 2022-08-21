package com.chibuzo.datemomo.model

import kotlinx.serialization.Serializable

@Serializable
data class DateMomoModel(var memberId: Int,
                         var fullName: String,
                         var sex: String,
                         var age: Int,
                         var userName: String,
                         var emailAddress: String,
                         var phoneNumber: String,
                         var memberCountry: String,
                         var memberState: String,
                         var password: String,
                         var registrationDate: String,
                         var userRole: String,
                         var profilePicture: String)


