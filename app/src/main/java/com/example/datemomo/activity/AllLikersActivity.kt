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
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.datemomo.R
import com.example.datemomo.adapter.AllLikersAdapter
import com.example.datemomo.databinding.ActivityAllLikersBinding
import com.example.datemomo.model.AllLikersModel
import com.example.datemomo.model.response.UserLikerResponse
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.IOException

class AllLikersActivity : AppCompatActivity() {
    private var deviceWidth: Int = 0
    private var deviceHeight: Int = 0
    private lateinit var bundle: Bundle
    private var leastRootViewHeight: Int = 0
    private lateinit var requestProcess: String
    private lateinit var binding: ActivityAllLikersBinding
    private lateinit var buttonClickEffect: AlphaAnimation
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var userLikerResponseArray: Array<UserLikerResponse>
    private lateinit var sharedPreferencesEditor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAllLikersBinding.inflate(layoutInflater)
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
            userLikerResponseArray = mapper.readValue(bundle.getString("jsonResponse")!!)

            val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
            binding.allLikersRecyclerView.layoutManager = layoutManager
            binding.allLikersRecyclerView.itemAnimator = DefaultItemAnimator()

            val allLikersModel = AllLikersModel(this, deviceWidth)

            val allLikersAdapter = AllLikersAdapter(userLikerResponseArray, allLikersModel)
            binding.allLikersRecyclerView.adapter = allLikersAdapter
        } catch (exception: IOException) {
            Log.e(HomeDisplayActivity.TAG, "Error message from here is ${exception.message}")
        }
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

    companion object {
        const val TAG = "AllLikersActivity"
    }
}


