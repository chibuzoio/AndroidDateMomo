package com.chibuzo.datemomo.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.marginStart
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.chibuzo.datemomo.R
import com.chibuzo.datemomo.databinding.ActivityUserProfileBinding
import com.chibuzo.datemomo.model.ActivityInstanceModel
import com.chibuzo.datemomo.model.instance.*
import com.chibuzo.datemomo.model.request.*
import com.chibuzo.datemomo.model.response.*
import com.chibuzo.datemomo.service.LocationTracker
import com.chibuzo.datemomo.utility.Utility
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.*
import java.io.File
import java.io.IOException
import java.util.*

class UserProfileActivity : AppCompatActivity() {
    private var deviceWidth: Int = 0
    private var deviceHeight: Int = 0
    private lateinit var bundle: Bundle
    private var photoFile: File? = null
    private val PICK_IMAGE_REQUEST = 200
    private var theBitmap: Bitmap? = null
    private val CAPTURE_IMAGE_REQUEST = 100
    private var requestProcess: String = ""
    private var requestedActivity: String = ""
    private var userUpdatedLocation: String = ""
    private lateinit var buttonClickEffect: AlphaAnimation
    private lateinit var binding: ActivityUserProfileBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var userProfileInstance: UserProfileInstance
    private lateinit var activitySavedInstance: ActivitySavedInstance
    private lateinit var userInformationRequest: UserInformationRequest
    private lateinit var sharedPreferencesEditor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserProfileBinding.inflate(layoutInflater)
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

        redrawBottomMenuIcons(getString(R.string.clicked_account_menu))

        checkMessageUpdate()
        checkNotificationUpdate()

        if (LocationTracker(this).canGetLocation) {
            val latitude = LocationTracker(this).getLatitude()
            val longitude = LocationTracker(this).getLongitude()

            val addresses: List<Address>
            val geocoder = Geocoder(this, Locale.getDefault())

            try {
                addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    1
                ) as List<Address> // Here 1 represent max location result to returned, by documents it recommended 1 to 5

                val address: String =
                    addresses[0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()

                val city = addresses[0].locality
                val state = addresses[0].adminArea
                val country = addresses[0].countryName
                val postalCode = addresses[0].postalCode
                val knownName = addresses[0].featureName

                userUpdatedLocation = if (!city.isNullOrEmpty()) {
                    city
                } else if (!state.isNullOrEmpty()) {
                    state
                } else {
                    country
                }

                if (sharedPreferences.getString(getString(R.string.current_location), "").isNullOrEmpty()) {
                    sharedPreferencesEditor.putString(getString(R.string.current_location), userUpdatedLocation)
                    sharedPreferencesEditor.apply()

                    requestProcess = getString(R.string.request_update_current_location)
                    updateCurrentLocation()
                } else {
                    sharedPreferencesEditor.putString(getString(R.string.updated_location), userUpdatedLocation)
                    sharedPreferencesEditor.apply()

                    // notify user of location change here
                }
            } catch (exception: Exception) {
                exception.printStackTrace()

                when (exception) {
                    is IOException -> {
                        Log.e(TAG, "IOException was caught, with message = ${exception.message}")
                    }
                    is IndexOutOfBoundsException -> {
                        Log.e(TAG, "IndexOutOfBoundsException was caught, with message = ${exception.message}")
                    }
                    else -> {
                        Log.e(TAG, "Error message from here is ${exception.message}")
                    }
                }
            }
        }

        try {
            val mapper = jacksonObjectMapper()
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            activitySavedInstance = mapper.readValue(bundle.getString(getString(R.string.activity_saved_instance))!!)
            userProfileInstance = mapper.readValue(activitySavedInstance.activityStateData)

            if (userProfileInstance.userLikerResponses.size > 1) {
                binding.allLikesCount.text = getString(R.string.many_likers_count,
                    userProfileInstance.userLikerResponses.size)
            }

            if (userProfileInstance.userLikerResponses.size == 1) {
                binding.allLikesCount.text = getString(R.string.single_liker_count)
            }

            if (userProfileInstance.userLikerResponses.isEmpty()) {
                binding.allLikersDisplayLayout.visibility = View.GONE
            } else {
                binding.allLikersFirstLayout.visibility = View.VISIBLE

                if (userProfileInstance.userLikerResponses.size == 1) {
                    initializeFirstLikerLayout()
                }

                if (userProfileInstance.userLikerResponses.size == 2) {
                    initializeSecondLikerLayout()
                    initializeFirstLikerLayout()
                }

                if (userProfileInstance.userLikerResponses.size >= 3) {
                    initializeSecondLikerLayout()
                    initializeFirstLikerLayout()
                    initializeThirdLikerLayout()
                }

                if (userProfileInstance.userLikerResponses.size <= 3) {
                    binding.allLikersSecondLayout.visibility = View.GONE
                } else if (userProfileInstance.userLikerResponses.size > 3) {
                    binding.allLikersSecondLayout.visibility = View.VISIBLE

                    if (userProfileInstance.userLikerResponses.size == 4) {
                        initializeFourthLikerLayout()
                    }

                    if (userProfileInstance.userLikerResponses.size == 5) {
                        initializeFourthLikerLayout()
                        initializeFifthLikerLayout()
                    }

                    if (userProfileInstance.userLikerResponses.size >= 6) {
                        initializeFourthLikerLayout()
                        initializeSixthLikerLayout()
                        initializeFifthLikerLayout()
                    }
                }
            }
        } catch (exception: IOException) {
            exception.printStackTrace()
            Log.e(TAG, "Error message from here is ${exception.message}")
        }

