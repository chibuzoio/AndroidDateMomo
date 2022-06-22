package com.example.datemomo.model

import com.example.datemomo.databinding.ActivityMessageBinding
import kotlinx.serialization.Serializable

@Serializable
data class MessageModel(var senderId: Int,
                        var receiverId: Int,
                        var binding: ActivityMessageBinding)


