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
import com.example.datemomo.model.request.PictureUploadRequest
import com.example.datemomo.model.request.RegistrationRequest
import com.example.datemomo.model.response.PictureUploadResponse
import com.example.datemomo.model.response.RegistrationResponse
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
    private var userNameValid = false
    private var passwordValid = false
    private val PERMISSION_CODE = 1001
    private var photoFile: File? = null
    private val PICK_IMAGE_REQUEST = 200
    private var theBitmap: Bitmap? = null
    private lateinit var password: String
    private lateinit var userName: String
    private val CAPTURE_IMAGE_REQUEST = 100
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

                password =
                    binding.passwordInput.leftIconInputField.genericInputField.text.toString()
                        .trim()
                userName =
                    binding.userNameInput.leftIconInputField.genericInputField.text.toString()
                        .trim()
                validateLoginPassword(password)
                validateLoginUserName(userName)

                if (passwordValid && userNameValid) {
                    authenticateUser()
                }
            }

            binding.registrationButton.hollowButtonLayout.setOnClickListener {
                binding.registrationButton.hollowButtonLayout.startAnimation(buttonClickEffect)
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

                password =
                    binding.passwordInput.leftIconInputField.genericInputField.text.toString()
                        .trim()
                userName =
                    binding.userNameInput.leftIconInputField.genericInputField.text.toString()
                        .trim()
                validatePassword(password)
                validateUserName(userName)

                if (passwordValid && userNameValid) {
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
                // go back to login page
                binding.authenticationLayout.visibility = View.VISIBLE
                binding.pictureUploadLayout.visibility = View.GONE
                binding.registrationLayout.visibility = View.GONE
            }
            else -> {
                super.onBackPressed()
            }
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
            binding.userAgeInputError.visibility = View.VISIBLE
            binding.userAgeInputError.text = errorType
        }
    }

    private fun validateLoginPassword(password: String) {
        val errorType = "Password field is empty"

        if (password.isEmpty()) {
            binding.loginPassword.leftIconInputLayout.background = ContextCompat.getDrawable(this, R.drawable.error_edit_text)
            binding.loginPassword.leftIconInputImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_password_red))
            binding.loginPasswordError.visibility = View.VISIBLE
            binding.loginPasswordError.text = errorType
            passwordValid = false
        } else {
            passwordValid = true
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

    private fun validateLoginUserName(userName: String) {
        val errorType = "User name field is empty"

        if (userName.isEmpty()) {
            binding.loginUserName.leftIconInputLayout.background = ContextCompat.getDrawable(this, R.drawable.error_edit_text)
            binding.loginUserName.leftIconInputImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_person_red))
            binding.loginUserNameError.visibility = View.VISIBLE
            binding.loginUserNameError.text = errorType
            userAgeValid = false
        } else {
            userNameValid = true
        }
    }

    private fun validateUserName(userName: String) {
        var errorType = "User name is too short"

        if (userName.length > 3) {
            userNameValid = true

            for (userNameModel in userNameArray) {
                if (userNameModel.userName.equals(userName, ignoreCase = true)) {
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
            base64Picture
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
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val myResponse: String = response.body()!!.string()
                val pictureUploadResponse = mapper.readValue<PictureUploadResponse>(myResponse)

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


        // inside onResponse do this:
        runOnUiThread {
            binding.loginAccountSubmit.blueButtonLayout.visibility = View.VISIBLE
            binding.loginProgressIcon.visibility = View.GONE
        }

    }

    @Throws(IOException::class)
    fun registerUser() {
        val mapper = jacksonObjectMapper()
        val registrationRequest = RegistrationRequest(
            userName,
            password
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
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val myResponse: String = response.body()!!.string()
                val registrationResponse = mapper.readValue<RegistrationResponse>(myResponse)

                if (registrationResponse.memberId > 0) {
                    sharedPreferencesEditor.putInt("memberId", registrationResponse.memberId)
                    sharedPreferencesEditor.putString("userName", registrationResponse.userName)
                    sharedPreferencesEditor.putString("userRole", registrationResponse.userRole)
                    sharedPreferencesEditor.putString("userLevel", registrationResponse.userLevel)
                    sharedPreferencesEditor.putBoolean("authenticated", registrationResponse.authenticated)
                    sharedPreferencesEditor.putString("registrationDate", registrationResponse.registrationDate)
                    sharedPreferencesEditor.apply()

                    runOnUiThread {
                        binding.createAccountSubmit.blueButtonLayout.visibility = View.VISIBLE
                        binding.pictureUploadLayout.visibility = View.VISIBLE
                        binding.registerProgressIcon.visibility = View.GONE
                        binding.authenticationLayout.visibility = View.GONE
                        binding.registrationLayout.visibility = View.GONE
                    }
                }

                fetchUserNames()
            }
        })
    }

    companion object {
        const val TAG = "MainActivity"
    }
}


