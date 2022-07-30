package com.example.datemomo.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.example.datemomo.R
import com.example.datemomo.databinding.RecyclerImageDisplayBinding
import com.example.datemomo.model.AllLikersModel
import com.example.datemomo.model.PictureCompositeModel
import kotlin.math.roundToInt

class ImageDisplayAdapter(private var pictureCompositeModels: ArrayList<PictureCompositeModel>,
                          private var allLikersModel: AllLikersModel) :
    RecyclerView.Adapter<ImageDisplayAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = RecyclerImageDisplayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val pictureSeparatorWidth: Float =
            (((20 / 100) * allLikersModel.deviceWidth) / 4).toFloat() // look into this
        val pictureDisplayWidth = (allLikersModel.deviceWidth - ((20 / 100) * allLikersModel.deviceWidth)) / 3
        val pictureDisplayHeight = (1.1 * pictureDisplayWidth).roundToInt()

        Log.e(TAG, "pictureSeparatorWidth value here is $pictureSeparatorWidth")
        Log.e(TAG, "pictureDisplayWidth value here is $pictureDisplayWidth")
        Log.e(TAG, "pictureDisplayHeight value here is $pictureDisplayHeight")

        holder.binding.firstSeparator.layoutParams.width = pictureSeparatorWidth.toInt()
        holder.binding.thirdSeparator.layoutParams.width = pictureSeparatorWidth.toInt()
        holder.binding.fourthSeparator.layoutParams.width = pictureSeparatorWidth.toInt()
        holder.binding.secondSeparator.layoutParams.width = pictureSeparatorWidth.toInt()

        holder.binding.firstPictureView.layoutParams.width = pictureDisplayWidth
        holder.binding.thirdPictureView.layoutParams.width = pictureDisplayWidth
        holder.binding.secondPictureView.layoutParams.width = pictureDisplayWidth

        holder.binding.firstPictureView.layoutParams.height = pictureDisplayHeight
        holder.binding.thirdPictureView.layoutParams.height = pictureDisplayHeight
        holder.binding.secondPictureView.layoutParams.height = pictureDisplayHeight

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

    companion object {
        const val TAG = "ImageDisplayAdapter"
    }
}


