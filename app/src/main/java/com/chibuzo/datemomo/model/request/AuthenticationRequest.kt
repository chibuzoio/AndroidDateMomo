package com.chibuzo.datemomo.model.request

import kotlinx.serialization.Serializable

@Serializable
data class AuthenticationRequest(var userName: String,
                                 var password: String)


