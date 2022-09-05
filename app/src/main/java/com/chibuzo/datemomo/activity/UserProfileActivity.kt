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
import android.view.View
import android.view.animation.AlphaAnimation
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
import com.chibuzo.datemomo.model.ActivityStackModel
import com.chibuzo.datemomo.model.request.*
import com.chibuzo.datemomo.model.response.CommittedResponse
import com.chibuzo.datemomo.model.response.PictureUpdateResponse
import com.chibuzo.datemomo.model.response.UserLikerResponse
import com.chibuzo.datemomo.service.LocationTracker
import com.chibuzo.datemomo.utility.Utility
import com.fasterxml.jackson.databind.DeserializationFeature
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
    private lateinit var userInformationRequest: UserInformationRequest
    private lateinit var userLikerResponseArray: Array<UserLikerResponse>
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
                ) // Here 1 represent max location result to returned, by documents it recommended 1 to 5

                val address: String =
                    addresses[0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()

                val city = addresses[0].locality
                val state = addresses[0].adminArea
                val country = addresses[0].countryName
                val postalCode = addresses[0].postalCode
                val knownName = addresses[0].featureName

                if (knownName.isNullOrEmpty()) {
                    sharedPreferencesEditor.putString(getString(R.string.updated_location), city)
                    sharedPreferencesEditor.apply()
                } else {
                    sharedPreferencesEditor.putString(getString(R.string.updated_location), knownName)
                    sharedPreferencesEditor.apply()
                }

                // notify user of location change here
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
            userLikerResponseArray = mapper.readValue(bundle.getString("jsonResponse")!!)

            if (userLikerResponseArray.size > 1) {
                binding.allLikesCount.text = getString(R.string.many_likers_count, userLikerResponseArray.size)
            }

            if (userLikerResponseArray.size == 1) {
                binding.allLikesCount.text = getString(R.string.single_liker_count)
            }

            if (userLikerResponseArray.isEmpty()) {
                binding.allLikersDisplayLayout.visibility = View.GONE
            } else {
                binding.allLikersFirstLayout.visibility = View.VISIBLE

                if (userLikerResponseArray.size == 1) {
                    initializeFirstLikerLayout()
                }

                if (userLikerResponseArray.size == 2) {
                    initializeSecondLikerLayout()
                    initializeFirstLikerLayout()
                }

                if (userLikerResponseArray.size >= 3) {
                    initializeSecondLikerLayout()
                    initializeFirstLikerLayout()
                    initializeThirdLikerLayout()
                }

                if (userLikerResponseArray.size <= 3) {
                    binding.allLikersSecondLayout.visibility = View.GONE
                } else if (userLikerResponseArray.size > 3) {
                    binding.allLikersSecondLayout.visibility = View.VISIBLE

                    if (userLikerResponseArray.size == 4) {
                        initializeFourthLikerLayout()
                    }

                    if (userLikerResponseArray.size == 5) {
                        initializeFourthLikerLayout()
                        initializeFifthLikerLayout()
                    }

                    if (userLikerResponseArray.size >= 6) {
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
            if (userLikerResponseArray.size > 6) {
                val mapper = jacksonObjectMapper()
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                val userLikerResponseString = mapper.writeValueAsString(userLikerResponseArray)

                val intent = Intent(baseContext, AllLikersActivity::class.java)
                intent.putExtra("jsonResponse", userLikerResponseString)
                startActivity(intent)
            } else {
                userInformationRequest = UserInformationRequest(userLikerResponseArray[5].memberId)
                requestProcess = getString(R.string.request_fetch_user_information)
                fetchUserInformation()
            }
        }

        binding.fifthLikerFrameLayout.setOnClickListener {
            userInformationRequest = UserInformationRequest(userLikerResponseArray[4].memberId)
            requestProcess = getString(R.string.request_fetch_user_information)
            fetchUserInformation()
        }

        binding.fourthLikerFrameLayout.setOnClickListener {
            userInformationRequest = UserInformationRequest(userLikerResponseArray[3].memberId)
            requestProcess = getString(R.string.request_fetch_user_information)
            fetchUserInformation()
        }

        binding.thirdLikerFrameLayout.setOnClickListener {
            userInformationRequest = UserInformationRequest(userLikerResponseArray[2].memberId)
            requestProcess = getString(R.string.request_fetch_user_information)
            fetchUserInformation()
        }

        binding.secondLikerFrameLayout.setOnClickListener {
            userInformationRequest = UserInformationRequest(userLikerResponseArray[1].memberId)
            requestProcess = getString(R.string.request_fetch_user_information)
            fetchUserInformation()
        }

        binding.firstLikerFrameLayout.setOnClickListener {
            userInformationRequest = UserInformationRequest(userLikerResponseArray[0].memberId)
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

        binding.userLocation.text = sharedPreferences.getString(getString(R.string.current_location), "")

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
            binding.userBisexual.blueButtonLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.gay_category), 0) > 0) {
            binding.userGay.blueButtonLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.lesbian_category), 0) > 0) {
            binding.userLesbian.blueButtonLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.straight_category), 0) > 0) {
            binding.userStraight.blueButtonLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.sugar_daddy_category), 0) > 0) {
            binding.userSugarDaddy.blueButtonLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.sugar_mommy_category), 0) > 0) {
            binding.userSugarMommy.blueButtonLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.toy_boy_category), 0) > 0) {
            binding.userToyBoy.blueButtonLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.toy_girl_category), 0) > 0) {
            binding.userToyGirl.blueButtonLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.bisexual_interest), 0) > 0) {
            binding.bisexualInterest.blueButtonLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.friendship_interest), 0) > 0) {
            binding.friendshipInterest.blueButtonLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.gay_interest), 0) > 0) {
            binding.gayInterest.blueButtonLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.straight_interest), 0) > 0) {
            binding.straightInterest.blueButtonLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.lesbian_interest), 0) > 0) {
            binding.lesbianInterest.blueButtonLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.relationship_interest), 0) > 0) {
            binding.relationshipInterest.blueButtonLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.sugar_daddy_interest), 0) > 0) {
            binding.sugarDaddyInterest.blueButtonLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.sugar_mommy_interest), 0) > 0) {
            binding.sugarMommyInterest.blueButtonLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.toy_boy_interest), 0) > 0) {
            binding.toyBoyInterest.blueButtonLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.toy_girl_interest), 0) > 0) {
            binding.toyGirlInterest.blueButtonLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.anal_sex_experience), 0) > 0) {
            binding.analSexExperience.blueButtonLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.missionary_experience), 0) > 0) {
            binding.missionaryExperience.blueButtonLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.sixty_nine_experience), 0) > 0) {
            binding.sixtyNineExperience.blueButtonLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.camera_sex_experience), 0) > 0) {
            binding.cameraSexExperience.blueButtonLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.car_sex_experience), 0) > 0) {
            binding.carSexExperience.blueButtonLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.threesome_experience), 0) > 0) {
            binding.threesomeExperience.blueButtonLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.given_head_experience), 0) > 0) {
            binding.givenHeadExperience.blueButtonLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.received_head_experience), 0) > 0) {
            binding.receivedHeadExperience.blueButtonLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.one_night_stand_experience), 0) > 0) {
            binding.oneNightStandExperience.blueButtonLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.orgy_experience), 0) > 0) {
            binding.orgySexExperience.blueButtonLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.pool_sex_experience), 0) > 0) {
            binding.poolSexExperience.blueButtonLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.sex_toy_experience), 0) > 0) {
            binding.sexToyExperience.blueButtonLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.video_sex_experience), 0) > 0) {
            binding.videoSexExperience.blueButtonLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.public_sex_experience), 0) > 0) {
            binding.publicSexExperience.blueButtonLayout.visibility = View.VISIBLE
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
                getString(R.string.activity_messenger) -> {
                    requestProcess = getString(R.string.request_fetch_user_messengers)
                    fetchUserMessengers()
                }
                getString(R.string.activity_user_account) -> {
                    requestProcess = getString(R.string.request_fetch_liked_users)
                    fetchLikedUsers()
                }
                getString(R.string.activity_user_profile) -> {
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
                getString(R.string.activity_user_information) -> {
                    requestProcess = getString(R.string.request_fetch_user_information)
                    fetchUserInformation()
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

        Log.e(TAG, "The value of activityStackModel here is ${sharedPreferences.getString(getString(R.string.activity_stack), "")}")
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
            sharedPreferences.getInt(getString(R.string.member_id), 0)
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
                    if (activityStackModel.activityStack.peek() != getString(R.string.activity_image_display)) {
                        activityStackModel.activityStack.push(getString(R.string.activity_image_display))
                    }

                    Intent(baseContext, ImageDisplayActivity::class.java)
                } else {
                    if (activityStackModel.activityStack.peek() != getString(R.string.activity_image_slider)) {
                        activityStackModel.activityStack.push(getString(R.string.activity_image_slider))
                    }

                    Intent(baseContext, ImageSliderActivity::class.java)
                }

                val activityStackString = mapper.writeValueAsString(activityStackModel)
                sharedPreferencesEditor.putString(getString(R.string.activity_stack), activityStackString)
                sharedPreferencesEditor.apply()

                Log.e(TAG, "The value of activityStackModel here is ${sharedPreferences.getString(getString(R.string.activity_stack), "")}")

                requestedActivity = ""

                intent.putExtra("memberId", sharedPreferences.getInt(getString(R.string.member_id), 0))
                intent.putExtra("jsonResponse", myResponse)
                intent.putExtra("currentPosition", 0)
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
                var pictureUpdateResponse = PictureUpdateResponse("")

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
                    + userLikerResponseArray[5].profilePicture)
            .transform(CenterCrop(), RoundedCorners(33))
            .into(binding.sixthLikerImage)

        if (userLikerResponseArray[5].fullName != "") {
            binding.sixthLikerUsername.text = getString(
                R.string.name_and_age_text,
                userLikerResponseArray[5].fullName, userLikerResponseArray[5].age
            )
        } else {
            binding.sixthLikerUsername.text = getString(
                R.string.name_and_age_text,
                userLikerResponseArray[5].userName.replaceFirstChar { it.uppercase() },
                userLikerResponseArray[5].age
            )
        }

        if (userLikerResponseArray.size > 6) {
            val moreLikersCount = userLikerResponseArray.size - 5
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
                    + userLikerResponseArray[4].profilePicture)
            .transform(CenterCrop(), RoundedCorners(33))
            .into(binding.fifthLikerImage)

        if (userLikerResponseArray[4].fullName != "") {
            binding.fifthLikerUsername.text = getString(
                R.string.name_and_age_text,
                userLikerResponseArray[4].fullName, userLikerResponseArray[4].age
            )
        } else {
            binding.fifthLikerUsername.text = getString(
                R.string.name_and_age_text,
                userLikerResponseArray[4].userName.replaceFirstChar { it.uppercase() },
                userLikerResponseArray[4].age
            )
        }
    }

    private fun initializeFourthLikerLayout() {
        binding.fourthLikerFrameLayout.visibility = View.VISIBLE

        Glide.with(this)
            .load(getString(R.string.date_momo_api) + getString(R.string.api_image)
                    + userLikerResponseArray[3].profilePicture)
            .transform(CenterCrop(), RoundedCorners(33))
            .into(binding.fourthLikerImage)

        if (userLikerResponseArray[3].fullName != "") {
            binding.fourthLikerUsername.text = getString(
                R.string.name_and_age_text,
                userLikerResponseArray[3].fullName, userLikerResponseArray[3].age
            )
        } else {
            binding.fourthLikerUsername.text = getString(
                R.string.name_and_age_text,
                userLikerResponseArray[3].userName.replaceFirstChar { it.uppercase() },
                userLikerResponseArray[3].age
            )
        }
    }

    private fun initializeThirdLikerLayout() {
        binding.thirdLikerFrameLayout.visibility = View.VISIBLE

        Glide.with(this)
            .load(getString(R.string.date_momo_api) + getString(R.string.api_image)
                    + userLikerResponseArray[2].profilePicture)
            .transform(CenterCrop(), RoundedCorners(33))
            .into(binding.thirdLikerImage)

        if (userLikerResponseArray[2].fullName != "") {
            binding.thirdLikerUsername.text = getString(
                R.string.name_and_age_text,
                userLikerResponseArray[2].fullName, userLikerResponseArray[2].age
            )
        } else {
            binding.thirdLikerUsername.text = getString(
                R.string.name_and_age_text,
                userLikerResponseArray[2].userName.replaceFirstChar { it.uppercase() },
                userLikerResponseArray[2].age
            )
        }
    }

    private fun initializeSecondLikerLayout() {
        binding.secondLikerFrameLayout.visibility = View.VISIBLE

        Glide.with(this)
            .load(getString(R.string.date_momo_api) + getString(R.string.api_image)
                    + userLikerResponseArray[1].profilePicture)
            .transform(CenterCrop(), RoundedCorners(33))
            .into(binding.secondLikerImage)

        if (userLikerResponseArray[1].fullName != "") {
            binding.secondLikerUsername.text = getString(
                R.string.name_and_age_text,
                userLikerResponseArray[1].fullName, userLikerResponseArray[1].age
            )
        } else {
            binding.secondLikerUsername.text = getString(
                R.string.name_and_age_text,
                userLikerResponseArray[1].userName.replaceFirstChar { it.uppercase() },
                userLikerResponseArray[1].age
            )
        }
    }

    private fun initializeFirstLikerLayout() {
        binding.firstLikerFrameLayout.visibility = View.VISIBLE

        Glide.with(this)
            .load(getString(R.string.date_momo_api) + getString(R.string.api_image)
                    + userLikerResponseArray[0].profilePicture)
            .transform(CenterCrop(), RoundedCorners(33))
            .into(binding.firstLikerImage)

        if (userLikerResponseArray[0].fullName != "") {
            binding.firstLikerUsername.text = getString(
                R.string.name_and_age_text,
                userLikerResponseArray[0].fullName, userLikerResponseArray[0].age
            )
        } else {
            binding.firstLikerUsername.text = getString(
                R.string.name_and_age_text,
                userLikerResponseArray[0].userName.replaceFirstChar { it.uppercase() },
                userLikerResponseArray[0].age
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

                val activityStackModel: ActivityStackModel =
                    mapper.readValue(sharedPreferences.getString(getString(R.string.activity_stack), "")!!)

                if (activityStackModel.activityStack.peek() != getString(R.string.activity_notification)) {
                    activityStackModel.activityStack.push(getString(R.string.activity_notification))
                    val activityStackString = mapper.writeValueAsString(activityStackModel)
                    sharedPreferencesEditor.putString(
                        getString(R.string.activity_stack),
                        activityStackString
                    )
                    sharedPreferencesEditor.apply()
                }

                Log.e(TAG, "The value of activityStackModel here is ${sharedPreferences.getString(getString(R.string.activity_stack), "")}")

                val intent = Intent(baseContext, NotificationActivity::class.java)
                intent.putExtra("jsonResponse", myResponse)
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

                val activityStackModel: ActivityStackModel =
                    mapper.readValue(sharedPreferences.getString(getString(R.string.activity_stack), "")!!)

                if (activityStackModel.activityStack.peek() != getString(R.string.activity_user_information)) {
                    activityStackModel.activityStack.push(getString(R.string.activity_user_information))
                    val activityStackString = mapper.writeValueAsString(activityStackModel)
                    sharedPreferencesEditor.putString(
                        getString(R.string.activity_stack),
                        activityStackString
                    )
                    sharedPreferencesEditor.apply()
                }

                Log.e(TAG, "The value of activityStackModel here is ${sharedPreferences.getString(getString(R.string.activity_stack), "")}")

                val intent = Intent(baseContext, UserInformationActivity::class.java)
                intent.putExtra("jsonResponse", myResponse)
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

                val activityStackModel: ActivityStackModel =
                    mapper.readValue(sharedPreferences.getString(getString(R.string.activity_stack), "")!!)

                if (activityStackModel.activityStack.peek() != getString(R.string.activity_user_account)) {
                    activityStackModel.activityStack.push(getString(R.string.activity_user_account))
                    val activityStackString = mapper.writeValueAsString(activityStackModel)
                    sharedPreferencesEditor.putString(
                        getString(R.string.activity_stack),
                        activityStackString
                    )
                    sharedPreferencesEditor.apply()
                }

                Log.e(TAG, "The value of activityStackModel here is ${sharedPreferences.getString(getString(R.string.activity_stack), "")}")

                val intent = Intent(baseContext, UserAccountActivity::class.java)
                intent.putExtra("jsonResponse", myResponse)
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

                val activityStackModel: ActivityStackModel =
                    mapper.readValue(sharedPreferences.getString(getString(R.string.activity_stack), "")!!)

                if (activityStackModel.activityStack.peek() != getString(R.string.activity_messenger)) {
                    activityStackModel.activityStack.push(getString(R.string.activity_messenger))
                    val activityStackString = mapper.writeValueAsString(activityStackModel)
                    sharedPreferencesEditor.putString(
                        getString(R.string.activity_stack),
                        activityStackString
                    )
                    sharedPreferencesEditor.apply()
                }

                Log.e(TAG, "The value of activityStackModel here is ${sharedPreferences.getString(getString(R.string.activity_stack), "")}")

                val intent = Intent(baseContext, MessengerActivity::class.java)
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

                val activityStackModel: ActivityStackModel =
                    mapper.readValue(sharedPreferences.getString(getString(R.string.activity_stack), "")!!)

                if (activityStackModel.activityStack.peek() != getString(R.string.activity_home_display)) {
                    activityStackModel.activityStack.push(getString(R.string.activity_home_display))
                    val activityStackString = mapper.writeValueAsString(activityStackModel)
                    sharedPreferencesEditor.putString(
                        getString(R.string.activity_stack),
                        activityStackString
                    )
                    sharedPreferencesEditor.apply()
                }

                Log.e(TAG, "The value of activityStackModel here is ${sharedPreferences.getString(getString(R.string.activity_stack), "")}")

                val intent = Intent(baseContext, HomeDisplayActivity::class.java)
                intent.putExtra("jsonResponse", myResponse)
                startActivity(intent)
            }
        })
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


