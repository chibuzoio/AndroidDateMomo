package com.example.datemomo.model.request

import kotlinx.serialization.Serializable

@Serializable
data class RegistrationRequest(var userName: String,
                               var password: String)


