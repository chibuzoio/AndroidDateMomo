package com.example.datemomo.utility

import android.content.Context
import android.util.DisplayMetrics

class Utility {

    companion object {
        fun screenImageWidth(context: Context, imageWidth: Int,
                             imageHeight: Int, deviceWidth: Int, xMarginSum: Int): Int {
            return (imageHeight * (deviceWidth - dimen(context, xMarginSum.toFloat()))) / imageWidth
        }

        private fun dimen(context: Context, densityPixel: Float): Int {
            return (densityPixel * (context.resources
                .displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
        }
    }
}


