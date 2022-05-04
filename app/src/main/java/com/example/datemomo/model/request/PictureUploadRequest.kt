package com.example.datemomo.model.request

import kotlinx.serialization.Serializable

@Serializable
data class PictureUploadRequest(var memberId: Int,
                                var base64Picture: String)


