package com.chibuzo.datemomo.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chibuzo.datemomo.R
import com.chibuzo.datemomo.databinding.RecyclerMessageBinding
import com.chibuzo.datemomo.model.MessageModel
import com.chibuzo.datemomo.model.request.DeleteChatRequest
import com.chibuzo.datemomo.model.request.EditMessageRequest
import com.chibuzo.datemomo.model.request.PostMessageRequest
import com.chibuzo.datemomo.model.response.MessageResponse
import com.chibuzo.datemomo.model.response.PostMessageResponse
import com.chibuzo.datemomo.utility.Utility
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.*
import java.io.IOException

class MessageAdapter(private var messageResponses: Array<MessageResponse>, private val messageModel: MessageModel) :
    RecyclerView.Adapter<MessageAdapter.MyViewHolder>() {
    private var editMessageMode: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = RecyclerMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        messageModel.currentPosition = position

        messageModel.binding.messageEditMenu.setOnClickListener {
            editMessageMode = true

//            val editorMessage = EmojiCompat.get().process(messageResponses[messageModel.currentPosition].message)

            val editorMessage = Utility.decodeEmoji(messageResponses[messageModel.currentPosition].message)
            messageModel.binding.messageInputField.setText(editorMessage)

            messageModel.binding.deleteForEveryoneMenu.visibility = View.VISIBLE
            messageModel.binding.messengerMenuLayout.visibility = View.GONE
            messageModel.binding.messageEditMenu.visibility = View.VISIBLE
            messageModel.binding.messageMenuLayout.visibility = View.GONE
        }

        messageModel.binding.deleteForEveryoneMenu.setOnClickListener {
            val deleteChatRequest = DeleteChatRequest(messageModel.senderId,
                messageResponses[messageModel.currentPosition].messageId,
                messageModel.receiverId, 3)
            messageModel.messageActivity.deleteSingleMessage(deleteChatRequest)
            messageResponses = removeAt(messageModel.currentPosition)
            notifyItemRemoved(messageModel.currentPosition)

            messageModel.binding.deleteForEveryoneMenu.visibility = View.VISIBLE
            messageModel.binding.messengerMenuLayout.visibility = View.GONE
            messageModel.binding.messageEditMenu.visibility = View.VISIBLE
            messageModel.binding.messageMenuLayout.visibility = View.GONE
        }

        messageModel.binding.deleteForMeMenu.setOnClickListener {
            val deleteMessageType = if (messageModel.senderId ==
                messageResponses[messageModel.currentPosition].messenger) {
                1
            } else {
                2
            }

            val deleteChatRequest = DeleteChatRequest(messageModel.senderId,
                messageResponses[messageModel.currentPosition].messageId,
                messageModel.receiverId, deleteMessageType)
            messageModel.messageActivity.deleteSingleMessage(deleteChatRequest)
            messageResponses = removeAt(messageModel.currentPosition)
            notifyItemRemoved(messageModel.currentPosition)

            messageModel.binding.deleteForEveryoneMenu.visibility = View.VISIBLE
            messageModel.binding.messengerMenuLayout.visibility = View.GONE
            messageModel.binding.messageEditMenu.visibility = View.VISIBLE
            messageModel.binding.messageMenuLayout.visibility = View.GONE
        }

        messageModel.binding.messageCopyMenu.setOnClickListener {
            messageModel.binding.deleteForEveryoneMenu.visibility = View.VISIBLE
            messageModel.binding.messengerMenuLayout.visibility = View.GONE
            messageModel.binding.messageEditMenu.visibility = View.VISIBLE
            messageModel.binding.messageMenuLayout.visibility = View.GONE
        }

        holder.binding.senderMessageLayout.setOnLongClickListener {
            messageModel.binding.messageMenuLayout.visibility = View.VISIBLE
            messageModel.currentPosition = position
            return@setOnLongClickListener true
        }

        holder.binding.receiverMessageLayout.setOnLongClickListener {
            messageModel.binding.deleteForEveryoneMenu.visibility = View.GONE
            messageModel.binding.messageMenuLayout.visibility = View.VISIBLE
            messageModel.binding.messageEditMenu.visibility = View.GONE
            messageModel.currentPosition = position
            return@setOnLongClickListener true
        }

        when (messageResponses[position].messenger) {
            messageModel.senderId -> {
                holder.binding.senderMessageLayout.visibility = View.VISIBLE
                holder.binding.receiverMessageLayout.visibility = View.GONE

//                val senderMessage = EmojiCompat.get().process(messageResponses[messageModel.currentPosition].message)

                val senderMessage = Utility.decodeEmoji(messageResponses[messageModel.currentPosition].message)
                holder.binding.senderMessageText.text = senderMessage
            }
            messageModel.receiverId -> {
                holder.binding.receiverMessageLayout.visibility = View.VISIBLE
                holder.binding.senderMessageLayout.visibility = View.GONE

//                val receiverMessage = EmojiCompat.get().process(messageResponses[messageModel.currentPosition].message)

                val receiverMessage = Utility.decodeEmoji(messageResponses[messageModel.currentPosition].message)
                holder.binding.receiverMessageText.text = receiverMessage
            }
            else -> {
                holder.binding.receiverMessageLayout.visibility = View.GONE
                holder.binding.senderMessageLayout.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int {
        messageModel.binding.messageSenderLayout.setOnClickListener {
            var senderMessage = messageModel.binding.messageInputField.text.toString().trim()

            messageModel.binding.messageInputField.setText("")

//            senderMessage = EmojiCompat.get().process(senderMessage).toString()

            senderMessage = Utility.encodeEmoji(senderMessage).toString()

            Log.e(TAG, "senderMessage value here is $senderMessage")

            if (senderMessage.isNotEmpty()) {
                if (editMessageMode) {
                    editMessageMode = false
                    val editMessageRequest = EditMessageRequest(messageModel.senderId,
                        messageResponses[messageModel.currentPosition].messageId,
                        senderMessage, messageModel.receiverId)

                    val messageResponse = messageResponses[messageModel.currentPosition]
                    messageResponse.message = senderMessage

                    messageResponses = replace(messageResponse, messageModel.currentPosition)

                    notifyItemChanged(messageModel.currentPosition)

                    messageModel.messageActivity.editSingleMessage(editMessageRequest)
                } else {
                    val messageResponse = MessageResponse(
                        0, messageModel.senderId, senderMessage,
                        0, 0, 0, ""
                    )

                    val insertPosition = itemCount

                    messageResponses = append(messageResponse)
                    notifyItemInserted(insertPosition)

                    messageModel.binding.messageRecyclerView.layoutManager!!.scrollToPosition(
                        messageResponses.size - 1
                    )
                    messageModel.binding.welcomeMessageLayout.visibility = View.GONE
                    messageModel.binding.messageInputField.setText("")

                    val postMessageRequest = PostMessageRequest(
                        messageModel.senderId,
                        messageModel.receiverId, insertPosition, senderMessage
                    )

                    postSenderMessage(messageModel.context, postMessageRequest)
                }
            }
        }

        return messageResponses.size
    }

    class MyViewHolder(val binding: RecyclerMessageBinding) :
        RecyclerView.ViewHolder(binding.root)

    @Throws(IOException::class)
    private fun postSenderMessage(context: Context, postMessageRequest: PostMessageRequest) {
        val mapper = jacksonObjectMapper()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
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
                    exception.printStackTrace()
                    Log.e(TAG, "Exception from postSenderMessage is ${exception.message}")
                }
            }
        })
    }

    private fun append(messageResponse: MessageResponse): Array<MessageResponse> {
        val messageResponseList = messageResponses.toMutableList()
        messageResponseList.add(messageResponse)
        return messageResponseList.toTypedArray()
    }

    private fun replace(messageResponse: MessageResponse, position: Int): Array<MessageResponse> {
        val messageResponseList = messageResponses.toMutableList()
        messageResponseList.add(position, messageResponse)
        return messageResponseList.toTypedArray()
    }

    private fun removeAt(position: Int): Array<MessageResponse> {
        val messageResponseList = messageResponses.toMutableList()
        messageResponseList.removeAt(position)
        return messageResponseList.toTypedArray()
    }

    companion object {
        const val TAG = "MessageAdapter"
    }
}


