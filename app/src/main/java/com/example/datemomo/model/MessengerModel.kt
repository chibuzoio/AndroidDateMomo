package com.example.datemomo.model

import com.example.datemomo.activity.MessengerActivity
import com.example.datemomo.databinding.ActivityMessengerBinding
import kotlinx.serialization.Serializable
import java.text.FieldPosition

@Serializable
data class MessengerModel(var deviceWidth: Int,
                          var currentPosition: Int,
                          var requestProcess: String,
                          var binding: ActivityMessengerBinding,
                          var messengerActivity: MessengerActivity)


