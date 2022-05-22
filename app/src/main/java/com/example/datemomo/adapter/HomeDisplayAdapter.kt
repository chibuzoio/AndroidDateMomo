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

class HomeDisplayAdapter(private val homeDisplayImages: ArrayList<HomeDisplayResponse>) :
    RecyclerView.Adapter<HomeDisplayAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = RecyclerHomeDisplayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.binding.userImage.layoutParams.height = 676
        holder.binding.userImageBack.layoutParams.height = 676
        holder.binding.userImageLayout.layoutParams.height = 676

        Glide.with(holder.itemView.context)
            .asGif()
            .load(R.drawable.motion_placeholder)
            .transform(RoundedCorners(20))
            .into(holder.binding.userImageBack)

/*
        Glide.with(holder.itemView.context)
            .load(ContextCompat.getDrawable(holder.itemView.context, homeDisplayImages[position].imageId))
            .thumbnail(Glide.with(holder.itemView.context).load(R.drawable.motion_placeholder))
            .transform(FitCenter(), RoundedCorners(33))
            .into(holder.binding.userImage);
*/

        Glide.with(holder.itemView.context)
            .load("")
            .transform(CenterCrop(), RoundedCorners(33))
            .into(holder.binding.userImage)
    }

    override fun getItemCount(): Int {
        return homeDisplayImages.size
    }

    class MyViewHolder(val binding: RecyclerHomeDisplayBinding) :
        RecyclerView.ViewHolder(binding.root)

    companion object {
        const val TAG = "HomeDisplayAdapter"
    }
}


