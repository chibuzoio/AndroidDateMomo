package com.example.datemomo.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.datemomo.databinding.RecyclerHomeDisplayBinding
import com.example.datemomo.model.HomeDisplayModel

class HomeDisplayAdapter(private val homeDisplayImages: ArrayList<HomeDisplayModel>) :
    RecyclerView.Adapter<HomeDisplayAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = RecyclerHomeDisplayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(holder.itemView.context)
            .load(ContextCompat.getDrawable(holder.itemView.context, homeDisplayImages[position].imageId))
            .transform(FitCenter(), RoundedCorners(33))
            .into(holder.binding.userImage)
    }

    override fun getItemCount(): Int {
        return homeDisplayImages.size
    }

    class MyViewHolder(val binding: RecyclerHomeDisplayBinding) :
        RecyclerView.ViewHolder(binding.root)
}


