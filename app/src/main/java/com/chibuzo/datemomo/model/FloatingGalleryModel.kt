package com.chibuzo.datemomo.model

import com.chibuzo.datemomo.activity.HomeDisplayActivity
import com.chibuzo.datemomo.databinding.ActivityHomeDisplayBinding
import kotlinx.serialization.Serializable

@Serializable
data class FloatingGalleryModel(var floatingLayoutWidth: Int,
                                var leftRightBigPictureWidth: Int,
                                var leftRightBigPictureHeight: Int,
                                var leftRightPictureWidthHeight: Int,
                                var tripleBottomBigPictureHeight: Int,
                                var binding: ActivityHomeDisplayBinding,
                                var homeDisplayActivity: HomeDisplayActivity)


