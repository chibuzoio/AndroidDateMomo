package com.example.datemomo.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.datemomo.R
import com.example.datemomo.databinding.ActivityProfileEditorBinding
import com.example.datemomo.model.request.PictureUpdateRequest
import com.example.datemomo.model.request.ProfileEditorRequest
import com.example.datemomo.model.response.PictureUpdateResponse
import com.example.datemomo.utility.Utility
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

class ProfileEditorActivity : AppCompatActivity() {
    private var photoFile: File? = null
    private val PICK_IMAGE_REQUEST = 200
    private var theBitmap: Bitmap? = null
    private val CAPTURE_IMAGE_REQUEST = 100
    private lateinit var requestProcess: String
    private lateinit var buttonClickEffect: AlphaAnimation
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivityProfileEditorBinding
    private lateinit var profileEditorRequest: ProfileEditorRequest
    private lateinit var sharedPreferencesEditor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hideSystemUI()

        buttonClickEffect = AlphaAnimation(1f, 0f)
        sharedPreferences =
            getSharedPreferences(getString(R.string.shared_preferences), Context.MODE_PRIVATE)
        sharedPreferencesEditor = sharedPreferences.edit()

        binding.profileEditorSubmitButton.blueButtonText.text = "Submit Update"

        binding.userGay.hollowButtonText.text = "Gay"
        binding.userToyBoy.hollowButtonText.text = "Toy Boy"
        binding.userLesbian.hollowButtonText.text = "Lesbian"
        binding.userToyGirl.hollowButtonText.text = "Toy Girl"
        binding.userBisexual.hollowButtonText.text = "Bisexual"
        binding.userStraight.hollowButtonText.text = "Straight"
        binding.userSugarDaddy.hollowButtonText.text = "Sugar Daddy"
        binding.userSugarMommy.hollowButtonText.text = "Sugar Mommy"

        binding.gayInterest.hollowButtonText.text = "Gay"
        binding.toyBoyInterest.hollowButtonText.text = "Toy Boy"
        binding.lesbianInterest.hollowButtonText.text = "Lesbian"
        binding.toyGirlInterest.hollowButtonText.text = "Toy Girl"
        binding.bisexualInterest.hollowButtonText.text = "Bisexual"
        binding.straightInterest.hollowButtonText.text = "Straight"
        binding.sugarDaddyInterest.hollowButtonText.text = "Sugar Daddy"
        binding.sugarMommyInterest.hollowButtonText.text = "Sugar Mommy"

        binding.sixtyNineExperience.hollowButtonText.text = "69"
        binding.analSexExperience.hollowButtonText.text = "Anal Sex"
        binding.orgySexExperience.hollowButtonText.text = "Orgy Sex"
        binding.poolSexExperience.hollowButtonText.text = "Pool Sex"
        binding.carSexExperience.hollowButtonText.text = "Sexed In Car"
        binding.threesomeExperience.hollowButtonText.text = "Threesome"
        binding.givenHeadExperience.hollowButtonText.text = "Given Head"
        binding.sexToyExperience.hollowButtonText.text = "Used Sex Toys"
        binding.videoSexExperience.hollowButtonText.text = "Video Sex Chat"
        binding.publicSexExperience.hollowButtonText.text = "Sexed In Public"
        binding.receivedHeadExperience.hollowButtonText.text = "Received Head"
        binding.cameraSexExperience.hollowButtonText.text = "Sexed With Camera"
        binding.oneNightStandExperience.hollowButtonText.text = "One-night Stand"

        profileEditorRequest = ProfileEditorRequest(
            memberId = sharedPreferences.getInt("memberId", 0),
            userStatus = "",
            currentLocation = "",
            bisexualCategory = 0,
            gayCategory = 0,
            lesbianCategory = 0,
            straightCategory = 0,
            sugarDaddyCategory = 0,
            sugarMommyCategory = 0,
            toyBoyCategory = 0,
            toyGirlCategory = 0,
            bisexualInterest = 0,
            gayInterest = 0,
            lesbianInterest = 0,
            straightInterest = 0,
            sugarDaddyInterest = 0,
            sugarMommyInterest = 0,
            toyBoyInterest = 0,
            toyGirlInterest = 0,
            sixtyNineExperience = 0,
            analSexExperience = 0,
            givenHeadExperience = 0,
            oneNightStandExperience = 0,
            orgySexExperience = 0,
            poolSexExperience = 0,
            receivedHeadExperience = 0,
            carSexExperience = 0,
            publicSexExperience = 0,
            cameraSexExperience = 0,
            threesomeExperience = 0,
            sexToyExperience = 0,
            videoSexExperience = 0
        )

