package com.example.datemomo.model.response

import kotlinx.serialization.Serializable

@Serializable
data class PictureUploadResponse(var age: Int,
                                 var pictureId: Int,
                                 var userLevel: String,
                                 var profilePicture: String)


