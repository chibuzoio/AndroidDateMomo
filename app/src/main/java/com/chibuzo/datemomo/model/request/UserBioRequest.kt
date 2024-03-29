package com.chibuzo.datemomo.model.request

import kotlinx.serialization.Serializable

@Serializable
data class UserBioRequest(var memberId: Int,
                          var userLevel: String,
                          var bisexualCategory: Int,
                          var gayCategory: Int,
                          var lesbianCategory: Int,
                          var straightCategory: Int,
                          var sugarDaddyCategory: Int,
                          var sugarMommyCategory: Int,
                          var toyBoyCategory: Int,
                          var toyGirlCategory: Int,
                          var bisexualInterest: Int,
                          var gayInterest: Int,
                          var lesbianInterest: Int,
                          var straightInterest: Int,
                          var friendshipInterest: Int,
                          var sugarDaddyInterest: Int,
                          var sugarMommyInterest: Int,
                          var relationshipInterest: Int,
                          var toyBoyInterest: Int,
                          var toyGirlInterest: Int,
                          var sixtyNineExperience: Int,
                          var analSexExperience: Int,
                          var givenHeadExperience: Int,
                          var missionaryExperience: Int,
                          var oneNightStandExperience: Int,
                          var orgySexExperience: Int,
                          var poolSexExperience: Int,
                          var receivedHeadExperience: Int,
                          var carSexExperience: Int,
                          var publicSexExperience: Int,
                          var cameraSexExperience: Int,
                          var threesomeExperience: Int,
                          var sexToyExperience: Int,
                          var videoSexExperience: Int)


