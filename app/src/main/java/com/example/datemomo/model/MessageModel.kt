package com.example.datemomo.model

import android.content.Context
import com.example.datemomo.activity.MessageActivity
import com.example.datemomo.databinding.ActivityMessageBinding
import kotlinx.serialization.Serializable

@Serializable
data class MessageModel(var senderId: Int,
                        var receiverId: Int,
                        var context: Context,
                        var binding: ActivityMessageBinding,
                        var messageActivity: MessageActivity)


