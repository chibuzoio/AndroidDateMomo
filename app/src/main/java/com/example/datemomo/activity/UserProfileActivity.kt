package com.example.datemomo.activity

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
import androidx.core.view.marginStart
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.datemomo.R
import com.example.datemomo.databinding.ActivityUserProfileBinding
import com.example.datemomo.model.ActivityStackModel
import com.example.datemomo.model.request.HomeDisplayRequest
import com.example.datemomo.model.request.UserInformationRequest
import com.example.datemomo.model.request.UserLikerRequest
import com.example.datemomo.model.response.UserLikerResponse
import com.example.datemomo.utility.Utility
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.*
import java.io.IOException

class UserProfileActivity : AppCompatActivity() {
    private var deviceWidth: Int = 0
    private var deviceHeight: Int = 0
    private lateinit var bundle: Bundle
    private lateinit var requestProcess: String
    private lateinit var originalRequestProcess: String
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

        try {
            val mapper = jacksonObjectMapper()
            userLikerResponseArray = mapper.readValue(bundle.getString("jsonResponse")!!)

            // check if it's up to six, less than six or greater than six
            // get the total count and set it
            if (userLikerResponseArray.size > 1) {
                binding.allLikesCount.text = getString(R.string.many_likers_count, userLikerResponseArray.size)
            }

            if (userLikerResponseArray.size == 1) {
                binding.allLikesCount.text = getString(R.string.single_liker_count)
            }

            if (userLikerResponseArray.isEmpty()) {
                binding.allLikersDisplayLayout.visibility = View.GONE
            } else {
                if (userLikerResponseArray.size == 1) {
                    initializeFirstLikerLayout()
                }

                if (userLikerResponseArray.size == 2) {
                    initializeSecondLikerLayout()
                    initializeFirstLikerLayout()
                }

                if (userLikerResponseArray.size == 3) {
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

        binding.sixthLikerFrameLayout.setOnClickListener {
            if (userLikerResponseArray.size > 6) {
                // Navigate to all likers' activity by clicking on this layout
            } else {
                userInformationRequest = UserInformationRequest(userLikerResponseArray[5].memberId)
                fetchUserInformation()
            }
        }

        binding.fifthLikerFrameLayout.setOnClickListener {
            userInformationRequest = UserInformationRequest(userLikerResponseArray[4].memberId)
            fetchUserInformation()
        }

        binding.fourthLikerFrameLayout.setOnClickListener {
            userInformationRequest = UserInformationRequest(userLikerResponseArray[3].memberId)
            fetchUserInformation()
        }

        binding.thirdLikerFrameLayout.setOnClickListener {
            userInformationRequest = UserInformationRequest(userLikerResponseArray[2].memberId)
            fetchUserInformation()
        }

        binding.secondLikerFrameLayout.setOnClickListener {
            userInformationRequest = UserInformationRequest(userLikerResponseArray[1].memberId)
            fetchUserInformation()
        }

        binding.firstLikerFrameLayout.setOnClickListener {
            userInformationRequest = UserInformationRequest(userLikerResponseArray[0].memberId)
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
        binding.sugarDaddyInterest.blueButtonText.text = "Sugar Daddy"
        binding.sugarMommyInterest.blueButtonText.text = "Sugar Mommy"

        binding.sixtyNineExperience.blueButtonText.text = "69"
        binding.analSexExperience.blueButtonText.text = "Anal Sex"
        binding.orgySexExperience.blueButtonText.text = "Orgy Sex"
        binding.poolSexExperience.blueButtonText.text = "Pool Sex"
        binding.carSexExperience.blueButtonText.text = "Sexed In Car"
        binding.threesomeExperience.blueButtonText.text = "Threesome"
        binding.givenHeadExperience.blueButtonText.text = "Given Head"
        binding.sexToyExperience.blueButtonText.text = "Used Sex Toys"
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

        if (sharedPreferences.getInt(getString(R.string.gay_interest), 0) > 0) {
            binding.gayInterest.blueButtonLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.straight_interest), 0) > 0) {
            binding.straightInterest.blueButtonLayout.visibility = View.VISIBLE
        }

        if (sharedPreferences.getInt(getString(R.string.lesbian_interest), 0) > 0) {
            binding.lesbianInterest.blueButtonLayout.visibility = View.VISIBLE
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
        val activityStackModel: ActivityStackModel =
            mapper.readValue(sharedPreferences.getString(getString(R.string.activity_stack), "")!!)

        activityStackModel.activityStack.pop()

        val activityStackString = mapper.writeValueAsString(activityStackModel)
        sharedPreferencesEditor.putString(getString(R.string.activity_stack), activityStackString)
        sharedPreferencesEditor.apply()

        when (activityStackModel.activityStack.peek()) {
            getString(R.string.activity_home_display) -> fetchMatchedUsers()
            getString(R.string.activity_messenger) -> fetchUserMessengers()
            else -> super.onBackPressed()
        }
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
                userLikerResponseArray[5].userName, userLikerResponseArray[5].age
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
                userLikerResponseArray[4].userName, userLikerResponseArray[4].age
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
                userLikerResponseArray[3].userName, userLikerResponseArray[3].age
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
                userLikerResponseArray[2].userName, userLikerResponseArray[2].age
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
                userLikerResponseArray[1].userName, userLikerResponseArray[1].age
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
                userLikerResponseArray[0].userName, userLikerResponseArray[0].age
            )
        }
    }

