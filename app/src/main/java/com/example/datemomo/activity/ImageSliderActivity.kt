package com.example.datemomo.activity

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.datemomo.R
import com.example.datemomo.adapter.ImageSliderAdapter
import com.example.datemomo.control.ZoomOutPageTransformer
import com.example.datemomo.databinding.ActivityImageSliderBinding
import com.example.datemomo.model.response.UserPictureResponse
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.IOException


class ImageSliderActivity : AppCompatActivity() {
    private var deviceWidth: Int = 0
    private var deviceHeight: Int = 0
    private lateinit var bundle: Bundle
    private lateinit var requestProcess: String
    private lateinit var buttonClickEffect: AlphaAnimation
    private lateinit var binding: ActivityImageSliderBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferencesEditor: SharedPreferences.Editor
    private lateinit var userPictureComposite: ArrayList<UserPictureResponse>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityImageSliderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hideSystemUI()

        val displayMetrics = DisplayMetrics()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display?.getRealMetrics(displayMetrics)
        } else {
            @Suppress("DEPRECATION")
            val display = windowManager.defaultDisplay
            @Suppress("DEPRECATION")
            display.getMetrics(displayMetrics)
        }

        deviceWidth = displayMetrics.widthPixels
        deviceHeight = displayMetrics.heightPixels

        bundle = intent.extras!!

        buttonClickEffect = AlphaAnimation(1f, 0f)
        sharedPreferences =
            getSharedPreferences(getString(R.string.shared_preferences), Context.MODE_PRIVATE)
        sharedPreferencesEditor = sharedPreferences.edit()

        try {
            val mapper = jacksonObjectMapper()
            userPictureComposite = mapper.readValue(bundle.getString("jsonResponse")!!)

            binding.genericPicturePager.adapter = ImageSliderAdapter(this, userPictureComposite)
            binding.genericPicturePager.setPageTransformer(ZoomOutPageTransformer())
        } catch (exception: IOException) {
            Log.e(UserProfileActivity.TAG, "Error message from here is ${exception.message}")
        }
    }

    override fun onStart() {
        super.onStart()
        hideSystemUI()
    }

    override fun onBackPressed() {
        super.onBackPressed()

        /*if (binding.genericPicturePager.currentItem == 0) {
            super.onBackPressed()
        } else {
            binding.genericPicturePager.currentItem =
                binding.genericPicturePager.currentItem - 1
        }*/
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.root).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun showSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, binding.root).show(WindowInsetsCompat.Type.systemBars())
    }
}


