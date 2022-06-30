package com.example.datemomo.activity

import android.content.Context
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
import com.example.datemomo.R
import com.example.datemomo.databinding.ActivityUserInformationBinding
import com.example.datemomo.model.response.HomeDisplayResponse
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.IOException

class UserInformationActivity : AppCompatActivity() {
    private var deviceWidth: Int = 0
    private var deviceHeight: Int = 0
    private lateinit var bundle: Bundle
    private lateinit var requestProcess: String
    private lateinit var originalRequestProcess: String
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
            homeDisplayResponse = mapper.readValue(bundle.getString("jsonResponse")!!)
        } catch (exception: IOException) {
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

        if (homeDisplayResponse.fullName != "") {
            binding.userFullName.text = getString(R.string.name_and_age_text,
                homeDisplayResponse.fullName, homeDisplayResponse.age
            )
        } else {
            binding.userFullName.text = getString(R.string.name_and_age_text,
                homeDisplayResponse.userName, homeDisplayResponse.age
            )
        }

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

        if (homeDisplayResponse.gayInterest > 0) {
            binding.gayInterest.blueButtonLayout.visibility = View.VISIBLE
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

    override fun onStart() {
        super.onStart()
        hideSystemUI()
    }

    private fun triggerRequestProcess() {

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