    private fun triggerRequestProcess() {
        when (requestProcess) {
            getString(R.string.request_fetch_user_messengers) -> fetchUserMessengers()
            getString(R.string.request_fetch_matched_users) -> fetchMatchedUsers()
        }
    }

    @Throws(IOException::class)
    fun fetchUserInformation() {
        val mapper = jacksonObjectMapper()
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

                val intent = Intent(baseContext, UserInformationActivity::class.java)
                intent.putExtra("jsonResponse", myResponse)
                startActivity(intent)
            }
        })
    }

    @Throws(IOException::class)
    fun fetchUserMessengers() {
        val mapper = jacksonObjectMapper()
        val userLikerRequest = UserLikerRequest(sharedPreferences.getInt(getString(R.string.member_id), 0))

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
                activityStackModel.activityStack.push(getString(R.string.activity_messenger))
                val activityStackString = mapper.writeValueAsString(activityStackModel)
                sharedPreferencesEditor.putString(getString(R.string.activity_stack), activityStackString)
                sharedPreferencesEditor.apply()

                val intent = Intent(baseContext, MessengerActivity::class.java)
                intent.putExtra("jsonResponse", myResponse)
                startActivity(intent)
            }
        })
    }

    @Throws(IOException::class)
    fun fetchMatchedUsers() {
        val mapper = jacksonObjectMapper()
        val homeDisplayRequest = HomeDisplayRequest(
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
            sharedPreferences.getInt(getString(R.string.sugar_daddy_interest), 0),
            sharedPreferences.getInt(getString(R.string.sugar_mommy_interest), 0),
            sharedPreferences.getInt(getString(R.string.toy_boy_interest), 0),
            sharedPreferences.getInt(getString(R.string.toy_girl_interest), 0),
            sharedPreferences.getInt(getString(R.string.sixty_nine_experience), 0),
            sharedPreferences.getInt(getString(R.string.anal_sex_experience), 0),
            sharedPreferences.getInt(getString(R.string.given_head_experience), 0),
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
                activityStackModel.activityStack.push(getString(R.string.activity_home_display))
                val activityStackString = mapper.writeValueAsString(activityStackModel)
                sharedPreferencesEditor.putString(getString(R.string.activity_stack), activityStackString)
                sharedPreferencesEditor.apply()

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
                binding.bottomNavigationLayout.messageMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.ignored_bottom_menu)
                binding.bottomNavigationLayout.bottomAccountMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_account_blue))
                binding.bottomNavigationLayout.bottomMessageMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_message_blue))
            }
            getString(R.string.clicked_account_menu) -> {
                binding.bottomNavigationLayout.homeMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.ignored_bottom_menu)
                binding.bottomNavigationLayout.bottomHomeMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_home_blue))
                binding.bottomNavigationLayout.messageMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.ignored_bottom_menu)
                binding.bottomNavigationLayout.accountMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.selected_bottom_menu)
                binding.bottomNavigationLayout.bottomMessageMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_message_blue))
                binding.bottomNavigationLayout.bottomAccountMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_account_white))
            }
            getString(R.string.clicked_message_menu) -> {
                binding.bottomNavigationLayout.homeMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.ignored_bottom_menu)
                binding.bottomNavigationLayout.bottomHomeMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_home_blue))
                binding.bottomNavigationLayout.accountMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.ignored_bottom_menu)
                binding.bottomNavigationLayout.messageMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.selected_bottom_menu)
                binding.bottomNavigationLayout.bottomAccountMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_account_blue))
                binding.bottomNavigationLayout.bottomMessageMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_message_white))
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


