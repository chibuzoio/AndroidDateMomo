package com.chibuzo.datemomo.model.instance

import com.chibuzo.datemomo.model.response.UserLikerResponse

@kotlinx.serialization.Serializable
data class AllLikedInstance(var userLikerResponses: ArrayList<UserLikerResponse>)


