package com.chibuzo.datemomo.model

import com.chibuzo.datemomo.model.response.UserPictureResponse
import kotlinx.serialization.Serializable

@Serializable
data class PictureCollectionModel(var pictureLayoutType: String,
                                  var userPictureResponses: ArrayList<UserPictureResponse>)


