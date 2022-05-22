package com.example.datemomo.model.composite

import com.example.datemomo.model.response.HomeDisplayResponse
import kotlinx.serialization.Serializable

@Serializable
data class HomeDisplayComposite(var homeDisplayResponses: ArrayList<HomeDisplayResponse>)


