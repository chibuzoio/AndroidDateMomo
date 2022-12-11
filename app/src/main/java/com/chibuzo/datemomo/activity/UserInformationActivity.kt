package com.chibuzo.datemomo.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.chibuzo.datemomo.R
import com.chibuzo.datemomo.databinding.ActivityUserInformationBinding
import com.chibuzo.datemomo.model.ActivityInstanceModel
import com.chibuzo.datemomo.model.instance.*
import com.chibuzo.datemomo.model.request.MessageRequest
import com.chibuzo.datemomo.model.request.OuterHomeDisplayRequest
import com.chibuzo.datemomo.model.request.UserLikerRequest
import com.chibuzo.datemomo.model.request.UserPictureRequest
import com.chibuzo.datemomo.model.response.*
import com.chibuzo.datemomo.utility.Utility
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.*
import java.io.IOException
import java.util.*

class UserInformationActivity : AppCompatActivity() {
    private var deviceWidth: Int = 0
    private var deviceHeight: Int = 0
    private lateinit var bundle: Bundle
    private var requestProcess: String = ""
    private var requestedActivity: String = ""
    private var originalRequestProcess: String = ""
    private lateinit var messageRequest: MessageRequest
    private lateinit var buttonClickEffect: AlphaAnimation
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivityUserInformationBinding
    private lateinit var homeDisplayResponse: HomeDisplayResponse
    private lateinit var activitySavedInstance: ActivitySavedInstance
    private lateinit var sharedPreferencesEditor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserInformationBinding.inflate(layoutInflater)
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
            activitySavedInstance = mapper.readValue(bundle.getString(getString(R.string.activity_saved_instance))!!)
            homeDisplayResponse = mapper.readValue(activitySavedInstance.activityStateData)
        } catch (exception: IOException) {
            exception.printStackTrace()
            finish()
        }

        messageRequest = MessageRequest(
            sharedPreferences.getInt(getString(R.string.member_id), 0),
            homeDisplayResponse.memberId,
            homeDisplayResponse.fullName,
            homeDisplayResponse.userName.replaceFirstChar { it.uppercase() },
            "",
            homeDisplayResponse.profilePicture,
            homeDisplayResponse.userBlockedStatus)

        binding.userMessageButton.iconHollowButtonLayout.setOnClickListener {
            binding.userMessageButton.iconHollowButtonLayout.startAnimation(buttonClickEffect)
            requestProcess = getString(R.string.request_fetch_user_messages)
            fetchUserMessages()
        }

        binding.photoGalleryButton.iconHollowButtonLayout.setOnClickListener {
            binding.photoGalleryButton.iconHollowButtonLayout.startAnimation(buttonClickEffect)
            requestedActivity = getString(R.string.activity_image_display)
            requestProcess = getString(R.string.request_fetch_user_pictures)
            fetchUserPictures()
        }

        binding.profilePictureCover.setOnClickListener {
            requestedActivity = getString(R.string.activity_image_slider)
            requestProcess = getString(R.string.request_fetch_user_pictures)
            fetchUserPictures()
        }

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

        Glide.with(this)
            .asGif()
            .load(R.drawable.loading_puzzle)
            .into(binding.progressIconImage)

        Glide.with(this)
            .load(ColorDrawable(ContextCompat.getColor(this, R.color.grey_picture_placeholder)))
            .transform(CircleCrop())
            .into(binding.profilePicturePlaceholder)

        Glide.with(this)
            .load(getString(R.string.date_momo_api) + getString(R.string.api_image)
                    + homeDisplayResponse.profilePicture)
            .transform(CircleCrop(), CenterCrop())
            .into(binding.accountProfilePicture)

        binding.userMessageButton.iconHollowButtonText.text = "Message"
        binding.userMessageButton.iconHollowButtonIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_message_blue))
        binding.userMessageButton.iconHollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_grey_button)

        binding.photoGalleryButton.iconHollowButtonText.text = "Photos"
        binding.photoGalleryButton.iconHollowButtonIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_gallery_blue))
        binding.photoGalleryButton.iconHollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_grey_button)

        binding.userGay.blueLabelText.text = "Gay"
        binding.userToyBoy.blueLabelText.text = "Toy Boy"
        binding.userLesbian.blueLabelText.text = "Lesbian"
        binding.userToyGirl.blueLabelText.text = "Toy Girl"
        binding.userBisexual.blueLabelText.text = "Bisexual"
        binding.userStraight.blueLabelText.text = "Straight"
        binding.userSugarDaddy.blueLabelText.text = "Sugar Daddy"
        binding.userSugarMommy.blueLabelText.text = "Sugar Mommy"

        binding.gayInterest.blueLabelText.text = "Gay"
        binding.toyBoyInterest.blueLabelText.text = "Toy Boy"
        binding.lesbianInterest.blueLabelText.text = "Lesbian"
        binding.toyGirlInterest.blueLabelText.text = "Toy Girl"
        binding.bisexualInterest.blueLabelText.text = "Bisexual"
        binding.straightInterest.blueLabelText.text = "Straight"
        binding.friendshipInterest.blueLabelText.text = "Friendship"
        binding.sugarDaddyInterest.blueLabelText.text = "Sugar Daddy"
        binding.sugarMommyInterest.blueLabelText.text = "Sugar Mommy"
        binding.relationshipInterest.blueLabelText.text = "Relationship"

        binding.sixtyNineExperience.blueLabelText.text = "69"
        binding.analSexExperience.blueLabelText.text = "Anal Sex"
        binding.orgySexExperience.blueLabelText.text = "Orgy Sex"
        binding.poolSexExperience.blueLabelText.text = "Pool Sex"
        binding.carSexExperience.blueLabelText.text = "Sexed In Car"
        binding.threesomeExperience.blueLabelText.text = "Threesome"
        binding.givenHeadExperience.blueLabelText.text = "Given Head"
        binding.sexToyExperience.blueLabelText.text = "Used Sex Toys"
        binding.missionaryExperience.blueLabelText.text = "Missionary"
        binding.videoSexExperience.blueLabelText.text = "Video Sex Chat"
        binding.publicSexExperience.blueLabelText.text = "Sexed In Public"
        binding.receivedHeadExperience.blueLabelText.text = "Received Head"
        binding.cameraSexExperience.blueLabelText.text = "Sexed With Camera"
        binding.oneNightStandExperience.blueLabelText.text = "One-night Stand"

        val userFullName = homeDisplayResponse.fullName.ifEmpty {
            homeDisplayResponse.userName.replaceFirstChar { it.uppercase() }
        }

        binding.userInterestTitle.text = getString(R.string.title_interest, userFullName)
        binding.userSexualityTitle.text = getString(R.string.title_sexuality, userFullName)
        binding.userExperienceTitle.text = getString(R.string.title_experience, userFullName)

        binding.userStatusText.text = homeDisplayResponse.userStatus
        binding.userLocation.text = homeDisplayResponse.currentLocation.ifEmpty { "Location Not Set" }

        binding.userFullName.text = getString(R.string.name_and_age_text,
            userFullName, homeDisplayResponse.age
        )

        if (homeDisplayResponse.bisexualCategory > 0) {
            binding.userBisexual.blueLabelLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.gayCategory > 0) {
            binding.userGay.blueLabelLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.lesbianCategory > 0) {
            binding.userLesbian.blueLabelLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.straightCategory > 0) {
            binding.userStraight.blueLabelLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.sugarDaddyCategory > 0) {
            binding.userSugarDaddy.blueLabelLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.sugarMommyCategory > 0) {
            binding.userSugarMommy.blueLabelLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.toyBoyCategory > 0) {
            binding.userToyBoy.blueLabelLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.toyGirlCategory > 0) {
            binding.userToyGirl.blueLabelLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.bisexualInterest > 0) {
            binding.bisexualInterest.blueLabelLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.friendshipInterest > 0) {
            binding.friendshipInterest.blueLabelLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.gayInterest > 0) {
            binding.gayInterest.blueLabelLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.relationshipInterest > 0) {
            binding.relationshipInterest.blueLabelLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.straightInterest > 0) {
            binding.straightInterest.blueLabelLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.lesbianInterest > 0) {
            binding.lesbianInterest.blueLabelLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.sugarDaddyInterest > 0) {
            binding.sugarDaddyInterest.blueLabelLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.sugarMommyInterest > 0) {
            binding.sugarMommyInterest.blueLabelLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.toyBoyInterest > 0) {
            binding.toyBoyInterest.blueLabelLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.toyGirlInterest > 0) {
            binding.toyGirlInterest.blueLabelLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.analSexExperience > 0) {
            binding.analSexExperience.blueLabelLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.missionaryExperience > 0) {
            binding.missionaryExperience.blueLabelLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.sixtyNineExperience > 0) {
            binding.sixtyNineExperience.blueLabelLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.cameraSexExperience > 0) {
            binding.cameraSexExperience.blueLabelLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.carSexExperience > 0) {
            binding.carSexExperience.blueLabelLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.threesomeExperience > 0) {
            binding.threesomeExperience.blueLabelLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.givenHeadExperience > 0) {
            binding.givenHeadExperience.blueLabelLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.receivedHeadExperience > 0) {
            binding.receivedHeadExperience.blueLabelLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.oneNightStandExperience > 0) {
            binding.oneNightStandExperience.blueLabelLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.orgySexExperience > 0) {
            binding.orgySexExperience.blueLabelLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.poolSexExperience > 0) {
            binding.poolSexExperience.blueLabelLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.sexToyExperience > 0) {
            binding.sexToyExperience.blueLabelLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.videoSexExperience > 0) {
            binding.videoSexExperience.blueLabelLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.publicSexExperience > 0) {
            binding.publicSexExperience.blueLabelLayout.visibility = View.VISIBLE
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
                getString(R.string.activity_messenger) -> {
                    requestProcess = getString(R.string.request_fetch_user_messengers)
                    fetchUserMessengers()
                }
                getString(R.string.activity_message) -> {
                    requestProcess = getString(R.string.request_fetch_user_messages)
                    fetchUserMessages()
                }
                getString(R.string.activity_user_information) -> {
                    activityInstanceModel.activityInstanceStack.pop()

                    val activityInstanceModelString = mapper.writeValueAsString(activityInstanceModel)
                    sharedPreferencesEditor.putString(getString(R.string.activity_instance_model), activityInstanceModelString)
                    sharedPreferencesEditor.apply()

                    this.onBackPressed()
                }
                getString(R.string.activity_home_display) -> {
                    requestProcess = getString(R.string.request_fetch_matched_users)
                    fetchMatchedUsers()
                }
                getString(R.string.activity_user_profile) -> {
                    requestProcess = getString(R.string.request_fetch_user_information)
                    fetchUserLikers()
                }
                getString(R.string.activity_image_display) -> {
                    requestProcess = getString(R.string.request_fetch_user_pictures)
                    fetchUserPictures()
                }
                getString(R.string.activity_image_slider) -> {
                    requestProcess = getString(R.string.request_fetch_user_pictures)
                    fetchUserPictures()
                }
                getString(R.string.activity_user_account) -> {
                    requestProcess = getString(R.string.request_fetch_liked_users)
                    fetchLikedUsers()
                }
                getString(R.string.activity_all_likers) -> {
                    requestProcess = getString(R.string.request_fetch_all_likers)
                    fetchAllLikers()
                }
                getString(R.string.activity_all_liked) -> {
                    requestProcess = getString(R.string.request_fetch_all_liked)
                    fetchAllLiked()
                }
                getString(R.string.activity_notification) -> {
                    requestProcess = getString(R.string.request_fetch_notifications)
                    fetchNotifications()
                }
                else -> super.onBackPressed()
            }
        } catch (exception: EmptyStackException) {
            exception.printStackTrace()
            Log.e(TAG, "Exception from trying to peek activityStack here is ${exception.message}")
        }
    }

    override fun onStart() {
        super.onStart()
        hideSystemUI()
    }

    private fun triggerRequestProcess() {
        when (requestProcess) {
            getString(R.string.request_fetch_user_messages) -> fetchUserMessages()
            getString(R.string.request_fetch_user_pictures) -> fetchUserPictures()
        }
    }

    @Throws(IOException::class)
    fun fetchNotifications() {
        val mapper = jacksonObjectMapper()
        val userLikerRequest =
            UserLikerRequest(sharedPreferences.getInt(getString(R.string.member_id), 0))

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        val jsonObjectString = mapper.writeValueAsString(userLikerRequest)
        val requestBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            jsonObjectString
        )

        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(getString(R.string.date_momo_api) + getString(R.string.api_user_notifications))
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
                val notificationResponses: java.util.ArrayList<NotificationResponse> = mapper.readValue(myResponse)
                var notificationInstance = NotificationInstance(
                    scrollToPosition = 0,
                    notificationResponses = notificationResponses)

                val activityInstanceModel: ActivityInstanceModel =
                    mapper.readValue(sharedPreferences.getString(getString(R.string.activity_instance_model), "")!!)

                try {
                    if (activityInstanceModel.activityInstanceStack.peek().activity ==
                        getString(R.string.activity_notification)) {
                        activitySavedInstance = activityInstanceModel.activityInstanceStack.peek()
                        notificationInstance = mapper.readValue(activitySavedInstance.activityStateData)
                    }

                    val activityStateData = mapper.writeValueAsString(notificationInstance)

                    activitySavedInstance = ActivitySavedInstance(
                        activity = getString(R.string.activity_notification),
                        activityStateData = activityStateData)

                    if (activityInstanceModel.activityInstanceStack.peek().activity != getString(
                            R.string.activity_notification
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
                val intent = Intent(this@UserInformationActivity, NotificationActivity::class.java)
                intent.putExtra(getString(R.string.activity_saved_instance), activitySavedInstanceString)
                startActivity(intent)
            }
        })
    }

    @Throws(IOException::class)
    fun fetchAllLiked() {
        val mapper = jacksonObjectMapper()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        val activitySavedInstanceString = mapper.writeValueAsString(activitySavedInstance)
        val intent = Intent(this@UserInformationActivity, AllLikedActivity::class.java)
        intent.putExtra(getString(R.string.activity_saved_instance), activitySavedInstanceString)
        startActivity(intent)
    }

    @Throws(IOException::class)
    fun fetchAllLikers() {
        val mapper = jacksonObjectMapper()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        val activitySavedInstanceString = mapper.writeValueAsString(activitySavedInstance)
        val intent = Intent(this@UserInformationActivity, AllLikersActivity::class.java)
        intent.putExtra(getString(R.string.activity_saved_instance), activitySavedInstanceString)
        startActivity(intent)
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
                val intent = Intent(this@UserInformationActivity, UserAccountActivity::class.java)
                intent.putExtra(getString(R.string.activity_saved_instance), activitySavedInstanceString)
                startActivity(intent)
            }
        })
    }

    @Throws(IOException::class)
    fun fetchUserLikers() {
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
            .url(getString(R.string.date_momo_api) + getString(R.string.api_user_likers_data))
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
                val userLikerResponses: java.util.ArrayList<UserLikerResponse> = mapper.readValue(myResponse)
                val userProfileInstance = UserProfileInstance(userLikerResponses)

                val activityStateData = mapper.writeValueAsString(userProfileInstance)

                val activityInstanceModel: ActivityInstanceModel =
                    mapper.readValue(sharedPreferences.getString(getString(R.string.activity_instance_model), "")!!)

                try {
                    activitySavedInstance = ActivitySavedInstance(
                        activity = getString(R.string.activity_user_profile),
                        activityStateData = activityStateData)

                    if (activityInstanceModel.activityInstanceStack.peek().activity != getString(
                            R.string.activity_user_profile
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

                Log.e(TAG, "The number of activities on the stack here is ${activityInstanceModel.activityInstanceStack.size}")

                val activitySavedInstanceString = mapper.writeValueAsString(activitySavedInstance)
                val intent = Intent(this@UserInformationActivity, UserProfileActivity::class.java)
                intent.putExtra(getString(R.string.activity_saved_instance), activitySavedInstanceString)
                startActivity(intent)
            }
        })
    }

    @Throws(IOException::class)
    fun fetchMatchedUsers() {
        val mapper = jacksonObjectMapper()
        val homeDisplayRequest = OuterHomeDisplayRequest(
            sharedPreferences.getInt(getString(R.string.member_id), 0),
            sharedPreferences.getInt(getString(R.string.age), 0),
            sharedPreferences.getString(getString(R.string.sex), "")!!,
            sharedPreferences.getString(getString(R.string.registration_date), "")!!,
            sharedPreferences.getInt(getString(R.string.bisexual_category), 0),
            sharedPreferences.getInt(getString(R.string.gay_category), 0),
            sharedPreferences.getInt(getString(R.string.lesbian_category), 0),
            sharedPreferences.getInt(getString(R.string.straight_category), 0),
            sharedPreferences.getInt(getString(R.string.sugar_daddy_category), 0),
            sharedPreferences.getInt(getString(R.string.sugar_mommy_category), 0),
            sharedPreferences.getInt(getString(R.string.toy_boy_category), 0),
            sharedPreferences.getInt(getString(R.string.toy_girl_category), 0),
            sharedPreferences.getInt(getString(R.string.bisexual_interest), 0),
            sharedPreferences.getInt(getString(R.string.gay_interest), 0),
            sharedPreferences.getInt(getString(R.string.lesbian_interest), 0),
            sharedPreferences.getInt(getString(R.string.straight_interest), 0),
            sharedPreferences.getInt(getString(R.string.friendship_interest), 0),
            sharedPreferences.getInt(getString(R.string.sugar_daddy_interest), 0),
            sharedPreferences.getInt(getString(R.string.sugar_mommy_interest), 0),
            sharedPreferences.getInt(getString(R.string.relationship_interest), 0),
            sharedPreferences.getInt(getString(R.string.toy_boy_interest), 0),
            sharedPreferences.getInt(getString(R.string.toy_girl_interest), 0),
            sharedPreferences.getInt(getString(R.string.sixty_nine_experience), 0),
            sharedPreferences.getInt(getString(R.string.anal_sex_experience), 0),
            sharedPreferences.getInt(getString(R.string.given_head_experience), 0),
            sharedPreferences.getInt(getString(R.string.missionary_experience), 0),
            sharedPreferences.getInt(getString(R.string.one_night_stand_experience), 0),
            sharedPreferences.getInt(getString(R.string.orgy_experience), 0),
            sharedPreferences.getInt(getString(R.string.pool_sex_experience), 0),
            sharedPreferences.getInt(getString(R.string.received_head_experience), 0),
            sharedPreferences.getInt(getString(R.string.car_sex_experience), 0),
            sharedPreferences.getInt(getString(R.string.public_sex_experience), 0),
            sharedPreferences.getInt(getString(R.string.camera_sex_experience), 0),
            sharedPreferences.getInt(getString(R.string.threesome_experience), 0),
            sharedPreferences.getInt(getString(R.string.sex_toy_experience), 0),
            sharedPreferences.getInt(getString(R.string.video_sex_experience), 0))

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        val jsonObjectString = mapper.writeValueAsString(homeDisplayRequest)
        val requestBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            jsonObjectString
        )

        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(getString(R.string.date_momo_api) + getString(R.string.api_matched_user_data))
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
                val outerHomeDisplayResponse: OuterHomeDisplayResponse = mapper.readValue(myResponse)
                var homeDisplayInstance = HomeDisplayInstance(
                    scrollToPosition = 0,
                    outerHomeDisplayResponse = outerHomeDisplayResponse)

                val activityInstanceModel: ActivityInstanceModel =
                    mapper.readValue(sharedPreferences.getString(getString(R.string.activity_instance_model), "")!!)

                try {
                    if (activityInstanceModel.activityInstanceStack.peek().activity ==
                        getString(R.string.activity_home_display)) {
                        activitySavedInstance = activityInstanceModel.activityInstanceStack.peek()
                        homeDisplayInstance = mapper.readValue(activitySavedInstance.activityStateData)
                    }

                    val activityStateData = mapper.writeValueAsString(homeDisplayInstance)

                    activitySavedInstance = ActivitySavedInstance(
                        activity = getString(R.string.activity_home_display),
                        activityStateData = activityStateData)

                    if (activityInstanceModel.activityInstanceStack.peek().activity != getString(
                            R.string.activity_home_display
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
                val intent = Intent(this@UserInformationActivity, HomeDisplayActivity::class.java)
                intent.putExtra(getString(R.string.activity_saved_instance), activitySavedInstanceString)
                startActivity(intent)
            }
        })
    }

    @Throws(IOException::class)
    fun fetchUserMessengers() {
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
            .url(getString(R.string.date_momo_api) + getString(R.string.api_user_messengers_data))
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
                val messengerResponses: ArrayList<MessengerResponse> = mapper.readValue(myResponse)
                val messengerInstance = MessengerInstance(
                    scrollToPosition = 0,
                    messengerResponses = messengerResponses)

                val activityInstanceModel: ActivityInstanceModel =
                    mapper.readValue(sharedPreferences.getString(getString(R.string.activity_instance_model), "")!!)

                try {
                    // This is not required here because messenger activity always
                    // needs to be refreshed when it's newly navigated to
/*
                if (activityInstanceModel.activityInstanceStack.peek().activity ==
                    getString(R.string.activity_messenger)) {
                    activitySavedInstance = activityInstanceModel.activityInstanceStack.peek()
                    messengerInstance = mapper.readValue(activitySavedInstance.activityStateData)
                }
*/

                    val activityStateData = mapper.writeValueAsString(messengerInstance)

                    activitySavedInstance = ActivitySavedInstance(
                        activity = getString(R.string.activity_messenger),
                        activityStateData = activityStateData)

                    if (activityInstanceModel.activityInstanceStack.peek().activity != getString(
                            R.string.activity_messenger
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

                Log.e(TAG, "The number of activities on the stack here is ${activityInstanceModel.activityInstanceStack.size}")

                val activitySavedInstanceString = mapper.writeValueAsString(activitySavedInstance)
                val intent = Intent(this@UserInformationActivity, MessengerActivity::class.java)
                intent.putExtra(getString(R.string.activity_saved_instance), activitySavedInstanceString)
                startActivity(intent)
            }
        })
    }

    @Throws(IOException::class)
    fun fetchUserMessages() {
        val mapper = jacksonObjectMapper()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        val jsonObjectString = mapper.writeValueAsString(messageRequest)
        val requestBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            jsonObjectString
        )

        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(getString(R.string.date_momo_api) + getString(R.string.api_user_messages_data))
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()

                runOnUiThread {

                }

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
                val messageResponses: ArrayList<MessageResponse> = mapper.readValue(myResponse)
                val messageInstance = MessageInstance(
                    senderId = messageRequest.senderId,
                    fullName = messageRequest.fullName,
                    userName = messageRequest.userName,
                    scrollToPosition = messageResponses.size - 1,
                    receiverId = messageRequest.receiverId,
                    lastActiveTime = messageRequest.lastActiveTime,
                    profilePicture = messageRequest.profilePicture,
                    userBlockedStatus = messageRequest.userBlockedStatus,
                    messageResponses = messageResponses)

                val activityStateData = mapper.writeValueAsString(messageInstance)

                val activityInstanceModel: ActivityInstanceModel =
                    mapper.readValue(sharedPreferences.getString(getString(R.string.activity_instance_model), "")!!)

                try {
                    activitySavedInstance = ActivitySavedInstance(
                        activity = getString(R.string.activity_message),
                        activityStateData = activityStateData)

                    if (activityInstanceModel.activityInstanceStack.peek().activity != getString(
                            R.string.activity_message
                        )) {
                        activityInstanceModel.activityInstanceStack.push(activitySavedInstance)
                    } else {
                        activityInstanceModel.activityInstanceStack.pop()
                        activityInstanceModel.activityInstanceStack.push(activitySavedInstance)
                    }

                    commitInstanceModel(mapper, activityInstanceModel)
                } catch (exception: EmptyStackException) {
                    exception.printStackTrace()
                    Log.e(HomeDisplayActivity.TAG, "Exception from trying to peek and pop activityInstanceStack here is ${exception.message}")
                }

                Log.e(TAG, "The number of activities on the stack here is ${activityInstanceModel.activityInstanceStack.size}")

                val activitySavedInstanceString = mapper.writeValueAsString(activitySavedInstance)
                val intent = Intent(this@UserInformationActivity, MessageActivity::class.java)
                intent.putExtra(getString(R.string.activity_saved_instance), activitySavedInstanceString)
                startActivity(intent)
            }
        })
    }

    @Throws(IOException::class)
    fun fetchUserPictures() {
        val mapper = jacksonObjectMapper()
        val userPictureRequest = UserPictureRequest(
            memberId = homeDisplayResponse.memberId,
            currentPosition = 0,
        )

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        val jsonObjectString = mapper.writeValueAsString(userPictureRequest)
        val requestBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            jsonObjectString
        )

        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(getString(R.string.date_momo_api) + getString(R.string.api_user_picture))
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

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val myResponse: String = response.body()!!.string()
                val userPictureResponses: ArrayList<UserPictureResponse> = mapper.readValue(myResponse)

                val activityInstanceModel: ActivityInstanceModel =
                    mapper.readValue(sharedPreferences.getString(getString(R.string.activity_instance_model), "")!!)

                val intent = if (requestedActivity == getString(R.string.activity_image_display)) {
                    var imageDisplayInstance = ImageDisplayInstance(
                        memberId = homeDisplayResponse.memberId,
                        scrollToPosition = 0,
                        userPictureResponses = userPictureResponses)

                    try {
                        if (activityInstanceModel.activityInstanceStack.peek().activity ==
                            getString(R.string.activity_image_display)) {
                            activitySavedInstance = activityInstanceModel.activityInstanceStack.peek()
                            imageDisplayInstance = mapper.readValue(activitySavedInstance.activityStateData)
                        }

                        val activityStateData = mapper.writeValueAsString(imageDisplayInstance)

                        activitySavedInstance = ActivitySavedInstance(
                            activity = getString(R.string.activity_image_display),
                            activityStateData = activityStateData)

                        if (activityInstanceModel.activityInstanceStack.peek().activity != getString(
                                R.string.activity_image_display
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

                    Intent(this@UserInformationActivity, ImageDisplayActivity::class.java)
                } else {
                    var imageSliderInstance = ImageSliderInstance(
                        memberId = homeDisplayResponse.memberId,
                        currentPosition = 0,
                        userPictureResponses = userPictureResponses)

                    try {
                        if (activityInstanceModel.activityInstanceStack.peek().activity ==
                            getString(R.string.activity_image_slider)) {
                            activitySavedInstance = activityInstanceModel.activityInstanceStack.peek()
                            imageSliderInstance = mapper.readValue(activitySavedInstance.activityStateData)
                        }

                        val activityStateData = mapper.writeValueAsString(imageSliderInstance)

                        activitySavedInstance = ActivitySavedInstance(
                            activity = getString(R.string.activity_image_slider),
                            activityStateData = activityStateData)

                        if (activityInstanceModel.activityInstanceStack.peek().activity != getString(
                                R.string.activity_image_slider
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

                    Intent(this@UserInformationActivity, ImageSliderActivity::class.java)
                }

                requestedActivity = ""

                Log.e(TAG, "The number of activities on the stack here is ${activityInstanceModel.activityInstanceStack.size}")

                val activitySavedInstanceString = mapper.writeValueAsString(activitySavedInstance)
                intent.putExtra(getString(R.string.activity_saved_instance), activitySavedInstanceString)
                startActivity(intent)
            }
        })
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

    companion object {
        const val TAG = "UserInformationActivity"
    }
}


