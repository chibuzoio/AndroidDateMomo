package com.example.datemomo.model.response

import kotlinx.serialization.Serializable

@Serializable
class RegistrationResponse(var memberId: Int,
                           var userName: String,
                           var userRole: String,
                           var userLevel: String,
                           var authenticated: Boolean,
                           var registrationDate: String)


