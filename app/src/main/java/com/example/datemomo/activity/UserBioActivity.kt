package com.example.datemomo.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.datemomo.MainApplication.Companion.setNavigationBarDarkIcons
import com.example.datemomo.MainApplication.Companion.setStatusBarDarkIcons
import com.example.datemomo.R
import com.example.datemomo.databinding.ActivityUserBioBinding
import com.example.datemomo.model.request.PictureUploadRequest
import com.example.datemomo.model.request.UserBioRequest
import com.example.datemomo.model.response.CommittedResponse
import com.example.datemomo.model.response.PictureUploadResponse
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.*
import java.io.ByteArrayOutputStream
import java.io.IOException

class UserBioActivity : AppCompatActivity() {
    private lateinit var userBioRequest: UserBioRequest
    private lateinit var binding: ActivityUserBioBinding
    private lateinit var buttonClickEffect: AlphaAnimation
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferencesEditor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserBioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.setStatusBarDarkIcons(true)
        window.setNavigationBarDarkIcons(true)

        buttonClickEffect = AlphaAnimation(1f, 0f)
        sharedPreferences =
            getSharedPreferences(getString(R.string.shared_preferences), Context.MODE_PRIVATE)
        sharedPreferencesEditor = sharedPreferences.edit()

        userBioRequest = UserBioRequest(
            sharedPreferences.getInt("memberId", 0),
            bisexualCategory = false,
            gayCategory = false,
            lesbianCategory = false,
            straightCategory = false,
            sugarDaddyCategory = false,
            sugarMommyCategory = false,
            toyBoyCategory = false,
            toyGirlCategory = false,
            bisexualInterest = false,
            gayInterest = false,
            lesbianInterest = false,
            straightInterest = false,
            sugarDaddyInterest = false,
            sugarMommyInterest = false,
            toyBoyInterest = false,
            toyGirlInterest = false,
            sixtyNineExperience = false,
            analSexExperience = false,
            givenHeadExperience = false,
            oneNightStandExperience = false,
            orgySexExperience = false,
            poolSexExperience = false,
            receivedHeadExperience = false,
            carSexExperience = false,
            publicSexExperience = false,
            cameraSexExperience = false,
            threesomeExperience = false,
            sexToyExperience = false,
            videoSexExperience = false
        )

        binding.userKYCSubmitButton.blueButtonLayout.setOnClickListener {
            binding.userKYCSubmitButton.blueButtonLayout.startAnimation(buttonClickEffect)

            var isCategoryFilled = false
            var isInterestFilled = false

            if (!userBioRequest.bisexualCategory &&
                !userBioRequest.gayCategory &&
                !userBioRequest.lesbianCategory &&
                !userBioRequest.straightCategory &&
                !userBioRequest.sugarDaddyCategory &&
                !userBioRequest.sugarMommyCategory &&
                !userBioRequest.toyBoyCategory &&
                !userBioRequest.toyGirlCategory) {
                binding.userCategoryError.visibility = View.VISIBLE
            } else {
                binding.userCategoryError.visibility = View.GONE
                isCategoryFilled = true
            }

            if (!userBioRequest.bisexualInterest &&
                !userBioRequest.gayInterest &&
                !userBioRequest.lesbianInterest &&
                !userBioRequest.straightInterest &&
                !userBioRequest.sugarDaddyInterest &&
                !userBioRequest.sugarMommyInterest &&
                !userBioRequest.toyBoyInterest &&
                !userBioRequest.toyGirlInterest) {
                binding.userInterestError.visibility = View.VISIBLE
            } else {
                binding.userInterestError.visibility = View.GONE
                isInterestFilled = true
            }

            if (isCategoryFilled && isInterestFilled) {
                commitUserBiometrics()
            }
        }

        binding.userKYCSkipButton.greyButtonLayout.setOnClickListener {
            binding.userKYCSkipButton.greyButtonLayout.startAnimation(buttonClickEffect)

            userBioRequest.straightCategory = true
            userBioRequest.straightInterest = true

            commitUserBiometrics()
        }

