package com.chibuzo.datemomo.model.response

import kotlinx.serialization.Serializable

@Serializable
data class NotificationResponse(var readStatus: Int,
                                var notificationId: Int,
                                var seenStatusBack: Int,
                                var seenStatusFront: Int,
                                var profilePicture: String,
                                var notificationDate: String,
                                var notificationType: String,
                                var notificationOriginId: Int,
                                var genericNotification: String,
                                var notificationEffectorId: Int,
                                var notificationOriginTable: String,
                                var notificationRequirement: String)


