package com.example.datemomo.model.response

import kotlinx.serialization.Serializable

@Serializable
data class PictureUploadResponse(var memberId: Int,
                                var profilePicture: String)


