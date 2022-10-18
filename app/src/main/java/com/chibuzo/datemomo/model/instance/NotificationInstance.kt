package com.chibuzo.datemomo.model.instance

import com.chibuzo.datemomo.model.response.NotificationResponse

@kotlinx.serialization.Serializable
data class NotificationInstance(var notificationResponses: ArrayList<NotificationResponse>)


