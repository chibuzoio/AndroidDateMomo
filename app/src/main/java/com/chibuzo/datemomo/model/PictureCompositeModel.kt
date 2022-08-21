package com.chibuzo.datemomo.model

import com.chibuzo.datemomo.model.response.UserPictureResponse
import kotlinx.serialization.Serializable

@Serializable
data class PictureCompositeModel(var userPictureResponses: ArrayList<UserPictureResponse>)


