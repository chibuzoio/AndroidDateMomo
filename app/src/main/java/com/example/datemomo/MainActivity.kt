package com.example.datemomo

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.example.datemomo.MainApplication.Companion.setNavigationBarDarkIcons
import com.example.datemomo.MainApplication.Companion.setStatusBarDarkIcons
import com.example.datemomo.databinding.ActivityMainBinding
import com.example.datemomo.model.DateMomoModel
import com.example.datemomo.model.UserNameModel
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.*
import java.io.IOException


class MainActivity : AppCompatActivity() {
    private var userNameValid = false
    private var passwordValid = false
    private lateinit var password: String
    private lateinit var userName: String
    private lateinit var binding: ActivityMainBinding
    private lateinit var buttonClickEffect: AlphaAnimation
    private var userNameArray: Array<UserNameModel> = emptyArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.setStatusBarDarkIcons(true)
        window.setNavigationBarDarkIcons(true)

        buttonClickEffect = AlphaAnimation(1f, 0f)

        fetchUserNames()

        binding.createAccountSubmit.blueButtonText.text = "Sign Up"
        binding.userNameInput.leftIconInputField.genericInputField.hint = "User Name"
        binding.passwordInput.leftIconInputField.genericInputField.hint = "Password"
        binding.userNameInput.leftIconInputImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_person))
        binding.passwordInput.leftIconInputImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_password))
        binding.passwordInput.leftIconInputField.genericInputField.transformationMethod =
            PasswordTransformationMethod.getInstance()

        binding.createAccountSubmit.blueButtonLayout.setOnClickListener {
            binding.createAccountSubmit.blueButtonLayout.startAnimation(buttonClickEffect)

            password = binding.passwordInput.leftIconInputField.genericInputField.text.toString().trim()
            userName = binding.userNameInput.leftIconInputField.genericInputField.text.toString().trim()
            validatePassword(password)
            validateUserName(userName)

            if (passwordValid && userNameValid) {
                registerUser()
            }
        }

        binding.userNameInput.leftIconInputField.genericInputField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                binding.userNameInput.leftIconInputLayout.background = ContextCompat.getDrawable(baseContext, R.drawable.focused_edit_text)
                binding.userNameInput.leftIconInputImage.setImageDrawable(ContextCompat.getDrawable(baseContext, R.drawable.icon_person_blue))
                binding.userNameInputError.visibility = View.GONE
            }
        })

        binding.passwordInput.leftIconInputField.genericInputField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                binding.passwordInput.leftIconInputLayout.background = ContextCompat.getDrawable(baseContext, R.drawable.focused_edit_text)
                binding.passwordInput.leftIconInputImage.setImageDrawable(ContextCompat.getDrawable(baseContext, R.drawable.icon_password_blue))
                binding.passwordInputError.visibility = View.GONE
            }
        })

        binding.userNameInput.leftIconInputField.genericInputField.setOnFocusChangeListener { _, focused ->
            if (focused) {
                binding.userNameInput.leftIconInputLayout.background = ContextCompat.getDrawable(this, R.drawable.focused_edit_text)
                binding.userNameInput.leftIconInputImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_person_blue))
                binding.userNameInputError.visibility = View.GONE
            } else {
                binding.userNameInput.leftIconInputLayout.background = ContextCompat.getDrawable(this, R.drawable.normal_edit_text)
                binding.userNameInput.leftIconInputImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_person))
            }
        }

        binding.passwordInput.leftIconInputField.genericInputField.setOnFocusChangeListener { _, focused ->
            if (focused) {
                binding.passwordInput.leftIconInputLayout.background = ContextCompat.getDrawable(this, R.drawable.focused_edit_text)
                binding.passwordInput.leftIconInputImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_password_blue))
                binding.passwordInputError.visibility = View.GONE
            } else {
                binding.passwordInput.leftIconInputLayout.background = ContextCompat.getDrawable(this, R.drawable.normal_edit_text)
                binding.passwordInput.leftIconInputImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_password))
            }
        }
    }

    private fun validatePassword(password: String) {
        var errorType = "Password is too short"

        if (password.length > 4) {
            passwordValid = true
        } else {
            binding.passwordInput.leftIconInputLayout.background = ContextCompat.getDrawable(this, R.drawable.error_edit_text)
            binding.passwordInput.leftIconInputImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_password_red))
            binding.passwordInputError.visibility = View.VISIBLE

            if (password.isEmpty()) {
                errorType = "Password field is empty"
            }

            binding.passwordInputError.text = errorType
            passwordValid = false
        }
    }

    private fun validateUserName(userName: String) {
        var errorType = "User name is too short"

        if (userName.length > 3) {
            userNameValid = true

            for (userNameModel in userNameArray) {
                if (userNameModel.userName.lowercase() == userName.lowercase()) {
                    errorType = "User name is already taken"
                    userNameValid = false
                    break
                }
            }
        } else {
            userNameValid = false
        }

        if (!userNameValid) {
            binding.userNameInput.leftIconInputLayout.background = ContextCompat.getDrawable(this, R.drawable.error_edit_text)
            binding.userNameInput.leftIconInputImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_person_red))
            binding.userNameInputError.visibility = View.VISIBLE

            if (userName.isEmpty()) {
                errorType = "User name field is empty"
            }

            binding.userNameInputError.text = errorType
        }
    }

    @Throws(IOException::class)
    fun fetchUserNames() {
        val client = OkHttpClient()
        val mapper = jacksonObjectMapper()
        val request: Request = Request.Builder()
            .url(getString(R.string.date_momo_api) + "service/usernamecomposite.php")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()
                Log.e(TAG, "Call to the server failed with the following message ${e.message}")
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val myResponse: String = response.body()!!.string()
                userNameArray = mapper.readValue(myResponse)
                Log.e(TAG, "Value of response from server is $myResponse")
            }
        })
    }

    @Throws(IOException::class)
    fun registerUser() {
        val mapper = jacksonObjectMapper()
        var userModel = DateMomoModel(
            0,
            "",
            "",
            userName,
            "",
            "",
            "",
            "",
            password,
            "",
            "",
            ""
        )

        var jsonObjectString = mapper.writeValueAsString(userModel)

        val requestBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json"), jsonObjectString
        )

        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(getString(R.string.date_momo_api) + "service/registermember.php")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val myResponse: String = response.body()!!.string()
                var dateMomoModel = mapper.readValue<DateMomoModel>(myResponse)
            }
        })
    }

    companion object {
        const val TAG = "MainActivity"
    }
}


