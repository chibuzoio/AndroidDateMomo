package com.chibuzo.datemomo.model.instance

import com.chibuzo.datemomo.model.response.UserPictureResponse

@kotlinx.serialization.Serializable
data class ImageDisplayInstance(var scrollToPosition: Int,
                                var userPictureResponses: ArrayList<UserPictureResponse>)


