package com.example.datemomo.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.datemomo.R
import com.example.datemomo.databinding.RecyclerAllLikersBinding
import com.example.datemomo.model.AllLikersModel
import com.example.datemomo.model.response.AllLikersResponse

class AllLikersAdapter(private var allLikersResponses: Array<AllLikersResponse>, private var allLikersModel: AllLikersModel) :
    RecyclerView.Adapter<AllLikersAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = RecyclerAllLikersBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val imageLayoutWidth = (allLikersModel.deviceWidth * 30) / 100;
        val informationLayoutWidth = allLikersModel.deviceWidth - imageLayoutWidth

        holder.binding.profilePictureLayout.layoutParams.width = imageLayoutWidth
        holder.binding.profilePictureLayout.layoutParams.height = imageLayoutWidth
        holder.binding.userInformationLayout.layoutParams.height = imageLayoutWidth
        holder.binding.userInformationLayout.layoutParams.width = informationLayoutWidth

        Glide.with(holder.itemView.context)
            .load(holder.itemView.context.getString(R.string.date_momo_api)
                    + holder.itemView.context.getString(R.string.api_image)
                    + allLikersResponses[position].profilePicture)
            .transform(CircleCrop(), CenterCrop())
            .into(holder.binding.likerProfilePicture)

        if (allLikersResponses[position].fullName.isEmpty()) {
            holder.binding.likerUserFullName.text =
                allLikersModel.context.getString(R.string.name_and_age_text,
                    allLikersResponses[position].fullName, allLikersResponses[position].age)
        } else {
            holder.binding.likerUserFullName.text =
                allLikersModel.context.getString(R.string.name_and_age_text,
                    allLikersResponses[position].userName, allLikersResponses[position].age)
        }

        holder.binding.likerCurrentLocation.text = allLikersResponses[position].currentLocation
    }

    override fun getItemCount(): Int {
        return allLikersResponses.size
    }

    class MyViewHolder(val binding: RecyclerAllLikersBinding) :
        RecyclerView.ViewHolder(binding.root)
    }


