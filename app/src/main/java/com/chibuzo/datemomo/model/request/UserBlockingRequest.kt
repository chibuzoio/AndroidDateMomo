package com.chibuzo.datemomo.model.request

import kotlinx.serialization.Serializable

@Serializable
data class UserBlockingRequest(var userAccusedId: Int,
                               var userBlockerId: Int,
                               var userBlockedStatus: Int,
                               var userBlockedDate: String)


