package com.chibuzo.datemomo.control

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs
import kotlin.math.max

class ZoomOutPageTransformer : ViewPager2.PageTransformer {

    override fun transformPage(page: View, position: Float) {
        page.apply {
            val pageWidth: Int = page.width
            val pageHeight: Int = page.height

            when {
                position < -1 -> {
                    page.alpha = 0f
                } position <= 1 -> {
                    val scaleFactor = max(MIN_SCALE, 1 - abs(position))
                    val verticalMargin = pageHeight * (1 - scaleFactor) / 2
                    val horizontalMargin = pageWidth * (1 - scaleFactor) / 2

                    page.translationX = if (position < 0) {
                        horizontalMargin - (verticalMargin / 2)
                    } else {
                        horizontalMargin + (verticalMargin / 2)
                    }

                    page.scaleX = scaleFactor
                    page.scaleY = scaleFactor

                    page.alpha = MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA)
                } else -> {
                    page.alpha = 0f
                }
            }
        }
    }

    companion object {
        private const val MIN_ALPHA: Float = 0.5f
        private const val MIN_SCALE: Float = 0.85f
    }
}


