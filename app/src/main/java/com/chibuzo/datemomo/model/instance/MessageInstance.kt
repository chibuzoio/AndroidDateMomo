package com.chibuzo.datemomo.model.instance

import com.chibuzo.datemomo.model.response.MessageResponse

@kotlinx.serialization.Serializable
data class MessageInstance(var senderId: Int,
                           var receiverId: Int,
                           var fullName: String,
                           var userName: String,
                           var lastActiveTime: String,
                           var profilePicture: String,
                           var userBlockedStatus: Int,
                           var messageResponses: ArrayList<MessageResponse>)


