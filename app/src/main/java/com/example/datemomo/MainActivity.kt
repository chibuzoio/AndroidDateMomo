package com.example.datemomo

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.*
import android.provider.MediaStore
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.datemomo.activity.HomeDisplayActivity
import com.example.datemomo.activity.UserBioActivity
import com.example.datemomo.databinding.ActivityMainBinding
import com.example.datemomo.model.ActivityStackModel
import com.example.datemomo.model.UserNameModel
import com.example.datemomo.model.request.AuthenticationRequest
import com.example.datemomo.model.request.HomeDisplayRequest
import com.example.datemomo.model.request.PictureUploadRequest
import com.example.datemomo.model.request.RegistrationRequest
import com.example.datemomo.model.response.AuthenticationResponse
import com.example.datemomo.model.response.PictureUploadResponse
import com.example.datemomo.model.response.RegistrationResponse
import com.example.datemomo.utility.Utility
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private var userAge = 0
    private var userAgeValid = false
    private var userSex: String = ""
    private val PERMISSION_CODE = 1001
    private var photoFile: File? = null
    private val PICK_IMAGE_REQUEST = 200
    private var theBitmap: Bitmap? = null
    private var loginPasswordValid = false
    private var loginUserNameValid = false
    private val CAPTURE_IMAGE_REQUEST = 100
    private var registerPasswordValid = false
    private var registerUserNameValid = false
    private lateinit var loginPassword: String
    private lateinit var loginUserName: String
    private lateinit var requestProcess: String
    private lateinit var registerPassword: String
    private lateinit var registerUserName: String
    private var mCurrentPhotoPath: String? = null
    private lateinit var binding: ActivityMainBinding
    private lateinit var buttonClickEffect: AlphaAnimation
    private var registrationInnerLayoutTopPadding: Int = 0
    private var pictureUploadInnerLayoutTopPadding: Int = 0
    private var authenticationInnerLayoutTopPadding: Int = 0
    private lateinit var sharedPreferences: SharedPreferences
    private var registrationInnerLayoutBottomPadding: Int = 0
    private var pictureUploadInnerLayoutBottomPadding: Int = 0
    private var authenticationInnerLayoutBottomPadding: Int = 0
    private var userNameArray: Array<UserNameModel> = emptyArray()
    private lateinit var sharedPreferencesEditor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        setContentView(R.layout.splash_screen)

        fetchUserNames()

        Handler(Looper.getMainLooper()).postDelayed({
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            if (sharedPreferences.getBoolean(getString(R.string.authenticated), false)) {
                when {
                    sharedPreferences.getString(getString(R.string.user_level), "")
                        .equals(getString(R.string.level_upload_profile_picture)) -> {
                        binding.loginPassword.leftIconInputField.genericInputField.setText("")
                        binding.loginUserName.leftIconInputField.genericInputField.setText("")
                        binding.pictureUploadLayout.visibility = View.VISIBLE
                        binding.authenticationLayout.visibility = View.GONE
                        binding.registrationLayout.visibility = View.GONE
                    }
                    sharedPreferences.getString(getString(R.string.user_level), "")
                        .equals(getString(R.string.level_select_sexuality_interest)) -> {
                        val intent = Intent(baseContext, UserBioActivity::class.java)
                        startActivity(intent)
                    }
                    sharedPreferences.getString(getString(R.string.user_level), "")
                        .equals(getString(R.string.level_display_matched_users)) -> {
                        requestProcess = getString(R.string.request_fetch_matched_users)
                        setContentView(R.layout.splash_screen)

                        fetchMatchedUsers()
                    }
                    else -> {
                        binding.authenticationLayout.visibility = View.VISIBLE
                        binding.pictureUploadLayout.visibility = View.GONE
                        binding.registrationLayout.visibility = View.GONE
                    }
                }
            } else {
                binding.authenticationLayout.visibility = View.VISIBLE
                binding.pictureUploadLayout.visibility = View.GONE
                binding.registrationLayout.visibility = View.GONE
            }

            Glide.with(this)
                .asGif()
                .load(R.drawable.loading_puzzle)
                .into(binding.loginProgressIcon)

            Glide.with(this)
                .asGif()
                .load(R.drawable.loading_puzzle)
                .into(binding.registerProgressIcon)

            Glide.with(this)
                .asGif()
                .load(R.drawable.loading_puzzle)
                .into(binding.uploadProgressIcon)

            binding.authenticationInnerLayout.viewTreeObserver.addOnGlobalLayoutListener {
                registrationInnerLayoutTopPadding = binding.registrationInnerLayout.paddingTop
                pictureUploadInnerLayoutTopPadding = binding.pictureUploadInnerLayout.paddingTop
                authenticationInnerLayoutTopPadding = binding.authenticationInnerLayout.paddingTop
                registrationInnerLayoutBottomPadding = binding.registrationInnerLayout.paddingBottom
                pictureUploadInnerLayoutBottomPadding = binding.pictureUploadInnerLayout.paddingBottom
                authenticationInnerLayoutBottomPadding = binding.authenticationInnerLayout.paddingBottom
            }

            binding.userAgeInput.genericInputField.hint = "Age"
            binding.userAgeInput.genericInputField.inputType = InputType.TYPE_CLASS_NUMBER

            binding.pictureUploadNext.blueButtonText.text = "Next"
            binding.maleGenderSelect.hollowButtonText.text = "Male"
            binding.femaleGenderSelect.hollowButtonText.text = "Female"
            binding.takePictureButton.iconHollowButtonText.text = "Take Picture"
            binding.uploadPictureButton.iconHollowButtonText.text = "Upload Picture"
            binding.takePictureButton.iconHollowButtonIcon.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.icon_camera_blue
                )
            )
            binding.uploadPictureButton.iconHollowButtonIcon.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.icon_gallery_blue
                )
            )

            binding.registrationInnerLayout.setOnClickListener {
                clearAllFieldFocus()
            }

            binding.pictureUploadInnerLayout.setOnClickListener {
                clearAllFieldFocus()
            }

            binding.authenticationInnerLayout.setOnClickListener {
                clearAllFieldFocus()
            }

            binding.activityFrameLayout.setOnClickListener {
                clearAllFieldFocus()
            }

            binding.authenticationLayout.setOnClickListener {
                clearAllFieldFocus()
            }

            binding.pictureUploadLayout.setOnClickListener {
                clearAllFieldFocus()
            }

            binding.registrationLayout.setOnClickListener {
                clearAllFieldFocus()
            }

            binding.singleButtonDialog.dialogRetryButton.setOnClickListener {
                binding.doubleButtonDialog.doubleButtonLayout.visibility = View.GONE
                binding.singleButtonDialog.singleButtonLayout.visibility = View.GONE
                triggerRequestProcess()
            }

            binding.singleButtonDialog.singleButtonLayout.setOnClickListener {
                binding.doubleButtonDialog.doubleButtonLayout.visibility = View.GONE
                binding.singleButtonDialog.singleButtonLayout.visibility = View.GONE
                hideAllProgressIcon()
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

                if (binding.doubleButtonDialog.dialogCancelButton.text == "Exit") {
                    finish()
                }
            }

            binding.doubleButtonDialog.doubleButtonLayout.setOnClickListener {
                binding.doubleButtonDialog.doubleButtonLayout.visibility = View.GONE
                binding.singleButtonDialog.singleButtonLayout.visibility = View.GONE
                hideAllProgressIcon()
            }

            binding.femaleGenderSelect.hollowButtonLayout.setOnClickListener {
                binding.femaleGenderSelect.hollowButtonLayout.startAnimation(buttonClickEffect)
                binding.femaleGenderSelect.hollowButtonLayout.background =
                    ContextCompat.getDrawable(this, R.drawable.blue_button)
                binding.maleGenderSelect.hollowButtonLayout.background =
                    ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                binding.femaleGenderSelect.hollowButtonText.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.white
                    )
                )
                binding.maleGenderSelect.hollowButtonText.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.blue
                    )
                )
                userSex = "Female"
            }

            binding.maleGenderSelect.hollowButtonLayout.setOnClickListener {
                binding.maleGenderSelect.hollowButtonLayout.startAnimation(buttonClickEffect)
                binding.maleGenderSelect.hollowButtonLayout.background =
                    ContextCompat.getDrawable(this, R.drawable.blue_button)
                binding.femaleGenderSelect.hollowButtonLayout.background =
                    ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                binding.femaleGenderSelect.hollowButtonText.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.blue
                    )
                )
                binding.maleGenderSelect.hollowButtonText.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.white
                    )
                )
                userSex = "Male"
            }

            binding.pictureUploadImage.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                        val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                        requestPermissions(permissions, PERMISSION_CODE)
                    } else {
                        pickImageFromGallery()
                    }
                } else {
                    pickImageFromGallery()
                }
            }

            binding.pictureUploadNext.blueButtonLayout.setOnClickListener {
                binding.pictureUploadNext.blueButtonLayout.startAnimation(buttonClickEffect)
                binding.pictureUploadNext.blueButtonLayout.visibility = View.GONE
                binding.uploadProgressIcon.visibility = View.VISIBLE

                userAge = if (binding.userAgeInput.genericInputField.text.toString().trim()
                        .isEmpty()
                ) 0
                else binding.userAgeInput.genericInputField.text.toString().trim().toInt()

                validateUserAge(userAge)

                if (theBitmap != null && userSex.isNotEmpty() && userAgeValid) {
                    requestProcess = getString(R.string.request_post_user_picture)

                    postUserPicture()
                }
            }

            binding.takePictureButton.iconHollowButtonLayout.setOnClickListener {
                binding.takePictureButton.iconHollowButtonLayout.startAnimation(
                    buttonClickEffect
                )
                captureCameraImage()
            }

            binding.uploadPictureButton.iconHollowButtonLayout.setOnClickListener {
                binding.uploadPictureButton.iconHollowButtonLayout.startAnimation(
                    buttonClickEffect
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                        val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                        requestPermissions(permissions, PERMISSION_CODE)
                    } else {
                        pickImageFromGallery()
                    }
                } else {
                    pickImageFromGallery()
                }
            }

            Glide.with(this)
                .load(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.placeholder
                    )
                )
                .transform(FitCenter(), RoundedCorners(33))
                .into(binding.pictureUploadImage)

            binding.loginAccountSubmit.blueButtonText.text = "Log In"
            binding.loginPassword.leftIconInputLabel.text = "Password"
            binding.loginUserName.leftIconInputLabel.text = "User Name"
            binding.registrationButton.hollowButtonText.text = "Sign Up"
            binding.loginPassword.leftIconInputField.genericInputField.hint = "Password"
            binding.loginUserName.leftIconInputField.genericInputField.hint = "User Name"
            binding.loginUserName.leftIconInputImage.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.icon_person
                )
            )
            binding.loginPassword.leftIconInputImage.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.icon_password
                )
            )
            binding.loginPassword.leftIconInputField.genericInputField.transformationMethod =
                PasswordTransformationMethod.getInstance()

            binding.loginAccountSubmit.blueButtonLayout.setOnClickListener {
                binding.loginAccountSubmit.blueButtonLayout.startAnimation(buttonClickEffect)
                binding.loginAccountSubmit.blueButtonLayout.visibility = View.GONE
                binding.loginProgressIcon.visibility = View.VISIBLE

                loginPassword =
                    binding.loginPassword.leftIconInputField.genericInputField.text.toString()
                        .trim()
                loginUserName =
                    binding.loginUserName.leftIconInputField.genericInputField.text.toString()
                        .trim().lowercase(Locale.getDefault())
                validateLoginPassword(loginPassword)
                validateLoginUserName(loginUserName)

                if (loginPasswordValid && loginUserNameValid) {
                    requestProcess = getString(R.string.request_authenticate_user)

                    authenticateUser()
                }
            }

            binding.registrationButton.hollowButtonLayout.setOnClickListener {
                binding.registrationButton.hollowButtonLayout.startAnimation(buttonClickEffect)
                binding.loginUserName.leftIconInputField.genericInputField.setText("")
                binding.loginPassword.leftIconInputField.genericInputField.setText("")
                binding.registrationLayout.visibility = View.VISIBLE
                binding.authenticationLayout.visibility = View.GONE
                binding.pictureUploadLayout.visibility = View.GONE
            }

            binding.passwordInput.leftIconInputLabel.text = "Password"
            binding.userNameInput.leftIconInputLabel.text = "User Name"
            binding.createAccountSubmit.blueButtonText.text = "Sign Up"
            binding.userNameInput.leftIconInputField.genericInputField.hint = "User Name"
            binding.passwordInput.leftIconInputField.genericInputField.hint = "Password"
            binding.userNameInput.leftIconInputImage.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.icon_person
                )
            )
            binding.passwordInput.leftIconInputImage.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.icon_password
                )
            )
            binding.passwordInput.leftIconInputField.genericInputField.transformationMethod =
                PasswordTransformationMethod.getInstance()

            binding.createAccountSubmit.blueButtonLayout.setOnClickListener {
                binding.createAccountSubmit.blueButtonLayout.startAnimation(buttonClickEffect)
                binding.createAccountSubmit.blueButtonLayout.visibility = View.GONE
                binding.registerProgressIcon.visibility = View.VISIBLE

                registerPassword =
                    binding.passwordInput.leftIconInputField.genericInputField.text.toString()
                        .trim()
                registerUserName =
                    binding.userNameInput.leftIconInputField.genericInputField.text.toString()
                        .trim().lowercase(Locale.getDefault())
                validateRegisterPassword(registerPassword)
                validateRegisterUserName(registerUserName)

                if (registerPasswordValid && registerUserNameValid) {
                    requestProcess = getString(R.string.request_register_user)

                    registerUser()
                }
            }

            binding.loginUserName.leftIconInputField.genericInputField.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(
                        p0: CharSequence?,
                        p1: Int,
                        p2: Int,
                        p3: Int
                    ) {

                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                    }

                    override fun afterTextChanged(p0: Editable?) {
                        binding.loginUserName.leftIconInputLayout.background =
                            ContextCompat.getDrawable(baseContext, R.drawable.focused_edit_text)
                        binding.loginUserName.leftIconInputImage.setImageDrawable(
                            ContextCompat.getDrawable(
                                baseContext,
                                R.drawable.icon_person_blue
                            )
                        )
                        binding.loginUserNameError.visibility = View.GONE
                    }
                })

            binding.userNameInput.leftIconInputField.genericInputField.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(
                        p0: CharSequence?,
                        p1: Int,
                        p2: Int,
                        p3: Int
                    ) {

                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                    }

                    override fun afterTextChanged(p0: Editable?) {
                        binding.userNameInput.leftIconInputLayout.background =
                            ContextCompat.getDrawable(baseContext, R.drawable.focused_edit_text)
                        binding.userNameInput.leftIconInputImage.setImageDrawable(
                            ContextCompat.getDrawable(
                                baseContext,
                                R.drawable.icon_person_blue
                            )
                        )
                        binding.userNameInputError.visibility = View.GONE
                    }
                })

            binding.loginPassword.leftIconInputField.genericInputField.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(
                        p0: CharSequence?,
                        p1: Int,
                        p2: Int,
                        p3: Int
                    ) {

                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                    }

                    override fun afterTextChanged(p0: Editable?) {
                        binding.loginPassword.leftIconInputLayout.background =
                            ContextCompat.getDrawable(baseContext, R.drawable.focused_edit_text)
                        binding.loginPassword.leftIconInputImage.setImageDrawable(
                            ContextCompat.getDrawable(
                                baseContext,
                                R.drawable.icon_password_blue
                            )
                        )
                        binding.loginPasswordError.visibility = View.GONE
                    }
                })

            binding.passwordInput.leftIconInputField.genericInputField.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(
                        p0: CharSequence?,
                        p1: Int,
                        p2: Int,
                        p3: Int
                    ) {

                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                    }

                    override fun afterTextChanged(p0: Editable?) {
                        binding.passwordInput.leftIconInputLayout.background =
                            ContextCompat.getDrawable(baseContext, R.drawable.focused_edit_text)
                        binding.passwordInput.leftIconInputImage.setImageDrawable(
                            ContextCompat.getDrawable(
                                baseContext,
                                R.drawable.icon_password_blue
                            )
                        )
                        binding.passwordInputError.visibility = View.GONE
                    }
                })

            binding.userAgeInput.genericInputField.setOnFocusChangeListener { _, focused ->
                setGravityOnFieldFocus(focused)
            }

            binding.loginUserName.leftIconInputField.genericInputField.setOnFocusChangeListener { _, focused ->
                setGravityOnFieldFocus(focused)

                if (focused) {
                    binding.loginUserName.leftIconInputLayout.background =
                        ContextCompat.getDrawable(this, R.drawable.focused_edit_text)
                    binding.loginUserName.leftIconInputImage.setImageDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.icon_person_blue
                        )
                    )
                    binding.loginUserNameError.visibility = View.GONE
                } else {
                    binding.loginUserName.leftIconInputLayout.background =
                        ContextCompat.getDrawable(this, R.drawable.normal_edit_text)
                    binding.loginUserName.leftIconInputImage.setImageDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.icon_person
                        )
                    )
                }
            }

            binding.userNameInput.leftIconInputField.genericInputField.setOnFocusChangeListener { _, focused ->
                setGravityOnFieldFocus(focused)

                if (focused) {
                    binding.userNameInput.leftIconInputLayout.background =
                        ContextCompat.getDrawable(this, R.drawable.focused_edit_text)
                    binding.userNameInput.leftIconInputImage.setImageDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.icon_person_blue
                        )
                    )
                    binding.userNameInputError.visibility = View.GONE
                } else {
                    binding.userNameInput.leftIconInputLayout.background =
                        ContextCompat.getDrawable(this, R.drawable.normal_edit_text)
                    binding.userNameInput.leftIconInputImage.setImageDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.icon_person
                        )
                    )
                }
            }

            binding.loginPassword.leftIconInputField.genericInputField.setOnFocusChangeListener { _, focused ->
                setGravityOnFieldFocus(focused)

                if (focused) {
                    binding.loginPassword.leftIconInputLayout.background =
                        ContextCompat.getDrawable(this, R.drawable.focused_edit_text)
                    binding.loginPassword.leftIconInputImage.setImageDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.icon_password_blue
                        )
                    )
                    binding.loginPasswordError.visibility = View.GONE
                } else {
                    binding.loginPassword.leftIconInputLayout.background =
                        ContextCompat.getDrawable(this, R.drawable.normal_edit_text)
                    binding.loginPassword.leftIconInputImage.setImageDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.icon_password
                        )
                    )
                }
            }

            binding.passwordInput.leftIconInputField.genericInputField.setOnFocusChangeListener { _, focused ->
                setGravityOnFieldFocus(focused)

                if (focused) {
                    binding.passwordInput.leftIconInputLayout.background =
                        ContextCompat.getDrawable(this, R.drawable.focused_edit_text)
                    binding.passwordInput.leftIconInputImage.setImageDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.icon_password_blue
                        )
                    )
                    binding.passwordInputError.visibility = View.GONE
                } else {
                    binding.passwordInput.leftIconInputLayout.background =
                        ContextCompat.getDrawable(this, R.drawable.normal_edit_text)
                    binding.passwordInput.leftIconInputImage.setImageDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.icon_password
                        )
                    )
                }
            }
        }, 2000)
    }

    override fun onBackPressed() {
        when {
            binding.pictureUploadLayout.isVisible -> {
                binding.doubleButtonDialog.dialogCancelButton.text = "Exit"
                binding.doubleButtonDialog.dialogRetryButton.text = "Cancel"
                binding.doubleButtonDialog.doubleButtonTitle.text = "Exit Notice!"
                binding.doubleButtonDialog.doubleButtonMessage.text = "Do you wish to exit the registration process and continue later?"
                binding.doubleButtonDialog.doubleButtonLayout.visibility = View.VISIBLE
            }
            binding.registrationLayout.isVisible -> {
                binding.loginPassword.leftIconInputField.genericInputField.setText("")
                binding.loginUserName.leftIconInputField.genericInputField.setText("")
                binding.userNameInput.leftIconInputField.genericInputField.setText("")
                binding.passwordInput.leftIconInputField.genericInputField.setText("")
                binding.createAccountSubmit.blueButtonLayout.visibility = View.VISIBLE
                binding.doubleButtonDialog.doubleButtonLayout.visibility = View.GONE
                binding.singleButtonDialog.singleButtonLayout.visibility = View.GONE
                binding.authenticationLayout.visibility = View.VISIBLE
                binding.registerProgressIcon.visibility = View.GONE
                binding.pictureUploadLayout.visibility = View.GONE
                binding.registrationLayout.visibility = View.GONE
            }
            else -> {
                super.onBackPressed()
            }
        }
    }

    private fun clearAllFieldFocus() {
        binding.userNameInput.leftIconInputField.genericInputField.clearFocus()
        binding.loginUserName.leftIconInputField.genericInputField.clearFocus()
        binding.passwordInput.leftIconInputField.genericInputField.clearFocus()
        binding.loginPassword.leftIconInputField.genericInputField.clearFocus()
        binding.userAgeInput.genericInputField.clearFocus()

        val inputMethodManager: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.loginUserName.leftIconInputField.genericInputField.windowToken, 0)
    }

    private fun setGravityOnFieldFocus(focused: Boolean) {
        if (focused) {
            binding.authenticationInnerLayout.setPadding(0, 0, 0, 0)
            binding.pictureUploadInnerLayout.setPadding(0, 0, 0, 0)
            binding.registrationInnerLayout.setPadding(0, 0, 0, 0)

            binding.activityFrameLayout.foregroundGravity = Gravity.NO_GRAVITY

            val genericLayoutParams = FrameLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.NO_GRAVITY
            }

            binding.authenticationLayout.layoutParams = genericLayoutParams
            binding.pictureUploadLayout.layoutParams = genericLayoutParams
            binding.registrationLayout.layoutParams = genericLayoutParams
        } else {
            binding.authenticationInnerLayout.setPadding(0, authenticationInnerLayoutTopPadding, 0, authenticationInnerLayoutBottomPadding)
            binding.pictureUploadInnerLayout.setPadding(0, pictureUploadInnerLayoutTopPadding, 0, pictureUploadInnerLayoutBottomPadding)
            binding.registrationInnerLayout.setPadding(0, registrationInnerLayoutTopPadding, 0, registrationInnerLayoutBottomPadding)

            binding.activityFrameLayout.foregroundGravity = Gravity.CENTER_VERTICAL

            val genericLayoutParams = FrameLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER_VERTICAL
            }

            binding.authenticationLayout.layoutParams = genericLayoutParams
            binding.pictureUploadLayout.layoutParams = genericLayoutParams
            binding.registrationLayout.layoutParams = genericLayoutParams
        }
    }

    private fun triggerRequestProcess() {
        when (requestProcess) {
            getString(R.string.request_register_user) -> binding.createAccountSubmit.blueButtonLayout.performClick()
            getString(R.string.request_authenticate_user) -> binding.loginAccountSubmit.blueButtonLayout.performClick()
            getString(R.string.request_post_user_picture) -> binding.pictureUploadNext.blueButtonLayout.performClick()
            getString(R.string.request_fetch_matched_users) -> fetchMatchedUsers()
        }
    }

    private fun hideAllProgressIcon() {
        binding.uploadPictureButton.iconHollowButtonLayout.visibility = View.VISIBLE
        binding.createAccountSubmit.blueButtonLayout.visibility = View.VISIBLE
        binding.loginAccountSubmit.blueButtonLayout.visibility = View.VISIBLE
        binding.pictureUploadNext.blueButtonLayout.visibility = View.VISIBLE
        binding.registerProgressIcon.visibility = View.GONE
        binding.uploadProgressIcon.visibility = View.GONE
        binding.loginProgressIcon.visibility = View.GONE
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAPTURE_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            theBitmap = BitmapFactory.decodeFile(photoFile!!.absolutePath)

            Glide.with(this)
                .load(theBitmap)
                .transform(FitCenter(), RoundedCorners(33))
                .into(binding.pictureUploadImage)
        } else if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            Glide.with(this)
                .load(data?.data)
                .transform(FitCenter(), RoundedCorners(33))
                .into(binding.pictureUploadImage)

            theBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(contentResolver, data?.data!!)
                ImageDecoder.decodeBitmap(source)
            } else{
                MediaStore.Images.Media.getBitmap(contentResolver, data?.data)
            }
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun captureCameraImage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
        } else {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            if (takePictureIntent.resolveActivity(packageManager) != null) {
                try {
                    photoFile = createImageFile()

                    if (photoFile != null) {
                        val photoURI = FileProvider.getUriForFile(this, "com.example.datemomo.fileprovider", photoFile!!)
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        startActivityForResult(takePictureIntent, CAPTURE_IMAGE_REQUEST)
                    }
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName, /* prefix */
            ".jpg", /* suffix */
            storageDir      /* directory */
        )

        mCurrentPhotoPath = image.absolutePath
        return image
    }

    private fun validateUserAge(age: Int) {
        var errorType = ""

        when {
            age in 1..17 -> {
                errorType = "You must be at least 18 years old"
                userAgeValid = false
            }
            age <= 0 -> {
                errorType = "Your age is required"
                userAgeValid = false
            }
            age > 80 -> {
                errorType = "Sorry, 80 years is the maximum age limit"
                userAgeValid = false
            }
            else -> {
                userAgeValid = true
            }
        }

        if (userAgeValid) {
            binding.userAgeInputError.visibility = View.GONE
        } else {
            binding.pictureUploadNext.blueButtonLayout.visibility = View.VISIBLE
            binding.userAgeInputError.visibility = View.VISIBLE
            binding.uploadProgressIcon.visibility = View.GONE
            binding.userAgeInputError.text = errorType
        }
    }

    private fun validateLoginPassword(password: String) {
        val errorType = "Password field is empty"

        if (password.isEmpty()) {
            binding.loginPassword.leftIconInputLayout.background = ContextCompat.getDrawable(this, R.drawable.error_edit_text)
            binding.loginPassword.leftIconInputImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_password_red))
            binding.loginAccountSubmit.blueButtonLayout.visibility = View.VISIBLE
            binding.loginPasswordError.visibility = View.VISIBLE
            binding.loginProgressIcon.visibility = View.GONE
            binding.loginPasswordError.text = errorType
            loginPasswordValid = false
        } else {
            loginPasswordValid = true
        }
    }

    private fun validateRegisterPassword(password: String) {
        var errorType = "Password is too short"

        if (password.length > 4) {
            registerPasswordValid = true
        } else {
            binding.passwordInput.leftIconInputLayout.background = ContextCompat.getDrawable(this, R.drawable.error_edit_text)
            binding.passwordInput.leftIconInputImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_password_red))
            binding.createAccountSubmit.blueButtonLayout.visibility = View.VISIBLE
            binding.passwordInputError.visibility = View.VISIBLE
            binding.registerProgressIcon.visibility = View.GONE

            if (password.isEmpty()) {
                errorType = "Password field is empty"
            }

            binding.passwordInputError.text = errorType
            registerPasswordValid = false
        }
    }

    private fun validateLoginUserName(userName: String) {
        val errorType = "User name field is empty"

        if (userName.isEmpty()) {
            binding.loginUserName.leftIconInputLayout.background = ContextCompat.getDrawable(this, R.drawable.error_edit_text)
            binding.loginUserName.leftIconInputImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_person_red))
            binding.loginAccountSubmit.blueButtonLayout.visibility = View.VISIBLE
            binding.loginUserNameError.visibility = View.VISIBLE
            binding.loginProgressIcon.visibility = View.GONE
            binding.loginUserNameError.text = errorType
            userAgeValid = false
        } else {
            loginUserNameValid = true
        }
    }

    private fun validateRegisterUserName(userName: String) {
        var errorType = "User name is too short"

        if (userName.length > 3) {
            registerUserNameValid = true

            if (registerUserName.contains(" ")) {
                errorType = "User name must not contain space"
                registerUserNameValid = false
            } else {
                for (userNameModel in userNameArray) {
                    if (userNameModel.userName.equals(userName, ignoreCase = true)) {
                        errorType = "User name is already taken"
                        registerUserNameValid = false
                        break
                    }
                }
            }
        } else {
            registerUserNameValid = false
        }

        if (!registerUserNameValid) {
            binding.userNameInput.leftIconInputLayout.background = ContextCompat.getDrawable(this, R.drawable.error_edit_text)
            binding.userNameInput.leftIconInputImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_person_red))
            binding.createAccountSubmit.blueButtonLayout.visibility = View.VISIBLE
            binding.userNameInputError.visibility = View.VISIBLE
            binding.registerProgressIcon.visibility = View.GONE

            if (userName.isEmpty()) {
                errorType = "User name field is empty"
            }

            binding.userNameInputError.text = errorType
        }
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
                    setContentView(binding.root)
                    binding.createAccountSubmit.blueButtonLayout.visibility = View.VISIBLE
                    binding.loginAccountSubmit.blueButtonLayout.visibility = View.VISIBLE
                    binding.authenticationLayout.visibility = View.VISIBLE
                    binding.registerProgressIcon.visibility = View.GONE
                    binding.pictureUploadLayout.visibility = View.GONE
                    binding.registrationLayout.visibility = View.GONE
                    binding.loginProgressIcon.visibility = View.GONE
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

                val activityStack = Stack<String>()
                activityStack.push(getString(R.string.activity_home_display))
                val activityStackString = mapper.writeValueAsString(ActivityStackModel(activityStack))
                sharedPreferencesEditor.putString(getString(R.string.activity_stack), activityStackString)
                sharedPreferencesEditor.apply()

                val intent = Intent(baseContext, HomeDisplayActivity::class.java)
                intent.putExtra("jsonResponse", myResponse)
                startActivity(intent)
            }
        })
    }

    @Throws(IOException::class)
    fun postUserPicture() {
        val imageWidth = theBitmap!!.width
        val imageHeight = theBitmap!!.height

        val byteArrayOutputStream = ByteArrayOutputStream()
        theBitmap!!.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        val base64Picture =
            android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)

        val mapper = jacksonObjectMapper()
        val pictureUploadRequest = PictureUploadRequest(
            userSex,
            sharedPreferences.getInt(getString(R.string.member_id), 0),
            userAge,
            imageWidth,
            imageHeight,
            base64Picture,
            getString(R.string.level_select_sexuality_interest)
        )

        val jsonObjectString = mapper.writeValueAsString(pictureUploadRequest)
        val requestBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            jsonObjectString
        )

        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(getString(R.string.date_momo_api) + getString(R.string.api_post_picture))
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()

                runOnUiThread {
                    binding.pictureUploadNext.blueButtonLayout.visibility = View.VISIBLE
                    binding.uploadProgressIcon.visibility = View.GONE
                }

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
                var pictureUploadResponse = PictureUploadResponse(
                    0, 0, "", "")

                try {
                    pictureUploadResponse = mapper.readValue(myResponse)
                } catch (exception: IOException) {
                    displaySingleButtonDialog(getString(R.string.server_error_title), getString(R.string.server_error_message))
                }

                if (pictureUploadResponse.pictureId > 0) {
                    sharedPreferencesEditor.putString(getString(R.string.profile_picture),
                        pictureUploadResponse.profilePicture)
                    sharedPreferencesEditor.putString(getString(R.string.sex), userSex)
                    sharedPreferencesEditor.putInt(getString(R.string.age), pictureUploadResponse.age)
                    sharedPreferencesEditor.putString(getString(R.string.user_level), pictureUploadResponse.userLevel)
                    sharedPreferencesEditor.apply()

                    runOnUiThread {
                        binding.pictureUploadNext.blueButtonLayout.visibility = View.VISIBLE
                        binding.uploadProgressIcon.visibility = View.GONE
                    }

                    val intent = Intent(baseContext, UserBioActivity::class.java)
                    startActivity(intent)
                }
            }
        })
    }

    @Throws(IOException::class)
    fun fetchUserNames() {
        val client = OkHttpClient()
        val mapper = jacksonObjectMapper()
        val request: Request = Request.Builder()
            .url(getString(R.string.date_momo_api) + getString(R.string.api_user_name_composite))
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
            }
        })
    }

    @Throws(IOException::class)
    private fun authenticateUser() {
        val mapper = jacksonObjectMapper()
        val authenticationRequest = AuthenticationRequest(
            loginUserName,
            loginPassword
        )

        val jsonObjectString = mapper.writeValueAsString(authenticationRequest)
        val requestBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            jsonObjectString
        )

        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(getString(R.string.date_momo_api) + getString(R.string.api_login_member))
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()

                runOnUiThread {
                    binding.loginAccountSubmit.blueButtonLayout.visibility = View.VISIBLE
                    binding.loginProgressIcon.visibility = View.GONE
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
                var authenticationResponse = AuthenticationResponse(0, 0, "",
                    "", "", "", "", "", "",
                    "", "", false, "",
                    "", 0, 0, 0,
                    0, 0, 0, 0,
                    0, 0, 0, 0, 0,
                    0, 0, 0, 0,
                    0, 0, 0, 0,
                    0, 0, 0, 0,
                    0, 0, 0, 0,
                    0)

                try {
                    authenticationResponse = mapper.readValue(myResponse)
                } catch (exception: IOException) {
                    displaySingleButtonDialog(getString(R.string.server_error_title), getString(R.string.server_error_message))
                }

                if (authenticationResponse.authenticated) {
                    sharedPreferencesEditor.putInt(getString(R.string.age), authenticationResponse.age)
                    sharedPreferencesEditor.putString(getString(R.string.sex), authenticationResponse.sex)
                    sharedPreferencesEditor.putString(getString(R.string.state), authenticationResponse.state)
                    sharedPreferencesEditor.putInt(getString(R.string.member_id), authenticationResponse.memberId)
                    sharedPreferencesEditor.putString(getString(R.string.country), authenticationResponse.country)
                    sharedPreferencesEditor.putString(getString(R.string.full_name), authenticationResponse.fullName)
                    sharedPreferencesEditor.putString(getString(R.string.user_name), authenticationResponse.userName)
                    sharedPreferencesEditor.putString(getString(R.string.user_role), authenticationResponse.userRole)
                    sharedPreferencesEditor.putString(getString(R.string.user_level), authenticationResponse.userLevel)
                    sharedPreferencesEditor.putString(getString(R.string.phone_number), authenticationResponse.phoneNumber)
                    sharedPreferencesEditor.putString(getString(R.string.email_address), authenticationResponse.emailAddress)
                    sharedPreferencesEditor.putBoolean(getString(R.string.authenticated), authenticationResponse.authenticated)
                    sharedPreferencesEditor.putString(getString(R.string.profile_picture), authenticationResponse.profilePicture)
                    sharedPreferencesEditor.putString(getString(R.string.registration_date), authenticationResponse.registrationDate)

                    sharedPreferencesEditor.putInt(getString(R.string.bisexual_category), authenticationResponse.bisexualCategory)
                    sharedPreferencesEditor.putInt(getString(R.string.gay_category), authenticationResponse.gayCategory)
                    sharedPreferencesEditor.putInt(getString(R.string.lesbian_category), authenticationResponse.lesbianCategory)
                    sharedPreferencesEditor.putInt(getString(R.string.straight_category), authenticationResponse.straightCategory)
                    sharedPreferencesEditor.putInt(getString(R.string.sugar_daddy_category), authenticationResponse.sugarDaddyCategory)
                    sharedPreferencesEditor.putInt(getString(R.string.sugar_mommy_category), authenticationResponse.sugarMommyCategory)
                    sharedPreferencesEditor.putInt(getString(R.string.toy_boy_category), authenticationResponse.toyBoyCategory)
                    sharedPreferencesEditor.putInt(getString(R.string.toy_girl_category), authenticationResponse.toyGirlCategory)
                    sharedPreferencesEditor.putInt(getString(R.string.bisexual_interest), authenticationResponse.bisexualInterest)
                    sharedPreferencesEditor.putInt(getString(R.string.gay_interest), authenticationResponse.gayInterest)
                    sharedPreferencesEditor.putInt(getString(R.string.lesbian_interest), authenticationResponse.lesbianInterest)
                    sharedPreferencesEditor.putInt(getString(R.string.straight_interest), authenticationResponse.straightInterest)
                    sharedPreferencesEditor.putInt(getString(R.string.sugar_daddy_interest), authenticationResponse.sugarDaddyInterest)
                    sharedPreferencesEditor.putInt(getString(R.string.sugar_mommy_interest), authenticationResponse.sugarMommyInterest)
                    sharedPreferencesEditor.putInt(getString(R.string.toy_boy_interest), authenticationResponse.toyBoyInterest)
                    sharedPreferencesEditor.putInt(getString(R.string.toy_girl_interest), authenticationResponse.toyGirlInterest)
                    sharedPreferencesEditor.putInt(getString(R.string.sixty_nine_experience), authenticationResponse.sixtyNineExperience)
                    sharedPreferencesEditor.putInt(getString(R.string.anal_sex_experience), authenticationResponse.analSexExperience)
                    sharedPreferencesEditor.putInt(getString(R.string.given_head_experience), authenticationResponse.givenHeadExperience)
                    sharedPreferencesEditor.putInt(getString(R.string.one_night_stand_experience), authenticationResponse.oneNightStandExperience)
                    sharedPreferencesEditor.putInt(getString(R.string.orgy_experience), authenticationResponse.orgySexExperience)
                    sharedPreferencesEditor.putInt(getString(R.string.pool_sex_experience), authenticationResponse.poolSexExperience)
                    sharedPreferencesEditor.putInt(getString(R.string.received_head_experience), authenticationResponse.receivedHeadExperience)
                    sharedPreferencesEditor.putInt(getString(R.string.car_sex_experience), authenticationResponse.carSexExperience)
                    sharedPreferencesEditor.putInt(getString(R.string.public_sex_experience), authenticationResponse.publicSexExperience)
                    sharedPreferencesEditor.putInt(getString(R.string.camera_sex_experience), authenticationResponse.cameraSexExperience)
                    sharedPreferencesEditor.putInt(getString(R.string.threesome_experience), authenticationResponse.threesomeExperience)
                    sharedPreferencesEditor.putInt(getString(R.string.sex_toy_experience), authenticationResponse.sexToyExperience)
                    sharedPreferencesEditor.putInt(getString(R.string.video_sex_experience), authenticationResponse.videoSexExperience)

                    sharedPreferencesEditor.apply()

                    runOnUiThread {
                        when (authenticationResponse.userLevel) {
                            getString(R.string.level_upload_profile_picture) -> {
                                binding.loginPassword.leftIconInputField.genericInputField.setText("")
                                binding.loginUserName.leftIconInputField.genericInputField.setText("")
                                binding.pictureUploadLayout.visibility = View.VISIBLE
                                binding.authenticationLayout.visibility = View.GONE
                                binding.registrationLayout.visibility = View.GONE
                            }
                            getString(R.string.level_select_sexuality_interest) -> {
                                val intent = Intent(baseContext, UserBioActivity::class.java)
                                startActivity(intent)
                            }
                            getString(R.string.level_display_matched_users) -> {
                                requestProcess = getString(R.string.request_fetch_matched_users)

                                fetchMatchedUsers()
                            }
                            else -> {
                                binding.loginAccountSubmit.blueButtonLayout.visibility =
                                    View.VISIBLE
                                binding.authenticationLayout.visibility = View.VISIBLE
                                binding.pictureUploadLayout.visibility = View.GONE
                                binding.registrationLayout.visibility = View.GONE
                                binding.loginProgressIcon.visibility = View.GONE

                                displaySingleButtonDialog(
                                    getString(R.string.server_error_title),
                                    getString(R.string.server_error_message)
                                )
                            }
                        }
                    }
                }
            }
        })
    }

    @Throws(IOException::class)
    fun registerUser() {
        val mapper = jacksonObjectMapper()
        val registrationRequest = RegistrationRequest(
            registerUserName,
            registerPassword,
            getString(R.string.level_upload_profile_picture)
        )

        val jsonObjectString = mapper.writeValueAsString(registrationRequest)
        val requestBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            jsonObjectString
        )

        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(getString(R.string.date_momo_api) + getString(R.string.api_register_member))
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()

                runOnUiThread {
                    binding.createAccountSubmit.blueButtonLayout.visibility = View.VISIBLE
                    binding.registerProgressIcon.visibility = View.GONE
                }

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
                var registrationResponse = RegistrationResponse(
                    0, "", "", "", false, "")

                try {
                    registrationResponse = mapper.readValue(myResponse)
                } catch (exception: IOException) {
                    displaySingleButtonDialog(getString(R.string.server_error_title), getString(R.string.server_error_message))
                }

                if (registrationResponse.authenticated) {
                    sharedPreferencesEditor.putInt(getString(R.string.member_id), registrationResponse.memberId)
                    sharedPreferencesEditor.putString(getString(R.string.user_name), registrationResponse.userName)
                    sharedPreferencesEditor.putString(getString(R.string.user_role), registrationResponse.userRole)
                    sharedPreferencesEditor.putString(getString(R.string.user_level), registrationResponse.userLevel)
                    sharedPreferencesEditor.putBoolean(getString(R.string.authenticated), registrationResponse.authenticated)
                    sharedPreferencesEditor.putString(getString(R.string.registration_date), registrationResponse.registrationDate)
                    sharedPreferencesEditor.apply()

                    runOnUiThread {
                        binding.passwordInput.leftIconInputField.genericInputField.setText("")
                        binding.userNameInput.leftIconInputField.genericInputField.setText("")
                        binding.loginPassword.leftIconInputField.genericInputField.setText("")
                        binding.loginUserName.leftIconInputField.genericInputField.setText("")
                        binding.createAccountSubmit.blueButtonLayout.visibility = View.VISIBLE
                        binding.loginAccountSubmit.blueButtonLayout.visibility = View.VISIBLE
                        binding.pictureUploadLayout.visibility = View.VISIBLE
                        binding.registerProgressIcon.visibility = View.GONE
                        binding.authenticationLayout.visibility = View.GONE
                        binding.registrationLayout.visibility = View.GONE
                        binding.loginProgressIcon.visibility = View.GONE
                    }
                }

                fetchUserNames()
            }
        })
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
        const val TAG = "MainActivity"
    }
}


