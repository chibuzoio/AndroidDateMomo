package com.example.datemomo.activity

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.datemomo.MainApplication.Companion.setNavigationBarDarkIcons
import com.example.datemomo.MainApplication.Companion.setStatusBarDarkIcons
import com.example.datemomo.R
import com.example.datemomo.adapter.HomeDisplayAdapter
import com.example.datemomo.databinding.ActivityHomeDisplayBinding
import com.example.datemomo.model.HomeDisplayModel
import com.google.android.flexbox.*


class HomeDisplayActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeDisplayBinding
    private lateinit var buttonClickEffect: AlphaAnimation
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferencesEditor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeDisplayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.setStatusBarDarkIcons(true)
        window.setNavigationBarDarkIcons(true)

        buttonClickEffect = AlphaAnimation(1f, 0f)
        sharedPreferences =
            getSharedPreferences(getString(R.string.shared_preferences), Context.MODE_PRIVATE)
        sharedPreferencesEditor = sharedPreferences.edit()

        val homeDisplayImages = ArrayList<HomeDisplayModel>()
        homeDisplayImages.add(HomeDisplayModel(R.drawable.image1))
        homeDisplayImages.add(HomeDisplayModel(R.drawable.image2))
        homeDisplayImages.add(HomeDisplayModel(R.drawable.image3))
        homeDisplayImages.add(HomeDisplayModel(R.drawable.image4))
        homeDisplayImages.add(HomeDisplayModel(R.drawable.image5))
        homeDisplayImages.add(HomeDisplayModel(R.drawable.image6))
        homeDisplayImages.add(HomeDisplayModel(R.drawable.image7))
        homeDisplayImages.add(HomeDisplayModel(R.drawable.image8))
        homeDisplayImages.add(HomeDisplayModel(R.drawable.image9))
        homeDisplayImages.add(HomeDisplayModel(R.drawable.image10))
        homeDisplayImages.add(HomeDisplayModel(R.drawable.image11))
        homeDisplayImages.add(HomeDisplayModel(R.drawable.image12))
        homeDisplayImages.add(HomeDisplayModel(R.drawable.image13))
        homeDisplayImages.add(HomeDisplayModel(R.drawable.image14))
        homeDisplayImages.add(HomeDisplayModel(R.drawable.image15))

//        val layoutManager = FlexboxLayoutManager(this)
//        layoutManager.flexDirection = FlexDirection.COLUMN
//        layoutManager.justifyContent = JustifyContent.FLEX_END

//        layoutManager.flexDirection = FlexDirection.ROW
//        layoutManager.justifyContent = JustifyContent.CENTER
//        layoutManager.alignItems = AlignItems.CENTER

//        layoutManager.flexWrap = FlexWrap.WRAP
//        layoutManager.flexDirection = FlexDirection.ROW
//        layoutManager.alignItems = AlignItems.STRETCH

        val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.homeDisplayRecyclerView.layoutManager = layoutManager
        binding.homeDisplayRecyclerView.itemAnimator = DefaultItemAnimator()

        val homeDisplayAdapter = HomeDisplayAdapter(homeDisplayImages)
        binding.homeDisplayRecyclerView.adapter = homeDisplayAdapter
    }
}