        val marginStartHere = binding.checkFrameStartMargin.marginStart
        val marginPercentage = (marginStartHere.toFloat() / deviceWidth) * 100
        val totalMarginValue = ((marginPercentage * 4) / 100) * deviceWidth
        val remainingPictureWidth = deviceWidth - totalMarginValue
        val eachPictureWidth = remainingPictureWidth / 3
        val eachMarginValue = totalMarginValue / 4
        val eachPictureHeight = 1.1 * eachPictureWidth
        val eachUsernameHeight = ((30 / 100F) * eachPictureHeight).toInt()

        binding.firstMargin.layoutParams.width = eachMarginValue.toInt()
        binding.secondMargin.layoutParams.width = eachMarginValue.toInt()
        binding.thirdMargin.layoutParams.width = eachMarginValue.toInt()
        binding.fourthMargin.layoutParams.width = eachMarginValue.toInt()
        binding.fifthMargin.layoutParams.width = eachMarginValue.toInt()
        binding.sixthMargin.layoutParams.width = eachMarginValue.toInt()
        binding.seventhMargin.layoutParams.width = eachMarginValue.toInt()
        binding.eighthMargin.layoutParams.width = eachMarginValue.toInt()

        binding.firstLikerImage.layoutParams.width = eachPictureWidth.toInt()
        binding.firstLikerImage.layoutParams.height = eachPictureHeight.toInt()
        binding.firstLikerFrameLayout.layoutParams.width = eachPictureWidth.toInt()
        binding.firstLikerPlaceholder.layoutParams.width = eachPictureWidth.toInt()
        binding.firstLikerFrameLayout.layoutParams.height = eachPictureHeight.toInt()
        binding.firstLikerPlaceholder.layoutParams.height = eachPictureHeight.toInt()

        binding.secondLikerImage.layoutParams.width = eachPictureWidth.toInt()
        binding.secondLikerImage.layoutParams.height = eachPictureHeight.toInt()
        binding.secondLikerFrameLayout.layoutParams.width = eachPictureWidth.toInt()
        binding.secondLikerPlaceholder.layoutParams.width = eachPictureWidth.toInt()
        binding.secondLikerFrameLayout.layoutParams.height = eachPictureHeight.toInt()
        binding.secondLikerPlaceholder.layoutParams.height = eachPictureHeight.toInt()

        binding.thirdLikerImage.layoutParams.width = eachPictureWidth.toInt()
        binding.thirdLikerImage.layoutParams.height = eachPictureHeight.toInt()
        binding.thirdLikerFrameLayout.layoutParams.width = eachPictureWidth.toInt()
        binding.thirdLikerPlaceholder.layoutParams.width = eachPictureWidth.toInt()
        binding.thirdLikerFrameLayout.layoutParams.height = eachPictureHeight.toInt()
        binding.thirdLikerPlaceholder.layoutParams.height = eachPictureHeight.toInt()

        binding.fourthLikerImage.layoutParams.width = eachPictureWidth.toInt()
        binding.fourthLikerImage.layoutParams.height = eachPictureHeight.toInt()
        binding.fourthLikerFrameLayout.layoutParams.width = eachPictureWidth.toInt()
        binding.fourthLikerPlaceholder.layoutParams.width = eachPictureWidth.toInt()
        binding.fourthLikerFrameLayout.layoutParams.height = eachPictureHeight.toInt()
        binding.fourthLikerPlaceholder.layoutParams.height = eachPictureHeight.toInt()

        binding.fifthLikerImage.layoutParams.width = eachPictureWidth.toInt()
        binding.fifthLikerImage.layoutParams.height = eachPictureHeight.toInt()
        binding.fifthLikerFrameLayout.layoutParams.width = eachPictureWidth.toInt()
        binding.fifthLikerPlaceholder.layoutParams.width = eachPictureWidth.toInt()
        binding.fifthLikerFrameLayout.layoutParams.height = eachPictureHeight.toInt()
        binding.fifthLikerPlaceholder.layoutParams.height = eachPictureHeight.toInt()

