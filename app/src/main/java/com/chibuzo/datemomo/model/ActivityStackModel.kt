package com.chibuzo.datemomo.model

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class ActivityStackModel(var activityStack: Stack<String>)