        binding.maleGay.hollowButtonText.text = "Gay"
        binding.maleToyBoy.hollowButtonText.text = "Toy Boy"
        binding.maleBisexual.hollowButtonText.text = "Bisexual"
        binding.maleStraight.hollowButtonText.text = "Straight"
        binding.maleSugarDaddy.hollowButtonText.text = "Sugar Daddy"

        binding.femaleLesbian.hollowButtonText.text = "Lesbian"
        binding.femaleToyGirl.hollowButtonText.text = "Toy Girl"
        binding.femaleBisexual.hollowButtonText.text = "Bisexual"
        binding.femaleStraight.hollowButtonText.text = "Straight"
        binding.femaleSugarMommy.hollowButtonText.text = "Sugar Mommy"

        binding.gay.hollowButtonText.text = "Gay"
        binding.toyBoy.hollowButtonText.text = "Toy Boy"
        binding.lesbian.hollowButtonText.text = "Lesbian"
        binding.toyGirl.hollowButtonText.text = "Toy Girl"
        binding.bisexual.hollowButtonText.text = "Bisexual"
        binding.straight.hollowButtonText.text = "Straight"
        binding.sugarDaddy.hollowButtonText.text = "Sugar Daddy"
        binding.sugarMommy.hollowButtonText.text = "Sugar Mommy"

        binding.sixtyNine.hollowButtonText.text = "69"
        binding.analSex.hollowButtonText.text = "Anal Sex"
        binding.orgySex.hollowButtonText.text = "Orgy Sex"
        binding.poolSex.hollowButtonText.text = "Pool Sex"
        binding.carSex.hollowButtonText.text = "Sexed In Car"
        binding.threesome.hollowButtonText.text = "Threesome"
        binding.givenHead.hollowButtonText.text = "Given Head"
        binding.sexToys.hollowButtonText.text = "Used Sex Toys"
        binding.videoSex.hollowButtonText.text = "Video Sex Chat"
        binding.publicSex.hollowButtonText.text = "Sexed In Public"
        binding.receivedHead.hollowButtonText.text = "Received Head"
        binding.cameraSex.hollowButtonText.text = "Sexed With Camera"
        binding.oneNightStand.hollowButtonText.text = "One-night Stand"

        binding.userKYCSkipButton.greyButtonText.text = "Skip"

        if (sharedPreferences.getString("sex", "") == "Male") {
            binding.maleSexualityOptions.visibility = View.VISIBLE
            binding.femaleSexualityOptions.visibility = View.GONE
        } else {
            binding.femaleSexualityOptions.visibility = View.VISIBLE
            binding.maleSexualityOptions.visibility = View.GONE
        }

