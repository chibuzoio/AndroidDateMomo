package com.example.datemomo.model.request

import kotlinx.serialization.Serializable

@Serializable
data class DeleteMessageRequest(var memberId: Int,
                                var messageTableName: String)


