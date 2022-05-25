package com.example.datemomo.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.datemomo.R
import com.example.datemomo.activity.UserBioActivity
import com.example.datemomo.databinding.RecyclerHomeDisplayBinding
import com.example.datemomo.model.request.AuthenticationRequest
import com.example.datemomo.model.request.LikeUserRequest
import com.example.datemomo.model.response.AuthenticationResponse
import com.example.datemomo.model.response.CommittedResponse
import com.example.datemomo.model.response.HomeDisplayResponse
import com.example.datemomo.utility.Utility
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.*
import java.io.IOException

class HomeDisplayAdapter(private val homeDisplayResponses: Array<HomeDisplayResponse>, private val deviceWidth: Int) :
    RecyclerView.Adapter<HomeDisplayAdapter.MyViewHolder>() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferencesEditor: SharedPreferences.Editor

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = RecyclerHomeDisplayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        sharedPreferences =
            holder.itemView.context.getSharedPreferences(holder
                .itemView.context.getString(R.string.shared_preferences), Context.MODE_PRIVATE)
        sharedPreferencesEditor = sharedPreferences.edit()

        val allImageWidth = deviceWidth - Utility.dimen(holder.itemView.context, 23f)
        val allImageHeight = (/* imageHeight */ 788 * (deviceWidth -
                Utility.dimen(holder.itemView.context, 23f))) / /* imageWidth */ 788

        holder.binding.userImage.layoutParams.width = allImageWidth
        holder.binding.userImage.layoutParams.height = allImageHeight
        holder.binding.userImageBack.layoutParams.width = allImageWidth
        holder.binding.userImageBack.layoutParams.height = allImageHeight
        holder.binding.userImageLayout.layoutParams.width = allImageWidth
        holder.binding.userImageLayout.layoutParams.height = allImageHeight

        Glide.with(holder.itemView.context)
            .asGif()
            .load(R.drawable.motion_placeholder)
            .transform(RoundedCorners(15))
            .into(holder.binding.userImageBack)

/*
        Glide.with(holder.itemView.context)
            .load(ContextCompat.getDrawable(holder.itemView.context, homeDisplayImages[position].imageId))
            .thumbnail(Glide.with(holder.itemView.context).load(R.drawable.motion_placeholder))
            .transform(FitCenter(), RoundedCorners(33))
            .into(holder.binding.userImage);
*/

        if (position == (itemCount - 1)) {
            val userDisplayLayoutParam = holder.binding.userDisplayLayout.layoutParams as ViewGroup.MarginLayoutParams
            userDisplayLayoutParam.bottomMargin = 25;
            holder.binding.userDisplayLayout.layoutParams = userDisplayLayoutParam
        }

        if (homeDisplayResponses[position].liked) {
            holder.binding.loveUserIcon.setImageDrawable(
                ContextCompat.getDrawable(
                    holder.itemView.context,
                    R.drawable.icon_heart_red
                )
            )
        } else {
            holder.binding.loveUserIcon.setImageDrawable(
                ContextCompat.getDrawable(
                    holder.itemView.context,
                    R.drawable.icon_heart_hollow
                )
            )
        }

        holder.binding.loveUserLayout.setOnClickListener {
            homeDisplayResponses[position].liked = !homeDisplayResponses[position].liked
            notifyItemChanged(position)

            processUserLike(holder.itemView.context, position)

            /*
            * notifyItemInserted(insertIndex)
            * notifyItemRangeInserted(insertIndex, items.size())
            * notifyItemChanged(updateIndex)
            * notifyItemRemoved(removeIndex)
            * notifyItemRangeRemoved(startIndex, count)
            * notifyItemMoved(fromPosition, toPosition)
            * notifyDataSetChanged()
            * */
        }

        Glide.with(holder.itemView.context)
            .load(holder.itemView.context.getString(R.string.date_momo_api)
                    + "client/image/" + homeDisplayResponses[position].profilePicture)
            .transform(CenterCrop(), RoundedCorners(33))
            .into(holder.binding.userImage)

        if (homeDisplayResponses[position].fullName.isEmpty()) {
            holder.binding.userFullName.text = homeDisplayResponses[position].userName
        } else {
            holder.binding.userFullName.text = homeDisplayResponses[position].fullName
        }
    }

    override fun getItemCount(): Int {
        return homeDisplayResponses.size
    }

    class MyViewHolder(val binding: RecyclerHomeDisplayBinding) :
        RecyclerView.ViewHolder(binding.root)

    @Throws(IOException::class)
    private fun processUserLike(context: Context, position: Int) {
        val mapper = jacksonObjectMapper()
        val likeUserRequest = LikeUserRequest(
            sharedPreferences.getInt("memberId", 0),
            homeDisplayResponses[position].liked,
            homeDisplayResponses[position].memberId)

        val jsonObjectString = mapper.writeValueAsString(likeUserRequest)
        val requestBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            jsonObjectString
        )

        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(context.getString(R.string.date_momo_api) + "service/likeuser.php")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()
            }

            override fun onResponse(call: Call, response: Response) {
                val myResponse: String = response.body()!!.string()
                var committedResponse = CommittedResponse(false)

                try {
                    committedResponse = mapper.readValue(myResponse)
                } catch (exception: IOException) {
                    Log.e(TAG, "Exception from processLikeUser is ${exception.message}")
                }
            }
        })
    }

    companion object {
        const val TAG = "HomeDisplayAdapter"
    }
}


