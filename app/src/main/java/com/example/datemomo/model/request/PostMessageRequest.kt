package com.example.datemomo.model.request

import kotlinx.serialization.Serializable

@Serializable
data class PostMessageRequest(var senderId: Int,
                              var messagePosition: Int,
                              var senderMessage: String,
                              var messengerTableName: String)


