package com.example.datemomo.activity

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.datemomo.R
import com.example.datemomo.databinding.ActivityAccountManagerBinding

class AccountManagerActivity : AppCompatActivity() {
    private lateinit var requestProcess: String
    private lateinit var buttonClickEffect: AlphaAnimation
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivityAccountManagerBinding
    private lateinit var sharedPreferencesEditor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAccountManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hideSystemUI()

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
//            fetchUserLikers()
        }

        binding.bottomNavigationLayout.bottomNotificationMenuLayout.setOnClickListener {
            redrawBottomMenuIcons(getString(R.string.clicked_notification_menu))

        }

        binding.bottomNavigationLayout.bottomGenericMenuLayout.setOnClickListener {
            redrawBottomMenuIcons(getString(R.string.clicked_generic_menu))

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
        const val TAG = "AccountManagerActivity"
    }
}


