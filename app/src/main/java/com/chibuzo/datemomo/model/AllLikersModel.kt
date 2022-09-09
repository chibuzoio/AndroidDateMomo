package com.chibuzo.datemomo.model

import androidx.appcompat.app.AppCompatActivity
import kotlinx.serialization.Serializable

@Serializable
data class AllLikersModel(var memberId: Int,
                          var deviceWidth: Int,
                          var requestProcess: String,
                          var appCompatActivity: AppCompatActivity)