        Glide.with(this)
            .load(getString(R.string.date_momo_api) + getString(R.string.api_image)
                    + sharedPreferences.getString(getString(R.string.profile_picture), ""))
            .transform(CircleCrop(), CenterCrop())
            .into(binding.accountProfilePicture)

        binding.currentUserStatus.text = sharedPreferences.getString(getString(R.string.status_default), "")
        binding.userStatusUpdater.setText(sharedPreferences.getString(getString(R.string.status_default), ""))

        binding.profilePictureChanger.setOnClickListener {
            pickImageFromGallery()
        }

        binding.userGay.hollowButtonLayout.setOnClickListener {
            binding.userGay.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.userGay.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.userGay.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.userGay.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                profileEditorRequest.gayCategory = 1
            } else {
                binding.userGay.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.userGay.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                profileEditorRequest.gayCategory = 0
            }
        }

        binding.userToyBoy.hollowButtonLayout.setOnClickListener {
            binding.userToyBoy.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.userToyBoy.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.userToyBoy.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.userToyBoy.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                profileEditorRequest.toyBoyCategory = 1
            } else {
                binding.userToyBoy.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.userToyBoy.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                profileEditorRequest.toyBoyCategory = 0
            }
        }

        binding.userBisexual.hollowButtonLayout.setOnClickListener {
            binding.userBisexual.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.userBisexual.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.userBisexual.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.userBisexual.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                profileEditorRequest.bisexualCategory = 1
            } else {
                binding.userBisexual.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.userBisexual.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                profileEditorRequest.bisexualCategory = 0
            }
        }

        binding.userStraight.hollowButtonLayout.setOnClickListener {
            binding.userStraight.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.userStraight.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.userStraight.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.userStraight.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                profileEditorRequest.straightCategory = 1
            } else {
                binding.userStraight.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.userStraight.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                profileEditorRequest.straightCategory = 0
            }
        }

        binding.userSugarDaddy.hollowButtonLayout.setOnClickListener {
            binding.userSugarDaddy.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.userSugarDaddy.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.userSugarDaddy.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.userSugarDaddy.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                profileEditorRequest.sugarDaddyCategory = 1
            } else {
                binding.userSugarDaddy.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.userSugarDaddy.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                profileEditorRequest.sugarDaddyCategory = 0
            }
        }

        binding.userLesbian.hollowButtonLayout.setOnClickListener {
            binding.userLesbian.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.userLesbian.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.userLesbian.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.userLesbian.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                profileEditorRequest.lesbianCategory = 1
            } else {
                binding.userLesbian.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.userLesbian.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                profileEditorRequest.lesbianCategory = 0
            }
        }

        binding.userToyGirl.hollowButtonLayout.setOnClickListener {
            binding.userToyGirl.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.userToyGirl.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.userToyGirl.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.userToyGirl.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                profileEditorRequest.toyGirlCategory = 1
            } else {
                binding.userToyGirl.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.userToyGirl.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                profileEditorRequest.toyGirlCategory = 0
            }
        }

        binding.userSugarMommy.hollowButtonLayout.setOnClickListener {
            binding.userSugarMommy.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.userSugarMommy.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.userSugarMommy.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.userSugarMommy.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                profileEditorRequest.sugarMommyCategory = 1
            } else {
                binding.userSugarMommy.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.userSugarMommy.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                profileEditorRequest.sugarMommyCategory = 0
            }
        }

        binding.gayInterest.hollowButtonLayout.setOnClickListener {
            binding.gayInterest.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.gayInterest.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.gayInterest.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.gayInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                profileEditorRequest.gayInterest = 1
            } else {
                binding.gayInterest.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.gayInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                profileEditorRequest.gayInterest = 0
            }
        }

        binding.toyBoyInterest.hollowButtonLayout.setOnClickListener {
            binding.toyBoyInterest.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.toyBoyInterest.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.toyBoyInterest.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.toyBoyInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                profileEditorRequest.toyBoyInterest = 1
            } else {
                binding.toyBoyInterest.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.toyBoyInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                profileEditorRequest.toyBoyInterest = 0
            }
        }

        binding.lesbianInterest.hollowButtonLayout.setOnClickListener {
            binding.lesbianInterest.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.lesbianInterest.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.lesbianInterest.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.lesbianInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                profileEditorRequest.lesbianInterest = 1
            } else {
                binding.lesbianInterest.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.lesbianInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                profileEditorRequest.lesbianInterest = 0
            }
        }

        binding.toyGirlInterest.hollowButtonLayout.setOnClickListener {
            binding.toyGirlInterest.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.toyGirlInterest.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.toyGirlInterest.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.toyGirlInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                profileEditorRequest.toyGirlInterest = 1
            } else {
                binding.toyGirlInterest.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.toyGirlInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                profileEditorRequest.toyGirlInterest = 0
            }
        }

        binding.bisexualInterest.hollowButtonLayout.setOnClickListener {
            binding.bisexualInterest.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.bisexualInterest.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.bisexualInterest.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.bisexualInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                profileEditorRequest.bisexualInterest = 1
            } else {
                binding.bisexualInterest.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.bisexualInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                profileEditorRequest.bisexualInterest = 0
            }
        }

        binding.straightInterest.hollowButtonLayout.setOnClickListener {
            binding.straightInterest.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.straightInterest.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.straightInterest.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.straightInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                profileEditorRequest.straightInterest = 1
            } else {
                binding.straightInterest.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.straightInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                profileEditorRequest.straightInterest = 0
            }
        }

        binding.sugarDaddyInterest.hollowButtonLayout.setOnClickListener {
            binding.sugarDaddyInterest.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.sugarDaddyInterest.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.sugarDaddyInterest.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.sugarDaddyInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                profileEditorRequest.sugarDaddyInterest = 1
            } else {
                binding.sugarDaddyInterest.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.sugarDaddyInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                profileEditorRequest.sugarDaddyInterest = 0
            }
        }

        binding.sugarMommyInterest.hollowButtonLayout.setOnClickListener {
            binding.sugarMommyInterest.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.sugarMommyInterest.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.sugarMommyInterest.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.sugarMommyInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                profileEditorRequest.sugarMommyInterest = 1
            } else {
                binding.sugarMommyInterest.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.sugarMommyInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                profileEditorRequest.sugarMommyInterest = 0
            }
        }

        binding.sixtyNineExperience.hollowButtonLayout.setOnClickListener {
            binding.sixtyNineExperience.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.sixtyNineExperience.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.sixtyNineExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.sixtyNineExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                profileEditorRequest.sixtyNineExperience = 1
            } else {
                binding.sixtyNineExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.sixtyNineExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                profileEditorRequest.sixtyNineExperience = 0
            }
        }

        binding.analSexExperience.hollowButtonLayout.setOnClickListener {
            binding.analSexExperience.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.analSexExperience.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.analSexExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.analSexExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                profileEditorRequest.analSexExperience = 1
            } else {
                binding.analSexExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.analSexExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                profileEditorRequest.analSexExperience = 0
            }
        }

        binding.orgySexExperience.hollowButtonLayout.setOnClickListener {
            binding.orgySexExperience.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.orgySexExperience.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.orgySexExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.orgySexExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                profileEditorRequest.orgySexExperience = 1
            } else {
                binding.orgySexExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.orgySexExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                profileEditorRequest.orgySexExperience = 0
            }
        }

        binding.poolSexExperience.hollowButtonLayout.setOnClickListener {
            binding.poolSexExperience.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.poolSexExperience.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.poolSexExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.poolSexExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                profileEditorRequest.poolSexExperience = 1
            } else {
                binding.poolSexExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.poolSexExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                profileEditorRequest.poolSexExperience = 0
            }
        }

        binding.carSexExperience.hollowButtonLayout.setOnClickListener {
            binding.carSexExperience.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.carSexExperience.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.carSexExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.carSexExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                profileEditorRequest.carSexExperience = 1
            } else {
                binding.carSexExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.carSexExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                profileEditorRequest.carSexExperience = 0
            }
        }

        binding.threesomeExperience.hollowButtonLayout.setOnClickListener {
            binding.threesomeExperience.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.threesomeExperience.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.threesomeExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.threesomeExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                profileEditorRequest.threesomeExperience = 1
            } else {
                binding.threesomeExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.threesomeExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                profileEditorRequest.threesomeExperience = 0
            }
        }

        binding.givenHeadExperience.hollowButtonLayout.setOnClickListener {
            binding.givenHeadExperience.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.givenHeadExperience.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.givenHeadExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.givenHeadExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                profileEditorRequest.givenHeadExperience = 1
            } else {
                binding.givenHeadExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.givenHeadExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                profileEditorRequest.givenHeadExperience = 0
            }
        }

        binding.sexToyExperience.hollowButtonLayout.setOnClickListener {
            binding.sexToyExperience.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.sexToyExperience.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.sexToyExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.sexToyExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                profileEditorRequest.sexToyExperience = 1
            } else {
                binding.sexToyExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.sexToyExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                profileEditorRequest.sexToyExperience = 0
            }
        }

        binding.videoSexExperience.hollowButtonLayout.setOnClickListener {
            binding.videoSexExperience.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.videoSexExperience.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.videoSexExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.videoSexExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                profileEditorRequest.videoSexExperience = 1
            } else {
                binding.videoSexExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.videoSexExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                profileEditorRequest.videoSexExperience = 0
            }
        }

        binding.publicSexExperience.hollowButtonLayout.setOnClickListener {
            binding.publicSexExperience.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.publicSexExperience.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.publicSexExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.publicSexExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                profileEditorRequest.publicSexExperience = 1
            } else {
                binding.publicSexExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.publicSexExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                profileEditorRequest.publicSexExperience = 0
            }
        }

        binding.receivedHeadExperience.hollowButtonLayout.setOnClickListener {
            binding.receivedHeadExperience.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.receivedHeadExperience.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.receivedHeadExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.receivedHeadExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                profileEditorRequest.receivedHeadExperience = 1
            } else {
                binding.receivedHeadExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.receivedHeadExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                profileEditorRequest.receivedHeadExperience = 0
            }
        }

        binding.cameraSexExperience.hollowButtonLayout.setOnClickListener {
            binding.cameraSexExperience.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.cameraSexExperience.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.cameraSexExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.cameraSexExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                profileEditorRequest.cameraSexExperience = 1
            } else {
                binding.cameraSexExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.cameraSexExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                profileEditorRequest.cameraSexExperience = 0
            }
        }

        binding.oneNightStandExperience.hollowButtonLayout.setOnClickListener {
            binding.oneNightStandExperience.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.oneNightStandExperience.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.oneNightStandExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.oneNightStandExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                profileEditorRequest.oneNightStandExperience = 1
            } else {
                binding.oneNightStandExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.oneNightStandExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                profileEditorRequest.oneNightStandExperience = 0
            }
        }

        if (sharedPreferences.getInt(getString(R.string.bisexual_category), 0) > 0) {
            binding.userBisexual.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
        }

        if (sharedPreferences.getInt(getString(R.string.gay_category), 0) > 0) {
            binding.userGay.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
        }

        if (sharedPreferences.getInt(getString(R.string.lesbian_category), 0) > 0) {
            binding.userLesbian.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
        }

        if (sharedPreferences.getInt(getString(R.string.straight_category), 0) > 0) {
            binding.userStraight.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
        }

        if (sharedPreferences.getInt(getString(R.string.sugar_daddy_category), 0) > 0) {
            binding.userSugarDaddy.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
        }

        if (sharedPreferences.getInt(getString(R.string.sugar_mommy_category), 0) > 0) {
            binding.userSugarMommy.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
        }

        if (sharedPreferences.getInt(getString(R.string.toy_boy_category), 0) > 0) {
            binding.userToyBoy.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
        }

        if (sharedPreferences.getInt(getString(R.string.toy_girl_category), 0) > 0) {
            binding.userToyGirl.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
        }

        if (sharedPreferences.getInt(getString(R.string.bisexual_interest), 0) > 0) {
            binding.bisexualInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
        }

        if (sharedPreferences.getInt(getString(R.string.gay_interest), 0) > 0) {
            binding.gayInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
        }

        if (sharedPreferences.getInt(getString(R.string.straight_interest), 0) > 0) {
            binding.straightInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
        }

        if (sharedPreferences.getInt(getString(R.string.lesbian_interest), 0) > 0) {
            binding.lesbianInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
        }

        if (sharedPreferences.getInt(getString(R.string.sugar_daddy_interest), 0) > 0) {
            binding.sugarDaddyInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
        }

        if (sharedPreferences.getInt(getString(R.string.sugar_mommy_interest), 0) > 0) {
            binding.sugarMommyInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
        }

        if (sharedPreferences.getInt(getString(R.string.toy_boy_interest), 0) > 0) {
            binding.toyBoyInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
        }

        if (sharedPreferences.getInt(getString(R.string.toy_girl_interest), 0) > 0) {
            binding.toyGirlInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
        }

        if (sharedPreferences.getInt(getString(R.string.anal_sex_experience), 0) > 0) {
            binding.analSexExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
        }

        if (sharedPreferences.getInt(getString(R.string.sixty_nine_experience), 0) > 0) {
            binding.sixtyNineExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
        }

        if (sharedPreferences.getInt(getString(R.string.camera_sex_experience), 0) > 0) {
            binding.cameraSexExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
        }

        if (sharedPreferences.getInt(getString(R.string.car_sex_experience), 0) > 0) {
            binding.carSexExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
        }

        if (sharedPreferences.getInt(getString(R.string.threesome_experience), 0) > 0) {
            binding.threesomeExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
        }

        if (sharedPreferences.getInt(getString(R.string.given_head_experience), 0) > 0) {
            binding.givenHeadExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
        }

        if (sharedPreferences.getInt(getString(R.string.received_head_experience), 0) > 0) {
            binding.receivedHeadExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
        }

        if (sharedPreferences.getInt(getString(R.string.one_night_stand_experience), 0) > 0) {
            binding.oneNightStandExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
        }

        if (sharedPreferences.getInt(getString(R.string.orgy_experience), 0) > 0) {
            binding.orgySexExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
        }

        if (sharedPreferences.getInt(getString(R.string.pool_sex_experience), 0) > 0) {
            binding.poolSexExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
        }

        if (sharedPreferences.getInt(getString(R.string.sex_toy_experience), 0) > 0) {
            binding.sexToyExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
        }

        if (sharedPreferences.getInt(getString(R.string.video_sex_experience), 0) > 0) {
            binding.videoSexExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
        }

        if (sharedPreferences.getInt(getString(R.string.public_sex_experience), 0) > 0) {
            binding.publicSexExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAPTURE_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            theBitmap = BitmapFactory.decodeFile(photoFile!!.absolutePath)

            Glide.with(this)
                .load(theBitmap)
                .transform(CircleCrop(), CenterCrop())
                .into(binding.accountProfilePicture)
        } else if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            Glide.with(this)
                .load(data?.data)
                .transform(CircleCrop(), CenterCrop())
                .into(binding.accountProfilePicture)

            theBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(contentResolver, data?.data!!)
                ImageDecoder.decodeBitmap(source)
            } else{
                MediaStore.Images.Media.getBitmap(contentResolver, data?.data)
            }
        }

        if (theBitmap != null) {
            updateProfilePicture()
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    @Throws(IOException::class)
    fun updateProfilePicture() {
        val imageWidth = theBitmap!!.width
        val imageHeight = theBitmap!!.height

        val byteArrayOutputStream = ByteArrayOutputStream()
        theBitmap!!.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        val base64Picture =
            android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)

        val mapper = jacksonObjectMapper()
        val pictureUpdateRequest = PictureUpdateRequest(
            sharedPreferences.getInt(getString(R.string.member_id), 0),
            imageWidth,
            imageHeight,
            base64Picture
        )

        val jsonObjectString = mapper.writeValueAsString(pictureUpdateRequest)
        val requestBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            jsonObjectString
        )

        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(getString(R.string.date_momo_api) + getString(R.string.api_update_picture))
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()

                if (!Utility.isConnected(baseContext)) {
                    displayDoubleButtonDialog()
                } else if (e.message!!.contains("after")) {
                    displaySingleButtonDialog(
                        getString(R.string.poor_internet_title),
                        getString(R.string.poor_internet_message)
                    )
                } else {
                    displaySingleButtonDialog(
                        getString(R.string.server_error_title),
                        getString(R.string.server_error_message)
                    )
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val myResponse: String = response.body()!!.string()
                val pictureUpdateResponse: PictureUpdateResponse

                try {
                    pictureUpdateResponse = mapper.readValue(myResponse)
                    sharedPreferencesEditor.putString(getString(R.string.profile_picture),
                        pictureUpdateResponse.profilePicture)
                    sharedPreferencesEditor.apply()
                } catch (exception: IOException) {
                    displaySingleButtonDialog(
                        getString(R.string.server_error_title),
                        getString(R.string.server_error_message)
                    )
                }
            }
        })
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
        const val TAG = "ProfileEditorActivity"
    }
}


