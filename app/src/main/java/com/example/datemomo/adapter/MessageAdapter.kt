package com.example.datemomo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.datemomo.databinding.RecyclerMessageBinding
import com.example.datemomo.model.MessageModel
import com.example.datemomo.model.response.MessageResponse

class MessageAdapter(private var messageResponses: Array<MessageResponse>, private val messageModel: MessageModel) :
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
    }

    override fun getItemCount(): Int {
        messageModel.binding.messageSenderLayout.setOnClickListener {
            val senderMessage = messageModel.binding.messageInputField.text.toString()

            if (senderMessage.isNotEmpty()) {
                val messageResponse = MessageResponse(
                    0, messageModel.senderId, senderMessage,
                    0, 0, 0, "")

                messageResponses = append(messageResponses, messageResponse)
                notifyItemInserted(itemCount)

                messageModel.binding.welcomeMessageLayout.visibility = View.GONE
            }
        }

        return messageResponses.size
    }

    class MyViewHolder(val binding: RecyclerMessageBinding) :
        RecyclerView.ViewHolder(binding.root)

    private fun append(messageResponses: Array<MessageResponse>, messageResponse: MessageResponse): Array<MessageResponse> {
        val messageResponseList: MutableList<MessageResponse> = messageResponses.toMutableList()
        messageResponseList.add(messageResponse)
        return messageResponseList.toTypedArray()
    }

    companion object {
        const val TAG = "MessageAdapter"
    }
}


