package com.example.datemomo.model.response

import kotlinx.serialization.Serializable

@Serializable
data class PictureUploadResponse(var pictureId: Int,
                                var profilePicture: String)


