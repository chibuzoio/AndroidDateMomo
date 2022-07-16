package com.example.datemomo.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bumptech.glide.Glide
import com.example.datemomo.R
import com.example.datemomo.databinding.ActivityUserBioBinding
import com.example.datemomo.model.request.HomeDisplayRequest
import com.example.datemomo.model.request.UserBioRequest
import com.example.datemomo.model.response.UserBioResponse
import com.example.datemomo.utility.Utility
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.*
import java.io.IOException

class UserBioActivity : AppCompatActivity() {
    private lateinit var requestProcess: String
    private lateinit var originalRequestProcess: String
    private lateinit var userBioRequest: UserBioRequest
    private lateinit var binding: ActivityUserBioBinding
    private lateinit var buttonClickEffect: AlphaAnimation
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferencesEditor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserBioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hideSystemUI()

        buttonClickEffect = AlphaAnimation(1f, 0f)
        sharedPreferences =
            getSharedPreferences(getString(R.string.shared_preferences), Context.MODE_PRIVATE)
        sharedPreferencesEditor = sharedPreferences.edit()

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

        Glide.with(this)
            .asGif()
            .load(R.drawable.loading_puzzle)
            .into(binding.kycSubmitProgressIcon)

        Glide.with(this)
            .asGif()
            .load(R.drawable.loading_puzzle)
            .into(binding.kycSkipProgressIcon)

        userBioRequest = UserBioRequest(
            sharedPreferences.getInt("memberId", 0),
            userLevel = getString(R.string.level_display_matched_users),
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

        binding.userKYCSubmitButton.blueButtonLayout.setOnClickListener {
            binding.userKYCSubmitButton.blueButtonLayout.startAnimation(buttonClickEffect)

            binding.userKYCSkipButton.greyButtonLayout.visibility = View.VISIBLE
            binding.userKYCSubmitButton.blueButtonLayout.visibility = View.GONE
            binding.kycSubmitProgressIcon.visibility = View.VISIBLE
            binding.kycSkipProgressIcon.visibility = View.GONE

            var isCategoryFilled = false
            var isInterestFilled = false

            if (userBioRequest.bisexualCategory == 0 &&
                userBioRequest.gayCategory == 0 &&
                userBioRequest.lesbianCategory == 0 &&
                userBioRequest.straightCategory == 0 &&
                userBioRequest.sugarDaddyCategory == 0 &&
                userBioRequest.sugarMommyCategory == 0 &&
                userBioRequest.toyBoyCategory == 0 &&
                userBioRequest.toyGirlCategory == 0) {
                binding.userKYCSkipButton.greyButtonLayout.visibility = View.VISIBLE
                binding.userKYCSubmitButton.blueButtonLayout.visibility = View.VISIBLE
                binding.kycSubmitProgressIcon.visibility = View.GONE
                binding.userCategoryError.visibility = View.VISIBLE
                binding.kycSkipProgressIcon.visibility = View.GONE
            } else {
                binding.userCategoryError.visibility = View.GONE
                isCategoryFilled = true
            }

            if (userBioRequest.bisexualInterest == 0 &&
                userBioRequest.gayInterest == 0 &&
                userBioRequest.lesbianInterest == 0 &&
                userBioRequest.straightInterest == 0 &&
                userBioRequest.sugarDaddyInterest == 0 &&
                userBioRequest.sugarMommyInterest == 0 &&
                userBioRequest.toyBoyInterest == 0 &&
                userBioRequest.toyGirlInterest == 0) {
                binding.userKYCSkipButton.greyButtonLayout.visibility = View.VISIBLE
                binding.userKYCSubmitButton.blueButtonLayout.visibility = View.VISIBLE
                binding.kycSubmitProgressIcon.visibility = View.GONE
                binding.userInterestError.visibility = View.VISIBLE
                binding.kycSkipProgressIcon.visibility = View.GONE
            } else {
                binding.userInterestError.visibility = View.GONE
                isInterestFilled = true
            }

            if (isCategoryFilled && isInterestFilled) {
                originalRequestProcess = getString(R.string.request_submit_sexuality_interest)
                requestProcess = getString(R.string.request_submit_sexuality_interest)

                commitUserBiometrics()
            }
        }

        binding.userKYCSkipButton.greyButtonLayout.setOnClickListener {
            binding.userKYCSkipButton.greyButtonLayout.startAnimation(buttonClickEffect)

            binding.userKYCSubmitButton.blueButtonLayout.visibility = View.VISIBLE
            binding.userKYCSkipButton.greyButtonLayout.visibility = View.GONE
            binding.kycSkipProgressIcon.visibility = View.VISIBLE
            binding.kycSubmitProgressIcon.visibility = View.GONE

            originalRequestProcess = getString(R.string.request_skip_sexuality_interest)
            requestProcess = getString(R.string.request_skip_sexuality_interest)

            userBioRequest.straightCategory = 1
            userBioRequest.straightInterest = 1

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
                userBioRequest.gayCategory = 1
            } else {
                binding.maleGay.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.maleGay.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.gayCategory = 0
            }
        }

