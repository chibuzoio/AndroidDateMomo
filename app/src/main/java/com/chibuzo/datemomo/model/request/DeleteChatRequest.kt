package com.chibuzo.datemomo.model.request

import kotlinx.serialization.Serializable

@Serializable
data class DeleteChatRequest(var senderId: Int,
                             var messageId: Int,
                             var receiverId: Int,
                             var deleteMessage: Int)


