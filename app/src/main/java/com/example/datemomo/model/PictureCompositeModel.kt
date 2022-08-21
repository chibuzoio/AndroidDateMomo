package com.example.datemomo.model

import com.example.datemomo.model.response.UserPictureResponse
import kotlinx.serialization.Serializable

@Serializable
data class PictureCompositeModel(var userPictureResponses: ArrayList<UserPictureResponse>)


