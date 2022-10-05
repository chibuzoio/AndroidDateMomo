package com.chibuzo.datemomo.model

import com.chibuzo.datemomo.databinding.ActivityHomeDisplayBinding
import kotlinx.serialization.Serializable

@Serializable
data class FloatingGalleryModel(var profileOwnerId: Int,
                                var floatingLayoutWidth: Int,
                                var leftRightBigPictureWidth: Int,
                                var tripleBottomLayoutHeight: Int,
                                var leftRightBigPictureHeight: Int,
                                var singlePictureLayoutHeight: Int,
                                var doubleLeftRightLayoutHeight: Int,
                                var floatingGalleryLayoutHeight: Int,
                                var leftRightPictureWidthHeight: Int,
                                var tripleBottomBigPictureHeight: Int,
                                var binding: ActivityHomeDisplayBinding)


