package com.example.datemomo

import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
    private lateinit var binding: ActivityMainBinding
    private lateinit var userNameArray: Array<UserNameModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.setStatusBarDarkIcons(true)
        window.setNavigationBarDarkIcons(true)

        fetchUserNames()

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
            "userName",
            "",
            "",
            "",
            "",
            "password",
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
            .url(getString(R.string.date_momo_api))
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val myResponse: String = response.body()!!.string()

            }
        })
    }

    companion object {
        const val TAG = "MainActivity"
    }
}


