package com.example.datemomo.model.response

import kotlinx.serialization.Serializable

@Serializable
data class MessageResponse(var messageId: Int,
                           var messenger: Int,
                           var message: String,
                           var readStatus: Int,
                           var seenStatus: Int,
                           var deleteMessage: Int,
                           var messageDate: String)


