package com.chibuzo.datemomo.model.request

import kotlinx.serialization.Serializable

@Serializable
data class UserReportRequest(var memberId: Int,
                             var userReportMessages: String)