        binding.sixthLikerImage.layoutParams.width = eachPictureWidth.toInt()
        binding.sixthLikerImage.layoutParams.height = eachPictureHeight.toInt()
        binding.sixthLikerFrameLayout.layoutParams.width = eachPictureWidth.toInt()
        binding.sixthLikerPlaceholder.layoutParams.width = eachPictureWidth.toInt()
        binding.sixthLikerFrameLayout.layoutParams.height = eachPictureHeight.toInt()
        binding.sixthLikerPlaceholder.layoutParams.height = eachPictureHeight.toInt()

        binding.firstLikerUsername.layoutParams.height = eachUsernameHeight
        binding.thirdLikerUsername.layoutParams.height = eachUsernameHeight
        binding.fifthLikerUsername.layoutParams.height = eachUsernameHeight
        binding.sixthLikerUsername.layoutParams.height = eachUsernameHeight
        binding.fourthLikerUsername.layoutParams.height = eachUsernameHeight
        binding.secondLikerUsername.layoutParams.height = eachUsernameHeight

        binding.userProfileScroller.setOnTouchListener{ _: View, motionEvent: MotionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_UP -> {
                    hideSystemUI()
                }
                MotionEvent.ACTION_DOWN -> {
                    hideSystemUI()
                }
            }

            return@setOnTouchListener false
        }

        binding.profileEditorButton.iconHollowButtonLayout.setOnClickListener {
            binding.profileEditorButton.iconHollowButtonLayout.startAnimation(buttonClickEffect)

            val intent = Intent(baseContext, ProfileEditorActivity::class.java)
            startActivity(intent)
        }

        binding.photoGalleryButton.iconHollowButtonLayout.setOnClickListener {
            binding.photoGalleryButton.iconHollowButtonLayout.startAnimation(buttonClickEffect)
            requestedActivity = getString(R.string.activity_image_display)
            fetchUserPictures()
        }

        binding.profilePictureCover.setOnClickListener {
            requestedActivity = getString(R.string.activity_image_slider)
            fetchUserPictures()
        }

        binding.profilePictureChanger.setOnClickListener {
            pickImageFromGallery()
        }

        binding.sixthLikerFrameLayout.setOnClickListener {
            if (userProfileInstance.userLikerResponses.size > 6) {
                val mapper = jacksonObjectMapper()
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

                var allLikersInstance = AllLikersInstance(
                    scrollToPosition = 0,
                    userLikerResponses = userProfileInstance.userLikerResponses
                )

                val activityInstanceModel: ActivityInstanceModel =
                    mapper.readValue(sharedPreferences.getString(getString(R.string.activity_instance_model), "")!!)

                try {
                    if (activityInstanceModel.activityInstanceStack.peek().activity ==
                        getString(R.string.activity_all_likers)) {
                        activitySavedInstance = activityInstanceModel.activityInstanceStack.peek()
                        allLikersInstance = mapper.readValue(activitySavedInstance.activityStateData)
                    }

                    val activityStateData = mapper.writeValueAsString(allLikersInstance)

                    activitySavedInstance = ActivitySavedInstance(
                        activity = getString(R.string.activity_all_likers),
                        activityStateData = activityStateData)

                    if (activityInstanceModel.activityInstanceStack.peek().activity != getString(
                            R.string.activity_all_likers
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
                val intent = Intent(this, AllLikersActivity::class.java)
                intent.putExtra(getString(R.string.activity_saved_instance), activitySavedInstanceString)
                startActivity(intent)
            } else {
                userInformationRequest = UserInformationRequest(userProfileInstance.userLikerResponses[5].memberId)
                requestProcess = getString(R.string.request_fetch_user_information)
                fetchUserInformation()
            }
        }

        binding.fifthLikerFrameLayout.setOnClickListener {
            userInformationRequest = UserInformationRequest(userProfileInstance.userLikerResponses[4].memberId)
            requestProcess = getString(R.string.request_fetch_user_information)
            fetchUserInformation()
        }

        binding.fourthLikerFrameLayout.setOnClickListener {
            userInformationRequest = UserInformationRequest(userProfileInstance.userLikerResponses[3].memberId)
            requestProcess = getString(R.string.request_fetch_user_information)
            fetchUserInformation()
        }

        binding.thirdLikerFrameLayout.setOnClickListener {
            userInformationRequest = UserInformationRequest(userProfileInstance.userLikerResponses[2].memberId)
            requestProcess = getString(R.string.request_fetch_user_information)
            fetchUserInformation()
        }

        binding.secondLikerFrameLayout.setOnClickListener {
            userInformationRequest = UserInformationRequest(userProfileInstance.userLikerResponses[1].memberId)
            requestProcess = getString(R.string.request_fetch_user_information)
            fetchUserInformation()
        }

        binding.firstLikerFrameLayout.setOnClickListener {
            userInformationRequest = UserInformationRequest(userProfileInstance.userLikerResponses[0].memberId)
            requestProcess = getString(R.string.request_fetch_user_information)
            fetchUserInformation()
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

        binding.bottomNavigationLayout.bottomHomeMenuLayout.setOnClickListener {
            redrawBottomMenuIcons(getString(R.string.clicked_home_menu))
            requestProcess = getString(R.string.request_fetch_matched_users)
            binding.progressIconLayout.visibility = View.VISIBLE
            fetchMatchedUsers()
        }

        binding.bottomNavigationLayout.bottomMessengerMenuLayout.setOnClickListener {
            redrawBottomMenuIcons(getString(R.string.clicked_message_menu))
            requestProcess = getString(R.string.request_fetch_user_messengers)
            binding.progressIconLayout.visibility = View.VISIBLE
            fetchUserMessengers()
        }

        binding.bottomNavigationLayout.bottomAccountMenuLayout.setOnClickListener {
            redrawBottomMenuIcons(getString(R.string.clicked_account_menu))
        }

        binding.bottomNavigationLayout.bottomNotificationMenuLayout.setOnClickListener {
            redrawBottomMenuIcons(getString(R.string.clicked_notification_menu))
            requestProcess = getString(R.string.request_fetch_notifications)
            fetchNotifications()
        }

        binding.bottomNavigationLayout.bottomGenericMenuLayout.setOnClickListener {
            redrawBottomMenuIcons(getString(R.string.clicked_generic_menu))
            requestProcess = getString(R.string.request_fetch_liked_users)
            fetchLikedUsers()
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
                    + sharedPreferences.getString(getString(R.string.profile_picture), ""))
            .transform(CircleCrop(), CenterCrop())
            .into(binding.accountProfilePicture)

        binding.photoGalleryButton.iconHollowButtonText.text = "Photos"
        binding.photoGalleryButton.iconHollowButtonIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_gallery_blue))
        binding.photoGalleryButton.iconHollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_grey_button)

        binding.profileEditorButton.iconHollowButtonText.text = "Edit Profile"
        binding.profileEditorButton.iconHollowButtonIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_edit_blue))
        binding.profileEditorButton.iconHollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_grey_button)

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

        binding.userLocation.text =
            sharedPreferences.getString(getString(R.string.current_location), "").toString().ifEmpty {
            "Location Not Set!"
        }

        if (sharedPreferences.getString(getString(R.string.full_name), "") != "") {
            binding.userFullName.text = getString(
                R.string.name_and_age_text,
                sharedPreferences.getString(getString(R.string.full_name), ""),
                sharedPreferences.getInt(getString(R.string.age), 0)
            )
        } else {
            binding.userFullName.text = getString(
                R.string.name_and_age_text,
                sharedPreferences.getString(getString(R.string.user_name), ""),
                sharedPreferences.getInt(getString(R.string.age), 0)
            )
        }

        binding.userStatusText.text = sharedPreferences.getString(getString(R.string.user_status), "")

        if (sharedPreferences.getInt(getString(R.string.bisexual_category), 0) > 0) {
            binding.userBisexual.blueLabelLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.gay_category), 0) > 0) {
            binding.userGay.blueLabelLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.lesbian_category), 0) > 0) {
            binding.userLesbian.blueLabelLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.straight_category), 0) > 0) {
            binding.userStraight.blueLabelLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.sugar_daddy_category), 0) > 0) {
            binding.userSugarDaddy.blueLabelLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.sugar_mommy_category), 0) > 0) {
            binding.userSugarMommy.blueLabelLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.toy_boy_category), 0) > 0) {
            binding.userToyBoy.blueLabelLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.toy_girl_category), 0) > 0) {
            binding.userToyGirl.blueLabelLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.bisexual_interest), 0) > 0) {
            binding.bisexualInterest.blueLabelLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.friendship_interest), 0) > 0) {
            binding.friendshipInterest.blueLabelLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.gay_interest), 0) > 0) {
            binding.gayInterest.blueLabelLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.straight_interest), 0) > 0) {
            binding.straightInterest.blueLabelLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.lesbian_interest), 0) > 0) {
            binding.lesbianInterest.blueLabelLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.relationship_interest), 0) > 0) {
            binding.relationshipInterest.blueLabelLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.sugar_daddy_interest), 0) > 0) {
            binding.sugarDaddyInterest.blueLabelLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.sugar_mommy_interest), 0) > 0) {
            binding.sugarMommyInterest.blueLabelLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.toy_boy_interest), 0) > 0) {
            binding.toyBoyInterest.blueLabelLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.toy_girl_interest), 0) > 0) {
            binding.toyGirlInterest.blueLabelLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.anal_sex_experience), 0) > 0) {
            binding.analSexExperience.blueLabelLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.missionary_experience), 0) > 0) {
            binding.missionaryExperience.blueLabelLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.sixty_nine_experience), 0) > 0) {
            binding.sixtyNineExperience.blueLabelLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.camera_sex_experience), 0) > 0) {
            binding.cameraSexExperience.blueLabelLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.car_sex_experience), 0) > 0) {
            binding.carSexExperience.blueLabelLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.threesome_experience), 0) > 0) {
            binding.threesomeExperience.blueLabelLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.given_head_experience), 0) > 0) {
            binding.givenHeadExperience.blueLabelLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.received_head_experience), 0) > 0) {
            binding.receivedHeadExperience.blueLabelLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.one_night_stand_experience), 0) > 0) {
            binding.oneNightStandExperience.blueLabelLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.orgy_experience), 0) > 0) {
            binding.orgySexExperience.blueLabelLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.pool_sex_experience), 0) > 0) {
            binding.poolSexExperience.blueLabelLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.sex_toy_experience), 0) > 0) {
            binding.sexToyExperience.blueLabelLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.video_sex_experience), 0) > 0) {
            binding.videoSexExperience.blueLabelLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.public_sex_experience), 0) > 0) {
            binding.publicSexExperience.blueLabelLayout.visibility = View.VISIBLE
        }
    }

    override fun onStart() {
        super.onStart()
        hideSystemUI()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val mapper = jacksonObjectMapper()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        val activityInstanceModel: ActivityInstanceModel =
            mapper.readValue(sharedPreferences.getString(getString(R.string.activity_instance_model), "")!!)

        try {
            when (activityInstanceModel.activityInstanceStack.peek().activity) {
                getString(R.string.activity_user_profile) -> {
                    activityInstanceModel.activityInstanceStack.pop()

                    val activityInstanceModelString = mapper.writeValueAsString(activityInstanceModel)
                    sharedPreferencesEditor.putString(getString(R.string.activity_instance_model), activityInstanceModelString)
                    sharedPreferencesEditor.apply()

                    this.onBackPressed()
                }
                else -> {
                    requestProcess = getString(R.string.request_fetch_matched_users)
                    fetchMatchedUsers()
                }
            }
        } catch (exception: EmptyStackException) {
            exception.printStackTrace()
            Log.e(TAG, "Exception from trying to peek activityStack here is ${exception.message}")
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAPTURE_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            theBitmap = BitmapFactory.decodeFile(photoFile!!.absolutePath)

            Glide.with(this)
                .load(theBitmap)
                .transform(CircleCrop(), CenterCrop())
                .into(binding.accountProfilePicture)
        } else if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            Glide.with(this)
                .load(data?.data)
                .transform(CircleCrop(), CenterCrop())
                .into(binding.accountProfilePicture)

            theBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(contentResolver, data?.data!!)
                ImageDecoder.decodeBitmap(source)
            } else{
                MediaStore.Images.Media.getBitmap(contentResolver, data?.data)
            }
        }

        if (theBitmap != null) {
            updateProfilePicture()
        }
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
                val intent = Intent(this@UserProfileActivity, HomeDisplayActivity::class.java)
                intent.putExtra(getString(R.string.activity_saved_instance), activitySavedInstanceString)
                startActivity(intent)
            }
        })
    }

    @Throws(IOException::class)
    fun updateCurrentLocation() {
        val mapper = jacksonObjectMapper()
        val updateLocationRequest =
            UpdateLocationRequest(sharedPreferences.getInt(getString(R.string.member_id), 0),
                userUpdatedLocation)

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        val jsonObjectString = mapper.writeValueAsString(updateLocationRequest)
        val requestBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            jsonObjectString
        )

        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(getString(R.string.date_momo_api) + getString(R.string.api_update_location))
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
            }
        })
    }

    @Throws(IOException::class)
    fun checkNotificationUpdate() {
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
            .url(getString(R.string.date_momo_api) + getString(R.string.api_check_notification))
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()
            }

            override fun onResponse(call: Call, response: Response) {
                val myResponse: String = response.body()!!.string()
                val committedResponse = mapper.readValue(myResponse) as CommittedResponse

                runOnUiThread {
                    binding.bottomNavigationLayout.newNotificationNotifier.visibility =
                        if (committedResponse.committed) { View.VISIBLE } else { View.GONE }
                }
            }
        })
    }

    @Throws(IOException::class)
    fun checkMessageUpdate() {
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
            .url(getString(R.string.date_momo_api) + getString(R.string.api_check_message))
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()
            }

            override fun onResponse(call: Call, response: Response) {
                val myResponse: String = response.body()!!.string()
                val committedResponse = mapper.readValue(myResponse) as CommittedResponse

                runOnUiThread {
                    binding.bottomNavigationLayout.newMessageNotifier.visibility =
                        if (committedResponse.committed) { View.VISIBLE } else { View.GONE }
                }
            }
        })
    }

    @Throws(IOException::class)
    fun fetchUserPictures() {
        val mapper = jacksonObjectMapper()
        val userPictureRequest = UserPictureRequest(
            memberId = sharedPreferences.getInt(getString(R.string.member_id), 0),
            currentPosition = 0
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
                        memberId = sharedPreferences.getInt(getString(R.string.member_id), 0),
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
                        Log.e(UserInformationActivity.TAG, "Exception from trying to peek and pop activityInstanceStack here is ${exception.message}")
                    }

                    Intent(this@UserProfileActivity, ImageDisplayActivity::class.java)
                } else {
                    var imageSliderInstance = ImageSliderInstance(
                        memberId = sharedPreferences.getInt(getString(R.string.member_id), 0),
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

                    Intent(this@UserProfileActivity, ImageSliderActivity::class.java)
                }

                requestedActivity = ""

                Log.e(TAG, "The number of activities on the stack here is ${activityInstanceModel.activityInstanceStack.size}")

                val activitySavedInstanceString = mapper.writeValueAsString(activitySavedInstance)
                intent.putExtra(getString(R.string.activity_saved_instance), activitySavedInstanceString)
                startActivity(intent)
            }
        })
    }

    @Throws(IOException::class)
    fun updateProfilePicture() {
        val imageWidth = theBitmap!!.width
        val imageHeight = theBitmap!!.height

        val base64Picture = Utility.encodeUploadImage(theBitmap!!)

        val mapper = jacksonObjectMapper()
        val pictureUpdateRequest = PictureUpdateRequest(
            sharedPreferences.getInt(getString(R.string.member_id), 0),
            imageWidth,
            imageHeight,
            base64Picture
        )

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        val jsonObjectString = mapper.writeValueAsString(pictureUpdateRequest)
        val requestBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            jsonObjectString
        )

        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(getString(R.string.date_momo_api) + getString(R.string.api_update_picture))
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()

                if (!Utility.isConnected(baseContext)) {
                    displayDoubleButtonDialog()
                } else if (e.message!!.contains("after")) {
                    displaySingleButtonDialog(
                        getString(R.string.poor_internet_title),
                        getString(R.string.poor_internet_message)
                    )
                } else {
                    displaySingleButtonDialog(
                        getString(R.string.server_error_title),
                        getString(R.string.server_error_message)
                    )
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val myResponse: String = response.body()!!.string()
                val pictureUpdateResponse: PictureUpdateResponse

                try {
                    pictureUpdateResponse = mapper.readValue(myResponse)
                    sharedPreferencesEditor.putString(getString(R.string.profile_picture),
                        pictureUpdateResponse.profilePicture)
                    sharedPreferencesEditor.apply()
                } catch (exception: IOException) {
                    exception.printStackTrace()
                    displaySingleButtonDialog(
                        getString(R.string.server_error_title),
                        getString(R.string.server_error_message)
                    )
                }
            }
        })
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun initializeSixthLikerLayout() {
        binding.sixthLikerFrameLayout.visibility = View.VISIBLE

        Glide.with(this)
            .load(getString(R.string.date_momo_api) + getString(R.string.api_image)
                    + userProfileInstance.userLikerResponses[5].profilePicture)
            .transform(CenterCrop(), RoundedCorners(33))
            .into(binding.sixthLikerImage)

        if (userProfileInstance.userLikerResponses[5].fullName != "") {
            binding.sixthLikerUsername.text = getString(
                R.string.name_and_age_text,
                userProfileInstance.userLikerResponses[5].fullName,
                userProfileInstance.userLikerResponses[5].age
            )
        } else {
            binding.sixthLikerUsername.text = getString(
                R.string.name_and_age_text,
                userProfileInstance.userLikerResponses[5].userName.replaceFirstChar { it.uppercase() },
                userProfileInstance.userLikerResponses[5].age
            )
        }

        if (userProfileInstance.userLikerResponses.size > 6) {
            val moreLikersCount = userProfileInstance.userLikerResponses.size - 5
            binding.moreLikersCount.visibility = View.VISIBLE
            binding.moreLikersCover.visibility = View.VISIBLE
            binding.moreLikersCount.text = getString(R.string.more_likers_count, moreLikersCount)
        } else {
            binding.moreLikersCount.visibility = View.GONE
            binding.moreLikersCover.visibility = View.GONE
        }
    }

    private fun initializeFifthLikerLayout() {
        binding.fifthLikerFrameLayout.visibility = View.VISIBLE

        Glide.with(this)
            .load(getString(R.string.date_momo_api) + getString(R.string.api_image)
                    + userProfileInstance.userLikerResponses[4].profilePicture)
            .transform(CenterCrop(), RoundedCorners(33))
            .into(binding.fifthLikerImage)

        if (userProfileInstance.userLikerResponses[4].fullName != "") {
            binding.fifthLikerUsername.text = getString(
                R.string.name_and_age_text,
                userProfileInstance.userLikerResponses[4].fullName,
                userProfileInstance.userLikerResponses[4].age
            )
        } else {
            binding.fifthLikerUsername.text = getString(
                R.string.name_and_age_text,
                userProfileInstance.userLikerResponses[4].userName.replaceFirstChar { it.uppercase() },
                userProfileInstance.userLikerResponses[4].age
            )
        }
    }

    private fun initializeFourthLikerLayout() {
        binding.fourthLikerFrameLayout.visibility = View.VISIBLE

        Glide.with(this)
            .load(getString(R.string.date_momo_api) + getString(R.string.api_image)
                    + userProfileInstance.userLikerResponses[3].profilePicture)
            .transform(CenterCrop(), RoundedCorners(33))
            .into(binding.fourthLikerImage)

        if (userProfileInstance.userLikerResponses[3].fullName != "") {
            binding.fourthLikerUsername.text = getString(
                R.string.name_and_age_text,
                userProfileInstance.userLikerResponses[3].fullName,
                userProfileInstance.userLikerResponses[3].age
            )
        } else {
            binding.fourthLikerUsername.text = getString(
                R.string.name_and_age_text,
                userProfileInstance.userLikerResponses[3].userName.replaceFirstChar { it.uppercase() },
                userProfileInstance.userLikerResponses[3].age
            )
        }
    }

    private fun initializeThirdLikerLayout() {
        binding.thirdLikerFrameLayout.visibility = View.VISIBLE

        Glide.with(this)
            .load(getString(R.string.date_momo_api) + getString(R.string.api_image)
                    + userProfileInstance.userLikerResponses[2].profilePicture)
            .transform(CenterCrop(), RoundedCorners(33))
            .into(binding.thirdLikerImage)

        if (userProfileInstance.userLikerResponses[2].fullName != "") {
            binding.thirdLikerUsername.text = getString(
                R.string.name_and_age_text,
                userProfileInstance.userLikerResponses[2].fullName,
                userProfileInstance.userLikerResponses[2].age
            )
        } else {
            binding.thirdLikerUsername.text = getString(
                R.string.name_and_age_text,
                userProfileInstance.userLikerResponses[2].userName.replaceFirstChar { it.uppercase() },
                userProfileInstance.userLikerResponses[2].age
            )
        }
    }

    private fun initializeSecondLikerLayout() {
        binding.secondLikerFrameLayout.visibility = View.VISIBLE

        Glide.with(this)
            .load(getString(R.string.date_momo_api) + getString(R.string.api_image)
                    + userProfileInstance.userLikerResponses[1].profilePicture)
            .transform(CenterCrop(), RoundedCorners(33))
            .into(binding.secondLikerImage)

        if (userProfileInstance.userLikerResponses[1].fullName != "") {
            binding.secondLikerUsername.text = getString(
                R.string.name_and_age_text,
                userProfileInstance.userLikerResponses[1].fullName,
                userProfileInstance.userLikerResponses[1].age
            )
        } else {
            binding.secondLikerUsername.text = getString(
                R.string.name_and_age_text,
                userProfileInstance.userLikerResponses[1].userName.replaceFirstChar { it.uppercase() },
                userProfileInstance.userLikerResponses[1].age
            )
        }
    }

    private fun initializeFirstLikerLayout() {
        binding.firstLikerFrameLayout.visibility = View.VISIBLE

        Glide.with(this)
            .load(getString(R.string.date_momo_api) + getString(R.string.api_image)
                    + userProfileInstance.userLikerResponses[0].profilePicture)
            .transform(CenterCrop(), RoundedCorners(33))
            .into(binding.firstLikerImage)

        if (userProfileInstance.userLikerResponses[0].fullName != "") {
            binding.firstLikerUsername.text = getString(
                R.string.name_and_age_text,
                userProfileInstance.userLikerResponses[0].fullName,
                userProfileInstance.userLikerResponses[0].age
            )
        } else {
            binding.firstLikerUsername.text = getString(
                R.string.name_and_age_text,
                userProfileInstance.userLikerResponses[0].userName.replaceFirstChar { it.uppercase() },
                userProfileInstance.userLikerResponses[0].age
            )
        }
    }

    private fun triggerRequestProcess() {
        when (requestProcess) {
            getString(R.string.request_fetch_user_information) -> fetchUserInformation()
            getString(R.string.request_fetch_user_messengers) -> fetchUserMessengers()
            getString(R.string.request_fetch_notifications) -> fetchNotifications()
            getString(R.string.request_fetch_matched_users) -> fetchMatchedUsers()
            getString(R.string.request_fetch_liked_users) -> fetchLikedUsers()
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
                val notificationResponses: ArrayList<NotificationResponse> = mapper.readValue(myResponse)
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
                val intent = Intent(this@UserProfileActivity, NotificationActivity::class.java)
                intent.putExtra(getString(R.string.activity_saved_instance), activitySavedInstanceString)
                startActivity(intent)
            }
        })
    }

    @Throws(IOException::class)
    fun fetchUserInformation() {
        val mapper = jacksonObjectMapper()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
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
                val homeDisplayResponse: HomeDisplayResponse = mapper.readValue(myResponse)

                val activityStateData = mapper.writeValueAsString(homeDisplayResponse)

                val activityInstanceModel: ActivityInstanceModel =
                    mapper.readValue(sharedPreferences.getString(getString(R.string.activity_instance_model), "")!!)

                try {
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
                val intent = Intent(this@UserProfileActivity, UserInformationActivity::class.java)
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
                val intent = Intent(this@UserProfileActivity, UserAccountActivity::class.java)
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

                runOnUiThread {
                    binding.progressIconLayout.visibility = View.GONE
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

                val activitySavedInstanceString = mapper.writeValueAsString(activitySavedInstance)
                val intent = Intent(this@UserProfileActivity, MessengerActivity::class.java)
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

    private fun redrawBottomMenuIcons(clickedBottomMenu: String) {
        when (clickedBottomMenu) {
            getString(R.string.clicked_home_menu) -> {
                binding.bottomNavigationLayout.homeMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.selected_bottom_menu)
                binding.bottomNavigationLayout.bottomHomeMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_home_white))
                binding.bottomNavigationLayout.accountMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.ignored_bottom_menu)
                binding.bottomNavigationLayout.genericMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.ignored_bottom_menu)
                binding.bottomNavigationLayout.messageMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.ignored_bottom_menu)
                binding.bottomNavigationLayout.bottomAccountMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_account_blue))
                binding.bottomNavigationLayout.bottomMessageMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_message_blue))
                binding.bottomNavigationLayout.notificationMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.ignored_bottom_menu)
                binding.bottomNavigationLayout.bottomGenericMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_menu_bar_blue))
                binding.bottomNavigationLayout.bottomNotificationMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_notification_blue))
            }
            getString(R.string.clicked_account_menu) -> {
                binding.bottomNavigationLayout.homeMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.ignored_bottom_menu)
                binding.bottomNavigationLayout.bottomHomeMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_home_blue))
                binding.bottomNavigationLayout.messageMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.ignored_bottom_menu)
                binding.bottomNavigationLayout.genericMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.ignored_bottom_menu)
                binding.bottomNavigationLayout.accountMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.selected_bottom_menu)
                binding.bottomNavigationLayout.bottomMessageMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_message_blue))
                binding.bottomNavigationLayout.notificationMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.ignored_bottom_menu)
                binding.bottomNavigationLayout.bottomGenericMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_menu_bar_blue))
                binding.bottomNavigationLayout.bottomAccountMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_account_white))
                binding.bottomNavigationLayout.bottomNotificationMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_notification_blue))
            }
            getString(R.string.clicked_message_menu) -> {
                binding.bottomNavigationLayout.homeMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.ignored_bottom_menu)
                binding.bottomNavigationLayout.bottomHomeMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_home_blue))
                binding.bottomNavigationLayout.accountMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.ignored_bottom_menu)
                binding.bottomNavigationLayout.genericMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.ignored_bottom_menu)
                binding.bottomNavigationLayout.messageMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.selected_bottom_menu)
                binding.bottomNavigationLayout.bottomAccountMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_account_blue))
                binding.bottomNavigationLayout.bottomGenericMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_menu_bar_blue))
                binding.bottomNavigationLayout.notificationMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.ignored_bottom_menu)
                binding.bottomNavigationLayout.bottomMessageMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_message_white))
                binding.bottomNavigationLayout.bottomNotificationMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_notification_blue))
            }
            getString(R.string.clicked_notification_menu) -> {
                binding.bottomNavigationLayout.homeMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.ignored_bottom_menu)
                binding.bottomNavigationLayout.bottomHomeMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_home_blue))
                binding.bottomNavigationLayout.accountMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.ignored_bottom_menu)
                binding.bottomNavigationLayout.genericMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.ignored_bottom_menu)
                binding.bottomNavigationLayout.messageMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.ignored_bottom_menu)
                binding.bottomNavigationLayout.bottomAccountMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_account_blue))
                binding.bottomNavigationLayout.bottomMessageMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_message_blue))
                binding.bottomNavigationLayout.bottomGenericMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_menu_bar_blue))
                binding.bottomNavigationLayout.notificationMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.selected_bottom_menu)
                binding.bottomNavigationLayout.bottomNotificationMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_notification_white))
            }
            getString(R.string.clicked_generic_menu) -> {
                binding.bottomNavigationLayout.homeMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.ignored_bottom_menu)
                binding.bottomNavigationLayout.bottomHomeMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_home_blue))
                binding.bottomNavigationLayout.accountMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.ignored_bottom_menu)
                binding.bottomNavigationLayout.messageMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.ignored_bottom_menu)
                binding.bottomNavigationLayout.genericMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.selected_bottom_menu)
                binding.bottomNavigationLayout.bottomAccountMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_account_blue))
                binding.bottomNavigationLayout.bottomMessageMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_message_blue))
                binding.bottomNavigationLayout.notificationMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.ignored_bottom_menu)
                binding.bottomNavigationLayout.bottomGenericMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_menu_bar_white))
                binding.bottomNavigationLayout.bottomNotificationMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_notification_blue))
            }
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
        const val TAG = "UserProfileActivity"
    }
}


