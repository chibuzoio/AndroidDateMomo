package com.example.datemomo

import android.app.Application
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowInsetsController

class MainApplication : Application() {

    companion object {
        @Suppress("DEPRECATION")
        fun Window.setStatusBarDarkIcons(dark: Boolean) {
            when {
                Build.VERSION_CODES.R <= Build.VERSION.SDK_INT -> insetsController?.setSystemBarsAppearance(
                    if (dark) WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS else 0,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
                Build.VERSION_CODES.M <= Build.VERSION.SDK_INT -> decorView.systemUiVisibility =
                    if (dark) {
                        decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    } else {
                        decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                    }
                else -> if (dark) {
                    // dark status bar icons not supported on API level below 23, set status bar
                    // color to black to keep icons visible
                    statusBarColor = Color.BLACK
                }
            }
        }

        @Suppress("DEPRECATION")
        fun Window.setNavigationBarDarkIcons(dark: Boolean) {
            when {
                Build.VERSION_CODES.R <= Build.VERSION.SDK_INT -> insetsController?.setSystemBarsAppearance(
                    if (dark) WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS else 0,
                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                )
                Build.VERSION_CODES.O <= Build.VERSION.SDK_INT -> decorView.systemUiVisibility =
                    if (dark) {
                        decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                    } else {
                        decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
                    }
                else -> if (dark) {
                    // dark navigation bar icons not supported on API level below 26, set navigation bar
                    // color to black to keep icons visible
                    navigationBarColor = Color.BLACK
                }
            }
        }
    }
}


