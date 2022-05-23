package com.example.datemomo.activity

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.datemomo.MainApplication.Companion.setNavigationBarDarkIcons
import com.example.datemomo.MainApplication.Companion.setStatusBarDarkIcons
import com.example.datemomo.R
import com.example.datemomo.adapter.HomeDisplayAdapter
import com.example.datemomo.databinding.ActivityHomeDisplayBinding
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

        bundle = intent.extras!!

        try {
            val mapper = jacksonObjectMapper()

            homeDisplayResponseArray = mapper.readValue(bundle.getString("jsonResponse")!!)

            for (homeDisplayResponse in homeDisplayResponseArray) {
                Log.e(TAG, "sex in homeDisplayResponses is ${homeDisplayResponse.sex}")
            }
        } catch (exception: IOException) {
            Log.e(TAG, "Error message from here is ${exception.message}")
        }

        buttonClickEffect = AlphaAnimation(1f, 0f)
        sharedPreferences =
            getSharedPreferences(getString(R.string.shared_preferences), Context.MODE_PRIVATE)
        sharedPreferencesEditor = sharedPreferences.edit()

        val displayMetrics = DisplayMetrics()

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            @Suppress("DEPRECATION")
            display!!.getRealMetrics(displayMetrics)
        } else {
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getMetrics(displayMetrics)
        }

        deviceWidth = displayMetrics.widthPixels
        deviceHeight = displayMetrics.heightPixels

        val homeDisplayImages = ArrayList<HomeDisplayResponse>()
//        homeDisplayImages.add(HomeDisplayResponse(R.drawable.image1))
//        homeDisplayImages.add(HomeDisplayResponse(R.drawable.image2))
//        homeDisplayImages.add(HomeDisplayResponse(R.drawable.image3))
//        homeDisplayImages.add(HomeDisplayResponse(R.drawable.image4))
//        homeDisplayImages.add(HomeDisplayResponse(R.drawable.image5))
//        homeDisplayImages.add(HomeDisplayResponse(R.drawable.image6))
//        homeDisplayImages.add(HomeDisplayResponse(R.drawable.image7))
//        homeDisplayImages.add(HomeDisplayResponse(R.drawable.image8))
//        homeDisplayImages.add(HomeDisplayResponse(R.drawable.image9))
//        homeDisplayImages.add(HomeDisplayResponse(R.drawable.image10))
//        homeDisplayImages.add(HomeDisplayResponse(R.drawable.image11))
//        homeDisplayImages.add(HomeDisplayResponse(R.drawable.image12))
//        homeDisplayImages.add(HomeDisplayResponse(R.drawable.image13))
//        homeDisplayImages.add(HomeDisplayResponse(R.drawable.image14))
//        homeDisplayImages.add(HomeDisplayResponse(R.drawable.image15))

//        val layoutManager = FlexboxLayoutManager(this)
//        layoutManager.flexDirection = FlexDirection.COLUMN
//        layoutManager.justifyContent = JustifyContent.FLEX_END

//        layoutManager.flexDirection = FlexDirection.ROW
//        layoutManager.justifyContent = JustifyContent.CENTER
//        layoutManager.alignItems = AlignItems.CENTER

//        layoutManager.flexWrap = FlexWrap.WRAP
//        layoutManager.flexDirection = FlexDirection.ROW
//        layoutManager.alignItems = AlignItems.STRETCH

        val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.homeDisplayRecyclerView.layoutManager = layoutManager
        binding.homeDisplayRecyclerView.itemAnimator = DefaultItemAnimator()

        val homeDisplayAdapter = HomeDisplayAdapter(homeDisplayImages)
        binding.homeDisplayRecyclerView.adapter = homeDisplayAdapter
    }

    companion object {
        const val TAG = "HomeDisplayActivity"
    }
}


