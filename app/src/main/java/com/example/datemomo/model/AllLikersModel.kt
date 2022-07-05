package com.example.datemomo.model

import android.content.Context
import kotlinx.serialization.Serializable

@Serializable
data class AllLikersModel(var context: Context,
                          var deviceWidth: Int)


