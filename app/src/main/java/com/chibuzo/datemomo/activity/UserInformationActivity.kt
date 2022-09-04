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
import com.chibuzo.datemomo.model.ActivityStackModel
import com.chibuzo.datemomo.model.request.MessageRequest
import com.chibuzo.datemomo.model.request.OuterHomeDisplayRequest
import com.chibuzo.datemomo.model.request.UserLikerRequest
import com.chibuzo.datemomo.model.request.UserPictureRequest
import com.chibuzo.datemomo.model.response.HomeDisplayResponse
import com.chibuzo.datemomo.utility.Utility
import com.fasterxml.jackson.databind.DeserializationFeature
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

        binding.userMessageButton.iconHollowButtonLayout.setOnClickListener {
            binding.userMessageButton.iconHollowButtonLayout.startAnimation(buttonClickEffect)

            messageRequest = MessageRequest(
                sharedPreferences.getInt(getString(R.string.member_id), 0),
                homeDisplayResponse.memberId,
                homeDisplayResponse.fullName,
                homeDisplayResponse.userName.replaceFirstChar { it.uppercase() },
                "",
                homeDisplayResponse.profilePicture)

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

        try {
            val mapper = jacksonObjectMapper()
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            homeDisplayResponse = mapper.readValue(bundle.getString("jsonResponse")!!)
        } catch (exception: IOException) {
            exception.printStackTrace()
            finish()
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

        binding.userGay.blueButtonText.text = "Gay"
        binding.userToyBoy.blueButtonText.text = "Toy Boy"
        binding.userLesbian.blueButtonText.text = "Lesbian"
        binding.userToyGirl.blueButtonText.text = "Toy Girl"
        binding.userBisexual.blueButtonText.text = "Bisexual"
        binding.userStraight.blueButtonText.text = "Straight"
        binding.userSugarDaddy.blueButtonText.text = "Sugar Daddy"
        binding.userSugarMommy.blueButtonText.text = "Sugar Mommy"

        binding.gayInterest.blueButtonText.text = "Gay"
        binding.toyBoyInterest.blueButtonText.text = "Toy Boy"
        binding.lesbianInterest.blueButtonText.text = "Lesbian"
        binding.toyGirlInterest.blueButtonText.text = "Toy Girl"
        binding.bisexualInterest.blueButtonText.text = "Bisexual"
        binding.straightInterest.blueButtonText.text = "Straight"
        binding.friendshipInterest.blueButtonText.text = "Friendship"
        binding.sugarDaddyInterest.blueButtonText.text = "Sugar Daddy"
        binding.sugarMommyInterest.blueButtonText.text = "Sugar Mommy"
        binding.relationshipInterest.blueButtonText.text = "Relationship"

        binding.sixtyNineExperience.blueButtonText.text = "69"
        binding.analSexExperience.blueButtonText.text = "Anal Sex"
        binding.orgySexExperience.blueButtonText.text = "Orgy Sex"
        binding.poolSexExperience.blueButtonText.text = "Pool Sex"
        binding.carSexExperience.blueButtonText.text = "Sexed In Car"
        binding.threesomeExperience.blueButtonText.text = "Threesome"
        binding.givenHeadExperience.blueButtonText.text = "Given Head"
        binding.sexToyExperience.blueButtonText.text = "Used Sex Toys"
        binding.missionaryExperience.blueButtonText.text = "Missionary"
        binding.videoSexExperience.blueButtonText.text = "Video Sex Chat"
        binding.publicSexExperience.blueButtonText.text = "Sexed In Public"
        binding.receivedHeadExperience.blueButtonText.text = "Received Head"
        binding.cameraSexExperience.blueButtonText.text = "Sexed With Camera"
        binding.oneNightStandExperience.blueButtonText.text = "One-night Stand"

        val userFullName = homeDisplayResponse.fullName.ifEmpty {
            homeDisplayResponse.userName.replaceFirstChar { it.uppercase() }
        }

        binding.userInterestTitle.text = getString(R.string.title_interest, userFullName)
        binding.userSexualityTitle.text = getString(R.string.title_sexuality, userFullName)
        binding.userExperienceTitle.text = getString(R.string.title_experience, userFullName)

        binding.userStatusText.text = homeDisplayResponse.userStatus
        binding.userLocation.text = homeDisplayResponse.currentLocation

        binding.userFullName.text = getString(R.string.name_and_age_text,
            userFullName, homeDisplayResponse.age
        )

        if (homeDisplayResponse.bisexualCategory > 0) {
            binding.userBisexual.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.gayCategory > 0) {
            binding.userGay.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.lesbianCategory > 0) {
            binding.userLesbian.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.straightCategory > 0) {
            binding.userStraight.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.sugarDaddyCategory > 0) {
            binding.userSugarDaddy.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.sugarMommyCategory > 0) {
            binding.userSugarMommy.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.toyBoyCategory > 0) {
            binding.userToyBoy.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.toyGirlCategory > 0) {
            binding.userToyGirl.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.bisexualInterest > 0) {
            binding.bisexualInterest.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.friendshipInterest > 0) {
            binding.friendshipInterest.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.gayInterest > 0) {
            binding.gayInterest.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.relationshipInterest > 0) {
            binding.relationshipInterest.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.straightInterest > 0) {
            binding.straightInterest.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.lesbianInterest > 0) {
            binding.lesbianInterest.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.sugarDaddyInterest > 0) {
            binding.sugarDaddyInterest.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.sugarMommyInterest > 0) {
            binding.sugarMommyInterest.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.toyBoyInterest > 0) {
            binding.toyBoyInterest.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.toyGirlInterest > 0) {
            binding.toyGirlInterest.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.analSexExperience > 0) {
            binding.analSexExperience.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.missionaryExperience > 0) {
            binding.missionaryExperience.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.sixtyNineExperience > 0) {
            binding.sixtyNineExperience.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.cameraSexExperience > 0) {
            binding.cameraSexExperience.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.carSexExperience > 0) {
            binding.carSexExperience.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.threesomeExperience > 0) {
            binding.threesomeExperience.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.givenHeadExperience > 0) {
            binding.givenHeadExperience.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.receivedHeadExperience > 0) {
            binding.receivedHeadExperience.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.oneNightStandExperience > 0) {
            binding.oneNightStandExperience.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.orgySexExperience > 0) {
            binding.orgySexExperience.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.poolSexExperience > 0) {
            binding.poolSexExperience.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.sexToyExperience > 0) {
            binding.sexToyExperience.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.videoSexExperience > 0) {
            binding.videoSexExperience.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponse.publicSexExperience > 0) {
            binding.publicSexExperience.blueButtonLayout.visibility = View.VISIBLE
        }
    }

    override fun onBackPressed() {
        val mapper = jacksonObjectMapper()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        val activityStackModel: ActivityStackModel =
            mapper.readValue(sharedPreferences.getString(getString(R.string.activity_stack), "")!!)

        try {
            when (activityStackModel.activityStack.peek()) {
                getString(R.string.activity_messenger) -> {
                    requestProcess = getString(R.string.request_fetch_user_messengers)
                    fetchUserMessengers()
                }
                getString(R.string.activity_message) -> {
                    requestProcess = getString(R.string.request_fetch_user_messages)
                    fetchUserMessages()
                }
                getString(R.string.activity_user_information) -> {
                    activityStackModel.activityStack.pop()

                    val activityStackString = mapper.writeValueAsString(activityStackModel)
                    sharedPreferencesEditor.putString(getString(R.string.activity_stack), activityStackString)
                    sharedPreferencesEditor.apply()

                    this.onBackPressed()
                }
                getString(R.string.activity_home_display) -> {
                    requestProcess = getString(R.string.request_fetch_matched_users)
                    fetchMatchedUsers()
                }
                getString(R.string.activity_user_profile) -> {
                    requestProcess = getString(R.string.request_fetch_user_likers)
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
                else -> super.onBackPressed()
            }
        } catch (exception: EmptyStackException) {
            exception.printStackTrace()
            Log.e(TAG, "Exception from trying to peek activityStack here is ${exception.message}")
        }

        Log.e(TAG, "The value of activityStackModel here is ${sharedPreferences.getString(getString(R.string.activity_stack), "")}")
    }

    override fun onStart() {
        super.onStart()
        hideSystemUI()
    }

    private fun triggerRequestProcess() {
        when (requestProcess) {
            getString(R.string.request_fetch_user_messengers) -> fetchUserMessengers()
            getString(R.string.request_fetch_user_messages) -> fetchUserMessages()
            getString(R.string.request_fetch_matched_users) -> fetchMatchedUsers()
            getString(R.string.request_fetch_user_pictures) -> fetchUserPictures()
            getString(R.string.request_fetch_user_likers) -> fetchUserLikers()
        }
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

                val activityStackModel: ActivityStackModel =
                    mapper.readValue(sharedPreferences.getString(getString(R.string.activity_stack), "")!!)
                activityStackModel.activityStack.push(getString(R.string.activity_messenger))
                val activityStackString = mapper.writeValueAsString(activityStackModel)
                sharedPreferencesEditor.putString(getString(R.string.activity_stack), activityStackString)
                sharedPreferencesEditor.apply()

                Log.e(TAG, "The value of activityStackModel here is ${sharedPreferences.getString(getString(R.string.activity_stack), "")}")

                val intent = Intent(baseContext, MessengerActivity::class.java)
                intent.putExtra("jsonResponse", myResponse)
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

                val activityStackModel: ActivityStackModel =
                    mapper.readValue(sharedPreferences.getString(getString(R.string.activity_stack), "")!!)
                activityStackModel.activityStack.push(getString(R.string.activity_user_profile))
                val activityStackString = mapper.writeValueAsString(activityStackModel)
                sharedPreferencesEditor.putString(getString(R.string.activity_stack), activityStackString)
                sharedPreferencesEditor.apply()

                Log.e(TAG, "The value of activityStackModel here is ${sharedPreferences.getString(getString(R.string.activity_stack), "")}")

                val intent = Intent(baseContext, UserProfileActivity::class.java)
                intent.putExtra("jsonResponse", myResponse)
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

                val activityStackModel: ActivityStackModel =
                    mapper.readValue(sharedPreferences.getString(getString(R.string.activity_stack), "")!!)
                activityStackModel.activityStack.push(getString(R.string.activity_home_display))
                val activityStackString = mapper.writeValueAsString(activityStackModel)
                sharedPreferencesEditor.putString(getString(R.string.activity_stack), activityStackString)
                sharedPreferencesEditor.apply()

                Log.e(TAG, "The value of activityStackModel here is ${sharedPreferences.getString(getString(R.string.activity_stack), "")}")

                val intent = Intent(baseContext, HomeDisplayActivity::class.java)
                intent.putExtra("jsonResponse", myResponse)
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

                val activityStackModel: ActivityStackModel =
                    mapper.readValue(sharedPreferences.getString(getString(R.string.activity_stack), "")!!)
                activityStackModel.activityStack.push(getString(R.string.activity_message))
                val activityStackString = mapper.writeValueAsString(activityStackModel)
                sharedPreferencesEditor.putString(getString(R.string.activity_stack), activityStackString)
                sharedPreferencesEditor.apply()

                Log.e(TAG, "The value of activityStackModel here is ${sharedPreferences.getString(getString(R.string.activity_stack), "")}")

                val intent = Intent(baseContext, MessageActivity::class.java)
                intent.putExtra("profilePicture", messageRequest.profilePicture)
                intent.putExtra("lastActiveTime", messageRequest.lastActiveTime)
                intent.putExtra("receiverId", messageRequest.receiverId)
                intent.putExtra("userName", messageRequest.userName)
                intent.putExtra("senderId", messageRequest.senderId)
                intent.putExtra("fullName", messageRequest.fullName)
                intent.putExtra("jsonResponse", myResponse)
                startActivity(intent)
            }
        })
    }

    @Throws(IOException::class)
    fun fetchUserPictures() {
        val mapper = jacksonObjectMapper()
        val userPictureRequest = UserPictureRequest(
            homeDisplayResponse.memberId
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
                val activityStackModel: ActivityStackModel =
                    mapper.readValue(sharedPreferences.getString(getString(R.string.activity_stack), "")!!)

                val intent = if (requestedActivity == getString(R.string.activity_image_display)) {
                    activityStackModel.activityStack.push(getString(R.string.activity_image_display))
                    Intent(baseContext, ImageDisplayActivity::class.java)
                } else {
                    activityStackModel.activityStack.push(getString(R.string.activity_image_slider))
                    Intent(baseContext, ImageSliderActivity::class.java)
                }

                val activityStackString = mapper.writeValueAsString(activityStackModel)
                sharedPreferencesEditor.putString(getString(R.string.activity_stack), activityStackString)
                sharedPreferencesEditor.apply()

                Log.e(TAG, "The value of activityStackModel here is ${sharedPreferences.getString(getString(R.string.activity_stack), "")}")

                requestedActivity = ""

                intent.putExtra("memberId", homeDisplayResponse.memberId)
                intent.putExtra("jsonResponse", myResponse)
                intent.putExtra("currentPosition", 0)
                startActivity(intent)
            }
        })
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


