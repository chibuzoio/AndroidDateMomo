package com.chibuzo.datemomo.adapter

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.chibuzo.datemomo.R
import com.chibuzo.datemomo.activity.UserExperienceActivity
import com.chibuzo.datemomo.databinding.RecyclerMessengerBinding
import com.chibuzo.datemomo.model.ActivityStackModel
import com.chibuzo.datemomo.model.MessengerModel
import com.chibuzo.datemomo.model.request.DeleteMessageRequest
import com.chibuzo.datemomo.model.request.MessageRequest
import com.chibuzo.datemomo.model.response.MessengerResponse
import com.chibuzo.datemomo.utility.Utility
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

class MessengerAdapter(private var messengerResponses: ArrayList<MessengerResponse>, private val messengerModel: MessengerModel) :
    RecyclerView.Adapter<MessengerAdapter.MyViewHolder>() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferencesEditor: SharedPreferences.Editor

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = RecyclerMessengerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        sharedPreferences =
            holder.itemView.context.getSharedPreferences(holder
                .itemView.context.getString(R.string.shared_preferences), Context.MODE_PRIVATE)
        sharedPreferencesEditor = sharedPreferences.edit()

        if (messengerResponses[position].fullName.isEmpty()) {
            messengerModel.binding.confirmMessengerDelete.doubleButtonMessage.text =
                "Delete chats with ${messengerResponses[position].userName.replaceFirstChar { it.uppercase() }}?"
        } else {
            messengerModel.binding.confirmMessengerDelete.doubleButtonMessage.text =
                "Delete chats with ${messengerResponses[position].fullName}?"
        }

        val messengerPropertyLeftPadding = holder.binding.messengerPropertyLayout.paddingLeft
        val imageLayoutHeight = ((messengerModel.deviceWidth - messengerPropertyLeftPadding) * 16) / 100
        val imageHeight = imageLayoutHeight - ((imageLayoutHeight * 5) / 100)

        holder.binding.messengerProfilePicture.layoutParams.width = imageHeight
        holder.binding.messengerProfilePicture.layoutParams.height = imageHeight
        holder.binding.profilePicturePlaceholder.layoutParams.width = imageHeight
        holder.binding.profilePicturePlaceholder.layoutParams.height = imageHeight
        holder.binding.messageStatusLayout.layoutParams.height = imageLayoutHeight
        holder.binding.profilePictureLayout.layoutParams.height = imageLayoutHeight
        holder.binding.messengerDetailsLayout.layoutParams.height = imageLayoutHeight

        Glide.with(holder.itemView.context)
            .load(holder.itemView.context.getString(R.string.date_momo_api)
                    + holder.itemView.context.getString(R.string.api_image)
                    + messengerResponses[position].profilePicture)
            .transform(CircleCrop(), CenterCrop())
            .into(holder.binding.messengerProfilePicture)

        if (messengerResponses[position].fullName == "") {
            holder.binding.userFullName.text = messengerResponses[position].userName
                .replaceFirstChar { it.uppercase() }
        } else {
            holder.binding.userFullName.text = messengerResponses[position].fullName
        }

        val decodedLastMessage = Utility.decodeEmoji(messengerResponses[position].lastMessage)

        holder.binding.lastMessage.text = if (decodedLastMessage!!.length > 35) {
            decodedLastMessage.substring(0, 35) + "..." } else { decodedLastMessage }

        holder.binding.messageStatusTime.text = Utility.getTimeDifference(messengerResponses[position].lastMessageDate.toLong())
        holder.binding.messageStatusCounter.text = messengerResponses[position].unreadMessageCount.toString()

        holder.binding.messageStatusCounter.visibility =
            if (messengerResponses[position].unreadMessageCount > 0) { View.VISIBLE } else { View.INVISIBLE }
        
        holder.binding.messengerPropertyLayout.setOnLongClickListener {
            messengerModel.binding.messengerMenuLayout.visibility = View.VISIBLE
            messengerModel.currentPosition = position
            return@setOnLongClickListener true
        }

        messengerModel.binding.messengerBlockUser.setOnClickListener {

        }

        messengerModel.binding.messengerReportUser.setOnClickListener {
            val mapper = jacksonObjectMapper()

            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

            val activityStackModel: ActivityStackModel =
                mapper.readValue(sharedPreferences.getString(holder.itemView.context.getString(R.string.activity_stack), "")!!)

            if (activityStackModel.activityStack.peek() != holder.itemView.context.getString(R.string.activity_user_experience)) {
                activityStackModel.activityStack.push(holder.itemView.context.getString(R.string.activity_user_experience))
                val activityStackString = mapper.writeValueAsString(activityStackModel)
                sharedPreferencesEditor.putString(
                    holder.itemView.context.getString(R.string.activity_stack),
                    activityStackString
                )
                sharedPreferencesEditor.apply()
            }

            Log.e(TAG, "The value of activityStackModel here is ${sharedPreferences.getString(holder.itemView.context.getString(R.string.activity_stack), "")}")

            val intent = Intent(holder.itemView.context, UserExperienceActivity::class.java)
            intent.putExtra("userBlockedStatus", messengerResponses[messengerModel.currentPosition].userBlockedStatus)
            intent.putExtra("profilePicture", messengerResponses[messengerModel.currentPosition].profilePicture)
            intent.putExtra("memberId", messengerResponses[messengerModel.currentPosition].chatmateId)
            intent.putExtra("userName", messengerResponses[messengerModel.currentPosition].userName)
            intent.putExtra("fullName", messengerResponses[messengerModel.currentPosition].fullName)
            intent.putExtra("lastActiveTime", "")
            holder.itemView.context.startActivity(intent)
        }

        messengerModel.binding.confirmMessengerDelete.doubleButtonLayout.setOnClickListener {
            messengerModel.binding.confirmMessengerDelete.doubleButtonLayout.visibility = View.GONE
        }

        messengerModel.binding.confirmMessengerDelete.dialogCancelButton.setOnClickListener {
            messengerModel.binding.confirmMessengerDelete.doubleButtonLayout.visibility = View.GONE
        }

        messengerModel.binding.confirmMessengerDelete.dialogRetryButton.setOnClickListener {
            messengerModel.binding.confirmMessengerDelete.doubleButtonLayout.visibility = View.GONE

            val deleteMessageRequest = DeleteMessageRequest(
                sharedPreferences.getInt(holder.itemView.context.getString(R.string.member_id), 0),
                messengerResponses[messengerModel.currentPosition].messageTableName)
            messengerModel.messengerActivity.deleteMessengerMessages(deleteMessageRequest)
            messengerModel.binding.messengerMenuLayout.visibility = View.GONE
            messengerResponses.removeAt(messengerModel.currentPosition)
            notifyItemRemoved(messengerModel.currentPosition)
        }

        messengerModel.binding.userInfoMenu.setOnClickListener {
            messengerModel.messengerActivity.fetchUserInformation(messengerResponses[messengerModel.currentPosition].chatmateId)
            messengerModel.requestProcess = holder.itemView.context.getString(R.string.request_fetch_user_information)
            messengerModel.binding.messengerMenuLayout.visibility = View.GONE
        }

        messengerModel.binding.deleteChatsMenu.setOnClickListener {
            messengerModel.binding.confirmMessengerDelete.doubleButtonLayout.visibility = View.VISIBLE
            messengerModel.binding.messengerMenuLayout.visibility = View.GONE
        }

        messengerModel.binding.cancelMenu.setOnClickListener {
            messengerModel.binding.messengerMenuLayout.visibility = View.GONE
        }

        messengerModel.binding.messengerMenuLayout.setOnClickListener {
            messengerModel.binding.messengerMenuLayout.visibility = View.GONE
        }

        holder.binding.messengerPropertyLayout.setOnClickListener {
            messengerModel.requestProcess = holder.itemView.context.getString(R.string.request_fetch_user_messages)

            val messageRequest = MessageRequest(
                sharedPreferences.getInt("memberId", 0),
                messengerResponses[position].chatmateId,
                messengerResponses[position].fullName,
                messengerResponses[position].userName,
                "",
                messengerResponses[position].profilePicture,
                0
            )

            messengerModel.messengerActivity.fetchUserMessages(messageRequest)
        }
    }

    override fun getItemCount(): Int {
        return messengerResponses.size
    }

    class MyViewHolder(val binding: RecyclerMessengerBinding) :
        RecyclerView.ViewHolder(binding.root)

    private fun removeAt(position: Int): Array<MessengerResponse> {
        val messengerResponseList = messengerResponses.toMutableList()
        messengerResponseList.removeAt(position)
        return messengerResponseList.toTypedArray()
    }

    companion object {
        const val TAG = "MessengerAdapter"
    }
}


