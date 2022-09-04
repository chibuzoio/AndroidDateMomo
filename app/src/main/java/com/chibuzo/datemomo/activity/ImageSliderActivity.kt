package com.chibuzo.datemomo.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.chibuzo.datemomo.R
import com.chibuzo.datemomo.adapter.ImageSliderAdapter
import com.chibuzo.datemomo.databinding.ActivityImageSliderBinding
import com.chibuzo.datemomo.model.ActivityStackModel
import com.chibuzo.datemomo.model.request.UserInformationRequest
import com.chibuzo.datemomo.model.response.UserPictureResponse
import com.chibuzo.datemomo.utility.Utility
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.*
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class ImageSliderActivity : AppCompatActivity() {
    private var deviceWidth: Int = 0
    private var deviceHeight: Int = 0
    private lateinit var bundle: Bundle
    private var requestProcess: String = ""
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

        binding.singleButtonDialog.dialogRetryButton.setOnClickListener {
            binding.doubleButtonDialog.doubleButtonLayout.visibility = View.GONE
            binding.singleButtonDialog.singleButtonLayout.visibility = View.GONE
            triggerRequestProcess()
        }

        binding.singleButtonDialog.singleButtonLayout.setOnClickListener {
            binding.doubleButtonDialog.doubleButtonLayout.visibility = View.GONE
            binding.singleButtonDialog.singleButtonLayout.visibility = View.GONE
        }

        binding.doubleButtonDialog.dialogRetryButton.setOnClickListener {
            binding.doubleButtonDialog.doubleButtonLayout.visibility = View.GONE
            binding.singleButtonDialog.singleButtonLayout.visibility = View.GONE

            if (binding.doubleButtonDialog.dialogRetryButton.text == "Retry") {
                triggerRequestProcess()
            }
        }

        binding.doubleButtonDialog.dialogCancelButton.setOnClickListener {
            binding.doubleButtonDialog.doubleButtonLayout.visibility = View.GONE
            binding.singleButtonDialog.singleButtonLayout.visibility = View.GONE
        }

        binding.doubleButtonDialog.doubleButtonLayout.setOnClickListener {
            binding.doubleButtonDialog.doubleButtonLayout.visibility = View.GONE
            binding.singleButtonDialog.singleButtonLayout.visibility = View.GONE
        }

        try {
            val mapper = jacksonObjectMapper()
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            userPictureComposite = mapper.readValue(bundle.getString("jsonResponse")!!)

            binding.genericPicturePager.adapter = ImageSliderAdapter(this, userPictureComposite)
            /*binding.genericPicturePager.setPageTransformer(ZoomOutPageTransformer())*/
            binding.genericPicturePager.setCurrentItem(bundle.getInt("currentPosition"), true)
        } catch (exception: IOException) {
            exception.printStackTrace()
            Log.e(TAG, "Error message from here is ${exception.message}")
        }
    }

    override fun onStart() {
        super.onStart()
        hideSystemUI()
    }

    override fun onBackPressed() {
        val mapper = jacksonObjectMapper()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        val activityStackModel: ActivityStackModel =
            mapper.readValue(sharedPreferences.getString(getString(R.string.activity_stack), "")!!)

        try {
            when (activityStackModel.activityStack.peek()) {
                getString(R.string.activity_image_slider) -> {
                    activityStackModel.activityStack.pop()

                    val activityStackString = mapper.writeValueAsString(activityStackModel)
                    sharedPreferencesEditor.putString(getString(R.string.activity_stack), activityStackString)
                    sharedPreferencesEditor.apply()

                    this.onBackPressed()
                }
                getString(R.string.activity_user_information) -> {
                    requestProcess = getString(R.string.request_fetch_user_information)
                    fetchUserInformation()
                }
                getString(R.string.activity_user_profile) -> super.onBackPressed()
                else -> super.onBackPressed()
            }
        } catch (exception: EmptyStackException) {
            exception.printStackTrace()
            Log.e(TAG, "Exception from trying to peek activityStack here is ${exception.message}")
        }

        Log.e(TAG, "The value of activityStackModel here is ${sharedPreferences.getString(getString(R.string.activity_stack), "")}")
    }

    @Throws(IOException::class)
    fun fetchUserInformation() {
        val mapper = jacksonObjectMapper()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        val userInformationRequest = UserInformationRequest(bundle.getInt("memberId"))
        val jsonObjectString = mapper.writeValueAsString(userInformationRequest)
        val requestBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            jsonObjectString
        )

        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(getString(R.string.date_momo_api) + getString(R.string.api_user_information))
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()

                if (!Utility.isConnected(baseContext)) {
                    displayDoubleButtonDialog()
                } else if (e.message!!.contains("after")) {
                    displaySingleButtonDialog(getString(R.string.poor_internet_title), getString(R.string.poor_internet_message))
                } else {
                    displaySingleButtonDialog(getString(R.string.server_error_title), getString(R.string.server_error_message))
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val myResponse: String = response.body()!!.string()

                val activityStackModel: ActivityStackModel =
                    mapper.readValue(sharedPreferences.getString(getString(R.string.activity_stack), "")!!)
                activityStackModel.activityStack.push(getString(R.string.activity_user_information))
                val activityStackString = mapper.writeValueAsString(activityStackModel)
                sharedPreferencesEditor.putString(getString(R.string.activity_stack), activityStackString)
                sharedPreferencesEditor.apply()

                Log.e(TAG, "The value of activityStackModel here is ${sharedPreferences.getString(getString(R.string.activity_stack), "")}")

                val intent = Intent(baseContext, UserInformationActivity::class.java)
                intent.putExtra("jsonResponse", myResponse)
                startActivity(intent)
            }
        })
    }

    private fun triggerRequestProcess() {
        when (requestProcess) {
            getString(R.string.request_fetch_user_information) -> fetchUserInformation()
        }
    }

    fun displayDoubleButtonDialog() {
        runOnUiThread {
            binding.doubleButtonDialog.doubleButtonTitle.text = getString(R.string.network_error_title)
            binding.doubleButtonDialog.doubleButtonMessage.text = getString(R.string.network_error_message)
            binding.doubleButtonDialog.doubleButtonLayout.visibility = View.VISIBLE
        }
    }

    fun displaySingleButtonDialog(title: String, message: String) {
        runOnUiThread {
            binding.singleButtonDialog.singleButtonTitle.text = title
            binding.singleButtonDialog.singleButtonMessage.text = message
            binding.singleButtonDialog.singleButtonLayout.visibility = View.VISIBLE
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
        const val TAG = "ImageSliderActivity"
    }
}


