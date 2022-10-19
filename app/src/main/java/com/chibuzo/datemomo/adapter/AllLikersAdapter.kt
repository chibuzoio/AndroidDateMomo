package com.chibuzo.datemomo.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.chibuzo.datemomo.R
import com.chibuzo.datemomo.activity.AllLikersActivity
import com.chibuzo.datemomo.databinding.RecyclerAllLikersBinding
import com.chibuzo.datemomo.model.AllLikersModel
import com.chibuzo.datemomo.model.request.UserInformationRequest
import com.chibuzo.datemomo.model.response.UserLikerResponse

class AllLikersAdapter(private var userLikerResponses: ArrayList<UserLikerResponse>, private var allLikersModel: AllLikersModel) :
    RecyclerView.Adapter<AllLikersAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = RecyclerAllLikersBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
            .into(holder.binding.likerProfilePicture)

/*
        val imageId = when (userLikerResponses[position].profilePicture) {
            "image1.jpg" -> R.drawable.image1
            "image2.jpg" -> R.drawable.image2
            "image3.jpg" -> R.drawable.image3
            "image4.jpg" -> R.drawable.image4
            "image5.jpg" -> R.drawable.image5
            "image6.jpg" -> R.drawable.image6
            else -> R.drawable.image1
        }

        Glide.with(holder.itemView.context)
            .load(ContextCompat.getDrawable(holder.itemView.context, imageId))
            .transform(CircleCrop(), CenterCrop())
            .into(holder.binding.likerProfilePicture)
*/

        if (userLikerResponses[position].fullName.isEmpty()) {
            holder.binding.likerUserFullName.text =
                holder.itemView.context.getString(R.string.name_and_age_text,
                    userLikerResponses[position].userName.replaceFirstChar { it.uppercase() },
                    userLikerResponses[position].age)
        } else {
            holder.binding.likerUserFullName.text =
                holder.itemView.context.getString(R.string.name_and_age_text,
                    userLikerResponses[position].fullName, userLikerResponses[position].age)
        }

        holder.binding.likerCurrentLocation.text = userLikerResponses[position].currentLocation

        holder.binding.userLikerRecyclerLayout.setOnClickListener {
            allLikersModel.requestProcess =
                holder.itemView.context.getString(R.string.request_fetch_user_information)
            val userInformationRequest = UserInformationRequest(userLikerResponses[position].memberId)
            (allLikersModel.appCompatActivity as AllLikersActivity).fetchUserInformation(userInformationRequest)
        }
    }

    override fun getItemCount(): Int {
        return userLikerResponses.size
    }

    class MyViewHolder(val binding: RecyclerAllLikersBinding) :
        RecyclerView.ViewHolder(binding.root)
    }


