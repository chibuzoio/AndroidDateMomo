package com.chibuzo.datemomo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.chibuzo.datemomo.R
import com.chibuzo.datemomo.databinding.RecyclerPictureCollectionBinding
import com.chibuzo.datemomo.model.HomeDisplayModel
import com.chibuzo.datemomo.model.PictureCollectionModel

class PictureCollectionAdapter(private val pictureCollectionModels: ArrayList<PictureCollectionModel>, private val homeDisplayModel: HomeDisplayModel) :
    RecyclerView.Adapter<PictureCollectionAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = RecyclerPictureCollectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val floatingLayoutWidth = homeDisplayModel.deviceWidth -
                (homeDisplayModel.binding.floatingInformationLayout.marginLeft +
                        homeDisplayModel.binding.floatingInformationLayout.marginRight)
        val leftRightPictureWidthHeight = ((40 / 100F) * floatingLayoutWidth.toFloat()).toInt()
        val tripleBottomBigPictureHeight = ((60 / 100F) * floatingLayoutWidth.toFloat()).toInt()
        val leftRightBigPictureWidth = floatingLayoutWidth - leftRightPictureWidthHeight
        val leftRightBigPictureHeight = leftRightPictureWidthHeight * 2

        holder.binding.singlePictureOuterLayout.singlePictureView.layoutParams.width = floatingLayoutWidth
        holder.binding.singlePictureOuterLayout.singlePictureView.layoutParams.height = tripleBottomBigPictureHeight
        holder.binding.singlePictureOuterLayout.singlePictureLayout.layoutParams.width = floatingLayoutWidth
        holder.binding.singlePictureOuterLayout.singlePictureLayout.layoutParams.height = tripleBottomBigPictureHeight
        holder.binding.singlePictureOuterLayout.singlePicturePlaceholder.layoutParams.width = floatingLayoutWidth
        holder.binding.singlePictureOuterLayout.singlePicturePlaceholder.layoutParams.height = tripleBottomBigPictureHeight

        holder.binding.doubleLeftPictureLayout.bigPictureView.layoutParams.width = leftRightBigPictureWidth
        holder.binding.doubleLeftPictureLayout.bigPictureView.layoutParams.height = leftRightBigPictureHeight
        holder.binding.doubleLeftPictureLayout.bigPictureLayout.layoutParams.width = leftRightBigPictureWidth
        holder.binding.doubleLeftPictureLayout.bigPictureLayout.layoutParams.height = leftRightBigPictureHeight
        holder.binding.doubleLeftPictureLayout.bigPicturePlaceholder.layoutParams.width = leftRightBigPictureWidth
        holder.binding.doubleLeftPictureLayout.bigPicturePlaceholder.layoutParams.height = leftRightBigPictureHeight

        holder.binding.doubleLeftPictureLayout.firstSmallPictureView.layoutParams.width = leftRightPictureWidthHeight
        holder.binding.doubleLeftPictureLayout.firstSmallPictureView.layoutParams.height = leftRightPictureWidthHeight
        holder.binding.doubleLeftPictureLayout.firstSmallPictureLayout.layoutParams.width = leftRightPictureWidthHeight
        holder.binding.doubleLeftPictureLayout.firstSmallPictureLayout.layoutParams.height = leftRightPictureWidthHeight
        holder.binding.doubleLeftPictureLayout.firstSmallPicturePlaceholder.layoutParams.width = leftRightPictureWidthHeight
        holder.binding.doubleLeftPictureLayout.firstSmallPicturePlaceholder.layoutParams.height = leftRightPictureWidthHeight

        holder.binding.doubleLeftPictureLayout.secondSmallPictureView.layoutParams.width = leftRightPictureWidthHeight
        holder.binding.doubleLeftPictureLayout.secondSmallPictureView.layoutParams.height = leftRightPictureWidthHeight
        holder.binding.doubleLeftPictureLayout.secondSmallPictureLayout.layoutParams.width = leftRightPictureWidthHeight
        holder.binding.doubleLeftPictureLayout.secondSmallPictureLayout.layoutParams.height = leftRightPictureWidthHeight
        holder.binding.doubleLeftPictureLayout.secondSmallPicturePlaceholder.layoutParams.width = leftRightPictureWidthHeight
        holder.binding.doubleLeftPictureLayout.secondSmallPicturePlaceholder.layoutParams.height = leftRightPictureWidthHeight

        holder.binding.doubleRightPictureLayout.bigPictureView.layoutParams.width = leftRightBigPictureWidth
        holder.binding.doubleRightPictureLayout.bigPictureView.layoutParams.height = leftRightBigPictureHeight
        holder.binding.doubleRightPictureLayout.bigPictureLayout.layoutParams.width = leftRightBigPictureWidth
        holder.binding.doubleRightPictureLayout.bigPictureLayout.layoutParams.height = leftRightBigPictureHeight
        holder.binding.doubleRightPictureLayout.bigPicturePlaceholder.layoutParams.width = leftRightBigPictureWidth
        holder.binding.doubleRightPictureLayout.bigPicturePlaceholder.layoutParams.height = leftRightBigPictureHeight

        holder.binding.doubleRightPictureLayout.firstSmallPictureView.layoutParams.width = leftRightPictureWidthHeight
        holder.binding.doubleRightPictureLayout.firstSmallPictureView.layoutParams.height = leftRightPictureWidthHeight
        holder.binding.doubleRightPictureLayout.firstSmallPictureLayout.layoutParams.width = leftRightPictureWidthHeight
        holder.binding.doubleRightPictureLayout.firstSmallPictureLayout.layoutParams.height = leftRightPictureWidthHeight
        holder.binding.doubleRightPictureLayout.firstSmallPicturePlaceholder.layoutParams.width = leftRightPictureWidthHeight
        holder.binding.doubleRightPictureLayout.firstSmallPicturePlaceholder.layoutParams.height = leftRightPictureWidthHeight

        holder.binding.doubleRightPictureLayout.secondSmallPictureView.layoutParams.width = leftRightPictureWidthHeight
        holder.binding.doubleRightPictureLayout.secondSmallPictureView.layoutParams.height = leftRightPictureWidthHeight
        holder.binding.doubleRightPictureLayout.secondSmallPictureLayout.layoutParams.width = leftRightPictureWidthHeight
        holder.binding.doubleRightPictureLayout.secondSmallPictureLayout.layoutParams.height = leftRightPictureWidthHeight
        holder.binding.doubleRightPictureLayout.secondSmallPicturePlaceholder.layoutParams.width = leftRightPictureWidthHeight
        holder.binding.doubleRightPictureLayout.secondSmallPicturePlaceholder.layoutParams.height = leftRightPictureWidthHeight

        holder.binding.tripleBottomPictureLayout.bigPictureView.layoutParams.width = floatingLayoutWidth
        holder.binding.tripleBottomPictureLayout.bigPictureView.layoutParams.height = tripleBottomBigPictureHeight
        holder.binding.tripleBottomPictureLayout.bigPictureLayout.layoutParams.width = floatingLayoutWidth
        holder.binding.tripleBottomPictureLayout.bigPictureLayout.layoutParams.height = tripleBottomBigPictureHeight
        holder.binding.tripleBottomPictureLayout.bigPicturePlaceholder.layoutParams.width = floatingLayoutWidth
        holder.binding.tripleBottomPictureLayout.bigPicturePlaceholder.layoutParams.height = tripleBottomBigPictureHeight

        holder.binding.tripleBottomPictureLayout.firstSmallPictureView.layoutParams.width = floatingLayoutWidth / 3
        holder.binding.tripleBottomPictureLayout.firstSmallPictureView.layoutParams.height = floatingLayoutWidth / 3
        holder.binding.tripleBottomPictureLayout.firstSmallPictureLayout.layoutParams.width = floatingLayoutWidth / 3
        holder.binding.tripleBottomPictureLayout.firstSmallPictureLayout.layoutParams.height = floatingLayoutWidth / 3
        holder.binding.tripleBottomPictureLayout.firstSmallPicturePlaceholder.layoutParams.width = floatingLayoutWidth / 3
        holder.binding.tripleBottomPictureLayout.firstSmallPicturePlaceholder.layoutParams.height = floatingLayoutWidth / 3

        holder.binding.tripleBottomPictureLayout.thirdSmallPictureView.layoutParams.width = floatingLayoutWidth / 3
        holder.binding.tripleBottomPictureLayout.thirdSmallPictureView.layoutParams.height = floatingLayoutWidth / 3
        holder.binding.tripleBottomPictureLayout.thirdSmallPictureLayout.layoutParams.width = floatingLayoutWidth / 3
        holder.binding.tripleBottomPictureLayout.thirdSmallPictureLayout.layoutParams.height = floatingLayoutWidth / 3
        holder.binding.tripleBottomPictureLayout.thirdSmallPicturePlaceholder.layoutParams.width = floatingLayoutWidth / 3
        holder.binding.tripleBottomPictureLayout.thirdSmallPicturePlaceholder.layoutParams.height = floatingLayoutWidth / 3

        holder.binding.tripleBottomPictureLayout.secondSmallPictureView.layoutParams.width = floatingLayoutWidth / 3
        holder.binding.tripleBottomPictureLayout.secondSmallPictureView.layoutParams.height = floatingLayoutWidth / 3
        holder.binding.tripleBottomPictureLayout.secondSmallPictureLayout.layoutParams.width = floatingLayoutWidth / 3
        holder.binding.tripleBottomPictureLayout.secondSmallPictureLayout.layoutParams.height = floatingLayoutWidth / 3
        holder.binding.tripleBottomPictureLayout.secondSmallPicturePlaceholder.layoutParams.width = floatingLayoutWidth / 3
        holder.binding.tripleBottomPictureLayout.secondSmallPicturePlaceholder.layoutParams.height = floatingLayoutWidth / 3

        if (pictureCollectionModels[position].pictureLayoutType == holder.itemView.context.getString(R.string.picture_layout_single)) {
            holder.binding.singlePictureOuterLayout.singlePictureOuterLayout.visibility = View.VISIBLE
            holder.binding.tripleBottomPictureLayout.tripleBottomPictureLayout.visibility = View.GONE
            holder.binding.doubleRightPictureLayout.doubleRightPictureLayout.visibility = View.GONE
            holder.binding.doubleLeftPictureLayout.doubleLeftPictureLayout.visibility = View.GONE

            Glide.with(holder.itemView.context)
                .load(holder.itemView.context.getString(R.string.date_momo_api)
                        + holder.itemView.context.getString(R.string.api_image)
                        + pictureCollectionModels[position].userPictureResponses[0].imageName)
                .transform(CenterCrop())
                .into(holder.binding.singlePictureOuterLayout.singlePictureView)
        }

        if (pictureCollectionModels[position].pictureLayoutType == holder.itemView.context.getString(R.string.picture_layout_double_left)) {
            holder.binding.tripleBottomPictureLayout.tripleBottomPictureLayout.visibility = View.GONE
            holder.binding.doubleLeftPictureLayout.doubleLeftPictureLayout.visibility = View.VISIBLE
            holder.binding.singlePictureOuterLayout.singlePictureOuterLayout.visibility = View.GONE
            holder.binding.doubleRightPictureLayout.doubleRightPictureLayout.visibility = View.GONE

            Glide.with(holder.itemView.context)
                .load(holder.itemView.context.getString(R.string.date_momo_api)
                        + holder.itemView.context.getString(R.string.api_image)
                        + pictureCollectionModels[position].userPictureResponses[0].imageName)
                .transform(CenterCrop())
                .into(holder.binding.doubleLeftPictureLayout.firstSmallPictureView)

            Glide.with(holder.itemView.context)
                .load(holder.itemView.context.getString(R.string.date_momo_api)
                        + holder.itemView.context.getString(R.string.api_image)
                        + pictureCollectionModels[position].userPictureResponses[1].imageName)
                .transform(CenterCrop())
                .into(holder.binding.doubleLeftPictureLayout.secondSmallPictureView)

            Glide.with(holder.itemView.context)
                .load(holder.itemView.context.getString(R.string.date_momo_api)
                        + holder.itemView.context.getString(R.string.api_image)
                        + pictureCollectionModels[position].userPictureResponses[2].imageName)
                .transform(CenterCrop())
                .into(holder.binding.doubleLeftPictureLayout.bigPictureView)
        }

        if (pictureCollectionModels[position].pictureLayoutType == holder.itemView.context.getString(R.string.picture_layout_double_right)) {
            holder.binding.doubleRightPictureLayout.doubleRightPictureLayout.visibility = View.VISIBLE
            holder.binding.tripleBottomPictureLayout.tripleBottomPictureLayout.visibility = View.GONE
            holder.binding.singlePictureOuterLayout.singlePictureOuterLayout.visibility = View.GONE
            holder.binding.doubleLeftPictureLayout.doubleLeftPictureLayout.visibility = View.GONE

            Glide.with(holder.itemView.context)
                .load(holder.itemView.context.getString(R.string.date_momo_api)
                        + holder.itemView.context.getString(R.string.api_image)
                        + pictureCollectionModels[position].userPictureResponses[0].imageName)
                .transform(CenterCrop())
                .into(holder.binding.doubleRightPictureLayout.bigPictureView)

            Glide.with(holder.itemView.context)
                .load(holder.itemView.context.getString(R.string.date_momo_api)
                        + holder.itemView.context.getString(R.string.api_image)
                        + pictureCollectionModels[position].userPictureResponses[1].imageName)
                .transform(CenterCrop())
                .into(holder.binding.doubleRightPictureLayout.firstSmallPictureView)

            Glide.with(holder.itemView.context)
                .load(holder.itemView.context.getString(R.string.date_momo_api)
                        + holder.itemView.context.getString(R.string.api_image)
                        + pictureCollectionModels[position].userPictureResponses[2].imageName)
                .transform(CenterCrop())
                .into(holder.binding.doubleRightPictureLayout.secondSmallPictureView)
        }

        if (pictureCollectionModels[position].pictureLayoutType == holder.itemView.context.getString(R.string.picture_layout_triple_bottom)) {
            holder.binding.tripleBottomPictureLayout.tripleBottomPictureLayout.visibility = View.VISIBLE
            holder.binding.doubleRightPictureLayout.doubleRightPictureLayout.visibility = View.GONE
            holder.binding.singlePictureOuterLayout.singlePictureOuterLayout.visibility = View.GONE
            holder.binding.doubleLeftPictureLayout.doubleLeftPictureLayout.visibility = View.GONE

            Glide.with(holder.itemView.context)
                .load(holder.itemView.context.getString(R.string.date_momo_api)
                        + holder.itemView.context.getString(R.string.api_image)
                        + pictureCollectionModels[position].userPictureResponses[0].imageName)
                .transform(CenterCrop())
                .into(holder.binding.tripleBottomPictureLayout.bigPictureView)

            Glide.with(holder.itemView.context)
                .load(holder.itemView.context.getString(R.string.date_momo_api)
                        + holder.itemView.context.getString(R.string.api_image)
                        + pictureCollectionModels[position].userPictureResponses[1].imageName)
                .transform(CenterCrop())
                .into(holder.binding.tripleBottomPictureLayout.firstSmallPictureView)

            Glide.with(holder.itemView.context)
                .load(holder.itemView.context.getString(R.string.date_momo_api)
                        + holder.itemView.context.getString(R.string.api_image)
                        + pictureCollectionModels[position].userPictureResponses[2].imageName)
                .transform(CenterCrop())
                .into(holder.binding.tripleBottomPictureLayout.secondSmallPictureView)

            Glide.with(holder.itemView.context)
                .load(holder.itemView.context.getString(R.string.date_momo_api)
                        + holder.itemView.context.getString(R.string.api_image)
                        + pictureCollectionModels[position].userPictureResponses[3].imageName)
                .transform(CenterCrop())
                .into(holder.binding.tripleBottomPictureLayout.thirdSmallPictureView)
        }
    }

    override fun getItemCount(): Int {
        return pictureCollectionModels.size
    }

    class MyViewHolder(val binding: RecyclerPictureCollectionBinding) :
        RecyclerView.ViewHolder(binding.root)
}


