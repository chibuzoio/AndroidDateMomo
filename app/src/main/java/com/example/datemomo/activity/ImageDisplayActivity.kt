package com.example.datemomo.activity

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.datemomo.R
import com.example.datemomo.adapter.ImageDisplayAdapter
import com.example.datemomo.databinding.ActivityImageDisplayBinding
import com.example.datemomo.model.AllLikersModel
import com.example.datemomo.model.PictureCompositeModel
import com.example.datemomo.model.response.UserPictureResponse
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.IOException

class ImageDisplayActivity : AppCompatActivity() {
    private var deviceWidth: Int = 0
    private var deviceHeight: Int = 0
    private lateinit var bundle: Bundle
    private lateinit var requestProcess: String
    private lateinit var binding: ActivityImageDisplayBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var pictureCompositeModel: PictureCompositeModel
    private lateinit var sharedPreferencesEditor: SharedPreferences.Editor
    private lateinit var userPictureResponseArray: ArrayList<UserPictureResponse>
    private lateinit var pictureCompositeModelArray: ArrayList<PictureCompositeModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityImageDisplayBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        sharedPreferences =
            getSharedPreferences(getString(R.string.shared_preferences), Context.MODE_PRIVATE)
        sharedPreferencesEditor = sharedPreferences.edit()

        try {
            val mapper = jacksonObjectMapper()
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            userPictureResponseArray = mapper.readValue(bundle.getString("jsonResponse")!!)

            pictureCompositeModelArray = ArrayList()
            pictureCompositeModel = PictureCompositeModel(ArrayList())

            userPictureResponseArray.forEachIndexed { index, userPictureResponse ->
                pictureCompositeModel.userPictureResponses.add(userPictureResponse)

                if (((index + 1) % 3) == 0) {
                    pictureCompositeModelArray.add(pictureCompositeModel)

                    pictureCompositeModel = PictureCompositeModel(ArrayList())
                }

                if ((index == (userPictureResponseArray.size - 1)) && ((userPictureResponseArray.size % 3) != 0)) {
                    pictureCompositeModelArray.add(pictureCompositeModel)
                }
            }

            val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
            binding.imageDisplayRecyclerView.layoutManager = layoutManager
            binding.imageDisplayRecyclerView.itemAnimator = DefaultItemAnimator()

            val allLikersModel = AllLikersModel(bundle.getInt("memberId"), deviceWidth)

            val imageDisplayAdapter = ImageDisplayAdapter(pictureCompositeModelArray, allLikersModel)
            binding.imageDisplayRecyclerView.adapter = imageDisplayAdapter
        } catch (exception: IOException) {
            exception.printStackTrace()
            Log.e(TAG, "Error message from here is ${exception.message}")
        }
    }

    override fun onStart() {
        super.onStart()
        hideSystemUI()
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
        const val TAG = "ImageDisplayActivity"
    }
}


