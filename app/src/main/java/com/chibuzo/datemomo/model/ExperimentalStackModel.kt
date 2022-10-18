package com.chibuzo.datemomo.model

import com.chibuzo.datemomo.model.instance.ActivitySavedInstance
import java.util.*

@kotlinx.serialization.Serializable
data class ExperimentalStackModel(var activityInstanceStack: Stack<ActivitySavedInstance>)


