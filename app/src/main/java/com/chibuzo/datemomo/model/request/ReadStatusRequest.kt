package com.chibuzo.datemomo.model.request

import kotlinx.serialization.Serializable

@Serializable
data class ReadStatusRequest(var memberId: Int,
                             var notificationId: Int,
                             var notificationPosition: Int)


