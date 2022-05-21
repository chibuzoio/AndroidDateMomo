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
import android.view.View
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.datemomo.MainApplication.Companion.setNavigationBarDarkIcons
import com.example.datemomo.MainApplication.Companion.setStatusBarDarkIcons
import com.example.datemomo.activity.UserBioActivity
import com.example.datemomo.databinding.ActivityMainBinding
import com.example.datemomo.model.UserNameModel
import com.example.datemomo.model.request.AuthenticationRequest
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
    private lateinit var sharedPreferences: SharedPreferences
    private var userNameArray: Array<UserNameModel> = emptyArray()
    private lateinit var sharedPreferencesEditor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.splash_screen)

        window.setStatusBarDarkIcons(true)
        window.setNavigationBarDarkIcons(true)

        buttonClickEffect = AlphaAnimation(1f, 0f)
        sharedPreferences =
            getSharedPreferences(getString(R.string.shared_preferences), Context.MODE_PRIVATE)
        sharedPreferencesEditor = sharedPreferences.edit()

        fetchUserNames()

        Handler(Looper.getMainLooper()).postDelayed({
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            if (sharedPreferences.getBoolean("authenticated", false)) {
                when {
                    sharedPreferences.getString("userLevel", "").equals("profile picture upload") -> {
                        binding.pictureUploadLayout.visibility = View.VISIBLE
                        binding.authenticationLayout.visibility = View.GONE
                        binding.registrationLayout.visibility = View.GONE
                    }
                    sharedPreferences.getString("userLevel", "").equals("sexuality") -> {
                        val intent = Intent(baseContext, UserBioActivity::class.java)
                        startActivity(intent)
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
                triggerRequestProcess()
            }

            binding.doubleButtonDialog.dialogCancelButton.setOnClickListener {
                binding.doubleButtonDialog.doubleButtonLayout.visibility = View.GONE
                binding.singleButtonDialog.singleButtonLayout.visibility = View.GONE
            }

            binding.doubleButtonDialog.doubleButtonLayout.setOnClickListener {
                binding.doubleButtonDialog.doubleButtonLayout.visibility = View.GONE
                binding.singleButtonDialog.singleButtonLayout.visibility = View.GONE
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
                        .trim()
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
                        .trim()
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

            binding.loginUserName.leftIconInputField.genericInputField.setOnFocusChangeListener { _, focused ->
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
                // you must not go back to registration page
                // Just display a beautiful exit dialogue asking the user if he wants to exit
                // and continue the registration process later

            }
            binding.registrationLayout.isVisible -> {
                binding.userNameInput.leftIconInputField.genericInputField.setText("")
                binding.passwordInput.leftIconInputField.genericInputField.setText("")
                binding.createAccountSubmit.blueButtonLayout.visibility = View.VISIBLE
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

    private fun triggerRequestProcess() {
        when (requestProcess) {
            getString(R.string.request_register_user) -> binding.createAccountSubmit.blueButtonLayout.performClick()
            getString(R.string.request_authenticate_user) -> binding.loginAccountSubmit.blueButtonLayout.performClick()
            getString(R.string.request_post_user_picture) -> binding.pictureUploadNext.blueButtonLayout.performClick()
        }
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
            sharedPreferences.getInt("memberId", 0),
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
            .url(getString(R.string.date_momo_api) + "service/postpicture.php")
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
                    sharedPreferencesEditor.putString("profilePicture",
                        pictureUploadResponse.profilePicture)
                    sharedPreferencesEditor.putString("sex", userSex)
                    sharedPreferencesEditor.putInt("age", pictureUploadResponse.age)
                    sharedPreferencesEditor.putString("userLevel", pictureUploadResponse.userLevel)
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
            .url(getString(R.string.date_momo_api) + "service/loginmember.php")
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
                    sharedPreferencesEditor.putInt("age", authenticationResponse.age)
                    sharedPreferencesEditor.putString("sex", authenticationResponse.sex)
                    sharedPreferencesEditor.putString("state", authenticationResponse.state)
                    sharedPreferencesEditor.putInt("memberId", authenticationResponse.memberId)
                    sharedPreferencesEditor.putString("country", authenticationResponse.country)
                    sharedPreferencesEditor.putString("fullName", authenticationResponse.fullName)
                    sharedPreferencesEditor.putString("userName", authenticationResponse.userName)
                    sharedPreferencesEditor.putString("userRole", authenticationResponse.userRole)
                    sharedPreferencesEditor.putString("userLevel", authenticationResponse.userLevel)
                    sharedPreferencesEditor.putString("phoneNumber", authenticationResponse.phoneNumber)
                    sharedPreferencesEditor.putString("emailAddress", authenticationResponse.emailAddress)
                    sharedPreferencesEditor.putBoolean("authenticated", authenticationResponse.authenticated)
                    sharedPreferencesEditor.putString("registrationDate", authenticationResponse.registrationDate)

                    sharedPreferencesEditor.putInt("bisexualCategory", authenticationResponse.bisexualCategory)
                    sharedPreferencesEditor.putInt("gayCategory", authenticationResponse.gayCategory)
                    sharedPreferencesEditor.putInt("lesbianCategory", authenticationResponse.lesbianCategory)
                    sharedPreferencesEditor.putInt("straightCategory", authenticationResponse.straightCategory)
                    sharedPreferencesEditor.putInt("sugarDaddyCategory", authenticationResponse.sugarDaddyCategory)
                    sharedPreferencesEditor.putInt("sugarMommyCategory", authenticationResponse.sugarMommyCategory)
                    sharedPreferencesEditor.putInt("toyBoyCategory", authenticationResponse.toyBoyCategory)
                    sharedPreferencesEditor.putInt("toyGirlCategory", authenticationResponse.toyGirlCategory)
                    sharedPreferencesEditor.putInt("bisexualInterest", authenticationResponse.bisexualInterest)
                    sharedPreferencesEditor.putInt("gayInterest", authenticationResponse.gayInterest)
                    sharedPreferencesEditor.putInt("lesbianInterest", authenticationResponse.lesbianInterest)
                    sharedPreferencesEditor.putInt("straightInterest", authenticationResponse.straightInterest)
                    sharedPreferencesEditor.putInt("sugarDaddyInterest", authenticationResponse.sugarDaddyInterest)
                    sharedPreferencesEditor.putInt("sugarMommyInterest", authenticationResponse.sugarMommyInterest)
                    sharedPreferencesEditor.putInt("toyBoyInterest", authenticationResponse.toyBoyInterest)
                    sharedPreferencesEditor.putInt("toyGirlInterest", authenticationResponse.toyGirlInterest)
                    sharedPreferencesEditor.putInt("sixtyNineExperience", authenticationResponse.sixtyNineExperience)
                    sharedPreferencesEditor.putInt("analSexExperience", authenticationResponse.analSexExperience)
                    sharedPreferencesEditor.putInt("givenHeadExperience", authenticationResponse.givenHeadExperience)
                    sharedPreferencesEditor.putInt("oneNightStandExperience", authenticationResponse.oneNightStandExperience)
                    sharedPreferencesEditor.putInt("orgySexExperience", authenticationResponse.orgySexExperience)
                    sharedPreferencesEditor.putInt("poolSexExperience", authenticationResponse.poolSexExperience)
                    sharedPreferencesEditor.putInt("receivedHeadExperience", authenticationResponse.receivedHeadExperience)
                    sharedPreferencesEditor.putInt("carSexExperience", authenticationResponse.carSexExperience)
                    sharedPreferencesEditor.putInt("publicSexExperience", authenticationResponse.publicSexExperience)
                    sharedPreferencesEditor.putInt("cameraSexExperience", authenticationResponse.cameraSexExperience)
                    sharedPreferencesEditor.putInt("threesomeExperience", authenticationResponse.threesomeExperience)
                    sharedPreferencesEditor.putInt("sexToyExperience", authenticationResponse.sexToyExperience)
                    sharedPreferencesEditor.putInt("videoSexExperience", authenticationResponse.videoSexExperience)

                    sharedPreferencesEditor.apply()

                    runOnUiThread {
                        binding.createAccountSubmit.blueButtonLayout.visibility = View.VISIBLE
                        binding.loginAccountSubmit.blueButtonLayout.visibility = View.VISIBLE
                        binding.authenticationLayout.visibility = View.VISIBLE
                        binding.registerProgressIcon.visibility = View.GONE
                        binding.pictureUploadLayout.visibility = View.GONE
                        binding.registrationLayout.visibility = View.GONE
                        binding.loginProgressIcon.visibility = View.GONE
                    }

                    Log.e(TAG, "User just got authenticated!!!!!!!!!!!!")

                    // call the process that will navigate to the next activity after fetching the required data

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
            .url(getString(R.string.date_momo_api) + "service/registermember.php")
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
                    sharedPreferencesEditor.putInt("memberId", registrationResponse.memberId)
                    sharedPreferencesEditor.putString("userName", registrationResponse.userName)
                    sharedPreferencesEditor.putString("userRole", registrationResponse.userRole)
                    sharedPreferencesEditor.putString("userLevel", registrationResponse.userLevel)
                    sharedPreferencesEditor.putBoolean("authenticated", registrationResponse.authenticated)
                    sharedPreferencesEditor.putString("registrationDate", registrationResponse.registrationDate)
                    sharedPreferencesEditor.apply()

                    runOnUiThread {
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


