package com.chibuzo.datemomo.model.response

import kotlinx.serialization.Serializable

@Serializable
data class UserBioResponse(var committed: Boolean,
                           var userLevel: String)