        binding.maleToyBoy.hollowButtonLayout.setOnClickListener {
            binding.maleToyBoy.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.maleToyBoy.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.maleToyBoy.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.maleToyBoy.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.toyBoyCategory = 1
            } else {
                binding.maleToyBoy.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.maleToyBoy.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.toyBoyCategory = 0
            }
        }

        binding.maleBisexual.hollowButtonLayout.setOnClickListener {
            binding.maleBisexual.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.maleBisexual.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.maleBisexual.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.maleBisexual.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.bisexualCategory = 1
            } else {
                binding.maleBisexual.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.maleBisexual.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.bisexualCategory = 0
            }
        }

        binding.maleStraight.hollowButtonLayout.setOnClickListener {
            binding.maleStraight.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.maleStraight.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.maleStraight.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.maleStraight.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.straightCategory = 1
            } else {
                binding.maleStraight.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.maleStraight.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.straightCategory = 0
            }
        }

        binding.maleSugarDaddy.hollowButtonLayout.setOnClickListener {
            binding.maleSugarDaddy.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.maleSugarDaddy.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.maleSugarDaddy.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.maleSugarDaddy.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.sugarDaddyCategory = 1
            } else {
                binding.maleSugarDaddy.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.maleSugarDaddy.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.sugarDaddyCategory = 0
            }
        }

        binding.femaleLesbian.hollowButtonLayout.setOnClickListener {
            binding.femaleLesbian.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.femaleLesbian.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.femaleLesbian.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.femaleLesbian.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.lesbianCategory = 1
            } else {
                binding.femaleLesbian.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.femaleLesbian.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.lesbianCategory = 0
            }
        }

        binding.femaleToyGirl.hollowButtonLayout.setOnClickListener {
            binding.femaleToyGirl.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.femaleToyGirl.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.femaleToyGirl.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.femaleToyGirl.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.toyGirlCategory = 1
            } else {
                binding.femaleToyGirl.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.femaleToyGirl.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.toyGirlCategory = 0
            }
        }

        binding.femaleBisexual.hollowButtonLayout.setOnClickListener {
            binding.femaleBisexual.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.femaleBisexual.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.femaleBisexual.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.femaleBisexual.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.bisexualCategory = 1
            } else {
                binding.femaleBisexual.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.femaleBisexual.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.bisexualCategory = 0
            }
        }

        binding.femaleStraight.hollowButtonLayout.setOnClickListener {
            binding.femaleStraight.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.femaleStraight.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.femaleStraight.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.femaleStraight.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.straightCategory = 1
            } else {
                binding.femaleStraight.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.femaleStraight.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.straightCategory = 0
            }
        }

        binding.femaleSugarMommy.hollowButtonLayout.setOnClickListener {
            binding.femaleSugarMommy.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.femaleSugarMommy.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.femaleSugarMommy.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.femaleSugarMommy.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.sugarMommyCategory = 1
            } else {
                binding.femaleSugarMommy.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.femaleSugarMommy.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.sugarMommyCategory = 0
            }
        }

        binding.gay.hollowButtonLayout.setOnClickListener {
            binding.gay.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.gay.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.gay.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.gay.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.gayInterest = 1
            } else {
                binding.gay.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.gay.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.gayInterest = 0
            }
        }

        binding.toyBoy.hollowButtonLayout.setOnClickListener {
            binding.toyBoy.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.toyBoy.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.toyBoy.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.toyBoy.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.toyBoyInterest = 1
            } else {
                binding.toyBoy.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.toyBoy.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.toyBoyInterest = 0
            }
        }

        binding.lesbian.hollowButtonLayout.setOnClickListener {
            binding.lesbian.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.lesbian.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.lesbian.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.lesbian.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.lesbianInterest = 1
            } else {
                binding.lesbian.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.lesbian.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.lesbianInterest = 0
            }
        }

        binding.toyGirl.hollowButtonLayout.setOnClickListener {
            binding.toyGirl.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.toyGirl.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.toyGirl.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.toyGirl.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.toyGirlInterest = 1
            } else {
                binding.toyGirl.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.toyGirl.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.toyGirlInterest = 0
            }
        }

        binding.bisexual.hollowButtonLayout.setOnClickListener {
            binding.bisexual.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.bisexual.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.bisexual.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.bisexual.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.bisexualInterest = 1
            } else {
                binding.bisexual.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.bisexual.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.bisexualInterest = 0
            }
        }

        binding.straight.hollowButtonLayout.setOnClickListener {
            binding.straight.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.straight.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.straight.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.straight.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.straightInterest = 1
            } else {
                binding.straight.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.straight.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.straightInterest = 0
            }
        }

        binding.sugarDaddy.hollowButtonLayout.setOnClickListener {
            binding.sugarDaddy.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.sugarDaddy.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.sugarDaddy.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.sugarDaddy.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.sugarDaddyInterest = 1
            } else {
                binding.sugarDaddy.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.sugarDaddy.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.sugarDaddyInterest = 0
            }
        }

        binding.sugarMommy.hollowButtonLayout.setOnClickListener {
            binding.sugarMommy.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.sugarMommy.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.sugarMommy.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.sugarMommy.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.sugarMommyInterest = 1
            } else {
                binding.sugarMommy.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.sugarMommy.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.sugarMommyInterest = 0
            }
        }

        binding.sixtyNine.hollowButtonLayout.setOnClickListener {
            binding.sixtyNine.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.sixtyNine.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.sixtyNine.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.sixtyNine.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.sixtyNineExperience = 1
            } else {
                binding.sixtyNine.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.sixtyNine.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.sixtyNineExperience = 0
            }
        }

        binding.analSex.hollowButtonLayout.setOnClickListener {
            binding.analSex.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.analSex.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.analSex.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.analSex.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.analSexExperience = 1
            } else {
                binding.analSex.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.analSex.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.analSexExperience = 0
            }
        }

        binding.orgySex.hollowButtonLayout.setOnClickListener {
            binding.orgySex.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.orgySex.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.orgySex.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.orgySex.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.orgySexExperience = 1
            } else {
                binding.orgySex.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.orgySex.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.orgySexExperience = 0
            }
        }

        binding.poolSex.hollowButtonLayout.setOnClickListener {
            binding.poolSex.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.poolSex.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.poolSex.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.poolSex.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.poolSexExperience = 1
            } else {
                binding.poolSex.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.poolSex.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.poolSexExperience = 0
            }
        }

        binding.carSex.hollowButtonLayout.setOnClickListener {
            binding.carSex.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.carSex.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.carSex.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.carSex.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.carSexExperience = 1
            } else {
                binding.carSex.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.carSex.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.carSexExperience = 0
            }
        }

        binding.threesome.hollowButtonLayout.setOnClickListener {
            binding.threesome.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.threesome.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.threesome.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.threesome.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.threesomeExperience = 1
            } else {
                binding.threesome.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.threesome.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.threesomeExperience = 0
            }
        }

        binding.givenHead.hollowButtonLayout.setOnClickListener {
            binding.givenHead.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.givenHead.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.givenHead.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.givenHead.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.givenHeadExperience = 1
            } else {
                binding.givenHead.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.givenHead.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.givenHeadExperience = 0
            }
        }

        binding.sexToys.hollowButtonLayout.setOnClickListener {
            binding.sexToys.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.sexToys.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.sexToys.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.sexToys.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.sexToyExperience = 1
            } else {
                binding.sexToys.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.sexToys.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.sexToyExperience = 0
            }
        }

        binding.videoSex.hollowButtonLayout.setOnClickListener {
            binding.videoSex.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.videoSex.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.videoSex.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.videoSex.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.videoSexExperience = 1
            } else {
                binding.videoSex.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.videoSex.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.videoSexExperience = 0
            }
        }

        binding.publicSex.hollowButtonLayout.setOnClickListener {
            binding.publicSex.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.publicSex.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.publicSex.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.publicSex.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.publicSexExperience = 1
            } else {
                binding.publicSex.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.publicSex.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.publicSexExperience = 0
            }
        }

        binding.receivedHead.hollowButtonLayout.setOnClickListener {
            binding.receivedHead.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.receivedHead.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.receivedHead.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.receivedHead.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.receivedHeadExperience = 1
            } else {
                binding.receivedHead.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.receivedHead.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.receivedHeadExperience = 0
            }
        }

        binding.cameraSex.hollowButtonLayout.setOnClickListener {
            binding.cameraSex.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.cameraSex.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.cameraSex.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.cameraSex.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.cameraSexExperience = 1
            } else {
                binding.cameraSex.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.cameraSex.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.cameraSexExperience = 0
            }
        }

        binding.oneNightStand.hollowButtonLayout.setOnClickListener {
            binding.oneNightStand.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.oneNightStand.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.oneNightStand.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.oneNightStand.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                userBioRequest.oneNightStandExperience = 1
            } else {
                binding.oneNightStand.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.oneNightStand.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                userBioRequest.oneNightStandExperience = 0
            }
        }
    }

    private fun triggerRequestProcess() {
        when (requestProcess) {
            getString(R.string.request_submit_sexuality_interest) -> binding.userKYCSubmitButton.blueButtonLayout.performClick()
            getString(R.string.request_skip_sexuality_interest) -> binding.userKYCSkipButton.greyButtonLayout.performClick()
            getString(R.string.request_fetch_matched_users) -> {
                when (originalRequestProcess) {
                    getString(R.string.request_submit_sexuality_interest) -> {
                        binding.userKYCSkipButton.greyButtonLayout.visibility = View.VISIBLE
                        binding.userKYCSubmitButton.blueButtonLayout.visibility = View.GONE
                        binding.kycSubmitProgressIcon.visibility = View.VISIBLE
                        binding.kycSkipProgressIcon.visibility = View.GONE
                    }
                    getString(R.string.request_skip_sexuality_interest) -> {
                        binding.userKYCSubmitButton.blueButtonLayout.visibility = View.VISIBLE
                        binding.userKYCSkipButton.greyButtonLayout.visibility = View.GONE
                        binding.kycSkipProgressIcon.visibility = View.VISIBLE
                        binding.kycSubmitProgressIcon.visibility = View.GONE
                    }
                }

                fetchMatchedUsers()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        hideSystemUI()
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
                    binding.userKYCSubmitButton.blueButtonLayout.visibility = View.VISIBLE
                    binding.userKYCSkipButton.greyButtonLayout.visibility = View.VISIBLE
                    binding.kycSubmitProgressIcon.visibility = View.GONE
                    binding.kycSkipProgressIcon.visibility = View.GONE
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
                val intent = Intent(baseContext, HomeDisplayActivity::class.java)
                intent.putExtra("jsonResponse", myResponse)
                startActivity(intent)
            }
        })
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
            .url(getString(R.string.date_momo_api) + getString(R.string.api_user_biometrics))
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()

                runOnUiThread {
                    binding.userKYCSubmitButton.blueButtonLayout.visibility = View.VISIBLE
                    binding.userKYCSkipButton.greyButtonLayout.visibility = View.VISIBLE
                    binding.kycSubmitProgressIcon.visibility = View.GONE
                    binding.kycSkipProgressIcon.visibility = View.GONE
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
                var userBioResponse = UserBioResponse(false, "")
                val myResponse: String = response.body()!!.string()

                try {
                    userBioResponse = mapper.readValue(myResponse)
                } catch (exception: IOException) {
                    displaySingleButtonDialog(getString(R.string.server_error_title), getString(R.string.server_error_message))
                }

                runOnUiThread {
                    binding.userKYCSubmitButton.blueButtonLayout.visibility = View.VISIBLE
                    binding.userKYCSkipButton.greyButtonLayout.visibility = View.VISIBLE
                    binding.kycSubmitProgressIcon.visibility = View.GONE
                    binding.kycSkipProgressIcon.visibility = View.GONE
                }

                if (userBioResponse.committed) {
                    requestProcess = getString(R.string.request_fetch_matched_users)

                    sharedPreferencesEditor.putInt(getString(R.string.bisexual_category), userBioRequest.bisexualCategory)
                    sharedPreferencesEditor.putInt(getString(R.string.gay_category), userBioRequest.gayCategory)
                    sharedPreferencesEditor.putInt(getString(R.string.lesbian_category), userBioRequest.lesbianCategory)
                    sharedPreferencesEditor.putInt(getString(R.string.straight_category), userBioRequest.straightCategory)
                    sharedPreferencesEditor.putInt(getString(R.string.sugar_daddy_category), userBioRequest.sugarDaddyCategory)
                    sharedPreferencesEditor.putInt(getString(R.string.sugar_mommy_category), userBioRequest.sugarMommyCategory)
                    sharedPreferencesEditor.putInt(getString(R.string.toy_boy_category), userBioRequest.toyBoyCategory)
                    sharedPreferencesEditor.putInt(getString(R.string.toy_girl_category), userBioRequest.toyGirlCategory)
                    sharedPreferencesEditor.putInt(getString(R.string.bisexual_interest), userBioRequest.bisexualInterest)
                    sharedPreferencesEditor.putInt(getString(R.string.gay_interest), userBioRequest.gayInterest)
                    sharedPreferencesEditor.putInt(getString(R.string.lesbian_interest), userBioRequest.lesbianInterest)
                    sharedPreferencesEditor.putInt(getString(R.string.straight_interest), userBioRequest.straightInterest)
                    sharedPreferencesEditor.putInt(getString(R.string.sugar_daddy_interest), userBioRequest.sugarDaddyInterest)
                    sharedPreferencesEditor.putInt(getString(R.string.sugar_mommy_interest), userBioRequest.sugarMommyInterest)
                    sharedPreferencesEditor.putInt(getString(R.string.toy_boy_interest), userBioRequest.toyBoyInterest)
                    sharedPreferencesEditor.putInt(getString(R.string.toy_girl_interest), userBioRequest.toyGirlInterest)
                    sharedPreferencesEditor.putInt(getString(R.string.sixty_nine_experience), userBioRequest.sixtyNineExperience)
                    sharedPreferencesEditor.putInt(getString(R.string.anal_sex_experience), userBioRequest.analSexExperience)
                    sharedPreferencesEditor.putInt(getString(R.string.given_head_experience), userBioRequest.givenHeadExperience)
                    sharedPreferencesEditor.putInt(getString(R.string.one_night_stand_experience), userBioRequest.oneNightStandExperience)
                    sharedPreferencesEditor.putInt(getString(R.string.orgy_experience), userBioRequest.orgySexExperience)
                    sharedPreferencesEditor.putInt(getString(R.string.pool_sex_experience), userBioRequest.poolSexExperience)
                    sharedPreferencesEditor.putInt(getString(R.string.received_head_experience), userBioRequest.receivedHeadExperience)
                    sharedPreferencesEditor.putInt(getString(R.string.car_sex_experience), userBioRequest.carSexExperience)
                    sharedPreferencesEditor.putInt(getString(R.string.public_sex_experience), userBioRequest.publicSexExperience)
                    sharedPreferencesEditor.putInt(getString(R.string.camera_sex_experience), userBioRequest.cameraSexExperience)
                    sharedPreferencesEditor.putInt(getString(R.string.threesome_experience), userBioRequest.threesomeExperience)
                    sharedPreferencesEditor.putInt(getString(R.string.sex_toy_experience), userBioRequest.sexToyExperience)
                    sharedPreferencesEditor.putInt(getString(R.string.video_sex_experience), userBioRequest.videoSexExperience)
                    sharedPreferencesEditor.putString(getString(R.string.user_level), userBioRequest.userLevel)
                    sharedPreferencesEditor.putString(getString(R.string.current_location), "")
                    sharedPreferencesEditor.apply()

                    fetchMatchedUsers()
                } else {
                    displaySingleButtonDialog(getString(R.string.server_error_title), getString(R.string.server_error_message))
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
        const val TAG = "UserBioActivity"
    }
}


