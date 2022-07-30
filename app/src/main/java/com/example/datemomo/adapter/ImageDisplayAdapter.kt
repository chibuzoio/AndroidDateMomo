package com.example.datemomo.adapter

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.example.datemomo.R
import com.example.datemomo.activity.ImageSliderActivity
import com.example.datemomo.databinding.RecyclerImageDisplayBinding
import com.example.datemomo.model.AllLikersModel
import com.example.datemomo.model.PictureCompositeModel
import com.example.datemomo.model.request.UserPictureRequest
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
            fetchUserPictures()
        }

        holder.binding.secondPictureView.setOnClickListener {
            currentPosition = (position * 3) + 1
            fetchUserPictures()
        }

        holder.binding.thirdPictureView.setOnClickListener {
            currentPosition = (position * 3) + 2
            fetchUserPictures()
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
            Log.e(TAG, "IndexOutOfBoundsException was caught, with message = ${exception.message}")
        }
    }

    override fun getItemCount(): Int {
        return pictureCompositeModels.size
    }

    class MyViewHolder(val binding: RecyclerImageDisplayBinding) :
        RecyclerView.ViewHolder(binding.root)

    @Throws(IOException::class)
    fun fetchUserPictures() {
        val mapper = jacksonObjectMapper()
        val userPictureRequest = UserPictureRequest(
            allLikersModel.memberId
        )

        val jsonObjectString = mapper.writeValueAsString(userPictureRequest)
        val requestBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            jsonObjectString
        )

        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(context.getString(R.string.date_momo_api) +
                    context.getString(R.string.api_user_picture))
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()

/*                if (!Utility.isConnected(baseContext)) {
                    displayDoubleButtonDialog()
                } else if (e.message!!.contains("after")) {
                    displaySingleButtonDialog(getString(R.string.poor_internet_title), getString(R.string.poor_internet_message))
                } else {
                    displaySingleButtonDialog(getString(R.string.server_error_title), getString(R.string.server_error_message))
                }*/
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val myResponse: String = response.body()!!.string()
                val intent = Intent(context, ImageSliderActivity::class.java)
                intent.putExtra("currentPosition", currentPosition)
                intent.putExtra("jsonResponse", myResponse)
                context.startActivity(intent)
            }
        })
    }

    companion object {
        const val TAG = "ImageDisplayAdapter"
    }
}


