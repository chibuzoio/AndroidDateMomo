package com.chibuzo.datemomo.model

import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import com.chibuzo.datemomo.activity.HomeDisplayActivity
import com.chibuzo.datemomo.databinding.ActivityHomeDisplayBinding
import kotlinx.serialization.Serializable

@Serializable
data class HomeDisplayModel(var deviceWidth: Int,
                            var requestProcess: String,
                            var bounceAnimation: Animation,
                            var buttonClickEffect: AlphaAnimation,
                            var binding: ActivityHomeDisplayBinding,
                            var homeDisplayActivity: HomeDisplayActivity)


