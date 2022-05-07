package com.example.datemomo.model.request

import kotlinx.serialization.Serializable

@Serializable
data class PictureUploadRequest(var sex: String,
                                var memberId: Int,
                                var userAge: Int,
                                var base64Picture: String)


