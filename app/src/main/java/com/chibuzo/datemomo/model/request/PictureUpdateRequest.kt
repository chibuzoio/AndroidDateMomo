package com.chibuzo.datemomo.model.request

import kotlinx.serialization.Serializable

@Serializable
data class PictureUpdateRequest(var memberId: Int,
                                var imageWidth: Int,
                                var imageHeight: Int,
                                var base64Picture: String)


