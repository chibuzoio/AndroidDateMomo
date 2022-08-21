package com.example.datemomo.model

import kotlinx.serialization.Serializable

@Serializable
data class UserPictureModel(var imageId: Int,
                            var imageName: String,
                            var imageWidth: Int,
                            var imageHeight: Int)


