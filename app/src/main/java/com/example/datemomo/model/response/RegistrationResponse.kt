package com.example.datemomo.model.response

import kotlinx.serialization.Serializable

@Serializable
class RegistrationResponse(var memberId: Int,
                           var userName: String,
                           var userRole: String,
                           var registrationDate: String)


