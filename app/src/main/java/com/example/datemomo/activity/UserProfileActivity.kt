package com.example.datemomo.activity

import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.datemomo.R
import com.example.datemomo.databinding.ActivityUserProfileBinding

class UserProfileActivity : AppCompatActivity() {
    private lateinit var requestProcess: String
    private lateinit var originalRequestProcess: String
    private lateinit var buttonClickEffect: AlphaAnimation
    private lateinit var binding: ActivityUserProfileBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferencesEditor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        window.decorView.systemUiVisibility = flags

        // Code below is to handle presses of Volume up or Volume down.
        // Without this, after pressing volume buttons, the navigation bar will
        // show up and won't hide

        // Code below is to handle presses of Volume up or Volume down.
        // Without this, after pressing volume buttons, the navigation bar will
        // show up and won't hide
        val decorView = window.decorView
        decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                decorView.systemUiVisibility = flags
            }
        }

        buttonClickEffect = AlphaAnimation(1f, 0f)
        sharedPreferences =
            getSharedPreferences(getString(R.string.shared_preferences), Context.MODE_PRIVATE)
        sharedPreferencesEditor = sharedPreferences.edit()

        redrawBottomMenuIcons(getString(R.string.clicked_account_menu))

        binding.bottomNavigationLayout.bottomHomeMenuLayout.setOnClickListener {
            redrawBottomMenuIcons(getString(R.string.clicked_home_menu))
        }

        binding.bottomNavigationLayout.bottomMessageMenuLayout.setOnClickListener {
            redrawBottomMenuIcons(getString(R.string.clicked_message_menu))
        }

        binding.bottomNavigationLayout.bottomAccountMenuLayout.setOnClickListener {
            redrawBottomMenuIcons(getString(R.string.clicked_account_menu))
        }

        Glide.with(this)
            .load(ColorDrawable(ContextCompat.getColor(this, R.color.grey_picture_placeholder)))
            .transform(CircleCrop())
            .into(binding.profilePicturePlaceholder)

        Glide.with(this)
            .load(ContextCompat.getDrawable(this, R.drawable.image1))
            .transform(CircleCrop(), CenterCrop())
            .into(binding.accountProfilePicture)

        binding.photoGalleryButton.iconHollowButtonText.text = "Photos"
        binding.photoGalleryButton.iconHollowButtonIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_gallery_blue))
        binding.photoGalleryButton.iconHollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_grey_button)

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

    override fun onBackPressed() {
        super.onBackPressed()
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
}


