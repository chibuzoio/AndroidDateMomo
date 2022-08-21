package com.example.datemomo.model.response

import kotlinx.serialization.Serializable

@Serializable
class UserPictureResponse(var imageId: Int,
                          var imageWidth: Int,
                          var imageHeight: Int,
                          var imageName: String)


