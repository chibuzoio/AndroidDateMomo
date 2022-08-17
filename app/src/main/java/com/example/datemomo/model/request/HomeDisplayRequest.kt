package com.example.datemomo.model.request

import kotlinx.serialization.Serializable

@Serializable
data class HomeDisplayRequest(var nextMatchedUsersIdArray: ArrayList<Int>)


