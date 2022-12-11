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
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chibuzo.datemomo.R
import com.chibuzo.datemomo.adapter.AllLikersAdapter
import com.chibuzo.datemomo.databinding.ActivityAllLikersBinding
import com.chibuzo.datemomo.model.ActivityInstanceModel
import com.chibuzo.datemomo.model.AllLikersModel
import com.chibuzo.datemomo.model.instance.ActivitySavedInstance
import com.chibuzo.datemomo.model.instance.AllLikersInstance
import com.chibuzo.datemomo.model.request.UserInformationRequest
import com.chibuzo.datemomo.model.response.HomeDisplayResponse
import com.chibuzo.datemomo.model.response.UserLikerResponse
import com.chibuzo.datemomo.utility.Utility
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.*
import java.io.IOException
import java.util.*

class AllLikersActivity : AppCompatActivity() {
    private var deviceWidth: Int = 0
    private var deviceHeight: Int = 0
    private lateinit var bundle: Bundle
    private var requestProcess: String = ""
    private var leastRootViewHeight: Int = 0
    private lateinit var binding: ActivityAllLikersBinding
    private lateinit var buttonClickEffect: AlphaAnimation
    private lateinit var allLikersInstance: AllLikersInstance
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var activitySavedInstance: ActivitySavedInstance
    private lateinit var userInformationRequest: UserInformationRequest
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
            activitySavedInstance = mapper.readValue(bundle.getString(getString(R.string.activity_saved_instance))!!)
            allLikersInstance = mapper.readValue(activitySavedInstance.activityStateData)

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
                AllLikersModel(sharedPreferences.getInt(getString(R.string.member_id), 0),
                    deviceWidth, "", this)

            val allLikersAdapter = AllLikersAdapter(allLikersInstance.userLikerResponses, allLikersModel)
            binding.allLikersRecyclerView.adapter = allLikersAdapter
            binding.allLikersRecyclerView.layoutManager!!.scrollToPosition(allLikersInstance.scrollToPosition)
        } catch (exception: IOException) {
            exception.printStackTrace()
            Log.e(TAG, "Error message from here is ${exception.message}")
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val mapper = jacksonObjectMapper()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        val activityInstanceModel: ActivityInstanceModel =
            mapper.readValue(sharedPreferences.getString(getString(R.string.activity_instance_model), "")!!)

        try {
            when (activityInstanceModel.activityInstanceStack.peek().activity) {
                getString(R.string.activity_all_likers) -> {
                    activityInstanceModel.activityInstanceStack.pop()

                    val activityInstanceModelString = mapper.writeValueAsString(activityInstanceModel)
                    sharedPreferencesEditor.putString(getString(R.string.activity_instance_model), activityInstanceModelString)
                    sharedPreferencesEditor.apply()

                    this.onBackPressed()
                }
                getString(R.string.activity_user_profile) -> super.onBackPressed()
                else -> super.onBackPressed()
            }
        } catch (exception: EmptyStackException) {
            exception.printStackTrace()
            Log.e(TAG, "Exception from trying to peek activityStack here is ${exception.message}")
        }
    }

    private fun triggerRequestProcess() {
        when (requestProcess) {

        }
    }

    @Throws(IOException::class)
    fun fetchUserInformation(userInformationRequest: UserInformationRequest) {
        val mapper = jacksonObjectMapper()
        this.userInformationRequest = userInformationRequest
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        val jsonObjectString = mapper.writeValueAsString(this.userInformationRequest)
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
                val homeDisplayResponse: HomeDisplayResponse = mapper.readValue(myResponse)

                val activityStateData = mapper.writeValueAsString(homeDisplayResponse)

                val activityInstanceModel: ActivityInstanceModel =
                    mapper.readValue(sharedPreferences.getString(getString(R.string.activity_instance_model), "")!!)

                try {
                    updateAllLikersInstance(activityInstanceModel)

                    // Always do this below the method above, updateAllLikersInstance
                    activitySavedInstance = ActivitySavedInstance(
                        activity = getString(R.string.activity_user_information),
                        activityStateData = activityStateData)

                    if (activityInstanceModel.activityInstanceStack.peek().activity != getString(
                            R.string.activity_user_information
                        )) {
                        activityInstanceModel.activityInstanceStack.push(activitySavedInstance)
                    } else {
                        activityInstanceModel.activityInstanceStack.pop()
                        activityInstanceModel.activityInstanceStack.push(activitySavedInstance)
                    }

                    commitInstanceModel(mapper, activityInstanceModel)
                } catch (exception: EmptyStackException) {
                    exception.printStackTrace()
                    Log.e(TAG, "Exception from trying to peek and pop activityInstanceStack here is ${exception.message}")
                }

                val activitySavedInstanceString = mapper.writeValueAsString(activitySavedInstance)
                val intent = Intent(this@AllLikersActivity, UserInformationActivity::class.java)
                intent.putExtra(getString(R.string.activity_saved_instance), activitySavedInstanceString)
                startActivity(intent)
            }
        })
    }

    private fun updateAllLikersInstance(activityInstanceModel: ActivityInstanceModel) {
        if (activityInstanceModel.activityInstanceStack.peek().activity == getString(R.string.activity_all_likers)) {
            val scrollToPosition =
                (binding.allLikersRecyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            activityInstanceModel.activityInstanceStack.pop()

            val allLikersInstance = AllLikersInstance(
                scrollToPosition = scrollToPosition,
                userLikerResponses = allLikersInstance.userLikerResponses)

            val mapper = jacksonObjectMapper()
            val activityStateData = mapper.writeValueAsString(allLikersInstance)

            activitySavedInstance = ActivitySavedInstance(
                activity = getString(R.string.activity_all_likers),
                activityStateData = activityStateData)

            activityInstanceModel.activityInstanceStack.push(activitySavedInstance)
        }
    }

    private fun commitInstanceModel(mapper: ObjectMapper, activityInstanceModel: ActivityInstanceModel) {
        val activityInstanceModelString =
            mapper.writeValueAsString(activityInstanceModel)
        sharedPreferencesEditor.putString(
            getString(R.string.activity_instance_model),
            activityInstanceModelString
        )
        sharedPreferencesEditor.apply()
    }

    private fun append(userLikerResponse: UserLikerResponse): Array<UserLikerResponse> {
        val userLikerResponseList = allLikersInstance.userLikerResponses.toMutableList()
        userLikerResponseList.add(userLikerResponse)
        return userLikerResponseList.toTypedArray()
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
        const val TAG = "AllLikersActivity"
    }
}


