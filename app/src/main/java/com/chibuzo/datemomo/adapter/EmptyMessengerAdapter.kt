package com.chibuzo.datemomo.adapter

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.chibuzo.datemomo.model.response.HomeDisplayResponse

class EmptyMessengerAdapter(private var homeDisplayResponses: ArrayList<HomeDisplayResponse>, private var allLikersModel: AllLikersModel) :
    RecyclerView.Adapter<EmptyMessengerAdapter.MyViewHolder>() {
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
            // Send waving hand icon directly to user

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
                profilePicture = homeDisplayResponses[position].profilePicture
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
        return homeDisplayResponses.size
    }

    class MyViewHolder(val binding: RecyclerEmptyMessengerBinding) :
        RecyclerView.ViewHolder(binding.root)
}


