package com.example.datemomo.model.request

import kotlinx.serialization.Serializable

@Serializable
data class UpdateStatusRequest(var memberInt: Int,
                               var isUserActive: Int)


