package com.chibuzo.datemomo.model

import com.chibuzo.datemomo.model.response.UserPictureResponse

@kotlinx.serialization.Serializable
data class GalleryPictureModel(var imagePosition: Int,
                               var userPictureResponse: UserPictureResponse)


