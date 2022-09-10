package com.chibuzo.datemomo.adapter

import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.chibuzo.datemomo.R
import com.chibuzo.datemomo.activity.NotificationActivity
import com.chibuzo.datemomo.databinding.RecyclerNotificationBinding
import com.chibuzo.datemomo.model.AllLikersModel
import com.chibuzo.datemomo.model.request.ReadStatusRequest
import com.chibuzo.datemomo.model.request.UserInformationRequest
import com.chibuzo.datemomo.model.response.NotificationResponse
import com.chibuzo.datemomo.utility.Utility

class NotificationAdapter(private var notificationResponses: ArrayList<NotificationResponse>,
                          private var allLikersModel: AllLikersModel) :
    RecyclerView.Adapter<NotificationAdapter.MyViewHolder>() {
    private val buttonClickEffect = AlphaAnimation(1f, 0f)
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferencesEditor: SharedPreferences.Editor

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = RecyclerNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        sharedPreferences =
            holder.itemView.context.getSharedPreferences(holder
                .itemView.context.getString(R.string.shared_preferences), Context.MODE_PRIVATE)
        sharedPreferencesEditor = sharedPreferences.edit()

        val imageLayoutWidth = (allLikersModel.deviceWidth * 23) / 100
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

        if (notificationResponses[position].readStatus < 1) {
            holder.binding.genericNotificationLayout.background =
                ContextCompat.getDrawable(holder.itemView.context, R.color.sky_blue)
        } else {
            holder.binding.genericNotificationLayout.background =
                ContextCompat.getDrawable(holder.itemView.context, R.color.white)
        }

        var notificationString = notificationResponses[position].genericNotification
        notificationString = notificationString.replace("{", "<b>")
        notificationString = notificationString.replace("}", "</b>")

        val notificationHtml = HtmlCompat.fromHtml(notificationString, HtmlCompat.FROM_HTML_MODE_LEGACY)

        holder.binding.genericNotification.text = notificationHtml
        holder.binding.notificationDate.text =
            Utility.getTimeDifference(notificationResponses[position].notificationDate.toLong())

        holder.binding.genericNotificationLayout.setOnClickListener {
            holder.binding.genericNotificationLayout.startAnimation(buttonClickEffect)

            val readStatusRequest = ReadStatusRequest(
                memberId = sharedPreferences.getInt(holder.itemView.context.getString(R.string.member_id), 0),
                notificationId = notificationResponses[position].notificationId,
                notificationPosition = position
            )

            (allLikersModel.appCompatActivity as NotificationActivity).updateNotificationReadStatus(readStatusRequest)

            allLikersModel.requestProcess = holder.itemView.context.getString(R.string.request_fetch_user_information)
            val userInformationRequest = UserInformationRequest(notificationResponses[position].notificationEffectorId)
            (allLikersModel.appCompatActivity as NotificationActivity).fetchUserInformation(userInformationRequest)
        }
    }

    override fun getItemCount(): Int {
        return notificationResponses.size
    }

    class MyViewHolder(val binding: RecyclerNotificationBinding) :
        RecyclerView.ViewHolder(binding.root)
}


