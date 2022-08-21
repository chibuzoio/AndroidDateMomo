package com.chibuzo.datemomo.model

import android.content.Context
import com.chibuzo.datemomo.activity.MessageActivity
import com.chibuzo.datemomo.databinding.ActivityMessageBinding
import kotlinx.serialization.Serializable

@Serializable
data class MessageModel(var senderId: Int,
                        var receiverId: Int,
                        var context: Context,
                        var currentPosition: Int,
                        var binding: ActivityMessageBinding,
                        var messageActivity: MessageActivity)


