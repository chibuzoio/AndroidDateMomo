package com.chibuzo.datemomo.activity

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
import com.chibuzo.datemomo.R
import com.chibuzo.datemomo.adapter.AllLikersAdapter
import com.chibuzo.datemomo.databinding.ActivityAllLikersBinding
import com.chibuzo.datemomo.model.AllLikersModel
import com.chibuzo.datemomo.model.response.UserLikerResponse
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.IOException

class AllLikersActivity : AppCompatActivity() {
    private var deviceWidth: Int = 0
    private var deviceHeight: Int = 0
    private lateinit var bundle: Bundle
    private var requestProcess: String = ""
    private var leastRootViewHeight: Int = 0
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
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            userLikerResponseArray = mapper.readValue(bundle.getString("jsonResponse")!!)

/*
            userLikerResponseArray = emptyArray()
            userLikerResponseArray = append(UserLikerResponse(1, 34, "male",
                "", "solution", "", "",
                "image1.jpg", "", "2022-05-21 10:53:08"))
            userLikerResponseArray = append(UserLikerResponse(2, 32, "female",
                "", "melas", "", "",
                "image2.jpg", "", "2022-05-21 11:07:06"))
            userLikerResponseArray = append(UserLikerResponse(3, 35, "female",
                "", "chiomzy", "", "",
                "image3.jpg", "", "2022-05-24 06:43:09"))
            userLikerResponseArray = append(UserLikerResponse(4, 35, "female",
                "", "frenzy", "", "",
                "image4.jpg", "", "2022-05-24 06:51:47"))
            userLikerResponseArray = append(UserLikerResponse(5, 33, "female",
                "", "floxy", "", "",
                "image5.jpg", "", "2022-05-24 08:24:24"))
            userLikerResponseArray = append(UserLikerResponse(6, 32, "female",
                "", "millicent", "", "",
                "image6.jpg", "", "2022-05-29 21:29:16"))
*/

            val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
            binding.allLikersRecyclerView.layoutManager = layoutManager
            binding.allLikersRecyclerView.itemAnimator = DefaultItemAnimator()

            val allLikersModel =
                AllLikersModel(sharedPreferences.getInt(getString(R.string.member_id), 0), deviceWidth)

            val allLikersAdapter = AllLikersAdapter(userLikerResponseArray, allLikersModel)
            binding.allLikersRecyclerView.adapter = allLikersAdapter
        } catch (exception: IOException) {
            exception.printStackTrace()
            Log.e(HomeDisplayActivity.TAG, "Error message from here is ${exception.message}")
        }
    }

    private fun append(userLikerResponse: UserLikerResponse): Array<UserLikerResponse> {
        val userLikerResponseList = userLikerResponseArray.toMutableList()
        userLikerResponseList.add(userLikerResponse)
        return userLikerResponseList.toTypedArray()
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


