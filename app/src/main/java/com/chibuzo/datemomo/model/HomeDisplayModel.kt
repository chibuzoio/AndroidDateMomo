package com.chibuzo.datemomo.model

import android.view.animation.AlphaAnimation
import com.chibuzo.datemomo.activity.HomeDisplayActivity
import com.chibuzo.datemomo.databinding.ActivityHomeDisplayBinding
import kotlinx.serialization.Serializable

@Serializable
data class HomeDisplayModel(var deviceWidth: Int,
                            var requestProcess: String,
                            var buttonClickEffect: AlphaAnimation,
                            var binding: ActivityHomeDisplayBinding,
                            var homeDisplayActivity: HomeDisplayActivity)


