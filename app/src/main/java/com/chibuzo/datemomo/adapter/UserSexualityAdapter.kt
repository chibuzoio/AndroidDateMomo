package com.chibuzo.datemomo.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chibuzo.datemomo.databinding.RecyclerUserSexualityBinding

class UserSexualityAdapter(private var userSexualities: ArrayList<String>) :
    RecyclerView.Adapter<UserSexualityAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = RecyclerUserSexualityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.binding.userSexualityText.text = userSexualities[position]
    }

    override fun getItemCount(): Int {
        return userSexualities.size
    }

    class MyViewHolder(val binding: RecyclerUserSexualityBinding) :
        RecyclerView.ViewHolder(binding.root)
}


