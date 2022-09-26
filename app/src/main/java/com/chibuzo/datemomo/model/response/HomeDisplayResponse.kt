package com.chibuzo.datemomo.model.response

import com.chibuzo.datemomo.model.UserPictureModel
import kotlinx.serialization.Serializable

@Serializable
data class HomeDisplayResponse(var memberId: Int,
                               var age: Int,
                               var sex: String,
                               var fullName: String,
                               var userName: String,
                               var userStatus: String,
                               var phoneNumber: String,
                               var emailAddress: String,
                               var profilePicture: String,
                               var userBlockedStatus: Int,
                               var currentLocation: String,
                               var registrationDate: String,
                               var messengerTableName: String,

                               var liked: Boolean,
                               var userPictureModels: ArrayList<UserPictureModel>,

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
                               var oneNightStandExperience: Int,
                               var orgySexExperience: Int,
                               var poolSexExperience: Int,
                               var receivedHeadExperience: Int,
                               var missionaryExperience: Int,
                               var carSexExperience: Int,
                               var publicSexExperience: Int,
                               var cameraSexExperience: Int,
                               var threesomeExperience: Int,
                               var sexToyExperience: Int,
                               var videoSexExperience: Int)


