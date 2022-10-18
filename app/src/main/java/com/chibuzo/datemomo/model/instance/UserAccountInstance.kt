package com.chibuzo.datemomo.model.instance

import com.chibuzo.datemomo.model.response.UserLikerResponse

@kotlinx.serialization.Serializable
data class UserAccountInstance(val userLikerResponses: ArrayList<UserLikerResponse>)


