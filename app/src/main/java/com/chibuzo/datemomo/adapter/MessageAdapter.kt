package com.chibuzo.datemomo.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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

class MessageAdapter(private var messageResponses: ArrayList<MessageResponse>, private val messageModel: MessageModel) :
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
            messageResponses.removeAt(messageModel.currentPosition)
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
            messageResponses.removeAt(messageModel.currentPosition)
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

                val senderMessage = Utility.decodeEmoji(messageResponses[messageModel.currentPosition].message)

                if (senderMessage == holder.itemView.context.getString(R.string.sticker_anim_wave)) {
                    holder.binding.senderMessageLowerLayout.background = ColorDrawable(ContextCompat.getColor(holder.itemView.context, R.color.white))
                    holder.binding.senderMessageUpperLayout.visibility = View.INVISIBLE
                    holder.binding.senderMessageImage.visibility = View.VISIBLE
                    holder.binding.senderMessageText.visibility = View.GONE

                    if (senderMessage.contains("anim")) {
                        Glide.with(holder.itemView.context)
                            .asGif()
                            .load(R.drawable.anime_waving_hand)
                            .into(holder.binding.senderMessageImage)
                    } else {
                        Glide.with(holder.itemView.context)
                            .load(R.drawable.anime_waving_hand)
                            .into(holder.binding.senderMessageImage)
                    }
                } else {
                    holder.binding.senderMessageLowerLayout.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.blue_message_lower_layout)
                    holder.binding.senderMessageUpperLayout.visibility = View.VISIBLE
                    holder.binding.senderMessageText.visibility = View.VISIBLE
                    holder.binding.senderMessageImage.visibility = View.GONE
                    holder.binding.senderMessageText.text = senderMessage
                }
            }
            messageModel.receiverId -> {
                holder.binding.receiverMessageLayout.visibility = View.VISIBLE
                holder.binding.senderMessageLayout.visibility = View.GONE

                val receiverMessage = Utility.decodeEmoji(messageResponses[messageModel.currentPosition].message)

                if (receiverMessage == holder.itemView.context.getString(R.string.sticker_anim_wave)) {
                    holder.binding.receiverMessageLowerLayout.background = ColorDrawable(ContextCompat.getColor(holder.itemView.context, R.color.white))
                    holder.binding.receiverMessageUpperLayout.visibility = View.INVISIBLE
                    holder.binding.receiverMessageImage.visibility = View.VISIBLE
                    holder.binding.receiverMessageText.visibility = View.GONE

                    if (receiverMessage.contains("anim")) {
                        Glide.with(holder.itemView.context)
                            .asGif()
                            .load(R.drawable.anime_waving_hand)
                            .into(holder.binding.receiverMessageImage)
                    } else {
                        Glide.with(holder.itemView.context)
                            .load(R.drawable.anime_waving_hand)
                            .into(holder.binding.receiverMessageImage)
                    }
                } else {
                    holder.binding.receiverMessageLowerLayout.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.grey_message_lower_layout)
                    holder.binding.receiverMessageUpperLayout.visibility = View.VISIBLE
                    holder.binding.receiverMessageText.visibility = View.VISIBLE
                    holder.binding.receiverMessageImage.visibility = View.GONE
                    holder.binding.receiverMessageText.text = receiverMessage
                }
            }
            else -> {
                holder.binding.receiverMessageLayout.visibility = View.GONE
                holder.binding.senderMessageLayout.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int {
        messageModel.binding.wavingHandSenderAnime.setOnClickListener {
            var senderMessage = messageModel.context.getString(R.string.sticker_anim_wave)
            senderMessage = Utility.encodeEmoji(senderMessage).toString()

            val messageResponse = MessageResponse(
                0, messageModel.senderId, senderMessage,
                0, 0, 0, ""
            )

            val insertPosition = itemCount

            messageResponses.add(messageResponse)

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

            messageModel.messageActivity.checkUnseenMessages()
        }

        messageModel.binding.messageSenderLayout.setOnClickListener {
            var senderMessage = messageModel.binding.messageInputField.text.toString().trim()

            messageModel.binding.messageInputField.setText("")

            senderMessage = Utility.encodeEmoji(senderMessage).toString()

            if (senderMessage.isNotEmpty()) {
                if (editMessageMode) {
                    editMessageMode = false
                    val editMessageRequest = EditMessageRequest(messageModel.senderId,
                        messageResponses[messageModel.currentPosition].messageId,
                        senderMessage, messageModel.receiverId)

                    val messageResponse = messageResponses[messageModel.currentPosition]
                    messageResponse.message = senderMessage

                    messageResponses[messageModel.currentPosition] = messageResponse

                    notifyItemChanged(messageModel.currentPosition)

                    messageModel.messageActivity.editSingleMessage(editMessageRequest)
                } else {
                    val messageResponse = MessageResponse(
                        0, messageModel.senderId, senderMessage,
                        0, 0, 0, ""
                    )

                    val insertPosition = itemCount

                    messageResponses.add(messageResponse)

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

            messageModel.messageActivity.checkUnseenMessages()
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
                    if (!editMessageMode) {
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


