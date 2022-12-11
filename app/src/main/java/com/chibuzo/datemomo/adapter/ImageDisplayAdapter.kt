package com.chibuzo.datemomo.adapter

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.chibuzo.datemomo.R
import com.chibuzo.datemomo.activity.ImageDisplayActivity
import com.chibuzo.datemomo.activity.ImageSliderActivity
import com.chibuzo.datemomo.databinding.RecyclerImageDisplayBinding
import com.chibuzo.datemomo.model.AllLikersModel
import com.chibuzo.datemomo.model.PictureCompositeModel
import com.chibuzo.datemomo.model.request.UserPictureRequest
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.*
import java.io.IOException

class ImageDisplayAdapter(private var pictureCompositeModels: ArrayList<PictureCompositeModel>,
                          private var allLikersModel: AllLikersModel) :
    RecyclerView.Adapter<ImageDisplayAdapter.MyViewHolder>() {
    private var currentPosition = 0
    private lateinit var context: Context
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferencesEditor: SharedPreferences.Editor

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = RecyclerImageDisplayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        context = holder.itemView.context

        sharedPreferences =
            context.getSharedPreferences(context.getString(R.string.shared_preferences), Context.MODE_PRIVATE)
        sharedPreferencesEditor = sharedPreferences.edit()

        val pictureSeparatorWidth = (((3 / 100F) * allLikersModel.deviceWidth) / 4).toInt() // look into this
        val pictureDisplayWidth = ((allLikersModel.deviceWidth - ((3 / 100F) * allLikersModel.deviceWidth)) / 3).toInt()
        val pictureDisplayHeight = (1.1 * pictureDisplayWidth).toInt()

        holder.binding.firstSeparator.layoutParams.width = pictureSeparatorWidth
        holder.binding.thirdSeparator.layoutParams.width = pictureSeparatorWidth
        holder.binding.fourthSeparator.layoutParams.width = pictureSeparatorWidth
        holder.binding.secondSeparator.layoutParams.width = pictureSeparatorWidth

        holder.binding.firstPictureView.layoutParams.width = pictureDisplayWidth
        holder.binding.thirdPictureView.layoutParams.width = pictureDisplayWidth
        holder.binding.secondPictureView.layoutParams.width = pictureDisplayWidth

        holder.binding.firstPictureView.layoutParams.height = pictureDisplayHeight
        holder.binding.thirdPictureView.layoutParams.height = pictureDisplayHeight
        holder.binding.secondPictureView.layoutParams.height = pictureDisplayHeight

        val firstPictureViewMarginLayoutParams =
            holder.binding.firstPictureView.layoutParams as ViewGroup.MarginLayoutParams
        firstPictureViewMarginLayoutParams.topMargin = pictureSeparatorWidth

        val secondPictureViewMarginLayoutParams =
            holder.binding.secondPictureView.layoutParams as ViewGroup.MarginLayoutParams
        secondPictureViewMarginLayoutParams.topMargin = pictureSeparatorWidth

        val thirdPictureViewMarginLayoutParams =
            holder.binding.thirdPictureView.layoutParams as ViewGroup.MarginLayoutParams
        thirdPictureViewMarginLayoutParams.topMargin = pictureSeparatorWidth

        holder.binding.firstPictureView.setOnClickListener {
            currentPosition = position * 3

            val userPictureRequest = UserPictureRequest(
                memberId = allLikersModel.memberId,
                currentPosition = currentPosition
            )

            (allLikersModel.appCompatActivity as ImageDisplayActivity).fetchUserPictures(userPictureRequest)
        }

        holder.binding.secondPictureView.setOnClickListener {
            currentPosition = (position * 3) + 1

            val userPictureRequest = UserPictureRequest(
                memberId = allLikersModel.memberId,
                currentPosition = currentPosition
            )

            (allLikersModel.appCompatActivity as ImageDisplayActivity).fetchUserPictures(userPictureRequest)
        }

        holder.binding.thirdPictureView.setOnClickListener {
            currentPosition = (position * 3) + 2

            val userPictureRequest = UserPictureRequest(
                memberId = allLikersModel.memberId,
                currentPosition = currentPosition
            )

            (allLikersModel.appCompatActivity as ImageDisplayActivity).fetchUserPictures(userPictureRequest)
        }

        try {
            Glide.with(holder.itemView.context)
                .load(
                    holder.itemView.context.getString(R.string.date_momo_api) +
                            holder.itemView.context.getString(R.string.api_image)
                            + pictureCompositeModels[position].userPictureResponses[0].imageName
                )
                .transform(CenterCrop())
                .into(holder.binding.firstPictureView)

            Glide.with(holder.itemView.context)
                .load(
                    holder.itemView.context.getString(R.string.date_momo_api) +
                            holder.itemView.context.getString(R.string.api_image)
                            + pictureCompositeModels[position].userPictureResponses[1].imageName
                )
                .transform(CenterCrop())
                .into(holder.binding.secondPictureView)

            Glide.with(holder.itemView.context)
                .load(
                    holder.itemView.context.getString(R.string.date_momo_api) +
                            holder.itemView.context.getString(R.string.api_image)
                            + pictureCompositeModels[position].userPictureResponses[2].imageName
                )
                .transform(CenterCrop())
                .into(holder.binding.thirdPictureView)
        } catch (exception: IndexOutOfBoundsException) {
            exception.printStackTrace()
            Log.e(TAG, "IndexOutOfBoundsException was caught, with message = ${exception.message}")
        }
    }

    override fun getItemCount(): Int {
        return pictureCompositeModels.size
    }

    class MyViewHolder(val binding: RecyclerImageDisplayBinding) :
        RecyclerView.ViewHolder(binding.root)

    companion object {
        const val TAG = "ImageDisplayAdapter"
    }
}


