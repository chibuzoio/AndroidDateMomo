package com.chibuzo.datemomo.model.request

import kotlinx.serialization.Serializable

@Serializable
data class CheckMessageRequest(var senderId: Int,
                               var receiverId: Int)


