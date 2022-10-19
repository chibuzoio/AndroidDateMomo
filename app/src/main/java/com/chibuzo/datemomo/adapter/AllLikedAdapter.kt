package com.chibuzo.datemomo.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.chibuzo.datemomo.R
import com.chibuzo.datemomo.activity.AllLikedActivity
import com.chibuzo.datemomo.databinding.RecyclerAllLikedBinding
import com.chibuzo.datemomo.model.AllLikersModel
import com.chibuzo.datemomo.model.request.UserInformationRequest
import com.chibuzo.datemomo.model.response.UserLikerResponse

class AllLikedAdapter(private var userLikerResponses: ArrayList<UserLikerResponse>, private var allLikersModel: AllLikersModel) :
    RecyclerView.Adapter<AllLikedAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = RecyclerAllLikedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val imageLayoutWidth = (allLikersModel.deviceWidth * 20) / 100;
        val informationLayoutWidth = allLikersModel.deviceWidth - imageLayoutWidth

        holder.binding.profilePictureLayout.layoutParams.width = imageLayoutWidth
        holder.binding.profilePictureLayout.layoutParams.height = imageLayoutWidth
        holder.binding.userInformationLayout.layoutParams.height = imageLayoutWidth
        holder.binding.userInformationLayout.layoutParams.width = informationLayoutWidth

        Glide.with(holder.itemView.context)
            .load(holder.itemView.context.getString(R.string.date_momo_api)
                    + holder.itemView.context.getString(R.string.api_image)
                    + userLikerResponses[position].profilePicture)
            .transform(CircleCrop(), CenterCrop())
            .into(holder.binding.likedProfilePicture)

        if (userLikerResponses[position].fullName.isEmpty()) {
            holder.binding.likedUserFullName.text =
                holder.itemView.context.getString(R.string.name_and_age_text,
                    userLikerResponses[position].userName.replaceFirstChar { it.uppercase() },
                    userLikerResponses[position].age)
        } else {
            holder.binding.likedUserFullName.text =
                holder.itemView.context.getString(R.string.name_and_age_text,
                    userLikerResponses[position].fullName, userLikerResponses[position].age)
        }

        holder.binding.likedCurrentLocation.text = userLikerResponses[position].currentLocation

        holder.binding.userLikedRecyclerLayout.setOnClickListener {
            allLikersModel.requestProcess =
                holder.itemView.context.getString(R.string.request_fetch_user_information)
            val userInformationRequest = UserInformationRequest(userLikerResponses[position].memberId)
            (allLikersModel.appCompatActivity as AllLikedActivity).fetchUserInformation(userInformationRequest)
        }
    }

    override fun getItemCount(): Int {
        return userLikerResponses.size
    }

    class MyViewHolder(val binding: RecyclerAllLikedBinding) :
        RecyclerView.ViewHolder(binding.root)
}


