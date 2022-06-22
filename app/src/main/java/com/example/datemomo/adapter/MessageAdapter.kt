package com.example.datemomo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.datemomo.databinding.RecyclerMessageBinding
import com.example.datemomo.model.MessageModel
import com.example.datemomo.model.response.MessageResponse

class MessageAdapter(private val messageResponses: Array<MessageResponse>, private val messageModel: MessageModel) :
    RecyclerView.Adapter<MessageAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = RecyclerMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        when (messageResponses[position].messenger) {
            messageModel.senderId -> {
                holder.binding.senderMessageLayout.visibility = View.VISIBLE
                holder.binding.receiverMessageLayout.visibility = View.GONE
                holder.binding.senderMessageText.text = messageResponses[position].message
            }
            messageModel.receiverId -> {
                holder.binding.receiverMessageLayout.visibility = View.VISIBLE
                holder.binding.senderMessageLayout.visibility = View.GONE
                holder.binding.receiverMessageText.text = messageResponses[position].message
            }
            else -> {
                holder.binding.receiverMessageLayout.visibility = View.GONE
                holder.binding.senderMessageLayout.visibility = View.GONE
            }
        }

        messageModel.binding.messageSenderLayout.setOnClickListener {

        }
    }

    override fun getItemCount(): Int {
        return messageResponses.size
    }

    class MyViewHolder(val binding: RecyclerMessageBinding) :
        RecyclerView.ViewHolder(binding.root)
}


