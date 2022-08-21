package com.chibuzo.datemomo.model.request

import kotlinx.serialization.Serializable

@Serializable
data class EditMessageRequest(var senderId: Int,
                              var messageId: Int,
                              var message: String,
                              var receiverId: Int)


