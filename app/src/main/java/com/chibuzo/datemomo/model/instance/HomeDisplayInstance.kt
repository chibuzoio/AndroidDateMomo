package com.chibuzo.datemomo.model.instance

import com.chibuzo.datemomo.model.response.OuterHomeDisplayResponse

@kotlinx.serialization.Serializable
data class HomeDisplayInstance(var scrollToPosition: Int,
                               var outerHomeDisplayResponse: OuterHomeDisplayResponse)


