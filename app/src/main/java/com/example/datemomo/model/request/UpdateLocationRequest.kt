package com.example.datemomo.model.request

import kotlinx.serialization.Serializable

@Serializable
data class UpdateLocationRequest(var memberId: Int,
                                 var currentLocation: String)


