package com.example.datemomo.model

import com.example.datemomo.activity.MessengerActivity
import com.example.datemomo.databinding.ActivityMessengerBinding
import kotlinx.serialization.Serializable

@Serializable
data class MessengerModel(var deviceWidth: Int,
                          var requestProcess: String,
                          var binding: ActivityMessengerBinding,
                          var messengerActivity: MessengerActivity)


