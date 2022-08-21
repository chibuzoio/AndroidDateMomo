package com.chibuzo.datemomo.model.request

import kotlinx.serialization.Serializable

@Serializable
data class UpdateActiveStatusRequest(var memberInt: Int,
                                     var isUserActive: Int)


