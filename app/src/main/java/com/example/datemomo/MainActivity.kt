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
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.datemomo.MainApplication.Companion.setNavigationBarDarkIcons
import com.example.datemomo.MainApplication.Companion.setStatusBarDarkIcons
import com.example.datemomo.databinding.ActivityMainBinding
import com.example.datemomo.model.DateMomoModel
import com.example.datemomo.model.UserNameModel
import com.example.datemomo.model.request.PictureUploadRequest
import com.example.datemomo.model.response.PictureUploadResponse
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.Base64.getEncoder

class MainActivity : AppCompatActivity() {
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

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.setStatusBarDarkIcons(true)
        window.setNavigationBarDarkIcons(true)

        buttonClickEffect = AlphaAnimation(1f, 0f)
        sharedPreferences =
            getSharedPreferences(getString(R.string.shared_preferences), Context.MODE_PRIVATE)
        sharedPreferencesEditor = sharedPreferences.edit()

        fetchUserNames()

        binding.pictureUploadNext.blueButtonText.text = "Next"
        binding.maleGenderSelect.hollowButtonText.text = "Male"
        binding.femaleGenderSelect.hollowButtonText.text = "Female"
        binding.takePictureButton.iconHollowButtonText.text = "Take Picture"
        binding.uploadPictureButton.iconHollowButtonText.text = "Upload Picture"
        binding.takePictureButton.iconHollowButtonIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_camera_blue))
        binding.uploadPictureButton.iconHollowButtonIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_gallery_blue))

        binding.femaleGenderSelect.hollowButtonLayout.setOnClickListener {
            binding.femaleGenderSelect.hollowButtonLayout.startAnimation(buttonClickEffect)
            binding.femaleGenderSelect.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
            binding.maleGenderSelect.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
            binding.femaleGenderSelect.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.maleGenderSelect.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
            userSex = "Female"
        }

        binding.maleGenderSelect.hollowButtonLayout.setOnClickListener {
            binding.maleGenderSelect.hollowButtonLayout.startAnimation(buttonClickEffect)
            binding.maleGenderSelect.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
            binding.femaleGenderSelect.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
            binding.femaleGenderSelect.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
            binding.maleGenderSelect.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
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

            if (theBitmap != null && userSex.isNotEmpty()) {
                postUserPicture()
            }
        }

        binding.takePictureButton.iconHollowButtonLayout.setOnClickListener {
            binding.takePictureButton.iconHollowButtonLayout.startAnimation(buttonClickEffect)
            captureCameraImage()
        }

        binding.uploadPictureButton.iconHollowButtonLayout.setOnClickListener {
            binding.uploadPictureButton.iconHollowButtonLayout.startAnimation(buttonClickEffect)

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

        binding.passwordInput.leftIconInputLabel.text = "Password"
        binding.userNameInput.leftIconInputLabel.text = "User Name"
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
    fun postUserPicture() {
        val byteArrayOutputStream = ByteArrayOutputStream()
        theBitmap!!.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        val base64Picture =
            android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)

        val mapper = jacksonObjectMapper()
        val pictureUploadRequest = PictureUploadRequest(
            userSex,
            sharedPreferences.getInt("memberId", 0),
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
                    sharedPreferencesEditor.apply()

                    // Proceed to UserBioActivity activity

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
                Log.e(TAG, "Value of response from server is $myResponse")
            }
        })
    }

    @Throws(IOException::class)
    fun registerUser() {
        val mapper = jacksonObjectMapper()
        val userModel = DateMomoModel(
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

        val jsonObjectString = mapper.writeValueAsString(userModel)
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
                val dateMomoModel = mapper.readValue<DateMomoModel>(myResponse)

                if (dateMomoModel.memberId > 0) {
                    sharedPreferencesEditor.putInt("memberId", dateMomoModel.memberId)
                    sharedPreferencesEditor.putString("userName", dateMomoModel.userName)
                    sharedPreferencesEditor.putString("userRole", dateMomoModel.userRole)
                    sharedPreferencesEditor.putString("registrationDate", dateMomoModel.registrationDate)
                    sharedPreferencesEditor.apply()

                    runOnUiThread {
                        binding.registrationLayout.visibility = View.GONE
                        binding.pictureUploadLayout.visibility = View.VISIBLE
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


