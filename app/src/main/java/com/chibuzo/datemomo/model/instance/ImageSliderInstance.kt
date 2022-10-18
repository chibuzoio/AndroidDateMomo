package com.chibuzo.datemomo.model.instance

import com.chibuzo.datemomo.model.response.UserPictureResponse

@kotlinx.serialization.Serializable
data class ImageSliderInstance(var memberId: Int,
                               var currentPosition: Int,
                               var userPictureResponses: ArrayList<UserPictureResponse>)


