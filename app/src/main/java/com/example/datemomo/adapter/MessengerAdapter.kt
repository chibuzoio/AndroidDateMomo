package com.example.datemomo.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.datemomo.R
import com.example.datemomo.activity.MessengerActivity
import com.example.datemomo.databinding.RecyclerMessengerBinding
import com.example.datemomo.model.MessengerModel
import com.example.datemomo.model.response.MessengerResponse

class MessengerAdapter(private val messengerResponses: Array<MessengerResponse>, private val messengerModel: MessengerModel) :
    RecyclerView.Adapter<MessengerAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessengerAdapter.MyViewHolder {
        val binding = RecyclerMessengerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessengerAdapter.MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessengerAdapter.MyViewHolder, position: Int) {
        val messengerPropertyLeftPadding = holder.binding.messengerPropertyLayout.paddingLeft
        val imageLayoutHeight = ((messengerModel.deviceWidth - messengerPropertyLeftPadding) * 20) / 100
        val imageHeight = imageLayoutHeight - ((imageLayoutHeight * 5) / 100)

        Log.e(MessengerActivity.TAG, "messengerPropertyLeftPadding value here is $messengerPropertyLeftPadding")

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
    }

    override fun getItemCount(): Int {
        return messengerResponses.size
    }

    class MyViewHolder(val binding: RecyclerMessengerBinding) :
        RecyclerView.ViewHolder(binding.root)
}


