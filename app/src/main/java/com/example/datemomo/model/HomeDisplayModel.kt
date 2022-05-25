package com.example.datemomo.model

import com.example.datemomo.databinding.ActivityHomeDisplayBinding
import kotlinx.serialization.Serializable

@Serializable
data class HomeDisplayModel(var deviceWidth: Int,
                            var binding: ActivityHomeDisplayBinding)


