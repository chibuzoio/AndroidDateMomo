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
import com.chibuzo.datemomo.model.MessageModel
import com.chibuzo.datemomo.model.instance.ActivitySavedInstance
import com.chibuzo.datemomo.model.instance.MessageInstance
import com.chibuzo.datemomo.model.instance.MessengerInstance
import com.chibuzo.datemomo.model.request.*
import com.chibuzo.datemomo.model.response.CommittedResponse
import com.chibuzo.datemomo.model.response.HomeDisplayResponse
import com.chibuzo.datemomo.model.response.MessengerResponse
import com.chibuzo.datemomo.model.response.UserExperienceResponse
import com.chibuzo.datemomo.utility.Utility
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
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

        messageModel = MessageModel(
            senderId = 0,
            receiverId = 0,
            context = this,
            currentPosition = 0,
            binding = binding,
            messageActivity = this
        )

        binding.messengerBlockUser.setOnClickListener {
            binding.messengerMenuLayout.visibility = View.GONE

            val accusedUser = messageInstance.fullName.ifEmpty() {
                messageInstance.userName.replaceFirstChar { it.uppercase() } }

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

            val userExperienceResponse = UserExperienceResponse(
                memberId = messageInstance.receiverId,
                fullName = messageInstance.fullName,
                userName = messageInstance.userName,
                lastActiveTime = messageInstance.lastActiveTime,
                profilePicture = messageInstance.profilePicture,
                userBlockedStatus = messageInstance.userBlockedStatus)

            val activityStateData = mapper.writeValueAsString(userExperienceResponse)

            val activityInstanceModel: ActivityInstanceModel =
                mapper.readValue(sharedPreferences.getString(getString(R.string.activity_instance_model), "")!!)

            try {
                activitySavedInstance = ActivitySavedInstance(
                    activity = getString(R.string.activity_user_experience),
                    activityStateData = activityStateData)

                if (activityInstanceModel.activityInstanceStack.peek().activity != getString(
                        R.string.activity_user_experience
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

            val activitySavedInstanceString = mapper.writeValueAsString(activitySavedInstance)
            val intent = Intent(this@MessageActivity, UserExperienceActivity::class.java)
            intent.putExtra(getString(R.string.activity_saved_instance), activitySavedInstanceString)
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
                    + messageInstance.profilePicture)
            .transform(CircleCrop(), CenterCrop())
            .into(binding.emptyMessageProfilePicture)

        binding.receiverUserName.text = messageInstance.fullName.ifEmpty {
            messageInstance.userName.replaceFirstChar { it.uppercase() }
        }
        binding.lastActiveTime.text = messageInstance.lastActiveTime.ifEmpty {
            "online"
        }
        binding.userFullName.text = messageInstance.fullName.ifEmpty() {
            messageInstance.userName.replaceFirstChar { it.uppercase() }
        }

        Glide.with(this)
            .load(getString(R.string.date_momo_api)
                    + getString(R.string.api_image)
                    + messageInstance.profilePicture)
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
            messageInstance = mapper.readValue(activitySavedInstance.activityStateData)
            userBlockedStatus = messageInstance.userBlockedStatus

            if (userBlockedStatus > 0) {
                binding.messageEditorLayout.visibility = View.GONE
                binding.blockedUserNote.visibility = View.VISIBLE
                binding.userBlockingText.text = "Unblock User"
            } else {
                binding.messageEditorLayout.visibility = View.VISIBLE
                binding.blockedUserNote.visibility = View.GONE
                binding.userBlockingText.text = "Block User"
            }

            if (messageInstance.messageResponses.isEmpty()) {
                binding.welcomeMessageLayout.visibility = View.VISIBLE
            } else {
                binding.welcomeMessageLayout.visibility = View.GONE
            }

            val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
            binding.messageRecyclerView.layoutManager = layoutManager
            binding.messageRecyclerView.itemAnimator = DefaultItemAnimator()

            messageModel = MessageModel(messageInstance.senderId,
                messageInstance.receiverId, this,
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
    }

    private fun triggerRequestProcess() {
        when (requestProcess) {
            getString(R.string.request_fetch_user_information) ->fetchUserInformation()
            getString(R.string.request_fetch_user_messages) -> fetchUserMessages()
        }
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

                    updateMessageInstance(activityInstanceModel)

                    // Always do this below the method above, updateMessageInstance
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

                val activitySavedInstanceString = mapper.writeValueAsString(activitySavedInstance)
                val intent = Intent(this@MessageActivity, MessengerActivity::class.java)
                intent.putExtra(getString(R.string.activity_saved_instance), activitySavedInstanceString)
                startActivity(intent)
            }
        })
    }

    @Throws(IOException::class)
    fun blockAccusedUser() {
        val unixTime = System.currentTimeMillis() / 1000L

        val userBlockingRequest = UserBlockingRequest(
            userAccusedId = messageInstance.receiverId,
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
        val userInformationRequest = UserInformationRequest(messageInstance.receiverId)
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
                    updateMessageInstance(activityInstanceModel)

                    // Always do this below the method above, updateMessageInstance
                    activitySavedInstance = ActivitySavedInstance(
                        activity = getString(R.string.activity_user_information),
                        activityStateData = activityStateData)

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

                val activitySavedInstanceString = mapper.writeValueAsString(activitySavedInstance)
                val intent = Intent(this@MessageActivity, UserInformationActivity::class.java)
                intent.putExtra(getString(R.string.activity_saved_instance), activitySavedInstanceString)
                startActivity(intent)
            }
        })
    }

    @Throws(IOException::class)
    fun checkUnseenMessages() {
        val mapper = jacksonObjectMapper()
        val checkMessageRequest = CheckMessageRequest(
            senderId = messageInstance.senderId,
            receiverId = messageInstance.receiverId
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
            senderId = messageInstance.senderId,
            receiverId = messageInstance.receiverId,
            fullName = messageInstance.fullName,
            userName = messageInstance.userName,
            lastActiveTime = messageInstance.lastActiveTime,
            profilePicture = messageInstance.profilePicture,
            userBlockedStatus = messageInstance.userBlockedStatus
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

    private fun updateMessageInstance(activityInstanceModel: ActivityInstanceModel) {
        if (activityInstanceModel.activityInstanceStack.peek().activity == getString(R.string.activity_message)) {
            val scrollToPosition =
                (binding.messageRecyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            activityInstanceModel.activityInstanceStack.pop()

            messageInstance.scrollToPosition = scrollToPosition

            val mapper = jacksonObjectMapper()
            val activityStateData = mapper.writeValueAsString(messageInstance)

            activitySavedInstance = ActivitySavedInstance(
                activity = getString(R.string.activity_image_slider),
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


