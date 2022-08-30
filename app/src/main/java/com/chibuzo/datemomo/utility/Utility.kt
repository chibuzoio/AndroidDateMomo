package com.chibuzo.datemomo.utility

import android.content.Context
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Log
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class Utility {

    companion object {

        fun encodeUploadImage(bitmap: Bitmap): String {
            val byteArrayOutputStream = ByteArrayOutputStream()
            val currentBitmapSize = bitmap.allocationByteCount

            if (currentBitmapSize > 174747195) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
            } else if (currentBitmapSize > 109216997) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 55, byteArrayOutputStream)
            } else if (currentBitmapSize > 65530198) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 60, byteArrayOutputStream)
            } else if (currentBitmapSize > 43686798) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 65, byteArrayOutputStream)
            } else if (currentBitmapSize > 32765100) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream)
            } else if (currentBitmapSize > 21843400) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 75, byteArrayOutputStream)
            } else if (currentBitmapSize > 16062577) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
            } else if (currentBitmapSize > 11838951) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, byteArrayOutputStream)
            } else if (currentBitmapSize > 7530000) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream)
            } else {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, byteArrayOutputStream)
            }

            return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT)
        }

        fun getTimeDifference(timeGottenInSeconds: Long): String {
            val timeInSeconds = (System.currentTimeMillis() / 1000) - timeGottenInSeconds

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


