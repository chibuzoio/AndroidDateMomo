package com.example.datemomo.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.datemomo.databinding.RecyclerNotificationBinding
import com.example.datemomo.model.response.NotificationResponse

class NotificationAdapter(private var notificationResponses: Array<NotificationResponse>) :
    RecyclerView.Adapter<NotificationAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = RecyclerNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

    }

    override fun getItemCount(): Int {
        return notificationResponses.size
    }

    class MyViewHolder(val binding: RecyclerNotificationBinding) :
        RecyclerView.ViewHolder(binding.root)
}


