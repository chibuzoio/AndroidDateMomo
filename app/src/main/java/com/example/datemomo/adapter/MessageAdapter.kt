package com.example.datemomo.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.datemomo.databinding.RecyclerMessageBinding
import com.example.datemomo.model.response.MessageResponse

class MessageAdapter(private val messageResponses: Array<MessageResponse>) :
    RecyclerView.Adapter<MessageAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = RecyclerMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

    }

    override fun getItemCount(): Int {
        return messageResponses.size
    }

    class MyViewHolder(val binding: RecyclerMessageBinding) :
        RecyclerView.ViewHolder(binding.root)
}


