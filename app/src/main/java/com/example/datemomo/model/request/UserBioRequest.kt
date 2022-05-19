package com.example.datemomo.model.request

import kotlinx.serialization.Serializable

@Serializable
data class UserBioRequest(var memberId: Int,
                          var userLevel: String,
                          var bisexualCategory: Boolean,
                          var gayCategory: Boolean,
                          var lesbianCategory: Boolean,
                          var straightCategory: Boolean,
                          var sugarDaddyCategory: Boolean,
                          var sugarMommyCategory: Boolean,
                          var toyBoyCategory: Boolean,
                          var toyGirlCategory: Boolean,
                          var bisexualInterest: Boolean,
                          var gayInterest: Boolean,
                          var lesbianInterest: Boolean,
                          var straightInterest: Boolean,
                          var sugarDaddyInterest: Boolean,
                          var sugarMommyInterest: Boolean,
                          var toyBoyInterest: Boolean,
                          var toyGirlInterest: Boolean,
                          var sixtyNineExperience: Boolean,
                          var analSexExperience: Boolean,
                          var givenHeadExperience: Boolean,
                          var oneNightStandExperience: Boolean,
                          var orgySexExperience: Boolean,
                          var poolSexExperience: Boolean,
                          var receivedHeadExperience: Boolean,
                          var carSexExperience: Boolean,
                          var publicSexExperience: Boolean,
                          var cameraSexExperience: Boolean,
                          var threesomeExperience: Boolean,
                          var sexToyExperience: Boolean,
                          var videoSexExperience: Boolean)


