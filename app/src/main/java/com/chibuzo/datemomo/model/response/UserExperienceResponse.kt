package com.chibuzo.datemomo.model.response

@kotlinx.serialization.Serializable
data class UserExperienceResponse(var memberId: Int,
                                  var fullName: String,
                                  var userName: String,
                                  var lastActiveTime: String,
                                  var profilePicture: String,
                                  var userBlockedStatus: Int)


