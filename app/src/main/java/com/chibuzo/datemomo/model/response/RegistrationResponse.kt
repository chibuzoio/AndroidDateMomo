package com.chibuzo.datemomo.model.response

import kotlinx.serialization.Serializable

@Serializable
data class RegistrationResponse(var memberId: Int,
                                var userName: String,
                                var userRole: String,
                                var impactCount: Int,
                                var userLevel: String,
                                var userStatus: String,
                                var authenticated: Boolean,
                                var registrationDate: String)


