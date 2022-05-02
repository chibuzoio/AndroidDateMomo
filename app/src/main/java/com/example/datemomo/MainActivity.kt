package com.example.datemomo

import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
import androidx.core.content.ContextCompat
import com.example.datemomo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.setStatusBarDarkIcons(true)
        window.setNavigationBarDarkIcons(true)

        binding.createAccountSubmit.blueButtonText.text = "Sign Up"
        binding.userNameInput.leftIconInputField.genericInputField.hint = "User Name"
        binding.passwordInput.leftIconInputField.genericInputField.hint = "Password"
        binding.userNameInput.leftIconInputImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_person))
        binding.passwordInput.leftIconInputImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_password))
        binding.passwordInput.leftIconInputField.genericInputField.transformationMethod =
            PasswordTransformationMethod.getInstance()

        binding.userNameInput.leftIconInputField.genericInputField.setOnFocusChangeListener { _, focused ->
            if (focused) {
                binding.userNameInput.leftIconInputLayout.background = ContextCompat.getDrawable(this, R.drawable.focused_edit_text)
                binding.userNameInput.leftIconInputImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_person_blue))
            } else {
                binding.userNameInput.leftIconInputLayout.background = ContextCompat.getDrawable(this, R.drawable.normal_edit_text)
                binding.userNameInput.leftIconInputImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_person))
            }
        }

        binding.passwordInput.leftIconInputField.genericInputField.setOnFocusChangeListener { _, focused ->
            if (focused) {
                binding.passwordInput.leftIconInputLayout.background = ContextCompat.getDrawable(this, R.drawable.focused_edit_text)
                binding.passwordInput.leftIconInputImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_password_blue))
            } else {
                binding.passwordInput.leftIconInputLayout.background = ContextCompat.getDrawable(this, R.drawable.normal_edit_text)
                binding.passwordInput.leftIconInputImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_password))
            }
        }
    }

    @Suppress("DEPRECATION")
    fun Window.setStatusBarDarkIcons(dark: Boolean) {
        when {
            Build.VERSION_CODES.R <= Build.VERSION.SDK_INT -> insetsController?.setSystemBarsAppearance(
                if (dark) WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS else 0,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
            Build.VERSION_CODES.M <= Build.VERSION.SDK_INT -> decorView.systemUiVisibility = if (dark) {
                decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }
            else -> if (dark) {
                // dark status bar icons not supported on API level below 23, set status bar
                // color to black to keep icons visible
                statusBarColor = Color.BLACK
            }
        }
    }

    @Suppress("DEPRECATION")
    fun Window.setNavigationBarDarkIcons(dark: Boolean) {
        when {
            Build.VERSION_CODES.R <= Build.VERSION.SDK_INT -> insetsController?.setSystemBarsAppearance(
                if (dark) WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS else 0,
                WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
            )
            Build.VERSION_CODES.O <= Build.VERSION.SDK_INT -> decorView.systemUiVisibility = if (dark) {
                decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            } else {
                decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
            }
            else -> if (dark) {
                // dark navigation bar icons not supported on API level below 26, set navigation bar
                // color to black to keep icons visible
                navigationBarColor = Color.BLACK
            }
        }
    }
}


