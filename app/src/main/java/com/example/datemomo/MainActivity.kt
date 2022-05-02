package com.example.datemomo

import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.datemomo.MainApplication.Companion.setNavigationBarDarkIcons
import com.example.datemomo.MainApplication.Companion.setStatusBarDarkIcons
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
}


