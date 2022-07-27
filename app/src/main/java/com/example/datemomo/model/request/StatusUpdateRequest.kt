package com.example.datemomo.model.request

import kotlinx.serialization.Serializable

@Serializable
data class StatusUpdateRequest(var memberId: Int,
                               var updatedStatus: String)


