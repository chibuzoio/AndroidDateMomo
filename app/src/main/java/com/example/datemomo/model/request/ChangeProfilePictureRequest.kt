package com.example.datemomo.model.request

import kotlinx.serialization.Serializable

@Serializable
data class ChangeProfilePictureRequest(var memberId: Int,
                                       var profilePicture: String)


