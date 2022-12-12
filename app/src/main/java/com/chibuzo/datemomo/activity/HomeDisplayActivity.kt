package com.chibuzo.datemomo.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.*
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chibuzo.datemomo.R
import com.chibuzo.datemomo.adapter.HomeDisplayAdapter
import com.chibuzo.datemomo.control.AppBounceInterpolator
import com.chibuzo.datemomo.databinding.ActivityHomeDisplayBinding
import com.chibuzo.datemomo.model.ActivityInstanceModel
import com.chibuzo.datemomo.model.FloatingGalleryModel
import com.chibuzo.datemomo.model.HomeDisplayModel
import com.chibuzo.datemomo.model.instance.*
import com.chibuzo.datemomo.model.request.*
import com.chibuzo.datemomo.model.response.*
import com.chibuzo.datemomo.service.LocationTracker
import com.chibuzo.datemomo.utility.Utility
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.*
import okio.ByteString
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit


class HomeDisplayActivity : AppCompatActivity() {
    private var deviceWidth: Int = 0
    private var deviceHeight: Int = 0
    private lateinit var bundle: Bundle
    private var photoFile: File? = null
    private var lastDisplayPage: Int = 0
    private val PICK_IMAGE_REQUEST = 200
    private var theBitmap: Bitmap? = null
    private val CAPTURE_IMAGE_REQUEST = 100
    private var requestProcess: String = ""
    private var totalAvailablePages: Int = 0
    private lateinit var slideUpTimer: Handler
    private var isActivityActive: Boolean = true
    private var userUpdatedLocation: String = ""
    private var originalRequestProcess: String = ""
    private lateinit var bounceAnimation: Animation
    private var clickBouncedPictureUploader = false
    private lateinit var slideUpAnimation: Animation
    private lateinit var slideDownAnimation: Animation
    private lateinit var messageRequest: MessageRequest
    private lateinit var slideUpTimerRunnable: Runnable
    private var canSlideDownFloatingButton: Boolean = true
    private lateinit var buttonClickEffect: AlphaAnimation
    private lateinit var homeDisplayModel: HomeDisplayModel
    private lateinit var binding: ActivityHomeDisplayBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var homeDisplayInstance: HomeDisplayInstance
    private lateinit var activitySavedInstance: ActivitySavedInstance
    private lateinit var sharedPreferencesEditor: SharedPreferences.Editor
    private lateinit var outerHomeDisplayResponse: OuterHomeDisplayResponse
    private lateinit var moreHomeDisplayResponses: ArrayList<HomeDisplayResponse>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeDisplayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hideSystemUI()

        val displayMetrics = DisplayMetrics()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display?.getRealMetrics(displayMetrics)
        } else {
            @Suppress("DEPRECATION")
            val display = windowManager.defaultDisplay
            @Suppress("DEPRECATION")
            display.getMetrics(displayMetrics)
        }

        deviceWidth = displayMetrics.widthPixels
        deviceHeight = displayMetrics.heightPixels

        bundle = intent.extras!!

        buttonClickEffect = AlphaAnimation(1f, 0f)
        slideUpTimer = Handler(Looper.getMainLooper())

        bounceAnimation = AnimationUtils.loadAnimation(this, R.anim.bounce)
        slideUpAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        slideDownAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_down)

        sharedPreferences =
            getSharedPreferences(getString(R.string.shared_preferences), Context.MODE_PRIVATE)
        sharedPreferencesEditor = sharedPreferences.edit()

        redrawBottomMenuIcons(getString(R.string.clicked_home_menu))

        checkMessageUpdate()
        setUserCurrentLocation()
        checkNotificationUpdate()

        binding.userGay.blueLabelText.text = "Gay"
        binding.userToyBoy.blueLabelText.text = "Toy Boy"
        binding.userLesbian.blueLabelText.text = "Lesbian"
        binding.userToyGirl.blueLabelText.text = "Toy Girl"
        binding.userBisexual.blueLabelText.text = "Bisexual"
        binding.userStraight.blueLabelText.text = "Straight"
        binding.userSugarDaddy.blueLabelText.text = "Sugar Daddy"
        binding.userSugarMommy.blueLabelText.text = "Sugar Mommy"

        binding.gayInterest.blueLabelText.text = "Gay"
        binding.toyBoyInterest.blueLabelText.text = "Toy Boy"
        binding.lesbianInterest.blueLabelText.text = "Lesbian"
        binding.toyGirlInterest.blueLabelText.text = "Toy Girl"
        binding.bisexualInterest.blueLabelText.text = "Bisexual"
        binding.straightInterest.blueLabelText.text = "Straight"
        binding.friendshipInterest.blueLabelText.text = "Friendship"
        binding.sugarDaddyInterest.blueLabelText.text = "Sugar Daddy"
        binding.sugarMommyInterest.blueLabelText.text = "Sugar Mommy"
        binding.relationshipInterest.blueLabelText.text = "Relationship"

        binding.sixtyNineExperience.blueLabelText.text = "69"
        binding.analSexExperience.blueLabelText.text = "Anal Sex"
        binding.orgySexExperience.blueLabelText.text = "Orgy Sex"
        binding.poolSexExperience.blueLabelText.text = "Pool Sex"
        binding.carSexExperience.blueLabelText.text = "Sexed In Car"
        binding.threesomeExperience.blueLabelText.text = "Threesome"
        binding.givenHeadExperience.blueLabelText.text = "Given Head"
        binding.sexToyExperience.blueLabelText.text = "Used Sex Toys"
        binding.missionaryExperience.blueLabelText.text = "Missionary"
        binding.videoSexExperience.blueLabelText.text = "Video Sex Chat"
        binding.publicSexExperience.blueLabelText.text = "Sexed In Public"
        binding.receivedHeadExperience.blueLabelText.text = "Received Head"
        binding.cameraSexExperience.blueLabelText.text = "Sexed With Camera"
        binding.oneNightStandExperience.blueLabelText.text = "One-night Stand"

