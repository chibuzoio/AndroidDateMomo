package com.example.datemomo.activity

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.AppCompatActivity
import com.example.datemomo.MainApplication.Companion.setNavigationBarDarkIcons
import com.example.datemomo.MainApplication.Companion.setStatusBarDarkIcons
import com.example.datemomo.R
import com.example.datemomo.databinding.ActivityUserBioBinding

class UserBioActivity : AppCompatActivity() {
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

        binding.userKYCSkipButton.hollowButtonText.text = "Skip"

        binding.femaleSexualityOptions.visibility = View.GONE



    }
}


