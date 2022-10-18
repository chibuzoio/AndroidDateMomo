package com.chibuzo.datemomo.model.instance

import com.chibuzo.datemomo.model.response.UserLikerResponse

@kotlinx.serialization.Serializable
data class UserProfileInstance(var userLikerResponses: ArrayList<UserLikerResponse>)


