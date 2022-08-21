package com.example.datemomo.utility

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.DisplayMetrics
import java.util.concurrent.TimeUnit

class Utility {

    companion object {
        fun getTimeDifference(timeInSeconds: Long): String {
            return if (timeInSeconds > 59) {
                val timeInMinutes =
                    TimeUnit.MINUTES.convert(timeInSeconds, TimeUnit.SECONDS).toInt()
                if (timeInMinutes > 59) {
                    val timeInHours =
                        TimeUnit.HOURS.convert(timeInMinutes.toLong(), TimeUnit.MINUTES).toInt()
                    if (timeInHours > 23) {
                        val timeInDays =
                            TimeUnit.DAYS.convert(timeInHours.toLong(), TimeUnit.HOURS).toInt()
                        if (timeInDays > 6) {
                            val timeInWeeks = timeInDays / 7
                            if (timeInWeeks > 3) {
                                "$timeInWeeks weeks ago"
                            } else {
                                if (timeInWeeks > 1) {
                                    "$timeInWeeks weeks ago"
                                } else {
                                    "$timeInWeeks week ago"
                                }
                            }
                        } else {
                            if (timeInDays > 1) {
                                "$timeInDays days ago"
                            } else {
                                "$timeInDays day ago"
                            }
                        }
                    } else {
                        if (timeInHours > 1) {
                            "$timeInHours hours ago"
                        } else {
                            "$timeInHours hour ago"
                        }
                    }
                } else {
                    if (timeInMinutes > 1) {
                        "$timeInMinutes minutes ago"
                    } else {
                        "$timeInMinutes minute ago"
                    }
                }
            } else {
                if (timeInSeconds <= 1) {
                    "Just Now"
                } else {
                    "$timeInSeconds seconds ago"
                }
            }
        }

        fun convertPixelToDp(context: Context, pixel: Float): Float {
            return pixel / (context.resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
        }

        fun convertDpToPixel(context: Context, densityPixel: Float): Float {
            return densityPixel * (context.resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
        }

        fun isConnected(context: Context): Boolean {
            var result = false
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val networkCapabilities = connectivityManager.activeNetwork ?: return false
                val activeNetwork =
                    connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
                result = when {
                    activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    else -> false
                }
            } else {
                connectivityManager.run {
                    connectivityManager.activeNetworkInfo?.run {
                        result = when (type) {
                            ConnectivityManager.TYPE_WIFI -> true
                            ConnectivityManager.TYPE_MOBILE -> true
                            ConnectivityManager.TYPE_ETHERNET -> true
                            else -> false
                        }
                    }
                }
            }

            return result
        }

        fun screenImageWidth(context: Context, imageWidth: Int,
                             imageHeight: Int, deviceWidth: Int, xMarginSum: Int): Int {
            return (imageHeight * (deviceWidth - dimen(context, xMarginSum.toFloat()))) / imageWidth
        }

        fun dimen(context: Context, densityPixel: Float): Int {
            return (densityPixel * (context.resources
                .displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
        }
    }
}


