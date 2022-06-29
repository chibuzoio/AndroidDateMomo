package com.example.datemomo.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.datemomo.R
import com.example.datemomo.adapter.MessageAdapter
import com.example.datemomo.databinding.ActivityMessageBinding
import com.example.datemomo.model.ActivityStackModel
import com.example.datemomo.model.MessageModel
import com.example.datemomo.model.request.HomeDisplayRequest
import com.example.datemomo.model.response.MessageResponse
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.*
import java.io.IOException

class MessageActivity : AppCompatActivity() {
    private lateinit var bundle: Bundle
    private var leastRootViewHeight: Int = 0
    private lateinit var requestProcess: String
    private lateinit var originalRequestProcess: String
    private lateinit var binding: ActivityMessageBinding
    private lateinit var buttonClickEffect: AlphaAnimation
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var viewRootHeightArray: MutableSet<Int>
    private lateinit var messageResponseArray: Array<MessageResponse>
    private lateinit var sharedPreferencesEditor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hideSystemUI()

        bundle = intent.extras!!

        viewRootHeightArray = mutableSetOf()

        buttonClickEffect = AlphaAnimation(1f, 0f)
        sharedPreferences =
            getSharedPreferences(getString(R.string.shared_preferences), Context.MODE_PRIVATE)
        sharedPreferencesEditor = sharedPreferences.edit()

        binding.messageMenuLayout.setOnClickListener {

        }

        binding.receiverUserName.setOnClickListener {

        }

        binding.profilePictureLayout.setOnClickListener {

        }

        binding.backArrowLayout.setOnClickListener {

        }

        Glide.with(this)
            .asGif()
            .load(R.drawable.hello_message)
            .into(binding.welcomeHelloMessage)

        binding.receiverUserName.text = bundle.getString("fullName")!!.ifEmpty {
            bundle.getString("userName")
        }
        binding.lastActiveTime.text = bundle.getString("lastActiveTime")!!.ifEmpty {
            "online"
        }

        Glide.with(this)
            .load(getString(R.string.date_momo_api)
                    + getString(R.string.api_image)
                    + bundle.getString("profilePicture"))
            .transform(CenterCrop(), CircleCrop())
            .into(binding.receiverProfilePicture)

        binding.root.viewTreeObserver.addOnGlobalLayoutListener {
            viewRootHeightArray.add(binding.root.height)

            if (viewRootHeightArray.size >= 3) {
                leastRootViewHeight = viewRootHeightArray.elementAt(0)

                for (viewRootHeight in viewRootHeightArray) {
                    if (viewRootHeight < leastRootViewHeight) {
                        leastRootViewHeight = viewRootHeight
                    }
                }
            }

            if (binding.root.height > leastRootViewHeight && leastRootViewHeight > 0) {
                binding.messageInputField.clearFocus()
                viewRootHeightArray = mutableSetOf()
                leastRootViewHeight = 0
                hideSystemUI()
            }
        }

        binding.messageInputField.setOnFocusChangeListener { _, focused ->
            if (focused) {
                showSystemUI()
            } else {
                hideSystemUI()
            }
        }

        try {
            val mapper = jacksonObjectMapper()
            messageResponseArray = mapper.readValue(bundle.getString("jsonResponse")!!)

            if (messageResponseArray.isEmpty()) {
                binding.welcomeMessageLayout.visibility = View.VISIBLE
            } else {
                binding.welcomeMessageLayout.visibility = View.GONE
            }

            val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
            binding.messageRecyclerView.layoutManager = layoutManager
            binding.messageRecyclerView.itemAnimator = DefaultItemAnimator()

            val messageModel = MessageModel(bundle.getInt("senderId"),
                bundle.getInt("receiverId"), this,
                binding, this)

            val messageAdapter = MessageAdapter(messageResponseArray, messageModel)
            binding.messageRecyclerView.adapter = messageAdapter
        } catch (exception: IOException) {
            Log.e(HomeDisplayActivity.TAG, "Error message from here is ${exception.message}")
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Log.e(TAG, "The particular key that was pressed here is $keyCode")

        return super.onKeyDown(keyCode, event)
    }

    override fun onBackPressed() {
        if (binding.messageInputField.isFocused) {
            Log.e(TAG, "binding.messageInputField is currently focused!!!!!!!!!!!!")
            binding.messageInputField.clearFocus()
        }

        val mapper = jacksonObjectMapper()
        val activityStackModel: ActivityStackModel =
            mapper.readValue(sharedPreferences.getString(getString(R.string.activity_stack), "")!!)

        activityStackModel.activityStack.pop()

        val activityStackString = mapper.writeValueAsString(activityStackModel)
        sharedPreferencesEditor.putString(getString(R.string.activity_stack), activityStackString)
        sharedPreferencesEditor.apply()

        when (activityStackModel.activityStack.peek()) {
            getString(R.string.activity_home_display) -> fetchMatchedUsers()
            else -> super.onBackPressed()
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

                }

 /*               if (!Utility.isConnected(baseContext)) {
                    displayDoubleButtonDialog()
                } else if (e.message!!.contains("after")) {
                    displaySingleButtonDialog(getString(R.string.poor_internet_title), getString(R.string.poor_internet_message))
                } else {
                    displaySingleButtonDialog(getString(R.string.server_error_title), getString(R.string.server_error_message))
                }*/
            }

            override fun onResponse(call: Call, response: Response) {
                val myResponse: String = response.body()!!.string()
                val intent = Intent(baseContext, HomeDisplayActivity::class.java)
                intent.putExtra("jsonResponse", myResponse)
                startActivity(intent)
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

    companion object {
        const val TAG = "MessageActivity"
    }
}


