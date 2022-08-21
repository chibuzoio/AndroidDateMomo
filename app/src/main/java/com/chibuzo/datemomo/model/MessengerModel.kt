package com.chibuzo.datemomo.model

import com.chibuzo.datemomo.activity.MessengerActivity
import com.chibuzo.datemomo.databinding.ActivityMessengerBinding
import kotlinx.serialization.Serializable

@Serializable
data class MessengerModel(var deviceWidth: Int,
                          var currentPosition: Int,
                          var requestProcess: String,
                          var binding: ActivityMessengerBinding,
                          var messengerActivity: MessengerActivity)


