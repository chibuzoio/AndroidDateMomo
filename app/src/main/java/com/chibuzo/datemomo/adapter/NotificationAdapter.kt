package com.chibuzo.datemomo.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.chibuzo.datemomo.R
import com.chibuzo.datemomo.databinding.RecyclerNotificationBinding
import com.chibuzo.datemomo.model.AllLikersModel
import com.chibuzo.datemomo.model.response.NotificationResponse
import com.chibuzo.datemomo.utility.Utility

class NotificationAdapter(private var notificationResponses: ArrayList<NotificationResponse>,
                          private var allLikersModel: AllLikersModel) :
    RecyclerView.Adapter<NotificationAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = RecyclerNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val imageLayoutWidth = (allLikersModel.deviceWidth * 25) / 100;
        val informationLayoutWidth = allLikersModel.deviceWidth - imageLayoutWidth

        holder.binding.profilePictureLayout.layoutParams.width = imageLayoutWidth
        holder.binding.profilePictureLayout.layoutParams.height = imageLayoutWidth
        holder.binding.notifierInformationLayout.layoutParams.height = imageLayoutWidth
        holder.binding.notifierInformationLayout.layoutParams.width = informationLayoutWidth

        Glide.with(holder.itemView.context)
            .load(holder.itemView.context.getString(R.string.date_momo_api)
                    + holder.itemView.context.getString(R.string.api_image)
                    + notificationResponses[position].profilePicture)
            .transform(CircleCrop(), CenterCrop())
            .into(holder.binding.notifierProfilePicture)

        holder.binding.genericNotification.text =
            notificationResponses[position].genericNotification
        holder.binding.notificationDate.text =
            Utility.getTimeDifference(notificationResponses[position].notificationDate.toLong())
    }

    override fun getItemCount(): Int {
        return notificationResponses.size
    }

    class MyViewHolder(val binding: RecyclerNotificationBinding) :
        RecyclerView.ViewHolder(binding.root)
}