//        val bitmapImage = BitmapFactory.decodeResource(resources, R.drawable.motion_placeholder)
//        Log.e(TAG, "bitmapImage width and height here are ${bitmapImage.width} and ${bitmapImage.height}")

        binding.profileDisplayButton.iconHollowButtonText.text = "View Profile"
        binding.profileDisplayButton.iconHollowButtonIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_view_blue))
        binding.profileDisplayButton.iconHollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_grey_button)

        binding.userMessageButton.iconHollowButtonText.text = "Message"
        binding.userMessageButton.iconHollowButtonIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_message_blue))
        binding.userMessageButton.iconHollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_grey_button)

        slideUpTimerRunnable = Runnable {
            binding.floatingPictureUploader.startAnimation(slideUpAnimation)
        }

        val appBounceInterpolator = AppBounceInterpolator(0.2, 20.0)
        bounceAnimation.interpolator = appBounceInterpolator

        bounceAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {

            }

            override fun onAnimationEnd(p0: Animation?) {
                if (clickBouncedPictureUploader) {
                    clickBouncedPictureUploader = false
                    pickImageFromGallery()
                }
            }

            override fun onAnimationRepeat(p0: Animation?) {

            }
        })

        slideDownAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {
                canSlideDownFloatingButton = false
            }

            override fun onAnimationEnd(p0: Animation?) {
                binding.floatingPictureUploader.visibility = View.GONE
            }

            override fun onAnimationRepeat(p0: Animation?) {

            }
        })

        slideUpAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {
                binding.floatingPictureUploader.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(p0: Animation?) {
                binding.floatingPictureUploader.startAnimation(bounceAnimation)
                canSlideDownFloatingButton = true
            }

            override fun onAnimationRepeat(p0: Animation?) {

            }
        })

        binding.floatingPictureUploader.setOnClickListener {
            binding.floatingPictureUploader.startAnimation(bounceAnimation)
            clickBouncedPictureUploader = true
        }

        binding.emptyTimelineDialog.dialogActivityButton.blueButtonText.text = "Go To Settings"
        binding.emptyTimelineDialog.dialogActivityText.text =
            "Your timeline is empty because you have few or no preferences that are " +
                    "related to any user's preferences. You might want to update your " +
                    "preferences to see users with preferences that are related to yours"

        Glide.with(this)
            .load(R.drawable.icon_timeline)
            .into(binding.emptyTimelineDialog.dialogActivityImage)

        binding.emptyTimelineDialog.dialogActivityButton.blueButtonLayout.setOnClickListener {
            binding.emptyTimelineDialog.dialogActivityButton.blueButtonLayout.startAnimation(buttonClickEffect)

            val intent = Intent(baseContext, ProfileEditorActivity::class.java)
            startActivity(intent)
        }

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

            if (binding.doubleButtonDialog.dialogRetryButton.text == "Retry") {
                triggerRequestProcess()
            }
        }

        binding.doubleButtonDialog.dialogCancelButton.setOnClickListener {
            binding.doubleButtonDialog.doubleButtonLayout.visibility = View.GONE
            binding.singleButtonDialog.singleButtonLayout.visibility = View.GONE
        }

        binding.doubleButtonDialog.doubleButtonLayout.setOnClickListener {
            binding.doubleButtonDialog.doubleButtonLayout.visibility = View.GONE
            binding.singleButtonDialog.singleButtonLayout.visibility = View.GONE
        }

        binding.bottomNavigationLayout.bottomHomeMenuLayout.setOnClickListener {
            redrawBottomMenuIcons(getString(R.string.clicked_home_menu))
            // Reload HomeDisplayActivity
        }

        binding.bottomNavigationLayout.bottomMessengerMenuLayout.setOnClickListener {
            redrawBottomMenuIcons(getString(R.string.clicked_message_menu))
            requestProcess = getString(R.string.request_fetch_user_messengers)
            fetchUserMessengers()
        }

        binding.bottomNavigationLayout.bottomAccountMenuLayout.setOnClickListener {
            redrawBottomMenuIcons(getString(R.string.clicked_account_menu))
            requestProcess = getString(R.string.request_fetch_user_likers)
            fetchUserLikers()
        }

        binding.bottomNavigationLayout.bottomNotificationMenuLayout.setOnClickListener {
            redrawBottomMenuIcons(getString(R.string.clicked_notification_menu))
            requestProcess = getString(R.string.request_fetch_notifications)
            fetchNotifications()
        }

        binding.bottomNavigationLayout.bottomGenericMenuLayout.setOnClickListener {
            redrawBottomMenuIcons(getString(R.string.clicked_generic_menu))
            requestProcess = getString(R.string.request_fetch_liked_users)
            fetchLikedUsers()
        }

        if ((sharedPreferences.getString(getString(R.string.current_location), "") == "") ||
            (sharedPreferences.getString(getString(R.string.updated_location), "") == "")) {
            val currentUnixTime = System.currentTimeMillis() / 1000L
            val oldLocationSettingTime =
                sharedPreferences.getLong(getString(R.string.last_location_setting_timestamp), 0)

            if ((currentUnixTime - oldLocationSettingTime) > 86400000) {
                Handler(Looper.getMainLooper()).postDelayed({ locationStatusCheck() }, 5000)
            }
        }

        try {
            val mapper = jacksonObjectMapper()
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            activitySavedInstance = mapper.readValue(bundle.getString(getString(R.string.activity_saved_instance))!!)
            homeDisplayInstance = mapper.readValue(activitySavedInstance.activityStateData)
            outerHomeDisplayResponse = homeDisplayInstance.outerHomeDisplayResponse

            if (outerHomeDisplayResponse.homeDisplayResponses.size > 0) {
                binding.emptyTimelineDialog.dialogActivityLayout.visibility = View.GONE

                lastDisplayPage = outerHomeDisplayResponse.homeDisplayResponses.size - 1
                totalAvailablePages = outerHomeDisplayResponse.homeDisplayResponses.size

                val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
                binding.homeDisplayRecyclerView.layoutManager = layoutManager
                binding.homeDisplayRecyclerView.itemAnimator = DefaultItemAnimator()

                val floatingLayoutWidth = deviceWidth - (binding.floatingInformationLayout.marginLeft +
                        binding.floatingInformationLayout.marginRight)
                val leftRightPictureWidthHeight = ((40 / 100F) * floatingLayoutWidth.toFloat()).toInt()
                val tripleBottomBigPictureHeight = ((60 / 100F) * floatingLayoutWidth.toFloat()).toInt()
                val leftRightBigPictureWidth = floatingLayoutWidth - leftRightPictureWidthHeight
                val leftRightBigPictureHeight = leftRightPictureWidthHeight * 2

                val doubleLeftRightLayoutHeight = leftRightBigPictureHeight
                val singlePictureLayoutHeight = tripleBottomBigPictureHeight
                val tripleBottomLayoutHeight = tripleBottomBigPictureHeight + (floatingLayoutWidth / 3)

                val floatingGalleryModel = FloatingGalleryModel(
                    profileOwnerId = 0,
                    floatingLayoutWidth = floatingLayoutWidth,
                    leftRightBigPictureWidth = leftRightBigPictureWidth,
                    tripleBottomLayoutHeight = tripleBottomLayoutHeight,
                    leftRightBigPictureHeight = leftRightBigPictureHeight,
                    singlePictureLayoutHeight = singlePictureLayoutHeight,
                    doubleLeftRightLayoutHeight = doubleLeftRightLayoutHeight,
                    floatingGalleryLayoutHeight = 0,
                    leftRightPictureWidthHeight = leftRightPictureWidthHeight,
                    tripleBottomBigPictureHeight = tripleBottomBigPictureHeight,
                    binding = binding
                )

                binding.innerArchLayout.layoutParams.width = floatingLayoutWidth

                homeDisplayModel = HomeDisplayModel(
                    deviceWidth, requestProcess, bounceAnimation,
                    buttonClickEffect, binding, this,
                    floatingGalleryModel
                )

                val homeDisplayAdapter =
                    HomeDisplayAdapter(
                        outerHomeDisplayResponse.homeDisplayResponses,
                        homeDisplayModel
                    )
                binding.homeDisplayRecyclerView.adapter = homeDisplayAdapter

                binding.homeDisplayRecyclerView.layoutManager!!.scrollToPosition(homeDisplayInstance.scrollToPosition)

                binding.homeDisplayRecyclerView.addOnScrollListener(object :
                    RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)

                        when (newState) {
                            RecyclerView.SCROLL_STATE_DRAGGING -> {
                                if (canSlideDownFloatingButton) {
                                    binding.floatingPictureUploader.startAnimation(
                                        slideDownAnimation
                                    )
                                }

                                slideUpTimer.removeCallbacks(slideUpTimerRunnable)
                            }
                            RecyclerView.SCROLL_STATE_IDLE -> {
                                slideUpTimer.postDelayed(slideUpTimerRunnable, 1000)
                            }
                        }
                    }

                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        hideSystemUI()

                        if (!binding.homeDisplayRecyclerView.canScrollVertically(1)) {
                            requestProcess = getString(R.string.request_fetch_more_matched_users)
                            fetchMoreMatchedUsers()
                        }
                    }
                })
            } else {
                binding.emptyTimelineDialog.dialogActivityLayout.visibility = View.VISIBLE
            }
        } catch (exception: IOException) {
            exception.printStackTrace()
            Log.e(TAG, "Error message from line 450 here is ${exception.message}")
        }
    }

    override fun onStart() {
        super.onStart()
        isActivityActive = true

        hideSystemUI()

        if (sharedPreferences.getBoolean(getString(R.string.authenticated), false)) {
//            Log.e(TAG, "User was truly authenticated!!!!!!!!!!!!")
//            establishSystemSocket()
        }

        if (sharedPreferences.getBoolean(getString(R.string.opened_location_activity), false)) {
            val manager = getSystemService(LOCATION_SERVICE) as LocationManager

            if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                val currentUnixTime = System.currentTimeMillis() / 1000L
                sharedPreferencesEditor.putLong(getString(R.string.last_location_setting_timestamp), currentUnixTime)
                sharedPreferencesEditor.apply()
                setUserCurrentLocation()
            }

            sharedPreferencesEditor.putBoolean(getString(R.string.opened_location_activity), false)
            sharedPreferencesEditor.apply()
        }
    }

    override fun onStop() {
        super.onStop()
        isActivityActive = false
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        hideSystemUI()

        if (binding.userInformationLayout.isVisible) {
            binding.userInformationLayout.visibility = View.GONE
            binding.userGay.blueLabelLayout.visibility = View.GONE
            binding.userToyBoy.blueLabelLayout.visibility = View.GONE
            binding.gayInterest.blueLabelLayout.visibility = View.GONE
            binding.userLesbian.blueLabelLayout.visibility = View.GONE
            binding.userToyGirl.blueLabelLayout.visibility = View.GONE
            binding.userBisexual.blueLabelLayout.visibility = View.GONE
            binding.userStraight.blueLabelLayout.visibility = View.GONE
            binding.toyBoyInterest.blueLabelLayout.visibility = View.GONE
            binding.userSugarDaddy.blueLabelLayout.visibility = View.GONE
            binding.userSugarMommy.blueLabelLayout.visibility = View.GONE
            binding.lesbianInterest.blueLabelLayout.visibility = View.GONE
            binding.toyGirlInterest.blueLabelLayout.visibility = View.GONE
            binding.bisexualInterest.blueLabelLayout.visibility = View.GONE
            binding.carSexExperience.blueLabelLayout.visibility = View.GONE
            binding.sexToyExperience.blueLabelLayout.visibility = View.GONE
            binding.straightInterest.blueLabelLayout.visibility = View.GONE
            binding.analSexExperience.blueLabelLayout.visibility = View.GONE
            binding.orgySexExperience.blueLabelLayout.visibility = View.GONE
            binding.poolSexExperience.blueLabelLayout.visibility = View.GONE
            binding.friendshipInterest.blueLabelLayout.visibility = View.GONE
            binding.sugarDaddyInterest.blueLabelLayout.visibility = View.GONE
            binding.sugarMommyInterest.blueLabelLayout.visibility = View.GONE
            binding.videoSexExperience.blueLabelLayout.visibility = View.GONE
            binding.cameraSexExperience.blueLabelLayout.visibility = View.GONE
            binding.givenHeadExperience.blueLabelLayout.visibility = View.GONE
            binding.publicSexExperience.blueLabelLayout.visibility = View.GONE
            binding.sixtyNineExperience.blueLabelLayout.visibility = View.GONE
            binding.threesomeExperience.blueLabelLayout.visibility = View.GONE
            binding.missionaryExperience.blueLabelLayout.visibility = View.GONE
            binding.relationshipInterest.blueLabelLayout.visibility = View.GONE
            binding.receivedHeadExperience.blueLabelLayout.visibility = View.GONE
            binding.oneNightStandExperience.blueLabelLayout.visibility = View.GONE
        } else {
            finishAffinity()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAPTURE_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            theBitmap = BitmapFactory.decodeFile(photoFile!!.absolutePath)
        } else if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
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

    @Throws(IOException::class)
    fun updateProfilePicture() {
        val imageWidth = theBitmap!!.width
        val imageHeight = theBitmap!!.height

        val base64Picture = Utility.encodeUploadImage(theBitmap!!)

        val mapper = jacksonObjectMapper()
        val pictureUpdateRequest = PictureUpdateRequest(
            sharedPreferences.getInt(getString(R.string.member_id), 0),
            imageWidth,
            imageHeight,
            base64Picture
        )

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

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

                try {
                    val pictureUpdateResponse = mapper.readValue(myResponse) as PictureUpdateResponse

                    sharedPreferencesEditor.putString(getString(R.string.profile_picture),
                        pictureUpdateResponse.profilePicture)
                    sharedPreferencesEditor.apply()

                    fetchUserLikers()
                } catch (exception: IOException) {
                    exception.printStackTrace()
                    displaySingleButtonDialog(
                        getString(R.string.server_error_title),
                        getString(R.string.server_error_message)
                    )
                }
            }
        })
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun startSystemSocket() {
        // wss test
        val client = OkHttpClient.Builder()
            .readTimeout(3, TimeUnit.SECONDS)
            //.sslSocketFactory() - ? нужно ли его указывать дополнительно
            .build()
        val request = Request.Builder()
            .url(getString(R.string.api_web_socket_test)) // 'wss' - для защищенного канала
            .build()
        val wsListener = EchoWebSocketListener ()
        val webSocket = client.newWebSocket(request, wsListener) // this provide to make 'Open ws connection'
    }

    private fun establishSystemSocket() {
        val request = Request.Builder().url(getString(R.string.api_web_socket_test)).build()
        val webSocketClient = OkHttpClient.Builder().readTimeout(0, TimeUnit.MILLISECONDS).build()

        Log.e(TAG, "Execution got here in establishSystemSocket, but hasn't gotten into newWebSocket")

        webSocketClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                val jsonObject = JSONObject()

                Log.e(TAG, "Execution got here in establishSystemSocket 0")

                try {
                    jsonObject.put("messengerTableName", "messengerTableName")
                    jsonObject.put("notificationTableName", "notificationTableName")
                } catch (exception: JSONException) {
                    exception.printStackTrace()
                }

                Log.e(TAG, "Execution got here in establishSystemSocket 1")

                webSocket.send("Hello my friend!")

                Log.e(TAG, "Execution got here in establishSystemSocket 2")

//                OkHttpUtility.updateUserStatus(baseContext, sharedPreferences, isActivityActive)
            }

            override fun onMessage(webSocket: WebSocket, message: String) {
                Log.e(TAG, "Execution got to onMessage method of webSocketClient with message = $message")

                try {
                    val notificationComposite = JSONArray(message)
                } catch (exception: JSONException) {
                    exception.printStackTrace()
                }

                try {
                    Thread.sleep(1000)
                } catch (exception: InterruptedException) {
                    exception.printStackTrace()
                }

                val jsonObject = JSONObject()

                try {
                    jsonObject.put("messengerTableName", "messengerTableName")
                    jsonObject.put("notificationTableName", "notificationTableName")
                } catch (exception: JSONException) {
                    exception.printStackTrace()
                }

                if (isActivityActive) {
                    webSocket.send(jsonObject.toString())
                } else {
                    checkSocketStatus(webSocket)
                }
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {}

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
                checkSocketStatus(webSocket)
                Log.e(TAG, "Execution got here in establishSystemSocket 4, with reason for closing $reason")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                checkSocketStatus(webSocket)
                Log.e(TAG, "Execution got here in establishSystemSocket 5, with closing reason $reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                t.printStackTrace()
                checkSocketStatus(webSocket)
                Log.e(TAG, "errorMessage from web socket onFailure method here is ${t.message}")
            }

            private fun checkSocketStatus(webSocket: WebSocket) {
                if (isActivityActive) {
                    if (webSocket.send("Hello World!")) {
                        webSocket.close(1000, "Closing Socket...")
                    }

                    if (sharedPreferences.getBoolean(getString(R.string.authenticated), false)) {
                        establishSystemSocket()
                    }
                } else {
                    if (webSocket.send("Hello World!")) {
                        webSocket.close(1000, "Closing Socket...")
                    }

//                    OkHttpUtility.updateUserStatus(baseContext, sharedPreferences, isActivityActive)
                }
            }
        })

        webSocketClient.dispatcher().executorService().shutdown()
    }

    private fun triggerRequestProcess() {
        when (requestProcess) {
            getString(R.string.request_fetch_more_matched_users) -> {
                requestProcess = getString(R.string.request_fetch_more_matched_users)
                fetchMoreMatchedUsers()
            }
            getString(R.string.request_fetch_user_messengers) -> {
                requestProcess = getString(R.string.request_fetch_user_messengers)
                fetchUserMessengers()
            }
            getString(R.string.request_fetch_notifications) -> {
                requestProcess = getString(R.string.request_fetch_notifications)
                fetchNotifications()
            }
            getString(R.string.request_fetch_user_messages) -> {
                requestProcess = getString(R.string.request_fetch_user_messages)
                fetchUserMessages(messageRequest)
            }
            getString(R.string.request_fetch_user_likers) -> {
                requestProcess = getString(R.string.request_fetch_user_likers)
                fetchUserLikers()
            }
            getString(R.string.request_fetch_liked_users) -> {
                requestProcess = getString(R.string.request_fetch_liked_users)
                fetchLikedUsers()
            }
        }
    }

    @Throws(IOException::class)
    fun checkNotificationUpdate() {
        val mapper = jacksonObjectMapper()
        val userLikerRequest =
            UserLikerRequest(sharedPreferences.getInt(getString(R.string.member_id), 0))

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        val jsonObjectString = mapper.writeValueAsString(userLikerRequest)
        val requestBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            jsonObjectString
        )

        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(getString(R.string.date_momo_api) + getString(R.string.api_check_notification))
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()
            }

            override fun onResponse(call: Call, response: Response) {
                val myResponse: String = response.body()!!.string()
                val committedResponse = mapper.readValue(myResponse) as CommittedResponse

                runOnUiThread {
                    binding.bottomNavigationLayout.newNotificationNotifier.visibility =
                        if (committedResponse.committed) { View.VISIBLE } else { View.GONE }
                }
            }
        })
    }

    @Throws(IOException::class)
    fun checkMessageUpdate() {
        val mapper = jacksonObjectMapper()
        val userLikerRequest =
            UserLikerRequest(sharedPreferences.getInt(getString(R.string.member_id), 0))

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        val jsonObjectString = mapper.writeValueAsString(userLikerRequest)
        val requestBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            jsonObjectString
        )

        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(getString(R.string.date_momo_api) + getString(R.string.api_check_message))
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()
            }

            override fun onResponse(call: Call, response: Response) {
                val myResponse: String = response.body()!!.string()
                val committedResponse = mapper.readValue(myResponse) as CommittedResponse

                runOnUiThread {
                    binding.bottomNavigationLayout.newMessageNotifier.visibility =
                        if (committedResponse.committed) { View.VISIBLE } else { View.GONE }
                }
            }
        })
    }

    @Throws(IOException::class)
    fun fetchMoreMatchedUsers() {
        if (totalAvailablePages < outerHomeDisplayResponse.thousandRandomCounter.size) {
            binding.moreMatchedUserProgressBar.visibility = View.VISIBLE

            var tenIterationCounter = 0
            val homeDisplayRequest = HomeDisplayRequest(
                sharedPreferences.getInt(getString(R.string.member_id), 0),
                arrayListOf())

            for (index in outerHomeDisplayResponse.thousandRandomCounter.indices) {
                if (index > lastDisplayPage) {
                    homeDisplayRequest.nextMatchedUsersIdArray.add(outerHomeDisplayResponse.thousandRandomCounter[index])
                    tenIterationCounter++

                    if (tenIterationCounter >= 10) {
                        break
                    }
                }
            }
        
            val mapper = jacksonObjectMapper()
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            val jsonObjectString = mapper.writeValueAsString(homeDisplayRequest)
            val requestBody: RequestBody = RequestBody.create(
                MediaType.parse("application/json"),
                jsonObjectString
            )

            val client = OkHttpClient()
            val request: Request = Request.Builder()
                .url(getString(R.string.date_momo_api) + getString(R.string.api_more_matched_user_data))
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    call.cancel()

                    if (!Utility.isConnected(baseContext)) {
                        displayDoubleButtonDialog()
                    } else if (e.message!!.contains("after")) {
                        displaySingleButtonDialog(getString(R.string.poor_internet_title), getString(R.string.poor_internet_message))
                    } else {
                        displaySingleButtonDialog(getString(R.string.server_error_title), getString(R.string.server_error_message))
                    }

                    runOnUiThread {
                        binding.moreMatchedUserProgressBar.visibility = View.GONE
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val myResponse: String = response.body()!!.string()
                    moreHomeDisplayResponses = mapper.readValue(myResponse)

                    runOnUiThread {
                        val scrollToPosition = outerHomeDisplayResponse.homeDisplayResponses.size

                        binding.moreMatchedUserProgressBar.visibility = View.GONE

                        outerHomeDisplayResponse.homeDisplayResponses.addAll(moreHomeDisplayResponses)
                        totalAvailablePages = outerHomeDisplayResponse.homeDisplayResponses.size

                        binding.homeDisplayRecyclerView.adapter!!.notifyItemRangeInserted(
                            lastDisplayPage + 1, outerHomeDisplayResponse.homeDisplayResponses.size)

                        lastDisplayPage = outerHomeDisplayResponse.homeDisplayResponses.size - 1

                        binding.homeDisplayRecyclerView.layoutManager!!.scrollToPosition(scrollToPosition)

                        val homeDisplayInstance = HomeDisplayInstance(
                            scrollToPosition = scrollToPosition,
                            outerHomeDisplayResponse = outerHomeDisplayResponse)

                        val activityStateData = mapper.writeValueAsString(homeDisplayInstance)

                        activitySavedInstance = ActivitySavedInstance(
                            activity = getString(R.string.activity_home_display),
                            activityStateData = activityStateData)

                        val activityInstanceModel: ActivityInstanceModel =
                            mapper.readValue(sharedPreferences.getString(getString(R.string.activity_instance_model), "")!!)

                        try {
                            if (activityInstanceModel.activityInstanceStack.peek().activity == getString(
                                    R.string.activity_home_display
                                )
                            ) {
                                activityInstanceModel.activityInstanceStack.pop()
                                activityInstanceModel.activityInstanceStack.push(
                                    activitySavedInstance
                                )
                            } else {
                                activityInstanceModel.activityInstanceStack.push(activitySavedInstance)
                            }

                            commitInstanceModel(mapper, activityInstanceModel)
                        } catch (exception: EmptyStackException) {
                            exception.printStackTrace()
                            Log.e(TAG, "Exception from trying to peek and pop activityInstanceStack here is ${exception.message}")
                        }
                    }
                }
            })
        }
    }

    @Throws(IOException::class)
    fun fetchUserPictures(userPictureRequest: UserPictureRequest) {
        val mapper = jacksonObjectMapper()

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        val jsonObjectString = mapper.writeValueAsString(userPictureRequest)
        val requestBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            jsonObjectString
        )

        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(getString(R.string.date_momo_api) + getString(R.string.api_user_picture))
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()

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
                val userPictureResponses: ArrayList<UserPictureResponse> = mapper.readValue(myResponse)
                var imageSliderInstance = ImageSliderInstance(
                    memberId = userPictureRequest.memberId,
                    currentPosition = userPictureRequest.currentPosition,
                    userPictureResponses = userPictureResponses)

                val activityInstanceModel: ActivityInstanceModel =
                    mapper.readValue(sharedPreferences.getString(getString(R.string.activity_instance_model), "")!!)

                try {
                    if (activityInstanceModel.activityInstanceStack.peek().activity ==
                        getString(R.string.activity_image_slider)) {
                        activitySavedInstance = activityInstanceModel.activityInstanceStack.peek()
                        imageSliderInstance = mapper.readValue(activitySavedInstance.activityStateData)
                    }

                    val activityStateData = mapper.writeValueAsString(imageSliderInstance)

                    updateHomeDisplayInstance(activityInstanceModel)

                    // Always do this below the method above, updateHomeDisplayInstance
                    activitySavedInstance = ActivitySavedInstance(
                        activity = getString(R.string.activity_image_slider),
                        activityStateData = activityStateData)

                    if (activityInstanceModel.activityInstanceStack.peek().activity != getString(
                            R.string.activity_image_slider
                        )) {
                        activityInstanceModel.activityInstanceStack.push(activitySavedInstance)
                    } else {
                        activityInstanceModel.activityInstanceStack.pop()
                        activityInstanceModel.activityInstanceStack.push(activitySavedInstance)
                    }

                    commitInstanceModel(mapper, activityInstanceModel)
                } catch (exception: EmptyStackException) {
                    exception.printStackTrace()
                    Log.e(TAG, "Exception from trying to peek and pop activityInstanceStack here is ${exception.message}")
                }

                Log.e(TAG, "The number of activities on the stack here is ${activityInstanceModel.activityInstanceStack.size}")

                val activitySavedInstanceString = mapper.writeValueAsString(activitySavedInstance)
                val intent = Intent(this@HomeDisplayActivity, ImageSliderActivity::class.java)
                intent.putExtra(getString(R.string.activity_saved_instance), activitySavedInstanceString)
                startActivity(intent)
            }
        })
    }

    @Throws(IOException::class)
    fun fetchUserInformation(userInformationRequest: UserInformationRequest) {
        val mapper = jacksonObjectMapper()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        val jsonObjectString = mapper.writeValueAsString(userInformationRequest)
        val requestBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            jsonObjectString
        )

        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(getString(R.string.date_momo_api) + getString(R.string.api_user_information))
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()

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
                val homeDisplayResponse: HomeDisplayResponse = mapper.readValue(myResponse)

                val activityStateData = mapper.writeValueAsString(homeDisplayResponse)

                val activityInstanceModel: ActivityInstanceModel =
                    mapper.readValue(sharedPreferences.getString(getString(R.string.activity_instance_model), "")!!)

                try {
                    updateHomeDisplayInstance(activityInstanceModel)

                    // Always do this below the method above, updateHomeDisplayInstance
                    activitySavedInstance = ActivitySavedInstance(
                        activity = getString(R.string.activity_user_information),
                        activityStateData = activityStateData
                    )

                    if (activityInstanceModel.activityInstanceStack.peek().activity != getString(
                            R.string.activity_user_information
                        )) {
                        activityInstanceModel.activityInstanceStack.push(activitySavedInstance)
                    } else {
                        activityInstanceModel.activityInstanceStack.pop()
                        activityInstanceModel.activityInstanceStack.push(activitySavedInstance)
                    }

                    commitInstanceModel(mapper, activityInstanceModel)
                } catch (exception: EmptyStackException) {
                    exception.printStackTrace()
                    Log.e(TAG, "Exception from trying to peek and pop activityInstanceStack here is ${exception.message}")
                }

                Log.e(TAG, "The number of activities on the stack here is ${activityInstanceModel.activityInstanceStack.size}")

                runOnUiThread {
                    binding.userInformationLayout.visibility = View.GONE
                }

                val activitySavedInstanceString = mapper.writeValueAsString(activitySavedInstance)
                val intent = Intent(this@HomeDisplayActivity, UserInformationActivity::class.java)
                intent.putExtra(getString(R.string.activity_saved_instance), activitySavedInstanceString)
                startActivity(intent)
            }
        })
    }

    @Throws(IOException::class)
    fun fetchNotifications() {
        val mapper = jacksonObjectMapper()
        val userLikerRequest =
            UserLikerRequest(sharedPreferences.getInt(getString(R.string.member_id), 0))

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        val jsonObjectString = mapper.writeValueAsString(userLikerRequest)
        val requestBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            jsonObjectString
        )

        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(getString(R.string.date_momo_api) + getString(R.string.api_user_notifications))
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()

                runOnUiThread {

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
                val notificationResponses: ArrayList<NotificationResponse> = mapper.readValue(myResponse)
                var notificationInstance = NotificationInstance(
                    scrollToPosition = 0,
                    notificationResponses = notificationResponses)

                val activityInstanceModel: ActivityInstanceModel =
                    mapper.readValue(sharedPreferences.getString(getString(R.string.activity_instance_model), "")!!)

                try {
                    if (activityInstanceModel.activityInstanceStack.peek().activity ==
                        getString(R.string.activity_notification)) {
                        activitySavedInstance = activityInstanceModel.activityInstanceStack.peek()
                        notificationInstance = mapper.readValue(activitySavedInstance.activityStateData)
                    }

                    val activityStateData = mapper.writeValueAsString(notificationInstance)

                    updateHomeDisplayInstance(activityInstanceModel)

                    // Always do this below the method above, updateHomeDisplayInstance
                    activitySavedInstance = ActivitySavedInstance(
                        activity = getString(R.string.activity_notification),
                        activityStateData = activityStateData
                    )

                    if (activityInstanceModel.activityInstanceStack.peek().activity != getString(
                            R.string.activity_notification
                        )) {
                        activityInstanceModel.activityInstanceStack.push(activitySavedInstance)
                    } else {
                        activityInstanceModel.activityInstanceStack.pop()
                        activityInstanceModel.activityInstanceStack.push(activitySavedInstance)
                    }

                    commitInstanceModel(mapper, activityInstanceModel)
                } catch (exception: EmptyStackException) {
                    exception.printStackTrace()
                    Log.e(TAG, "Exception from trying to peek and pop activityInstanceStack here is ${exception.message}")
                }

                runOnUiThread {
                    binding.userInformationLayout.visibility = View.GONE
                }

                Log.e(TAG, "The number of activities on the stack here is ${activityInstanceModel.activityInstanceStack.size}")

                val activitySavedInstanceString = mapper.writeValueAsString(activitySavedInstance)
                val intent = Intent(this@HomeDisplayActivity, NotificationActivity::class.java)
                intent.putExtra(getString(R.string.activity_saved_instance), activitySavedInstanceString)
                startActivity(intent)
            }
        })
    }

    @Throws(IOException::class)
    fun updateCurrentLocation() {
        val mapper = jacksonObjectMapper()
        val updateLocationRequest =
            UpdateLocationRequest(sharedPreferences.getInt(getString(R.string.member_id), 0),
                userUpdatedLocation)

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        val jsonObjectString = mapper.writeValueAsString(updateLocationRequest)
        val requestBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            jsonObjectString
        )

        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(getString(R.string.date_momo_api) + getString(R.string.api_update_location))
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()

                runOnUiThread {

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
            }
        })
    }

    @Throws(IOException::class)
    fun fetchUserMessages(messageRequest: MessageRequest) {
        val mapper = jacksonObjectMapper()
        this.messageRequest = messageRequest

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        val jsonObjectString = mapper.writeValueAsString(messageRequest)
        val requestBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            jsonObjectString
        )

        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(getString(R.string.date_momo_api) + getString(R.string.api_user_messages_data))
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()

                runOnUiThread {

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

                runOnUiThread {
                    binding.userInformationLayout.visibility = View.GONE
                }

                val messageResponses: ArrayList<MessageResponse> = mapper.readValue(myResponse)
                val messageInstance = MessageInstance(
                    senderId = messageRequest.senderId,
                    fullName = messageRequest.fullName,
                    userName = messageRequest.userName,
                    scrollToPosition = messageResponses.size - 1,
                    receiverId = messageRequest.receiverId,
                    lastActiveTime = messageRequest.lastActiveTime,
                    profilePicture = messageRequest.profilePicture,
                    userBlockedStatus = messageRequest.userBlockedStatus,
                    messageResponses = messageResponses)

                val activityStateData = mapper.writeValueAsString(messageInstance)

                val activityInstanceModel: ActivityInstanceModel =
                    mapper.readValue(sharedPreferences.getString(getString(R.string.activity_instance_model), "")!!)

                try {
                    updateHomeDisplayInstance(activityInstanceModel)

                    // Always do this below the method above, updateHomeDisplayInstance
                    activitySavedInstance = ActivitySavedInstance(
                        activity = getString(R.string.activity_message),
                        activityStateData = activityStateData)

                    if (activityInstanceModel.activityInstanceStack.peek().activity != getString(
                            R.string.activity_message
                        )) {
                        activityInstanceModel.activityInstanceStack.push(activitySavedInstance)
                    } else {
                        activityInstanceModel.activityInstanceStack.pop()
                        activityInstanceModel.activityInstanceStack.push(activitySavedInstance)
                    }

                    commitInstanceModel(mapper, activityInstanceModel)
                } catch (exception: EmptyStackException) {
                    exception.printStackTrace()
                    Log.e(TAG, "Exception from trying to peek and pop activityInstanceStack here is ${exception.message}")
                }

                Log.e(TAG, "The number of activities on the stack here is ${activityInstanceModel.activityInstanceStack.size}")

                val activitySavedInstanceString = mapper.writeValueAsString(activitySavedInstance)
                val intent = Intent(this@HomeDisplayActivity, MessageActivity::class.java)
                intent.putExtra(getString(R.string.activity_saved_instance), activitySavedInstanceString)
                startActivity(intent)
            }
        })
    }

    @Throws(IOException::class)
    fun fetchUserMessengers() {
        val mapper = jacksonObjectMapper()
        val userLikerRequest = UserLikerRequest(sharedPreferences.getInt(getString(R.string.member_id), 0))

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        val jsonObjectString = mapper.writeValueAsString(userLikerRequest)
        val requestBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            jsonObjectString
        )

        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(getString(R.string.date_momo_api) + getString(R.string.api_user_messengers_data))
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()

                runOnUiThread {

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
                val messengerResponses: ArrayList<MessengerResponse> = mapper.readValue(myResponse)
                val messengerInstance = MessengerInstance(
                    scrollToPosition = 0,
                    messengerResponses = messengerResponses)

                val activityInstanceModel: ActivityInstanceModel =
                    mapper.readValue(sharedPreferences.getString(getString(R.string.activity_instance_model), "")!!)

                try {
                    // This is not required here because messenger activity always
                    // needs to be refreshed when it's newly navigated to
/*
                if (activityInstanceModel.activityInstanceStack.peek().activity ==
                    getString(R.string.activity_messenger)) {
                    activitySavedInstance = activityInstanceModel.activityInstanceStack.peek()
                    messengerInstance = mapper.readValue(activitySavedInstance.activityStateData)
                }
*/

                    val activityStateData = mapper.writeValueAsString(messengerInstance)

                    updateHomeDisplayInstance(activityInstanceModel)

                    // Always do this below the method above, updateHomeDisplayInstance
                    activitySavedInstance = ActivitySavedInstance(
                        activity = getString(R.string.activity_messenger),
                        activityStateData = activityStateData)

                    if (activityInstanceModel.activityInstanceStack.peek().activity != getString(
                            R.string.activity_messenger
                        )) {
                        activityInstanceModel.activityInstanceStack.push(activitySavedInstance)
                    } else {
                        activityInstanceModel.activityInstanceStack.pop()
                        activityInstanceModel.activityInstanceStack.push(activitySavedInstance)
                    }

                    commitInstanceModel(mapper, activityInstanceModel)
                } catch (exception: EmptyStackException) {
                    exception.printStackTrace()
                    Log.e(TAG, "Exception from trying to peek and pop activityInstanceStack here is ${exception.message}")
                }

                Log.e(TAG, "The number of activities on the stack here is ${activityInstanceModel.activityInstanceStack.size}")

                runOnUiThread {
                    binding.userInformationLayout.visibility = View.GONE
                }

                val activitySavedInstanceString = mapper.writeValueAsString(activitySavedInstance)
                val intent = Intent(this@HomeDisplayActivity, MessengerActivity::class.java)
                intent.putExtra(getString(R.string.activity_saved_instance), activitySavedInstanceString)
                startActivity(intent)
            }
        })
    }

    @Throws(IOException::class)
    fun fetchLikedUsers() {
        val mapper = jacksonObjectMapper()
        val userLikerRequest = UserLikerRequest(sharedPreferences.getInt(getString(R.string.member_id), 0))

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        val jsonObjectString = mapper.writeValueAsString(userLikerRequest)
        val requestBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            jsonObjectString
        )

        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(getString(R.string.date_momo_api) + getString(R.string.api_liked_users_data))
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()

                runOnUiThread {

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
                val userLikerResponses: ArrayList<UserLikerResponse> = mapper.readValue(myResponse)
                val userAccountInstance = UserAccountInstance(userLikerResponses)

                val activityStateData = mapper.writeValueAsString(userAccountInstance)

                val activityInstanceModel: ActivityInstanceModel =
                    mapper.readValue(sharedPreferences.getString(getString(R.string.activity_instance_model), "")!!)

                try {
                    updateHomeDisplayInstance(activityInstanceModel)

                    // Always do this below the method above, updateHomeDisplayInstance
                    activitySavedInstance = ActivitySavedInstance(
                        activity = getString(R.string.activity_user_account),
                        activityStateData = activityStateData)

                    if (activityInstanceModel.activityInstanceStack.peek().activity != getString(
                            R.string.activity_user_account
                        )) {
                        activityInstanceModel.activityInstanceStack.push(activitySavedInstance)
                    } else {
                        activityInstanceModel.activityInstanceStack.pop()
                        activityInstanceModel.activityInstanceStack.push(activitySavedInstance)
                    }

                    commitInstanceModel(mapper, activityInstanceModel)
                } catch (exception: EmptyStackException) {
                    exception.printStackTrace()
                    Log.e(TAG, "Exception from trying to peek and pop activityInstanceStack here is ${exception.message}")
                }

                Log.e(TAG, "The number of activities on the stack here is ${activityInstanceModel.activityInstanceStack.size}")

                val activitySavedInstanceString = mapper.writeValueAsString(activitySavedInstance)
                val intent = Intent(this@HomeDisplayActivity, UserAccountActivity::class.java)
                intent.putExtra(getString(R.string.activity_saved_instance), activitySavedInstanceString)
                startActivity(intent)
            }
        })
    }

    @Throws(IOException::class)
    fun fetchUserLikers() {
        val mapper = jacksonObjectMapper()
        val userLikerRequest = UserLikerRequest(sharedPreferences.getInt(getString(R.string.member_id), 0))

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        val jsonObjectString = mapper.writeValueAsString(userLikerRequest)
        val requestBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            jsonObjectString
        )

        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(getString(R.string.date_momo_api) + getString(R.string.api_user_likers_data))
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()

                runOnUiThread {

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
                val userLikerResponses: ArrayList<UserLikerResponse> = mapper.readValue(myResponse)
                val userProfileInstance = UserProfileInstance(userLikerResponses)

                val activityStateData = mapper.writeValueAsString(userProfileInstance)

                val activityInstanceModel: ActivityInstanceModel =
                    mapper.readValue(sharedPreferences.getString(getString(R.string.activity_instance_model), "")!!)

                try {
                    updateHomeDisplayInstance(activityInstanceModel)

                    // Always do this below the method above, updateHomeDisplayInstance
                    activitySavedInstance = ActivitySavedInstance(
                        activity = getString(R.string.activity_user_profile),
                        activityStateData = activityStateData)

                    if (activityInstanceModel.activityInstanceStack.peek().activity != getString(
                            R.string.activity_user_profile
                        )) {
                        activityInstanceModel.activityInstanceStack.push(activitySavedInstance)
                    } else {
                        activityInstanceModel.activityInstanceStack.pop()
                        activityInstanceModel.activityInstanceStack.push(activitySavedInstance)
                    }

                    commitInstanceModel(mapper, activityInstanceModel)
                } catch (exception: EmptyStackException) {
                    exception.printStackTrace()
                    Log.e(TAG, "Exception from trying to peek and pop activityInstanceStack here is ${exception.message}")
                }

                Log.e(TAG, "The number of activities on the stack here is ${activityInstanceModel.activityInstanceStack.size}")

                runOnUiThread {
                    binding.userInformationLayout.visibility = View.GONE
                }

                val activitySavedInstanceString = mapper.writeValueAsString(activitySavedInstance)
                val intent = Intent(this@HomeDisplayActivity, UserProfileActivity::class.java)
                intent.putExtra(getString(R.string.activity_saved_instance), activitySavedInstanceString)
                startActivity(intent)
            }
        })
    }

    private fun setUserCurrentLocation() {
        if (LocationTracker(this).canGetLocation) {
            // Initialize location here and send it to the server if the user hasn't updated his
            // location for the first time. But, if the gotten location is different from the
            // user's saved location, request the user to update his location

            val latitude = LocationTracker(this).getLatitude()
            val longitude = LocationTracker(this).getLongitude()

            val addresses: List<Address>
            val geocoder = Geocoder(this, Locale.getDefault())

            try {
                addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    1
                ) as List<Address> // Here 1 represent max location result to returned, by documents it recommended 1 to 5

                val address: String =
                    addresses[0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()

                val city = addresses[0].locality
                val state = addresses[0].adminArea
                val country = addresses[0].countryName
                val postalCode = addresses[0].postalCode
                val knownName = addresses[0].featureName

                userUpdatedLocation = if (!city.isNullOrEmpty()) {
                    city
                } else if (!state.isNullOrEmpty()) {
                    state
                } else {
                    country
                }

                if (sharedPreferences.getString(getString(R.string.current_location), "").isNullOrEmpty()) {
                    sharedPreferencesEditor.putString(getString(R.string.current_location), userUpdatedLocation)
                    sharedPreferencesEditor.apply()

                    requestProcess = getString(R.string.request_update_current_location)
                    updateCurrentLocation()
                } else {
                    sharedPreferencesEditor.putString(getString(R.string.updated_location), userUpdatedLocation)
                    sharedPreferencesEditor.apply()

                    // notify user of location change here
                }
            } catch (exception: Exception) {
                exception.printStackTrace()

                when (exception) {
                    is IOException -> {
                        Log.e(TAG, "IOException was caught, with message = ${exception.message}")
                    }
                    is IndexOutOfBoundsException -> {
                        Log.e(TAG, "IndexOutOfBoundsException was caught, with message = ${exception.message}")
                    }
                    else -> {
                        Log.e(TAG, "Error message from line 382 here is ${exception.message}")
                    }
                }
            }
        }
    }

    private fun locationStatusCheck() {
        val manager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps()
        }
    }

    private fun buildAlertMessageNoGps() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setMessage("Turn on your GPS so as to effectively use DateMomo application")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                sharedPreferencesEditor.putBoolean(getString(R.string.opened_location_activity), true)
                sharedPreferencesEditor.apply()

                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.cancel()
            }
        val alert: AlertDialog = builder.create()
        alert.show()
    }

    private fun updateHomeDisplayInstance(activityInstanceModel: ActivityInstanceModel) {
        if (activityInstanceModel.activityInstanceStack.peek().activity == getString(R.string.activity_home_display)) {
            val scrollToPosition =
                (binding.homeDisplayRecyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            activityInstanceModel.activityInstanceStack.pop()

            val homeDisplayInstance = HomeDisplayInstance(
                scrollToPosition = scrollToPosition,
                outerHomeDisplayResponse = outerHomeDisplayResponse)

            val mapper = jacksonObjectMapper()
            val activityStateData = mapper.writeValueAsString(homeDisplayInstance)

            activitySavedInstance = ActivitySavedInstance(
                activity = getString(R.string.activity_home_display),
                activityStateData = activityStateData)

            activityInstanceModel.activityInstanceStack.push(activitySavedInstance)
        }
    }

    private fun commitInstanceModel(mapper: ObjectMapper, activityInstanceModel: ActivityInstanceModel) {
        val activityInstanceModelString =
            mapper.writeValueAsString(activityInstanceModel)
        sharedPreferencesEditor.putString(
            getString(R.string.activity_instance_model),
            activityInstanceModelString
        )
        sharedPreferencesEditor.apply()
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

    private fun redrawBottomMenuIcons(clickedBottomMenu: String) {
        when (clickedBottomMenu) {
            getString(R.string.clicked_home_menu) -> {
                binding.bottomNavigationLayout.homeMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.selected_bottom_menu)
                binding.bottomNavigationLayout.bottomHomeMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_home_white))
                binding.bottomNavigationLayout.accountMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.ignored_bottom_menu)
                binding.bottomNavigationLayout.genericMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.ignored_bottom_menu)
                binding.bottomNavigationLayout.messageMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.ignored_bottom_menu)
                binding.bottomNavigationLayout.bottomAccountMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_account_blue))
                binding.bottomNavigationLayout.bottomMessageMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_message_blue))
                binding.bottomNavigationLayout.notificationMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.ignored_bottom_menu)
                binding.bottomNavigationLayout.bottomGenericMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_menu_bar_blue))
                binding.bottomNavigationLayout.bottomNotificationMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_notification_blue))
            }
            getString(R.string.clicked_account_menu) -> {
                binding.bottomNavigationLayout.homeMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.ignored_bottom_menu)
                binding.bottomNavigationLayout.bottomHomeMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_home_blue))
                binding.bottomNavigationLayout.messageMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.ignored_bottom_menu)
                binding.bottomNavigationLayout.genericMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.ignored_bottom_menu)
                binding.bottomNavigationLayout.accountMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.selected_bottom_menu)
                binding.bottomNavigationLayout.bottomMessageMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_message_blue))
                binding.bottomNavigationLayout.notificationMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.ignored_bottom_menu)
                binding.bottomNavigationLayout.bottomGenericMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_menu_bar_blue))
                binding.bottomNavigationLayout.bottomAccountMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_account_white))
                binding.bottomNavigationLayout.bottomNotificationMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_notification_blue))
            }
            getString(R.string.clicked_message_menu) -> {
                binding.bottomNavigationLayout.homeMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.ignored_bottom_menu)
                binding.bottomNavigationLayout.bottomHomeMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_home_blue))
                binding.bottomNavigationLayout.accountMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.ignored_bottom_menu)
                binding.bottomNavigationLayout.genericMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.ignored_bottom_menu)
                binding.bottomNavigationLayout.messageMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.selected_bottom_menu)
                binding.bottomNavigationLayout.bottomAccountMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_account_blue))
                binding.bottomNavigationLayout.bottomGenericMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_menu_bar_blue))
                binding.bottomNavigationLayout.notificationMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.ignored_bottom_menu)
                binding.bottomNavigationLayout.bottomMessageMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_message_white))
                binding.bottomNavigationLayout.bottomNotificationMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_notification_blue))
            }
            getString(R.string.clicked_notification_menu) -> {
                binding.bottomNavigationLayout.homeMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.ignored_bottom_menu)
                binding.bottomNavigationLayout.bottomHomeMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_home_blue))
                binding.bottomNavigationLayout.accountMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.ignored_bottom_menu)
                binding.bottomNavigationLayout.genericMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.ignored_bottom_menu)
                binding.bottomNavigationLayout.messageMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.ignored_bottom_menu)
                binding.bottomNavigationLayout.bottomAccountMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_account_blue))
                binding.bottomNavigationLayout.bottomMessageMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_message_blue))
                binding.bottomNavigationLayout.bottomGenericMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_menu_bar_blue))
                binding.bottomNavigationLayout.notificationMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.selected_bottom_menu)
                binding.bottomNavigationLayout.bottomNotificationMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_notification_white))
            }
            getString(R.string.clicked_generic_menu) -> {
                binding.bottomNavigationLayout.homeMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.ignored_bottom_menu)
                binding.bottomNavigationLayout.bottomHomeMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_home_blue))
                binding.bottomNavigationLayout.accountMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.ignored_bottom_menu)
                binding.bottomNavigationLayout.messageMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.ignored_bottom_menu)
                binding.bottomNavigationLayout.genericMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.selected_bottom_menu)
                binding.bottomNavigationLayout.bottomAccountMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_account_blue))
                binding.bottomNavigationLayout.bottomMessageMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_message_blue))
                binding.bottomNavigationLayout.notificationMenuImageCover.background = ContextCompat.getDrawable(this, R.drawable.ignored_bottom_menu)
                binding.bottomNavigationLayout.bottomGenericMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_menu_bar_white))
                binding.bottomNavigationLayout.bottomNotificationMenuImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_notification_blue))
            }
        }
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

    private class EchoWebSocketListener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            webSocket.send("Hello, it's SSaurel !")
            webSocket.send("What's up ?")
            webSocket.send(ByteString.decodeHex("deadbeef"))
            webSocket.close(NORMAL_CLOSURE_STATUS, "Goodbye !")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            output("Receiving : $text")
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            output("Receiving bytes : " + bytes.hex())
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            webSocket.close(NORMAL_CLOSURE_STATUS, null)
            output("Closing : $code / $reason")
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            output("Error : " + t.message)
        }

        companion object {
            private const val NORMAL_CLOSURE_STATUS = 1000
        }

        private fun output(txt: String) {
            Log.v("WSS", txt)
        }
    }

    companion object {
        const val TAG = "HomeDisplayActivity"
    }
}


