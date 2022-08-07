package com.example.datemomo.activity

import android.content.Context
import android.content.SharedPreferences
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
import com.example.datemomo.databinding.ActivityUserAccountBinding
import com.example.datemomo.model.response.UserLikerResponse
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.IOException

class UserAccountActivity : AppCompatActivity() {
    private var deviceWidth: Int = 0
    private var deviceHeight: Int = 0
    private lateinit var bundle: Bundle
    private lateinit var requestProcess: String
    private lateinit var buttonClickEffect: AlphaAnimation
    private lateinit var binding: ActivityUserAccountBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferencesEditor: SharedPreferences.Editor
    private lateinit var userLikedResponseArray: ArrayList<UserLikerResponse>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserAccountBinding.inflate(layoutInflater)
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

        redrawBottomMenuIcons(getString(R.string.clicked_generic_menu))

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
            // Reload HomeDisplayActivity
        }

        binding.bottomNavigationLayout.bottomMessengerMenuLayout.setOnClickListener {
            redrawBottomMenuIcons(getString(R.string.clicked_message_menu))
            requestProcess = getString(R.string.request_fetch_user_messengers)
//            fetchUserMessengers()
        }

        binding.bottomNavigationLayout.bottomAccountMenuLayout.setOnClickListener {
            redrawBottomMenuIcons(getString(R.string.clicked_account_menu))
            requestProcess = getString(R.string.request_fetch_user_likers)
//            fetchUserLiked()
        }

        binding.bottomNavigationLayout.bottomNotificationMenuLayout.setOnClickListener {
            redrawBottomMenuIcons(getString(R.string.clicked_notification_menu))

        }

        binding.bottomNavigationLayout.bottomGenericMenuLayout.setOnClickListener {
            redrawBottomMenuIcons(getString(R.string.clicked_generic_menu))

        }

        try {
            val mapper = jacksonObjectMapper()
            userLikedResponseArray = mapper.readValue(bundle.getString("jsonResponse")!!)

            if (userLikedResponseArray.isEmpty()) {
                binding.userLikedDisplayLayout.visibility = View.GONE
            } else {
                if (userLikedResponseArray.size == 1) {
                    initializeFirstLikedLayout()
                }

                if (userLikedResponseArray.size == 2) {
                    initializeSecondLikedLayout()
                    initializeFirstLikedLayout()
                }

                if (userLikedResponseArray.size == 3) {
                    initializeSecondLikedLayout()
                    initializeFirstLikedLayout()
                    initializeThirdLikedLayout()
                }

                if (userLikedResponseArray.size >= 4) {
                    initializeFourthLikedLayout()
                    initializeSecondLikedLayout()
                    initializeFirstLikedLayout()
                    initializeThirdLikedLayout()
                }
            }
        } catch (exception: IOException) {
            Log.e(TAG, "Error message from here is ${exception.message}")
        }

        val marginStartHere = binding.checkFrameStartMargin.marginStart
        val marginPercentage = (marginStartHere.toFloat() / deviceWidth) * 100
        val totalMarginValue = ((marginPercentage * 5) / 100) * deviceWidth
        val remainingPictureWidth = deviceWidth - totalMarginValue
        val eachPictureWidth = remainingPictureWidth / 4
        val eachMarginValue = totalMarginValue / 5
        val eachPictureHeight = 1.1 * eachPictureWidth
        val eachUsernameHeight = ((30 / 100F) * eachPictureHeight).toInt()

        binding.firstMargin.layoutParams.width = eachMarginValue.toInt()
        binding.secondMargin.layoutParams.width = eachMarginValue.toInt()
        binding.thirdMargin.layoutParams.width = eachMarginValue.toInt()
        binding.fourthMargin.layoutParams.width = eachMarginValue.toInt()
        binding.fifthMargin.layoutParams.width = eachMarginValue.toInt()

        binding.firstLikedImage.layoutParams.width = eachPictureWidth.toInt()
        binding.firstLikedImage.layoutParams.height = eachPictureHeight.toInt()
        binding.firstLikedFrameLayout.layoutParams.width = eachPictureWidth.toInt()
        binding.firstLikedPlaceholder.layoutParams.width = eachPictureWidth.toInt()
        binding.firstLikedFrameLayout.layoutParams.height = eachPictureHeight.toInt()
        binding.firstLikedPlaceholder.layoutParams.height = eachPictureHeight.toInt()

        binding.secondLikedImage.layoutParams.width = eachPictureWidth.toInt()
        binding.secondLikedImage.layoutParams.height = eachPictureHeight.toInt()
        binding.secondLikedFrameLayout.layoutParams.width = eachPictureWidth.toInt()
        binding.secondLikedPlaceholder.layoutParams.width = eachPictureWidth.toInt()
        binding.secondLikedFrameLayout.layoutParams.height = eachPictureHeight.toInt()
        binding.secondLikedPlaceholder.layoutParams.height = eachPictureHeight.toInt()

        binding.thirdLikedImage.layoutParams.width = eachPictureWidth.toInt()
        binding.thirdLikedImage.layoutParams.height = eachPictureHeight.toInt()
        binding.thirdLikedFrameLayout.layoutParams.width = eachPictureWidth.toInt()
        binding.thirdLikedPlaceholder.layoutParams.width = eachPictureWidth.toInt()
        binding.thirdLikedFrameLayout.layoutParams.height = eachPictureHeight.toInt()
        binding.thirdLikedPlaceholder.layoutParams.height = eachPictureHeight.toInt()

        binding.fourthLikedImage.layoutParams.width = eachPictureWidth.toInt()
        binding.fourthLikedImage.layoutParams.height = eachPictureHeight.toInt()
        binding.fourthLikedFrameLayout.layoutParams.width = eachPictureWidth.toInt()
        binding.fourthLikedPlaceholder.layoutParams.width = eachPictureWidth.toInt()
        binding.fourthLikedFrameLayout.layoutParams.height = eachPictureHeight.toInt()
        binding.fourthLikedPlaceholder.layoutParams.height = eachPictureHeight.toInt()

        binding.firstLikedUsername.layoutParams.height = eachUsernameHeight
        binding.thirdLikedUsername.layoutParams.height = eachUsernameHeight
        binding.fourthLikedUsername.layoutParams.height = eachUsernameHeight
        binding.secondLikedUsername.layoutParams.height = eachUsernameHeight

        Glide.with(this)
            .asGif()
            .load(R.drawable.loading_puzzle)
            .into(binding.progressIconImage)

        Glide.with(this)
            .load(getString(R.string.date_momo_api) + getString(R.string.api_image)
                    + sharedPreferences.getString(getString(R.string.profile_picture), ""))
            .transform(CircleCrop(), CenterCrop())
            .into(binding.accountProfilePicture)
    }

    private fun initializeFourthLikedLayout() {
        binding.fourthLikedFrameLayout.visibility = View.VISIBLE

        Glide.with(this)
            .load(getString(R.string.date_momo_api) + getString(R.string.api_image)
                    + userLikedResponseArray[3].profilePicture)
            .transform(CenterCrop(), RoundedCorners(33))
            .into(binding.fourthLikedImage)

        binding.fourthLikedUsername.text = if (userLikedResponseArray[3].fullName != "") {
            getString(
                R.string.name_and_age_text,
                userLikedResponseArray[3].fullName, userLikedResponseArray[3].age
            )
        } else {
            getString(
                R.string.name_and_age_text,
                userLikedResponseArray[3].userName, userLikedResponseArray[3].age
            )
        }

        if (userLikedResponseArray.size > 4) {
            val moreLikedCount = userLikedResponseArray.size - 3
            binding.moreLikedCount.visibility = View.VISIBLE
            binding.moreLikedCover.visibility = View.VISIBLE
            binding.moreLikedCount.text = getString(R.string.more_liked_count, moreLikedCount)
        } else {
            binding.moreLikedCount.visibility = View.GONE
            binding.moreLikedCover.visibility = View.GONE
        }
    }

    private fun initializeThirdLikedLayout() {
        binding.thirdLikedFrameLayout.visibility = View.VISIBLE

        Glide.with(this)
            .load(getString(R.string.date_momo_api) + getString(R.string.api_image)
                    + userLikedResponseArray[2].profilePicture)
            .transform(CenterCrop(), RoundedCorners(33))
            .into(binding.thirdLikedImage)

        binding.thirdLikedUsername.text = if (userLikedResponseArray[2].fullName != "") {
            getString(
                R.string.name_and_age_text,
                userLikedResponseArray[2].fullName, userLikedResponseArray[2].age
            )
        } else {
            getString(
                R.string.name_and_age_text,
                userLikedResponseArray[2].userName, userLikedResponseArray[2].age
            )
        }
    }

    private fun initializeSecondLikedLayout() {
        binding.secondLikedFrameLayout.visibility = View.VISIBLE

        Glide.with(this)
            .load(getString(R.string.date_momo_api) + getString(R.string.api_image)
                    + userLikedResponseArray[1].profilePicture)
            .transform(CenterCrop(), RoundedCorners(33))
            .into(binding.secondLikedImage)

        binding.secondLikedUsername.text = if (userLikedResponseArray[1].fullName != "") {
            getString(
                R.string.name_and_age_text,
                userLikedResponseArray[1].fullName, userLikedResponseArray[1].age
            )
        } else {
            getString(
                R.string.name_and_age_text,
                userLikedResponseArray[1].userName, userLikedResponseArray[1].age
            )
        }
    }

    private fun initializeFirstLikedLayout() {
        binding.firstLikedFrameLayout.visibility = View.VISIBLE

        Glide.with(this)
            .load(getString(R.string.date_momo_api) + getString(R.string.api_image)
                    + userLikedResponseArray[0].profilePicture)
            .transform(CenterCrop(), RoundedCorners(33))
            .into(binding.firstLikedImage)

        binding.firstLikedUsername.text = if (userLikedResponseArray[0].fullName != "") {
            getString(
                R.string.name_and_age_text,
                userLikedResponseArray[0].fullName, userLikedResponseArray[0].age
            )
        } else {
            getString(
                R.string.name_and_age_text,
                userLikedResponseArray[0].userName, userLikedResponseArray[0].age
            )
        }
    }

    private fun triggerRequestProcess() {
        when (requestProcess) {
//            getString(R.string.request_fetch_user_messengers) -> fetchUserMessengers()
//            getString(R.string.request_fetch_user_messages) -> fetchUserMessages(messageRequest)
//            getString(R.string.request_fetch_matched_users) -> fetchUserLikers()
        }
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
                binding.bottomNavigationLayout.bottomNotificationMenuImage.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.icon_notification_blue))
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
                binding.bottomNavigationLayout.bottomNotificationMenuImage.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.icon_notification_blue))
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
                binding.bottomNavigationLayout.bottomNotificationMenuImage.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.icon_notification_blue))
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
                binding.bottomNavigationLayout.bottomNotificationMenuImage.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.icon_notification_white))
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
                binding.bottomNavigationLayout.bottomNotificationMenuImage.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.icon_notification_blue))
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
        const val TAG = "UserAccountActivity"
    }
}


