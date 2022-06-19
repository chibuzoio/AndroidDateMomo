package com.example.datemomo.model.request

import kotlinx.serialization.Serializable

@Serializable
data class MessageRequest(var senderId: Int,
                          var receiverId: Int)


