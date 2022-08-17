package com.example.datemomo.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.datemomo.R
import com.example.datemomo.adapter.HomeDisplayAdapter
import com.example.datemomo.databinding.ActivityHomeDisplayBinding
import com.example.datemomo.model.ActivityStackModel
import com.example.datemomo.model.HomeDisplayModel
import com.example.datemomo.model.request.*
import com.example.datemomo.model.response.HomeDisplayResponse
import com.example.datemomo.model.response.OuterHomeDisplayResponse
import com.example.datemomo.service.LocationTracker
import com.example.datemomo.utility.Utility
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.*
import okio.ByteString
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class HomeDisplayActivity : AppCompatActivity() {
    private var deviceWidth: Int = 0
    private var deviceHeight: Int = 0
    private lateinit var bundle: Bundle
    private var lastDisplayPage: Int = 0
    private var requestProcess: String = ""
    private var totalAvailablePages: Int = 0
    private var isActivityActive: Boolean = true
    private lateinit var userUpdatedLocation: String
    private lateinit var messageRequest: MessageRequest
    private lateinit var originalRequestProcess: String
    private lateinit var buttonClickEffect: AlphaAnimation
    private lateinit var homeDisplayModel: HomeDisplayModel
    private lateinit var binding: ActivityHomeDisplayBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferencesEditor: SharedPreferences.Editor
    private lateinit var outerHomeDisplayResponse: OuterHomeDisplayResponse
    private lateinit var homeDisplayResponseArray: ArrayList<HomeDisplayResponse>

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
        sharedPreferences =
            getSharedPreferences(getString(R.string.shared_preferences), Context.MODE_PRIVATE)
        sharedPreferencesEditor = sharedPreferences.edit()

        redrawBottomMenuIcons(getString(R.string.clicked_home_menu))

        binding.userGay.blueButtonText.text = "Gay"
        binding.userToyBoy.blueButtonText.text = "Toy Boy"
        binding.userLesbian.blueButtonText.text = "Lesbian"
        binding.userToyGirl.blueButtonText.text = "Toy Girl"
        binding.userBisexual.blueButtonText.text = "Bisexual"
        binding.userStraight.blueButtonText.text = "Straight"
        binding.userSugarDaddy.blueButtonText.text = "Sugar Daddy"
        binding.userSugarMommy.blueButtonText.text = "Sugar Mommy"

        binding.gayInterest.blueButtonText.text = "Gay"
        binding.toyBoyInterest.blueButtonText.text = "Toy Boy"
        binding.lesbianInterest.blueButtonText.text = "Lesbian"
        binding.toyGirlInterest.blueButtonText.text = "Toy Girl"
        binding.bisexualInterest.blueButtonText.text = "Bisexual"
        binding.straightInterest.blueButtonText.text = "Straight"
        binding.sugarDaddyInterest.blueButtonText.text = "Sugar Daddy"
        binding.sugarMommyInterest.blueButtonText.text = "Sugar Mommy"

        binding.sixtyNineExperience.blueButtonText.text = "69"
        binding.analSexExperience.blueButtonText.text = "Anal Sex"
        binding.orgySexExperience.blueButtonText.text = "Orgy Sex"
        binding.poolSexExperience.blueButtonText.text = "Pool Sex"
        binding.carSexExperience.blueButtonText.text = "Sexed In Car"
        binding.threesomeExperience.blueButtonText.text = "Threesome"
        binding.givenHeadExperience.blueButtonText.text = "Given Head"
        binding.sexToyExperience.blueButtonText.text = "Used Sex Toys"
        binding.videoSexExperience.blueButtonText.text = "Video Sex Chat"
        binding.publicSexExperience.blueButtonText.text = "Sexed In Public"
        binding.receivedHeadExperience.blueButtonText.text = "Received Head"
        binding.cameraSexExperience.blueButtonText.text = "Sexed With Camera"
        binding.oneNightStandExperience.blueButtonText.text = "One-night Stand"

//        val bitmapImage = BitmapFactory.decodeResource(resources, R.drawable.motion_placeholder)
//        Log.e(TAG, "bitmapImage width and height here are ${bitmapImage.width} and ${bitmapImage.height}")

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
                ) // Here 1 represent max location result to returned, by documents it recommended 1 to 5

                val address: String =
                    addresses[0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()

                val city = addresses[0].locality
                val state = addresses[0].adminArea
                val country = addresses[0].countryName
                val postalCode = addresses[0].postalCode
                val knownName = addresses[0].featureName

                if (sharedPreferences.getString(getString(R.string.current_location), "").isNullOrEmpty()) {
                    if (knownName.isNullOrEmpty()) {
                        userUpdatedLocation = city
                        sharedPreferencesEditor.putString(getString(R.string.current_location), city)
                        sharedPreferencesEditor.apply()
                    } else {
                        userUpdatedLocation = knownName
                        sharedPreferencesEditor.putString(getString(R.string.current_location), knownName)
                        sharedPreferencesEditor.apply()
                    }

                    requestProcess = getString(R.string.request_update_current_location)

                    updateCurrentLocation()
                } else {
                    if (knownName.isNullOrEmpty()) {
                        sharedPreferencesEditor.putString(getString(R.string.updated_location), city)
                        sharedPreferencesEditor.apply()
                    } else {
                        sharedPreferencesEditor.putString(getString(R.string.updated_location), knownName)
                        sharedPreferencesEditor.apply()
                    }

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
                        Log.e(TAG, "Error message from here is ${exception.message}")
                    }
                }
            }
        }

        try {
            val mapper = jacksonObjectMapper()
            outerHomeDisplayResponse = mapper.readValue(bundle.getString("jsonResponse")!!)

            lastDisplayPage = outerHomeDisplayResponse.homeDisplayResponses.size - 1
            totalAvailablePages = outerHomeDisplayResponse.homeDisplayResponses.size

            val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
            binding.homeDisplayRecyclerView.layoutManager = layoutManager
            binding.homeDisplayRecyclerView.itemAnimator = DefaultItemAnimator()

            homeDisplayModel = HomeDisplayModel(deviceWidth, requestProcess,
                buttonClickEffect, binding, this)

            val homeDisplayAdapter =
                HomeDisplayAdapter(outerHomeDisplayResponse.homeDisplayResponses, homeDisplayModel)
            binding.homeDisplayRecyclerView.adapter = homeDisplayAdapter

            binding.homeDisplayRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    if (!binding.homeDisplayRecyclerView.canScrollVertically(1)) {
//                        if (currentPage <= totalAvailablePages) {
                            fetchMoreMatchedUsers()
//                        }
                    }
                }
            })
        } catch (exception: IOException) {
            exception.printStackTrace()
            Log.e(TAG, "Error message from here is ${exception.message}")
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
    }

    override fun onStop() {
        super.onStop()
        isActivityActive = false
    }

    override fun onBackPressed() {
        if (binding.userInformationLayout.isVisible) {
            binding.userInformationLayout.visibility = View.GONE
        } else {
            finishAffinity()
        }
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
            getString(R.string.request_fetch_notifications) -> fetchNotifications()
            getString(R.string.request_fetch_user_messengers) -> fetchUserMessengers()
            getString(R.string.request_fetch_user_messages) -> fetchUserMessages(messageRequest)
            getString(R.string.request_fetch_user_likers) -> fetchUserLikers()
            getString(R.string.request_fetch_liked_users) -> fetchLikedUsers()
        }
    }

    private fun toggleProgressBar() {
        if (lastDisplayPage <= 0) {
            if (binding.defaultProgress.isShown) {
                binding.defaultProgress.visibility = View.GONE
            } else {
                binding.defaultProgress.visibility = View.VISIBLE
            }
        } else {
            if (binding.loadMoreProgress.isShown) {
                binding.loadMoreProgress.visibility = View.GONE
            } else {
                binding.loadMoreProgress.visibility = View.VISIBLE
            }
        }
    }

    @Throws(IOException::class)
    fun fetchMoreMatchedUsers() {
        toggleProgressBar()

        var tenIterationCounter = 0
        val homeDisplayRequest = HomeDisplayRequest(arrayListOf())

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
                    toggleProgressBar()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val myResponse: String = response.body()!!.string()
                homeDisplayResponseArray = mapper.readValue(myResponse)

                Log.e(TAG, "Response from fetchMoreMatchedUsers here is $homeDisplayResponseArray")

                runOnUiThread {
                    outerHomeDisplayResponse.homeDisplayResponses.addAll(homeDisplayResponseArray)
                    totalAvailablePages = outerHomeDisplayResponse.homeDisplayResponses.size

                    binding.homeDisplayRecyclerView.adapter!!.notifyItemRangeInserted(
                        lastDisplayPage + 1, outerHomeDisplayResponse.homeDisplayResponses.size)

                    lastDisplayPage = outerHomeDisplayResponse.homeDisplayResponses.size - 1

                    toggleProgressBar()
                }
            }
        })
    }

    @Throws(IOException::class)
    fun fetchUserInformation(userInformationRequest: UserInformationRequest) {
        val mapper = jacksonObjectMapper()
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

                val activityStackModel: ActivityStackModel =
                    mapper.readValue(sharedPreferences.getString(getString(R.string.activity_stack), "")!!)
                activityStackModel.activityStack.push(getString(R.string.activity_user_information))
                val activityStackString = mapper.writeValueAsString(activityStackModel)
                sharedPreferencesEditor.putString(getString(R.string.activity_stack), activityStackString)
                sharedPreferencesEditor.apply()

                runOnUiThread {
                    binding.userInformationLayout.visibility = View.GONE
                }

                val intent = Intent(baseContext, UserInformationActivity::class.java)
                intent.putExtra("jsonResponse", myResponse)
                startActivity(intent)
            }
        })
    }

    @Throws(IOException::class)
    fun fetchNotifications() {
        val mapper = jacksonObjectMapper()
        val userLikerRequest =
            UserLikerRequest(sharedPreferences.getInt(getString(R.string.member_id), 0))

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

                val activityStackModel: ActivityStackModel =
                    mapper.readValue(sharedPreferences.getString(getString(R.string.activity_stack), "")!!)
                activityStackModel.activityStack.push(getString(R.string.activity_message))
                val activityStackString = mapper.writeValueAsString(activityStackModel)
                sharedPreferencesEditor.putString(getString(R.string.activity_stack), activityStackString)
                sharedPreferencesEditor.apply()

                runOnUiThread {
                    binding.userInformationLayout.visibility = View.GONE
                }

                val intent = Intent(baseContext, NotificationActivity::class.java)
                intent.putExtra("jsonResponse", myResponse)
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

                val activityStackModel: ActivityStackModel =
                    mapper.readValue(sharedPreferences.getString(getString(R.string.activity_stack), "")!!)
                activityStackModel.activityStack.push(getString(R.string.activity_message))
                val activityStackString = mapper.writeValueAsString(activityStackModel)
                sharedPreferencesEditor.putString(getString(R.string.activity_stack), activityStackString)
                sharedPreferencesEditor.apply()

                runOnUiThread {
                    binding.userInformationLayout.visibility = View.GONE
                }

                val intent = Intent(baseContext, MessageActivity::class.java)
                intent.putExtra("profilePicture", messageRequest.profilePicture)
                intent.putExtra("lastActiveTime", messageRequest.lastActiveTime)
                intent.putExtra("receiverId", messageRequest.receiverId)
                intent.putExtra("userName", messageRequest.userName)
                intent.putExtra("senderId", messageRequest.senderId)
                intent.putExtra("fullName", messageRequest.fullName)
                intent.putExtra("jsonResponse", myResponse)
                startActivity(intent)
            }
        })
    }

    @Throws(IOException::class)
    fun fetchUserMessengers() {
        val mapper = jacksonObjectMapper()
        val userLikerRequest = UserLikerRequest(sharedPreferences.getInt(getString(R.string.member_id), 0))

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

                val activityStackModel: ActivityStackModel =
                    mapper.readValue(sharedPreferences.getString(getString(R.string.activity_stack), "")!!)
                activityStackModel.activityStack.push(getString(R.string.activity_messenger))
                val activityStackString = mapper.writeValueAsString(activityStackModel)
                sharedPreferencesEditor.putString(getString(R.string.activity_stack), activityStackString)
                sharedPreferencesEditor.apply()

                runOnUiThread {
                    binding.userInformationLayout.visibility = View.GONE
                }

                val intent = Intent(baseContext, MessengerActivity::class.java)
                intent.putExtra("jsonResponse", myResponse)
                startActivity(intent)
            }
        })
    }

    @Throws(IOException::class)
    fun fetchLikedUsers() {
        val mapper = jacksonObjectMapper()
        val userLikerRequest = UserLikerRequest(sharedPreferences.getInt(getString(R.string.member_id), 0))

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

                val activityStackModel: ActivityStackModel =
                    mapper.readValue(sharedPreferences.getString(getString(R.string.activity_stack), "")!!)
                activityStackModel.activityStack.push(getString(R.string.activity_user_profile))
                val activityStackString = mapper.writeValueAsString(activityStackModel)
                sharedPreferencesEditor.putString(getString(R.string.activity_stack), activityStackString)
                sharedPreferencesEditor.apply()

                val intent = Intent(baseContext, UserAccountActivity::class.java)
                intent.putExtra("jsonResponse", myResponse)
                startActivity(intent)
            }
        })
    }

    @Throws(IOException::class)
    fun fetchUserLikers() {
        val mapper = jacksonObjectMapper()
        val userLikerRequest = UserLikerRequest(sharedPreferences.getInt(getString(R.string.member_id), 0))

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

                val activityStackModel: ActivityStackModel =
                    mapper.readValue(sharedPreferences.getString(getString(R.string.activity_stack), "")!!)
                activityStackModel.activityStack.push(getString(R.string.activity_user_profile))
                val activityStackString = mapper.writeValueAsString(activityStackModel)
                sharedPreferencesEditor.putString(getString(R.string.activity_stack), activityStackString)
                sharedPreferencesEditor.apply()

                runOnUiThread {
                    binding.userInformationLayout.visibility = View.GONE
                }

                val intent = Intent(baseContext, UserProfileActivity::class.java)
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


