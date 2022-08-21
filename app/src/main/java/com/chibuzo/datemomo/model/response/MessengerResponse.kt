package com.chibuzo.datemomo.model.response

import kotlinx.serialization.Serializable

@Serializable
data class MessengerResponse(var chatmateId: Int,
                             var messengerId: Int,
                             var fullName: String,
                             var userName: String,
                             var userStatus: String,
                             var lastMessage: String,
                             var profilePicture: String,
                             var lastMessageDate: String,
                             var unreadMessageCount: Int,
                             var messageTableName: String)


