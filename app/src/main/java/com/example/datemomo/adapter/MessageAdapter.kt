package com.example.datemomo.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.datemomo.R
import com.example.datemomo.databinding.RecyclerMessageBinding
import com.example.datemomo.model.MessageModel
import com.example.datemomo.model.request.PostMessageRequest
import com.example.datemomo.model.response.MessageResponse
import com.example.datemomo.model.response.PostMessageResponse
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.*
import java.io.IOException

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

                val insertPosition = itemCount

                messageResponses = append(messageResponses, messageResponse)
                notifyItemInserted(insertPosition)

                messageModel.binding.welcomeMessageLayout.visibility = View.GONE

                val postMessageRequest = PostMessageRequest(messageModel.senderId,
                    messageModel.receiverId, insertPosition, senderMessage)

                postSenderMessage(messageModel.context, postMessageRequest)
            }
        }

        return messageResponses.size
    }

    class MyViewHolder(val binding: RecyclerMessageBinding) :
        RecyclerView.ViewHolder(binding.root)

    @Throws(IOException::class)
    private fun postSenderMessage(context: Context, postMessageRequest: PostMessageRequest) {
        val mapper = jacksonObjectMapper()
        val jsonObjectString = mapper.writeValueAsString(postMessageRequest)
        val requestBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            jsonObjectString
        )

        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(context.getString(R.string.date_momo_api) + context.getString(R.string.api_post_message))
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()
            }

            override fun onResponse(call: Call, response: Response) {
                val myResponse: String = response.body()!!.string()
                val postMessageResponse: PostMessageResponse

                try {
                    postMessageResponse = mapper.readValue(myResponse)

                    val messageResponse = MessageResponse(
                        postMessageResponse.messageId,
                        postMessageResponse.messenger,
                        postMessageResponse.message,
                        postMessageResponse.readStatus,
                        postMessageResponse.seenStatus,
                        postMessageResponse.deleteMessage,
                        postMessageResponse.messageDate
                    )

                    messageResponses[postMessageResponse.messagePosition] = messageResponse

                    messageModel.messageActivity.runOnUiThread {
                        notifyItemChanged(postMessageResponse.messagePosition)
                    }
                } catch (exception: IOException) {
                    Log.e(TAG, "Exception from postSenderMessage is ${exception.message}")
                }
            }
        })
    }

    private fun append(messageResponses: Array<MessageResponse>, messageResponse: MessageResponse): Array<MessageResponse> {
        val messageResponseList: MutableList<MessageResponse> = messageResponses.toMutableList()
        messageResponseList.add(messageResponse)
        return messageResponseList.toTypedArray()
    }

    companion object {
        const val TAG = "MessageAdapter"
    }
}