        binding.maleGay.hollowButtonLayout.setOnClickListener {
            binding.maleGay.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.maleGay.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.maleGay.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.maleGay.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.gayCategory = true
            } else {
                binding.maleGay.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.maleGay.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.gayCategory = false
            }
        }

        binding.maleToyBoy.hollowButtonLayout.setOnClickListener {
            binding.maleToyBoy.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.maleToyBoy.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.maleToyBoy.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.maleToyBoy.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.toyBoyCategory = true
            } else {
                binding.maleToyBoy.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.maleToyBoy.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.gayCategory = false
            }
        }

        binding.maleBisexual.hollowButtonLayout.setOnClickListener {
            binding.maleBisexual.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.maleBisexual.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.maleBisexual.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.maleBisexual.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.bisexualCategory = true
            } else {
                binding.maleBisexual.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.maleBisexual.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.bisexualCategory = false
            }
        }

        binding.maleStraight.hollowButtonLayout.setOnClickListener {
            binding.maleStraight.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.maleStraight.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.maleStraight.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.maleStraight.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.straightCategory = true
            } else {
                binding.maleStraight.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.maleStraight.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.straightCategory = false
            }
        }

        binding.maleSugarDaddy.hollowButtonLayout.setOnClickListener {
            binding.maleSugarDaddy.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.maleSugarDaddy.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.maleSugarDaddy.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.maleSugarDaddy.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.sugarDaddyCategory = true
            } else {
                binding.maleSugarDaddy.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.maleSugarDaddy.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.sugarDaddyCategory = false
            }
        }

        binding.femaleLesbian.hollowButtonLayout.setOnClickListener {
            binding.femaleLesbian.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.femaleLesbian.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.femaleLesbian.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.femaleLesbian.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.lesbianCategory = true
            } else {
                binding.femaleLesbian.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.femaleLesbian.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.lesbianCategory = false
            }
        }

        binding.femaleToyGirl.hollowButtonLayout.setOnClickListener {
            binding.femaleToyGirl.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.femaleToyGirl.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.femaleToyGirl.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.femaleToyGirl.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.toyGirlCategory = true
            } else {
                binding.femaleToyGirl.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.femaleToyGirl.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.toyGirlCategory = false
            }
        }

        binding.femaleBisexual.hollowButtonLayout.setOnClickListener {
            binding.femaleBisexual.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.femaleBisexual.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.femaleBisexual.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.femaleBisexual.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.bisexualCategory = true
            } else {
                binding.femaleBisexual.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.femaleBisexual.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.bisexualCategory = false
            }
        }

        binding.femaleStraight.hollowButtonLayout.setOnClickListener {
            binding.femaleStraight.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.femaleStraight.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.femaleStraight.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.femaleStraight.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.straightCategory = true
            } else {
                binding.femaleStraight.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.femaleStraight.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.straightCategory = false
            }
        }

        binding.femaleSugarMommy.hollowButtonLayout.setOnClickListener {
            binding.femaleSugarMommy.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.femaleSugarMommy.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.femaleSugarMommy.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.femaleSugarMommy.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.sugarMommyCategory = true
            } else {
                binding.femaleSugarMommy.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.femaleSugarMommy.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.sugarMommyCategory = false
            }
        }

        binding.gay.hollowButtonLayout.setOnClickListener {
            binding.gay.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.gay.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.gay.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.gay.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.gayInterest = true
            } else {
                binding.gay.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.gay.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.gayInterest = false
            }
        }

        binding.toyBoy.hollowButtonLayout.setOnClickListener {
            binding.toyBoy.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.toyBoy.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.toyBoy.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.toyBoy.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.toyBoyInterest = true
            } else {
                binding.toyBoy.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.toyBoy.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.toyBoyInterest = false
            }
        }

        binding.lesbian.hollowButtonLayout.setOnClickListener {
            binding.lesbian.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.lesbian.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.lesbian.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.lesbian.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.lesbianInterest = true
            } else {
                binding.lesbian.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.lesbian.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.lesbianInterest = false
            }
        }

        binding.toyGirl.hollowButtonLayout.setOnClickListener {
            binding.toyGirl.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.toyGirl.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.toyGirl.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.toyGirl.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.toyGirlInterest = true
            } else {
                binding.toyGirl.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.toyGirl.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.toyGirlInterest = false
            }
        }

        binding.bisexual.hollowButtonLayout.setOnClickListener {
            binding.bisexual.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.bisexual.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.bisexual.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.bisexual.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.bisexualInterest = true
            } else {
                binding.bisexual.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.bisexual.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.bisexualInterest = false
            }
        }

        binding.straight.hollowButtonLayout.setOnClickListener {
            binding.straight.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.straight.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.straight.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.straight.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.straightInterest = true
            } else {
                binding.straight.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.straight.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.straightInterest = false
            }
        }

        binding.sugarDaddy.hollowButtonLayout.setOnClickListener {
            binding.sugarDaddy.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.sugarDaddy.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.sugarDaddy.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.sugarDaddy.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.sugarDaddyInterest = true
            } else {
                binding.sugarDaddy.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.sugarDaddy.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.sugarDaddyInterest = false
            }
        }

        binding.sugarMommy.hollowButtonLayout.setOnClickListener {
            binding.sugarMommy.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.sugarMommy.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.sugarMommy.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.sugarMommy.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.sugarMommyInterest = true
            } else {
                binding.sugarMommy.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.sugarMommy.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.sugarMommyInterest = false
            }
        }

        binding.sixtyNine.hollowButtonLayout.setOnClickListener {
            binding.sixtyNine.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.sixtyNine.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.sixtyNine.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.sixtyNine.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.sixtyNineExperience = true
            } else {
                binding.sixtyNine.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.sixtyNine.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.sixtyNineExperience = false
            }
        }

        binding.analSex.hollowButtonLayout.setOnClickListener {
            binding.analSex.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.analSex.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.analSex.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.analSex.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.analSexExperience = true
            } else {
                binding.analSex.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.analSex.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.analSexExperience = false
            }
        }

        binding.orgySex.hollowButtonLayout.setOnClickListener {
            binding.orgySex.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.orgySex.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.orgySex.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.orgySex.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.orgySexExperience = true
            } else {
                binding.orgySex.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.orgySex.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.orgySexExperience = false
            }
        }

        binding.poolSex.hollowButtonLayout.setOnClickListener {
            binding.poolSex.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.poolSex.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.poolSex.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.poolSex.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.poolSexExperience = true
            } else {
                binding.poolSex.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.poolSex.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.poolSexExperience = false
            }
        }

        binding.carSex.hollowButtonLayout.setOnClickListener {
            binding.carSex.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.carSex.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.carSex.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.carSex.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.carSexExperience = true
            } else {
                binding.carSex.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.carSex.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.carSexExperience = false
            }
        }

        binding.threesome.hollowButtonLayout.setOnClickListener {
            binding.threesome.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.threesome.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.threesome.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.threesome.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.threesomeExperience = true
            } else {
                binding.threesome.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.threesome.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.threesomeExperience = false
            }
        }

        binding.givenHead.hollowButtonLayout.setOnClickListener {
            binding.givenHead.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.givenHead.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.givenHead.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.givenHead.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.givenHeadExperience = true
            } else {
                binding.givenHead.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.givenHead.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.givenHeadExperience = false
            }
        }

        binding.sexToys.hollowButtonLayout.setOnClickListener {
            binding.sexToys.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.sexToys.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.sexToys.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.sexToys.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.sexToyExperience = true
            } else {
                binding.sexToys.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.sexToys.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.sexToyExperience = false
            }
        }

        binding.videoSex.hollowButtonLayout.setOnClickListener {
            binding.videoSex.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.videoSex.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.videoSex.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.videoSex.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.videoSexExperience = true
            } else {
                binding.videoSex.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.videoSex.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.videoSexExperience = false
            }
        }

        binding.publicSex.hollowButtonLayout.setOnClickListener {
            binding.publicSex.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.publicSex.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.publicSex.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.publicSex.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.publicSexExperience = true
            } else {
                binding.publicSex.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.publicSex.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.publicSexExperience = false
            }
        }

        binding.receivedHead.hollowButtonLayout.setOnClickListener {
            binding.receivedHead.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.receivedHead.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.receivedHead.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.receivedHead.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.receivedHeadExperience = true
            } else {
                binding.receivedHead.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.receivedHead.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.receivedHeadExperience = false
            }
        }

        binding.cameraSex.hollowButtonLayout.setOnClickListener {
            binding.cameraSex.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.cameraSex.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.cameraSex.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.cameraSex.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.cameraSexExperience = true
            } else {
                binding.cameraSex.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.cameraSex.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.cameraSexExperience = false
            }
        }

        binding.oneNightStand.hollowButtonLayout.setOnClickListener {
            binding.oneNightStand.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.oneNightStand.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.oneNightStand.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.oneNightStand.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.oneNightStandExperience = true
            } else {
                binding.oneNightStand.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.oneNightStand.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.oneNightStandExperience = false
            }
        }
    }

    @Throws(IOException::class)
    fun commitUserBiometrics() {
        val mapper = jacksonObjectMapper()
        val jsonObjectString = mapper.writeValueAsString(userBioRequest)
        val requestBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            jsonObjectString
        )

        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(getString(R.string.date_momo_api) + "service/userbiometrics.php")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val myResponse: String = response.body()!!.string()
                val committedResponse = mapper.readValue<CommittedResponse>(myResponse)

                if (committedResponse.committed) {
                    val intent = Intent(baseContext, HomeDisplayActivity::class.java)
                    startActivity(intent)
                }
            }
        })
    }

    companion object {
        const val TAG = "UserBioActivity"
    }
}


