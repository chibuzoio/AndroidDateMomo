package com.chibuzo.datemomo.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.chibuzo.datemomo.R
import com.chibuzo.datemomo.adapter.MessageAdapter
import com.chibuzo.datemomo.databinding.ActivityMessageBinding
import com.chibuzo.datemomo.model.ActivityInstanceModel
import com.chibuzo.datemomo.model.ActivityStackModel
import com.chibuzo.datemomo.model.MessageModel
import com.chibuzo.datemomo.model.instance.ActivitySavedInstance
import com.chibuzo.datemomo.model.instance.MessageInstance
import com.chibuzo.datemomo.model.request.*
import com.chibuzo.datemomo.model.response.CommittedResponse
import com.chibuzo.datemomo.utility.Utility
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.*
import java.io.IOException
import java.util.*

class MessageActivity : AppCompatActivity() {
    private lateinit var bundle: Bundle
    private var userBlockedStatus: Int = 0
    private var requestProcess: String = ""
    private var leastRootViewHeight: Int = 0
    private var originalRequestProcess: String = ""
    private lateinit var messageModel: MessageModel
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var binding: ActivityMessageBinding
    private lateinit var messageInstance: MessageInstance
    private lateinit var buttonClickEffect: AlphaAnimation
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var viewRootHeightArray: MutableSet<Int>
    private lateinit var activitySavedInstance: ActivitySavedInstance
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

        userBlockedStatus = bundle.getInt("userBlockedStatus")

        messageModel = MessageModel(
            senderId = 0,
            receiverId = 0,
            context = this,
            currentPosition = 0,
            binding = binding,
            messageActivity = this
        )

        if (userBlockedStatus > 0) {
            binding.messageEditorLayout.visibility = View.GONE
            binding.blockedUserNote.visibility = View.VISIBLE
            binding.userBlockingText.text = "Unblock User"
        } else {
            binding.messageEditorLayout.visibility = View.VISIBLE
            binding.blockedUserNote.visibility = View.GONE
            binding.userBlockingText.text = "Block User"
        }

