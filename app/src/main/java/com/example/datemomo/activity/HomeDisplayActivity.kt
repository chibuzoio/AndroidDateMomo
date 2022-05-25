package com.example.datemomo.activity

import android.content.Context
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.datemomo.MainApplication.Companion.setNavigationBarDarkIcons
import com.example.datemomo.MainApplication.Companion.setStatusBarDarkIcons
import com.example.datemomo.R
import com.example.datemomo.adapter.HomeDisplayAdapter
import com.example.datemomo.databinding.ActivityHomeDisplayBinding
import com.example.datemomo.model.HomeDisplayModel
import com.example.datemomo.model.response.HomeDisplayResponse
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.IOException

class HomeDisplayActivity : AppCompatActivity() {
    private var deviceWidth: Int = 0
    private var deviceHeight: Int = 0
    private lateinit var bundle: Bundle
    private lateinit var binding: ActivityHomeDisplayBinding
    private lateinit var buttonClickEffect: AlphaAnimation
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferencesEditor: SharedPreferences.Editor
    private lateinit var homeDisplayResponseArray: Array<HomeDisplayResponse>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeDisplayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.setStatusBarDarkIcons(true)
        window.setNavigationBarDarkIcons(true)

        val displayMetrics = DisplayMetrics()

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
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

        val bitmapImage = BitmapFactory.decodeResource(resources, R.drawable.motion_placeholder)

        Log.e(TAG, "bitmapImage width and height here are ${bitmapImage.width} and ${bitmapImage.height}")

        // Image width and height are 788 and 788

        try {
            val mapper = jacksonObjectMapper()
            homeDisplayResponseArray = mapper.readValue(bundle.getString("jsonResponse")!!)

            val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
            binding.homeDisplayRecyclerView.layoutManager = layoutManager
            binding.homeDisplayRecyclerView.itemAnimator = DefaultItemAnimator()

            val homeDisplayModel = HomeDisplayModel(deviceWidth, binding)

            val homeDisplayAdapter = HomeDisplayAdapter(homeDisplayResponseArray, homeDisplayModel)
            binding.homeDisplayRecyclerView.adapter = homeDisplayAdapter
        } catch (exception: IOException) {
            Log.e(TAG, "Error message from here is ${exception.message}")
        }
    }

    override fun onBackPressed() {
        if (binding.userInformationLayout.isVisible) {
            binding.userInformationLayout.visibility = View.GONE
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        const val TAG = "HomeDisplayActivity"
    }
}


