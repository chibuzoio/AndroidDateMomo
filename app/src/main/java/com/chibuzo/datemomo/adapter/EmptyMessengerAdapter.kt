package com.chibuzo.datemomo.adapter

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.chibuzo.datemomo.R
import com.chibuzo.datemomo.activity.MessengerActivity
import com.chibuzo.datemomo.databinding.RecyclerEmptyMessengerBinding
import com.chibuzo.datemomo.model.AllLikersModel
import com.chibuzo.datemomo.model.request.MessageRequest
import com.chibuzo.datemomo.model.request.PostMessageRequest
import com.chibuzo.datemomo.model.response.HomeDisplayResponse
import com.chibuzo.datemomo.model.response.MessageResponse
import com.chibuzo.datemomo.model.response.PostMessageResponse
import com.chibuzo.datemomo.utility.Utility
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.*
import java.io.IOException

class EmptyMessengerAdapter(private var homeDisplayResponses: ArrayList<HomeDisplayResponse>, private var allLikersModel: AllLikersModel) :
    RecyclerView.Adapter<EmptyMessengerAdapter.MyViewHolder>() {
    private lateinit var buttonClickEffect: AlphaAnimation
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferencesEditor: SharedPreferences.Editor

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = RecyclerEmptyMessengerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        sharedPreferences =
            holder.itemView.context.getSharedPreferences(holder
                .itemView.context.getString(R.string.shared_preferences), Context.MODE_PRIVATE)
        sharedPreferencesEditor = sharedPreferences.edit()

        val imageLayoutWidth = (allLikersModel.deviceWidth * 25) / 100;
        val informationLayoutWidth = allLikersModel.deviceWidth - imageLayoutWidth

        holder.binding.profilePictureLayout.layoutParams.width = imageLayoutWidth
        holder.binding.profilePictureLayout.layoutParams.height = imageLayoutWidth
        holder.binding.userInformationLayout.layoutParams.height = imageLayoutWidth
        holder.binding.userInformationLayout.layoutParams.width = informationLayoutWidth

        holder.binding.wavingHandIconLayout.setOnClickListener {
            holder.binding.wavingHandIcon.startAnimation(buttonClickEffect)

            var senderMessage = holder.itemView.context.getString(R.string.sticker_anim_wave)
            senderMessage = Utility.encodeEmoji(senderMessage).toString()

            val messageResponse = MessageResponse(
                0, sharedPreferences.getInt(holder.itemView.context.getString(R.string.member_id), 0),
                senderMessage, 0, 0, 0, ""
            )

            val postMessageRequest = PostMessageRequest(
                sharedPreferences.getInt(holder.itemView.context.getString(R.string.member_id), 0),
                homeDisplayResponses[position].memberId, 0, senderMessage
            )

            val messageRequest = MessageRequest(
                senderId = sharedPreferences.getInt(holder.itemView.context.getString(R.string.member_id), 0),
                receiverId = homeDisplayResponses[position].memberId,
                fullName = homeDisplayResponses[position].fullName,
                userName = homeDisplayResponses[position].userName,
                lastActiveTime = "",
                profilePicture = homeDisplayResponses[position].profilePicture,
                userBlockedStatus = 0
            )

            postSenderMessage(holder.itemView.context, postMessageRequest, messageRequest)
        }

        holder.binding.userEmptyMessengerLayout.setOnClickListener {
            allLikersModel.requestProcess =
                holder.itemView.context.getString(R.string.request_fetch_user_messages)

            val messageRequest = MessageRequest(
                senderId = sharedPreferences.getInt(holder.itemView.context.getString(R.string.member_id), 0),
                receiverId = homeDisplayResponses[position].memberId,
                fullName = homeDisplayResponses[position].fullName,
                userName = homeDisplayResponses[position].userName,
                lastActiveTime = "",
                profilePicture = homeDisplayResponses[position].profilePicture,
                userBlockedStatus = 0
            )

            (allLikersModel.appCompatActivity as MessengerActivity).fetchUserMessages(messageRequest)
        }

        Glide.with(holder.itemView.context)
            .load(ContextCompat.getDrawable(holder.itemView.context, R.drawable.icon_waving_hand))
            .into(holder.binding.wavingHandIcon)

        Glide.with(holder.itemView.context)
            .load(holder.itemView.context.getString(R.string.date_momo_api)
                    + holder.itemView.context.getString(R.string.api_image)
                    + homeDisplayResponses[position].profilePicture)
            .transform(CircleCrop(), CenterCrop())
            .into(holder.binding.userProfilePicture)

        holder.binding.userFullName.text = if (homeDisplayResponses[position].fullName.isEmpty()) {
            holder.itemView.context.getString(R.string.name_and_age_text,
                homeDisplayResponses[position].userName.replaceFirstChar { it.uppercase() },
                homeDisplayResponses[position].age)
        } else {
            holder.itemView.context.getString(R.string.name_and_age_text,
                homeDisplayResponses[position].fullName, homeDisplayResponses[position].age)
        }

        holder.binding.userCurrentLocation.text =
            homeDisplayResponses[position].currentLocation.ifEmpty { "Location Not Set" }

        if (homeDisplayResponses[position].currentLocation.isEmpty()) {
            holder.binding.userCurrentLocation.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.edit_text_hint))
        } else {
            holder.binding.userCurrentLocation.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.edit_text))
        }

        val userSexualities = arrayListOf<String>()

        if (homeDisplayResponses[position].bisexualCategory > 0) {
            userSexualities.add("Bisexual")
        }

        if (homeDisplayResponses[position].gayCategory > 0) {
            userSexualities.add("Gay")
        }

        if (homeDisplayResponses[position].lesbianCategory > 0) {
            userSexualities.add("Lesbian")
        }

        if (homeDisplayResponses[position].straightCategory > 0) {
            userSexualities.add("Straight")
        }

        if (homeDisplayResponses[position].sugarDaddyCategory > 0) {
            userSexualities.add("Sugar Daddy")
        }

        if (homeDisplayResponses[position].sugarMommyCategory > 0) {
            userSexualities.add("Sugar Mommy")
        }

        if (homeDisplayResponses[position].toyBoyCategory > 0) {
            userSexualities.add("Toy Boy")
        }

        if (homeDisplayResponses[position].toyGirlCategory > 0) {
            userSexualities.add("Toy Girl")
        }

        val userSexualityAdapter = UserSexualityAdapter(userSexualities)
        val layoutManager = LinearLayoutManager(holder.itemView.context, RecyclerView.HORIZONTAL, false)
        holder.binding.userSexualityRecyclerView.layoutManager = layoutManager
        holder.binding.userSexualityRecyclerView.itemAnimator = DefaultItemAnimator()
        holder.binding.userSexualityRecyclerView.adapter = userSexualityAdapter
    }

    override fun getItemCount(): Int {
        buttonClickEffect = AlphaAnimation(1f, 0f)
        return homeDisplayResponses.size
    }

    class MyViewHolder(val binding: RecyclerEmptyMessengerBinding) :
        RecyclerView.ViewHolder(binding.root)

    @Throws(IOException::class)
    private fun postSenderMessage(context: Context, postMessageRequest: PostMessageRequest,
                                  messageRequest: MessageRequest) {
        val mapper = jacksonObjectMapper()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        val jsonObjectString = mapper.writeValueAsString(postMessageRequest)
        val requestBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            jsonObjectString
        )

        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(context.getString(R.string.date_momo_api) + context.getString(R.string.api_post_message))
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()
            }

            override fun onResponse(call: Call, response: Response) {
                val myResponse: String = response.body()!!.string()

                allLikersModel.requestProcess =
                    context.getString(R.string.request_fetch_user_messages)

                (allLikersModel.appCompatActivity as MessengerActivity).fetchUserMessages(messageRequest)
            }
        })
    }
}