        binding.messengerBlockUser.setOnClickListener {
            binding.messengerMenuLayout.visibility = View.GONE

            val accusedUser = bundle.getString("fullName")!!.ifEmpty() {
                bundle.getString("userName")!!.replaceFirstChar { it.uppercase() } }

            if (userBlockedStatus > 0) {
                binding.doubleButtonDialog.dialogRetryButton.text = "Unblock"
                binding.doubleButtonDialog.dialogCancelButton.text = "Cancel"
                binding.doubleButtonDialog.doubleButtonMessage.text =
                    "Do you want to unblock $accusedUser?"
                binding.doubleButtonDialog.dialogRetryButton.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.blue
                    )
                )
            } else {
                binding.doubleButtonDialog.dialogRetryButton.text = "Block"
                binding.doubleButtonDialog.dialogCancelButton.text = "Cancel"
                binding.doubleButtonDialog.doubleButtonMessage.text =
                    "Do you want to block $accusedUser?"
                binding.doubleButtonDialog.dialogRetryButton.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.red
                    )
                )
            }

            binding.doubleButtonDialog.dialogCancelButton.setTextColor(ContextCompat.getColor(this, R.color.blue))
            binding.doubleButtonDialog.doubleButtonLayout.visibility = View.VISIBLE
        }

        binding.messengerReportUser.setOnClickListener {
            val mapper = jacksonObjectMapper()

            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

            val activityStackModel: ActivityStackModel =
                mapper.readValue(sharedPreferences.getString(getString(R.string.activity_stack), "")!!)

            if (activityStackModel.activityStack.peek() != getString(R.string.activity_user_experience)) {
                activityStackModel.activityStack.push(getString(R.string.activity_user_experience))
                val activityStackString = mapper.writeValueAsString(activityStackModel)
                sharedPreferencesEditor.putString(
                    getString(R.string.activity_stack),
                    activityStackString
                )
                sharedPreferencesEditor.apply()
            }

            Log.e(TAG, "The value of activityStackModel here is ${sharedPreferences.getString(getString(R.string.activity_stack), "")}")

            val intent = Intent(this, UserExperienceActivity::class.java)
            intent.putExtra("profilePicture", bundle.getString("profilePicture"))
            intent.putExtra("lastActiveTime", bundle.getString("lastActiveTime"))
            intent.putExtra("userName", bundle.getString("userName"))
            intent.putExtra("fullName", bundle.getString("fullName"))
            intent.putExtra("memberId", bundle.getInt("receiverId"))
            intent.putExtra("userBlockedStatus", userBlockedStatus)
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
            } else {
                blockAccusedUser()
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

        binding.messengerMenuCancel.setOnClickListener {
            binding.deleteForEveryoneMenu.visibility = View.VISIBLE
            binding.messengerMenuLayout.visibility = View.GONE
            binding.messageEditMenu.visibility = View.VISIBLE
            binding.messageMenuLayout.visibility = View.GONE
        }

        binding.messageMenuCancel.setOnClickListener {
            binding.deleteForEveryoneMenu.visibility = View.VISIBLE
            binding.messengerMenuLayout.visibility = View.GONE
            binding.messageEditMenu.visibility = View.VISIBLE
            binding.messageMenuLayout.visibility = View.GONE
        }

        binding.messageMenuLayout.setOnClickListener {
            binding.deleteForEveryoneMenu.visibility = View.VISIBLE
            binding.messengerMenuLayout.visibility = View.GONE
            binding.messageEditMenu.visibility = View.VISIBLE
            binding.messageMenuLayout.visibility = View.GONE
        }

        binding.messengerMenuLayout.setOnClickListener {
            binding.deleteForEveryoneMenu.visibility = View.VISIBLE
            binding.messengerMenuLayout.visibility = View.GONE
            binding.messageEditMenu.visibility = View.VISIBLE
            binding.messageMenuLayout.visibility = View.GONE
        }

        binding.messageMenuIcon.setOnClickListener {
            binding.deleteForEveryoneMenu.visibility = View.VISIBLE
            binding.messageEditMenu.visibility = View.VISIBLE

            if (binding.messengerMenuLayout.isVisible) {
                binding.messengerMenuLayout.visibility = View.GONE
                binding.messageMenuLayout.visibility = View.GONE
            } else {
                binding.messengerMenuLayout.visibility = View.VISIBLE
                binding.messageMenuLayout.visibility = View.GONE
            }
        }

        binding.receiverUserName.setOnClickListener {
            requestProcess = getString(R.string.request_fetch_user_information)
            fetchUserInformation()
        }

        binding.profilePictureLayout.setOnClickListener {
            requestProcess = getString(R.string.request_fetch_user_information)
            fetchUserInformation()
        }

        binding.backArrowLayout.setOnClickListener {

        }

        Glide.with(this)
            .asGif()
            .load(R.drawable.anime_waving_hand)
            .into(binding.wavingHandSenderAnime)

        Glide.with(this)
            .load(getString(R.string.date_momo_api) + getString(R.string.api_image)
                    + bundle.getString("profilePicture"))
            .transform(CircleCrop(), CenterCrop())
            .into(binding.emptyMessageProfilePicture)

        binding.receiverUserName.text = bundle.getString("fullName")!!.ifEmpty {
            bundle.getString("userName").toString().replaceFirstChar { it.uppercase() }
        }
        binding.lastActiveTime.text = bundle.getString("lastActiveTime")!!.ifEmpty {
            "online"
        }
        binding.userFullName.text = bundle.getString("fullName")!!.ifEmpty() {
            bundle.getString("userName")!!.replaceFirstChar { it.uppercase() }
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

            checkUnseenMessages()
        }

        binding.messageInputField.setOnFocusChangeListener { _, focused ->
            if (focused) {
                showSystemUI()
            } else {
                hideSystemUI()
            }

            checkUnseenMessages()
        }

        try {
            val mapper = jacksonObjectMapper()
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            activitySavedInstance = mapper.readValue(bundle.getString(getString(R.string.activity_saved_instance))!!)

            val activityStateData = activitySavedInstance.activityStateData
            messageInstance = mapper.readValue(activityStateData)

            if (messageInstance.messageResponses.isEmpty()) {
                binding.welcomeMessageLayout.visibility = View.VISIBLE
            } else {
                binding.welcomeMessageLayout.visibility = View.GONE
            }

            val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
            binding.messageRecyclerView.layoutManager = layoutManager
            binding.messageRecyclerView.itemAnimator = DefaultItemAnimator()

            messageModel = MessageModel(bundle.getInt("senderId"),
                bundle.getInt("receiverId"), this,
                0, binding, this)

            messageAdapter = MessageAdapter(messageInstance.messageResponses, messageModel)
            binding.messageRecyclerView.adapter = messageAdapter

            (binding.messageRecyclerView.layoutManager as LinearLayoutManager).scrollToPosition(messageInstance.messageResponses.size - 1)
        } catch (exception: IOException) {
            exception.printStackTrace()
            Log.e(TAG, "Error message from here is ${exception.message}")
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return super.onKeyDown(keyCode, event)
    }

    override fun onStart() {
        super.onStart()
        hideSystemUI()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (binding.messageInputField.isFocused) {
            binding.messageInputField.clearFocus()
        }

        val mapper = jacksonObjectMapper()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        val activityInstanceModel: ActivityInstanceModel =
            mapper.readValue(sharedPreferences.getString(getString(R.string.activity_instance_model), "")!!)

        try {
            when (activityInstanceModel.activityInstanceStack.peek().activity) {
                getString(R.string.activity_message) -> {
                    activityInstanceModel.activityInstanceStack.pop()

                    val activityInstanceModelString = mapper.writeValueAsString(activityInstanceModel)
                    sharedPreferencesEditor.putString(getString(R.string.activity_instance_model), activityInstanceModelString)
                    sharedPreferencesEditor.apply()

                    this.onBackPressed()
                }
                else -> {
                    requestProcess = getString(R.string.request_fetch_user_messengers)
                    fetchUserMessengers()
                }
            }
        } catch (exception: EmptyStackException) {
            exception.printStackTrace()
            Log.e(TAG, "Exception from trying to peek activityStack here is ${exception.message}")
        }

        Log.e(TAG, "The value of activityStackModel here is ${sharedPreferences.getString(getString(R.string.activity_stack), "")}")
    }

    private fun triggerRequestProcess() {
        when (requestProcess) {
            getString(R.string.request_fetch_user_information) ->fetchUserInformation()
            getString(R.string.request_fetch_user_messengers) -> fetchUserMessengers()
            getString(R.string.request_fetch_matched_users) -> fetchMatchedUsers()
            getString(R.string.request_fetch_user_messages) -> fetchUserMessages()
        }
    }

    @Throws(IOException::class)
    fun blockAccusedUser() {
        val unixTime = System.currentTimeMillis() / 1000L

        val userBlockingRequest = UserBlockingRequest(
            userAccusedId = bundle.getInt("receiverId"),
            userBlockerId = sharedPreferences.getInt(getString(R.string.member_id), 0),
            userBlockedStatus = if (userBlockedStatus > 0) { 0 } else { 1 },
            userBlockedDate = unixTime.toString()
        )

        val mapper = jacksonObjectMapper()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        val jsonObjectString = mapper.writeValueAsString(userBlockingRequest)
        val requestBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            jsonObjectString
        )

        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(getString(R.string.date_momo_api) + getString(R.string.api_block_accused_user))
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()
            }

            override fun onResponse(call: Call, response: Response) {
                val myResponse: String = response.body()!!.string()
                val committedResponse: CommittedResponse = mapper.readValue(myResponse)

                if (committedResponse.committed) {
                    userBlockedStatus = userBlockingRequest.userBlockedStatus

                    runOnUiThread {
                        if (userBlockingRequest.userBlockedStatus > 0) {
                            binding.messageEditorLayout.visibility = View.GONE
                            binding.blockedUserNote.visibility = View.VISIBLE
                            binding.userBlockingText.text = "Unblock User"
                        } else {
                            binding.messageEditorLayout.visibility = View.VISIBLE
                            binding.blockedUserNote.visibility = View.GONE
                            binding.userBlockingText.text = "Block User"
                        }
                    }
                }
            }
        })
    }

    @Throws(IOException::class)
    fun fetchUserInformation() {
        val mapper = jacksonObjectMapper()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        val userInformationRequest = UserInformationRequest(bundle.getInt("receiverId"))
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

                if (activityStackModel.activityStack.peek() != getString(R.string.activity_user_information)) {
                    activityStackModel.activityStack.push(getString(R.string.activity_user_information))
                    val activityStackString = mapper.writeValueAsString(activityStackModel)
                    sharedPreferencesEditor.putString(
                        getString(R.string.activity_stack),
                        activityStackString
                    )
                    sharedPreferencesEditor.apply()
                }

                Log.e(TAG, "The value of activityStackModel here is ${sharedPreferences.getString(getString(R.string.activity_stack), "")}")

                val intent = Intent(baseContext, UserInformationActivity::class.java)
                intent.putExtra("jsonResponse", myResponse)
                startActivity(intent)
            }
        })
    }

    @Throws(IOException::class)
    fun checkUnseenMessages() {
        val mapper = jacksonObjectMapper()
        val checkMessageRequest = CheckMessageRequest(
            senderId = bundle.getInt("senderId"),
            receiverId = bundle.getInt("receiverId")
        )

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        val jsonObjectString = mapper.writeValueAsString(checkMessageRequest)
        val requestBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            jsonObjectString
        )

        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(getString(R.string.date_momo_api) + getString(R.string.api_check_unseen_message))
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()
            }

            override fun onResponse(call: Call, response: Response) {
                val myResponse: String = response.body()!!.string()
                val committedResponse = mapper.readValue(myResponse) as CommittedResponse

                if (committedResponse.committed) {
                    requestProcess = getString(R.string.request_fetch_user_messages)
                    fetchUserMessages()
                }
            }
        })
    }

    @Throws(IOException::class)
    fun fetchUserMessages() {
        val mapper = jacksonObjectMapper()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        val messageRequest = MessageRequest(
            senderId = bundle.getInt("senderId"),
            receiverId = bundle.getInt("receiverId"),
            fullName = bundle.getString("fullName").toString(),
            userName = bundle.getString("userName").toString(),
            lastActiveTime = bundle.getString("lastActiveTime").toString(),
            profilePicture = bundle.getString("profilePicture").toString(),
            userBlockedStatus = 0
        )

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
            }

            override fun onResponse(call: Call, response: Response) {
                val myResponse: String = response.body()!!.string()

                try {
                    messageInstance.messageResponses = mapper.readValue(myResponse)

                    if (messageInstance.messageResponses.size > 0) {
                        runOnUiThread {
                            messageAdapter = MessageAdapter(messageInstance.messageResponses, messageModel)
                            binding.messageRecyclerView.swapAdapter(messageAdapter, true)
                            (binding.messageRecyclerView.layoutManager as LinearLayoutManager).scrollToPosition(
                                messageInstance.messageResponses.size - 1
                            )
                        }
                    }
                } catch (exception: IOException) {
                    exception.printStackTrace()
                    Log.e(TAG, "Error message from here is ${exception.message}")
                }
            }
        })
    }

    @Throws(IOException::class)
    fun editSingleMessage(editMessageRequest: EditMessageRequest) {
        val mapper = jacksonObjectMapper()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        val jsonObjectString = mapper.writeValueAsString(editMessageRequest)
        val requestBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            jsonObjectString
        )

        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(getString(R.string.date_momo_api) + getString(R.string.api_edit_message))
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()
            }

            override fun onResponse(call: Call, response: Response) {
                val myResponse: String = response.body()!!.string()
                val committedResponse: CommittedResponse = mapper.readValue(myResponse)
            }
        })
    }

    @Throws(IOException::class)
    fun deleteSingleMessage(deleteChatRequest: DeleteChatRequest) {
        val mapper = jacksonObjectMapper()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        val jsonObjectString = mapper.writeValueAsString(deleteChatRequest)
        val requestBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            jsonObjectString
        )

        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(getString(R.string.date_momo_api) + getString(R.string.api_delete_message))
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()
            }

            override fun onResponse(call: Call, response: Response) {
                val myResponse: String = response.body()!!.string()
                val committedResponse: CommittedResponse = mapper.readValue(myResponse)
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

                if (activityStackModel.activityStack.peek() != getString(R.string.activity_messenger)) {
                    activityStackModel.activityStack.push(getString(R.string.activity_messenger))
                    val activityStackString = mapper.writeValueAsString(activityStackModel)
                    sharedPreferencesEditor.putString(
                        getString(R.string.activity_stack),
                        activityStackString
                    )
                    sharedPreferencesEditor.apply()
                }

                Log.e(TAG, "The value of activityStackModel here is ${sharedPreferences.getString(getString(R.string.activity_stack), "")}")

                val intent = Intent(baseContext, MessengerActivity::class.java)
                intent.putExtra("jsonResponse", myResponse)
                startActivity(intent)
            }
        })
    }

    @Throws(IOException::class)
    fun fetchMatchedUsers() {
        val mapper = jacksonObjectMapper()
        val homeDisplayRequest = OuterHomeDisplayRequest(
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
            sharedPreferences.getInt(getString(R.string.friendship_interest), 0),
            sharedPreferences.getInt(getString(R.string.sugar_daddy_interest), 0),
            sharedPreferences.getInt(getString(R.string.sugar_mommy_interest), 0),
            sharedPreferences.getInt(getString(R.string.relationship_interest), 0),
            sharedPreferences.getInt(getString(R.string.toy_boy_interest), 0),
            sharedPreferences.getInt(getString(R.string.toy_girl_interest), 0),
            sharedPreferences.getInt(getString(R.string.sixty_nine_experience), 0),
            sharedPreferences.getInt(getString(R.string.anal_sex_experience), 0),
            sharedPreferences.getInt(getString(R.string.given_head_experience), 0),
            sharedPreferences.getInt(getString(R.string.missionary_experience), 0),
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

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

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

                if (activityStackModel.activityStack.peek() != getString(R.string.activity_home_display)) {
                    activityStackModel.activityStack.push(getString(R.string.activity_home_display))
                    val activityStackString = mapper.writeValueAsString(activityStackModel)
                    sharedPreferencesEditor.putString(
                        getString(R.string.activity_stack),
                        activityStackString
                    )
                    sharedPreferencesEditor.apply()
                }

                Log.e(TAG, "The value of activityStackModel here is ${sharedPreferences.getString(getString(R.string.activity_stack), "")}")

                val intent = Intent(baseContext, HomeDisplayActivity::class.java)
                intent.putExtra("jsonResponse", myResponse)
                startActivity(intent)
            }
        })
    }

    fun displayDoubleButtonDialog() {
        runOnUiThread {
            binding.doubleButtonDialog.dialogRetryButton.text = "Retry"
            binding.doubleButtonDialog.dialogCancelButton.text = "Cancel"
            binding.doubleButtonDialog.doubleButtonTitle.text = getString(R.string.network_error_title)
            binding.doubleButtonDialog.doubleButtonMessage.text = getString(R.string.network_error_message)
            binding.doubleButtonDialog.dialogRetryButton.setTextColor(ContextCompat.getColor(this, R.color.blue))
            binding.doubleButtonDialog.dialogCancelButton.setTextColor(ContextCompat.getColor(this, R.color.red))
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


