package com.example.datemomo.model

import android.view.animation.AlphaAnimation
import com.example.datemomo.activity.HomeDisplayActivity
import com.example.datemomo.databinding.ActivityHomeDisplayBinding
import kotlinx.serialization.Serializable

@Serializable
data class HomeDisplayModel(var deviceWidth: Int,
                            var requestProcess: String,
                            var buttonClickEffect: AlphaAnimation,
                            var binding: ActivityHomeDisplayBinding,
                            var homeDisplayActivity: HomeDisplayActivity)


