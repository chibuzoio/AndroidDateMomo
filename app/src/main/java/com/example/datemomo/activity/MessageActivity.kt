package com.example.datemomo.activity

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.datemomo.R
import com.example.datemomo.adapter.MessageAdapter
import com.example.datemomo.adapter.MessengerAdapter
import com.example.datemomo.databinding.ActivityMessageBinding
import com.example.datemomo.model.MessengerModel
import com.example.datemomo.model.response.MessageResponse
import com.example.datemomo.model.response.MessengerResponse
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.IOException

class MessageActivity : AppCompatActivity() {
    private lateinit var bundle: Bundle
    private lateinit var requestProcess: String
    private lateinit var originalRequestProcess: String
    private lateinit var binding: ActivityMessageBinding
    private lateinit var buttonClickEffect: AlphaAnimation
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var messageResponseArray: Array<MessageResponse>
    private lateinit var sharedPreferencesEditor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        binding = ActivityMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        bundle = intent.extras!!

        buttonClickEffect = AlphaAnimation(1f, 0f)
        sharedPreferences =
            getSharedPreferences(getString(R.string.shared_preferences), Context.MODE_PRIVATE)
        sharedPreferencesEditor = sharedPreferences.edit()

        try {
            val mapper = jacksonObjectMapper()
            messageResponseArray = mapper.readValue(bundle.getString("jsonResponse")!!)

/*
            val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
            binding.messageRecyclerView.layoutManager = layoutManager
            binding.messageRecyclerView.itemAnimator = DefaultItemAnimator()

            val messageAdapter = MessageAdapter(messageResponseArray)
            binding.messageRecyclerView.adapter = messageAdapter
*/
        } catch (exception: IOException) {
            Log.e(HomeDisplayActivity.TAG, "Error message from here is ${exception.message}")
        }
    }

    companion object {
        const val TAG = "MessageActivity"
    }
}


