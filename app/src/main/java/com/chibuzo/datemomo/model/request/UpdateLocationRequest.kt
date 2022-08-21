package com.chibuzo.datemomo.model.request

import kotlinx.serialization.Serializable

@Serializable
data class UpdateLocationRequest(var memberId: Int,
                                 var updatedLocation: String)


