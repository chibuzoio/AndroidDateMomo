package com.chibuzo.datemomo.model.request

import kotlinx.serialization.Serializable

@Serializable
data class PictureUploadRequest(var sex: String,
                                var memberId: Int,
                                var userAge: Int,
                                var imageWidth: Int,
                                var imageHeight: Int,
                                var userLevel: String,
                                var base64Picture: String)


