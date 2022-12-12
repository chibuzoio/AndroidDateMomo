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
import com.chibuzo.datemomo.adapter.AllLikedAdapter
import com.chibuzo.datemomo.databinding.ActivityAllLikedBinding
import com.chibuzo.datemomo.model.ActivityInstanceModel
import com.chibuzo.datemomo.model.AllLikersModel
import com.chibuzo.datemomo.model.instance.ActivitySavedInstance
import com.chibuzo.datemomo.model.instance.AllLikedInstance
import com.chibuzo.datemomo.model.instance.UserAccountInstance
import com.chibuzo.datemomo.model.request.UserInformationRequest
import com.chibuzo.datemomo.model.request.UserLikerRequest
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

class AllLikedActivity : AppCompatActivity() {
    private var deviceWidth: Int = 0
    private var deviceHeight: Int = 0
    private lateinit var bundle: Bundle
    private var requestProcess: String = ""
    private var leastRootViewHeight: Int = 0
    private lateinit var binding: ActivityAllLikedBinding
    private lateinit var buttonClickEffect: AlphaAnimation
    private lateinit var allLikedInstance: AllLikedInstance
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var activitySavedInstance: ActivitySavedInstance
    private lateinit var userInformationRequest: UserInformationRequest
    private lateinit var sharedPreferencesEditor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAllLikedBinding.inflate(layoutInflater)
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
            Log.e(TAG, "Execution passed through 1")
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            Log.e(TAG, "Execution passed through 2")
            activitySavedInstance = mapper.readValue(bundle.getString(getString(R.string.activity_saved_instance))!!)
            Log.e(TAG, "Execution passed through 3")
            Log.e(TAG, "The value of this here is ${activitySavedInstance.activityStateData}")
            allLikedInstance = mapper.readValue(activitySavedInstance.activityStateData)
            Log.e(TAG, "Execution passed through 4")

            val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
            binding.allLikedRecyclerView.layoutManager = layoutManager
            binding.allLikedRecyclerView.itemAnimator = DefaultItemAnimator()

            val allLikersModel =
                AllLikersModel(sharedPreferences.getInt(getString(R.string.member_id), 0),
                    deviceWidth, "", this)

            val allLikersAdapter = AllLikedAdapter(allLikedInstance.userLikerResponses, allLikersModel)
            binding.allLikedRecyclerView.adapter = allLikersAdapter
            binding.allLikedRecyclerView.layoutManager!!.scrollToPosition(allLikedInstance.scrollToPosition)
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
                getString(R.string.activity_all_liked) -> {
                    activityInstanceModel.activityInstanceStack.pop()

                    val activityInstanceModelString = mapper.writeValueAsString(activityInstanceModel)
                    sharedPreferencesEditor.putString(getString(R.string.activity_instance_model), activityInstanceModelString)
                    sharedPreferencesEditor.apply()

                    this.onBackPressed()
                }
                getString(R.string.activity_user_account) -> {
                    requestProcess = getString(R.string.request_fetch_liked_users)
                    fetchLikedUsers()
                }
                getString(R.string.activity_user_profile) -> super.onBackPressed()
                else -> super.onBackPressed()
            }
        } catch (exception: EmptyStackException) {
            exception.printStackTrace()
            Log.e(AllLikersActivity.TAG, "Exception from trying to peek activityStack here is ${exception.message}")
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
                    updateAllLikedInstance(activityInstanceModel)

                    // Always do this below the method above, updateAllLikedInstance
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
                val intent = Intent(this@AllLikedActivity, UserInformationActivity::class.java)
                intent.putExtra(getString(R.string.activity_saved_instance), activitySavedInstanceString)
                startActivity(intent)
            }
        })
    }

    @Throws(IOException::class)
    fun fetchLikedUsers() {
        val mapper = jacksonObjectMapper()
        val userLikerRequest = UserLikerRequest(sharedPreferences.getInt(getString(R.string.member_id), 0))

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        val jsonObjectString = mapper.writeValueAsString(userLikerRequest)
        val requestBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            jsonObjectString
        )

        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(getString(R.string.date_momo_api) + getString(R.string.api_liked_users_data))
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
                val userLikerResponses: ArrayList<UserLikerResponse> = mapper.readValue(myResponse)
                val userAccountInstance = UserAccountInstance(userLikerResponses)

                val activityStateData = mapper.writeValueAsString(userAccountInstance)

                val activityInstanceModel: ActivityInstanceModel =
                    mapper.readValue(sharedPreferences.getString(getString(R.string.activity_instance_model), "")!!)

                try {
                    updateAllLikedInstance(activityInstanceModel)

                    // Always do this below the method above, updateAllLikedInstance
                    activitySavedInstance = ActivitySavedInstance(
                        activity = getString(R.string.activity_user_account),
                        activityStateData = activityStateData)

                    if (activityInstanceModel.activityInstanceStack.peek().activity != getString(
                            R.string.activity_user_account
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
                val intent = Intent(this@AllLikedActivity, UserAccountActivity::class.java)
                intent.putExtra(getString(R.string.activity_saved_instance), activitySavedInstanceString)
                startActivity(intent)
            }
        })
    }

    private fun updateAllLikedInstance(activityInstanceModel: ActivityInstanceModel) {
        if (activityInstanceModel.activityInstanceStack.peek().activity == getString(R.string.activity_all_liked)) {
            val scrollToPosition =
                (binding.allLikedRecyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            activityInstanceModel.activityInstanceStack.pop()

            val allLikedInstance = AllLikedInstance(
                scrollToPosition = scrollToPosition,
                userLikerResponses = allLikedInstance.userLikerResponses)

            val mapper = jacksonObjectMapper()
            val activityStateData = mapper.writeValueAsString(allLikedInstance)

            activitySavedInstance = ActivitySavedInstance(
                activity = getString(R.string.activity_all_liked),
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

    private fun triggerRequestProcess() {
        when (requestProcess) {
            getString(R.string.request_fetch_liked_users) -> fetchLikedUsers()
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
        const val TAG = "AllLikedActivity"
    }
}


