package com.example.datemomo.model.request

import kotlinx.serialization.Serializable

@Serializable
data class NotifyUserRequest(var notificationType: String,
                             var notificationOriginId: Int,
                             var genericNotification: String,
                             var notificationEffectorId: Int,
                             var notificationRequirement: String)


