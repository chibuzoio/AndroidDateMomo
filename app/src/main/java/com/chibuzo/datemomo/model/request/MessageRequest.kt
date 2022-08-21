package com.chibuzo.datemomo.model.request

import kotlinx.serialization.Serializable

@Serializable
data class MessageRequest(var senderId: Int,
                          var receiverId: Int,
                          var fullName: String,
                          var userName: String,
                          var lastActiveTime: String,
                          var profilePicture: String)


