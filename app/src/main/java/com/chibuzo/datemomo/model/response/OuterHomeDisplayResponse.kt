package com.chibuzo.datemomo.model.response

import kotlinx.serialization.Serializable

@Serializable
data class OuterHomeDisplayResponse(var thousandRandomCounter: ArrayList<Int>,
                                    var homeDisplayResponses: ArrayList<HomeDisplayResponse>)


