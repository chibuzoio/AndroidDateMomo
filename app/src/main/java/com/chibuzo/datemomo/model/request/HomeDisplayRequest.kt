package com.chibuzo.datemomo.model.request

import kotlinx.serialization.Serializable

@Serializable
data class HomeDisplayRequest(var memberId: Int,
                              var nextMatchedUsersIdArray: ArrayList<Int>)


