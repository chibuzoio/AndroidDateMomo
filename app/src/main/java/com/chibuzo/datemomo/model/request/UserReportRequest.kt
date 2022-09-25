package com.chibuzo.datemomo.model.request

import kotlinx.serialization.Serializable

@Serializable
data class UserReportRequest(var accusedId: Int,
                             var reporterId: Int,
                             var reportMessage: String)


