package com.chibuzo.datemomo.model.request

import kotlinx.serialization.Serializable

@Serializable
data class PostMessageRequest(var senderId: Int,
                              var receiverId: Int,
                              var messagePosition: Int,
                              var senderMessage: String)


