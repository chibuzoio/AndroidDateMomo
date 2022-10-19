package com.chibuzo.datemomo.model.instance

import com.chibuzo.datemomo.model.response.MessengerResponse

@kotlinx.serialization.Serializable
data class MessengerInstance(var scrollToPosition: Int,
                             var messengerResponses: ArrayList<MessengerResponse>)


