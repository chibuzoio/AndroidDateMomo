package com.example.datemomo.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.datemomo.R
import com.example.datemomo.databinding.RecyclerHomeDisplayBinding
import com.example.datemomo.model.response.HomeDisplayResponse
import com.example.datemomo.utility.Utility

class HomeDisplayAdapter(private val homeDisplayResponses: Array<HomeDisplayResponse>, private val deviceWidth: Int) :
    RecyclerView.Adapter<HomeDisplayAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = RecyclerHomeDisplayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val allImageWidth = deviceWidth - Utility.dimen(holder.itemView.context, 23f)
        val allImageHeight = (/* imageHeight */ 788 * (deviceWidth -
                Utility.dimen(holder.itemView.context, 23f))) / /* imageWidth */ 788

        holder.binding.userImage.layoutParams.width = allImageWidth
        holder.binding.userImage.layoutParams.height = allImageHeight
        holder.binding.userImageBack.layoutParams.width = allImageWidth
        holder.binding.userImageBack.layoutParams.height = allImageHeight
        holder.binding.userImageLayout.layoutParams.width = allImageWidth
        holder.binding.userImageLayout.layoutParams.height = allImageHeight

        Glide.with(holder.itemView.context)
            .asGif()
            .load(R.drawable.motion_placeholder)
            .transform(RoundedCorners(15))
            .into(holder.binding.userImageBack)

/*
        Glide.with(holder.itemView.context)
            .load(ContextCompat.getDrawable(holder.itemView.context, homeDisplayImages[position].imageId))
            .thumbnail(Glide.with(holder.itemView.context).load(R.drawable.motion_placeholder))
            .transform(FitCenter(), RoundedCorners(33))
            .into(holder.binding.userImage);
*/

        if (position == (itemCount - 1)) {
            val userDisplayLayoutParam = holder.binding.userDisplayLayout.layoutParams as ViewGroup.MarginLayoutParams
            userDisplayLayoutParam.bottomMargin = 25;
            holder.binding.userDisplayLayout.layoutParams = userDisplayLayoutParam
        }

        if (homeDisplayResponses[position].liked == 1) {
            holder.binding.loveUserIcon.setImageDrawable(
                ContextCompat.getDrawable(
                    holder.itemView.context,
                    R.drawable.icon_heart_red
                )
            )
        } else {
            holder.binding.loveUserIcon.setImageDrawable(
                ContextCompat.getDrawable(
                    holder.itemView.context,
                    R.drawable.icon_heart_hollow
                )
            )
        }

        holder.binding.loveUserLayout.setOnClickListener {
            if (homeDisplayResponses[position].liked == 0) {
                holder.binding.loveUserIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        holder.itemView.context,
                        R.drawable.icon_heart_red
                    )
                )

                // commit it to the server
            } else {
                holder.binding.loveUserIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        holder.itemView.context,
                        R.drawable.icon_heart_hollow
                    )
                )

                // commit it to the server
            }
        }

        Glide.with(holder.itemView.context)
            .load(holder.itemView.context.getString(R.string.date_momo_api)
                    + "client/image/" + homeDisplayResponses[position].profilePicture)
            .transform(CenterCrop(), RoundedCorners(33))
            .into(holder.binding.userImage)

        if (homeDisplayResponses[position].fullName.isEmpty()) {
            holder.binding.userFullName.text = homeDisplayResponses[position].userName
        } else {
            holder.binding.userFullName.text = homeDisplayResponses[position].fullName
        }
    }

    override fun getItemCount(): Int {
        return homeDisplayResponses.size
    }

    class MyViewHolder(val binding: RecyclerHomeDisplayBinding) :
        RecyclerView.ViewHolder(binding.root)

    companion object {
        const val TAG = "HomeDisplayAdapter"
    }
}


