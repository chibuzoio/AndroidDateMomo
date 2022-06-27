package com.example.datemomo.adapter

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.datemomo.R
import com.example.datemomo.databinding.RecyclerMessengerBinding
import com.example.datemomo.model.MessengerModel
import com.example.datemomo.model.request.DeleteMessageRequest
import com.example.datemomo.model.request.MessageRequest
import com.example.datemomo.model.response.MessengerResponse

class MessengerAdapter(private var messengerResponses: Array<MessengerResponse>, private val messengerModel: MessengerModel) :
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
        } else {
            holder.binding.userFullName.text = messengerResponses[position].fullName
        }

        holder.binding.lastMessage.text = messengerResponses[position].lastMessage
        holder.binding.messageStatusTime.text = messengerResponses[position].lastMessageDate
        holder.binding.messageStatusCounter.text = messengerResponses[position].unreadMessageCount.toString()

        holder.binding.messengerPropertyLayout.setOnLongClickListener {
            messengerModel.binding.messengerMenuLayout.visibility = View.VISIBLE
            messengerModel.currentPosition = position
            return@setOnLongClickListener true
        }

        messengerModel.binding.userInfoMenu.setOnClickListener {
            messengerModel.messengerActivity.fetchUserInformation(messengerResponses[messengerModel.currentPosition].chatmateId)
            messengerModel.binding.messengerMenuLayout.visibility = View.GONE
        }

        messengerModel.binding.deleteChatsMenu.setOnClickListener {
            /*
            * 0 = None deleted the message
            * 1 = Sender deleted the message
            * 2 = Receiver Deleted the message
            * 3 = Delete for everyone and can only be effected by the sender
            * */

            val deleteMessageRequest = DeleteMessageRequest(
                sharedPreferences.getInt(holder.itemView.context.getString(R.string.member_id), 0),
                messengerResponses[messengerModel.currentPosition].messageTableName)
            messengerModel.messengerActivity.deleteMessengerMessages(deleteMessageRequest)
            messengerModel.binding.messengerMenuLayout.visibility = View.GONE
            messengerResponses = removeAt(messengerModel.currentPosition)
            notifyItemRemoved(messengerModel.currentPosition)
        }

        messengerModel.binding.cancelMenu.setOnClickListener {
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
                messengerResponses[position].profilePicture
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


