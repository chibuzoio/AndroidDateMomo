package com.chibuzo.datemomo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.chibuzo.datemomo.R
import com.chibuzo.datemomo.databinding.RecyclerPictureCollectionBinding
import com.chibuzo.datemomo.model.FloatingGalleryModel
import com.chibuzo.datemomo.model.PictureCollectionModel

class PictureCollectionAdapter(private val pictureCollectionModels: ArrayList<PictureCollectionModel>,
                               private val floatingGalleryModel: FloatingGalleryModel) :
    RecyclerView.Adapter<PictureCollectionAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = RecyclerPictureCollectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.binding.singlePictureOuterLayout.singlePictureView.layoutParams.width = floatingGalleryModel.floatingLayoutWidth
        holder.binding.singlePictureOuterLayout.singlePictureView.layoutParams.height = floatingGalleryModel.tripleBottomBigPictureHeight
        holder.binding.singlePictureOuterLayout.singlePictureLayout.layoutParams.width = floatingGalleryModel.floatingLayoutWidth
        holder.binding.singlePictureOuterLayout.singlePictureLayout.layoutParams.height = floatingGalleryModel.tripleBottomBigPictureHeight
        holder.binding.singlePictureOuterLayout.singlePictureOuterLayout.layoutParams.width = floatingGalleryModel.floatingLayoutWidth
        holder.binding.singlePictureOuterLayout.singlePictureOuterLayout.layoutParams.height = floatingGalleryModel.tripleBottomBigPictureHeight
        holder.binding.singlePictureOuterLayout.singlePicturePlaceholder.layoutParams.width = floatingGalleryModel.floatingLayoutWidth
        holder.binding.singlePictureOuterLayout.singlePicturePlaceholder.layoutParams.height = floatingGalleryModel.tripleBottomBigPictureHeight

        holder.binding.doubleLeftPictureLayout.bigPictureView.layoutParams.width = floatingGalleryModel.leftRightBigPictureWidth
        holder.binding.doubleLeftPictureLayout.bigPictureView.layoutParams.height = floatingGalleryModel.leftRightBigPictureHeight
        holder.binding.doubleLeftPictureLayout.bigPictureLayout.layoutParams.width = floatingGalleryModel.leftRightBigPictureWidth
        holder.binding.doubleLeftPictureLayout.bigPictureLayout.layoutParams.height = floatingGalleryModel.leftRightBigPictureHeight
        holder.binding.doubleLeftPictureLayout.bigPicturePlaceholder.layoutParams.width = floatingGalleryModel.leftRightBigPictureWidth
        holder.binding.doubleLeftPictureLayout.bigPicturePlaceholder.layoutParams.height = floatingGalleryModel.leftRightBigPictureHeight

        holder.binding.doubleLeftPictureLayout.firstSmallPictureView.layoutParams.width = floatingGalleryModel.leftRightPictureWidthHeight
        holder.binding.doubleLeftPictureLayout.firstSmallPictureView.layoutParams.height = floatingGalleryModel.leftRightPictureWidthHeight
        holder.binding.doubleLeftPictureLayout.firstSmallPictureLayout.layoutParams.width = floatingGalleryModel.leftRightPictureWidthHeight
        holder.binding.doubleLeftPictureLayout.firstSmallPictureLayout.layoutParams.height = floatingGalleryModel.leftRightPictureWidthHeight
        holder.binding.doubleLeftPictureLayout.firstSmallPicturePlaceholder.layoutParams.width = floatingGalleryModel.leftRightPictureWidthHeight
        holder.binding.doubleLeftPictureLayout.firstSmallPicturePlaceholder.layoutParams.height = floatingGalleryModel.leftRightPictureWidthHeight

        holder.binding.doubleLeftPictureLayout.secondSmallPictureView.layoutParams.width = floatingGalleryModel.leftRightPictureWidthHeight
        holder.binding.doubleLeftPictureLayout.secondSmallPictureView.layoutParams.height = floatingGalleryModel.leftRightPictureWidthHeight
        holder.binding.doubleLeftPictureLayout.secondSmallPictureLayout.layoutParams.width = floatingGalleryModel.leftRightPictureWidthHeight
        holder.binding.doubleLeftPictureLayout.secondSmallPictureLayout.layoutParams.height = floatingGalleryModel.leftRightPictureWidthHeight
        holder.binding.doubleLeftPictureLayout.secondSmallPicturePlaceholder.layoutParams.width = floatingGalleryModel.leftRightPictureWidthHeight
        holder.binding.doubleLeftPictureLayout.secondSmallPicturePlaceholder.layoutParams.height = floatingGalleryModel.leftRightPictureWidthHeight

        holder.binding.doubleRightPictureLayout.bigPictureView.layoutParams.width = floatingGalleryModel.leftRightBigPictureWidth
        holder.binding.doubleRightPictureLayout.bigPictureView.layoutParams.height = floatingGalleryModel.leftRightBigPictureHeight
        holder.binding.doubleRightPictureLayout.bigPictureLayout.layoutParams.width = floatingGalleryModel.leftRightBigPictureWidth
        holder.binding.doubleRightPictureLayout.bigPictureLayout.layoutParams.height = floatingGalleryModel.leftRightBigPictureHeight
        holder.binding.doubleRightPictureLayout.bigPicturePlaceholder.layoutParams.width = floatingGalleryModel.leftRightBigPictureWidth
        holder.binding.doubleRightPictureLayout.bigPicturePlaceholder.layoutParams.height = floatingGalleryModel.leftRightBigPictureHeight

        holder.binding.doubleRightPictureLayout.firstSmallPictureView.layoutParams.width = floatingGalleryModel.leftRightPictureWidthHeight
        holder.binding.doubleRightPictureLayout.firstSmallPictureView.layoutParams.height = floatingGalleryModel.leftRightPictureWidthHeight
        holder.binding.doubleRightPictureLayout.firstSmallPictureLayout.layoutParams.width = floatingGalleryModel.leftRightPictureWidthHeight
        holder.binding.doubleRightPictureLayout.firstSmallPictureLayout.layoutParams.height = floatingGalleryModel.leftRightPictureWidthHeight
        holder.binding.doubleRightPictureLayout.firstSmallPicturePlaceholder.layoutParams.width = floatingGalleryModel.leftRightPictureWidthHeight
        holder.binding.doubleRightPictureLayout.firstSmallPicturePlaceholder.layoutParams.height = floatingGalleryModel.leftRightPictureWidthHeight

        holder.binding.doubleRightPictureLayout.secondSmallPictureView.layoutParams.width = floatingGalleryModel.leftRightPictureWidthHeight
        holder.binding.doubleRightPictureLayout.secondSmallPictureView.layoutParams.height = floatingGalleryModel.leftRightPictureWidthHeight
        holder.binding.doubleRightPictureLayout.secondSmallPictureLayout.layoutParams.width = floatingGalleryModel.leftRightPictureWidthHeight
        holder.binding.doubleRightPictureLayout.secondSmallPictureLayout.layoutParams.height = floatingGalleryModel.leftRightPictureWidthHeight
        holder.binding.doubleRightPictureLayout.secondSmallPicturePlaceholder.layoutParams.width = floatingGalleryModel.leftRightPictureWidthHeight
        holder.binding.doubleRightPictureLayout.secondSmallPicturePlaceholder.layoutParams.height = floatingGalleryModel.leftRightPictureWidthHeight

        holder.binding.tripleBottomPictureLayout.bigPictureView.layoutParams.width = floatingGalleryModel.floatingLayoutWidth
        holder.binding.tripleBottomPictureLayout.bigPictureView.layoutParams.height = floatingGalleryModel.tripleBottomBigPictureHeight
        holder.binding.tripleBottomPictureLayout.bigPictureLayout.layoutParams.width = floatingGalleryModel.floatingLayoutWidth
        holder.binding.tripleBottomPictureLayout.bigPictureLayout.layoutParams.height = floatingGalleryModel.tripleBottomBigPictureHeight
        holder.binding.tripleBottomPictureLayout.bigPicturePlaceholder.layoutParams.width = floatingGalleryModel.floatingLayoutWidth
        holder.binding.tripleBottomPictureLayout.bigPicturePlaceholder.layoutParams.height = floatingGalleryModel.tripleBottomBigPictureHeight

        holder.binding.tripleBottomPictureLayout.firstSmallPictureView.layoutParams.width = floatingGalleryModel.floatingLayoutWidth / 3
        holder.binding.tripleBottomPictureLayout.firstSmallPictureView.layoutParams.height = floatingGalleryModel.floatingLayoutWidth / 3
        holder.binding.tripleBottomPictureLayout.firstSmallPictureLayout.layoutParams.width = floatingGalleryModel.floatingLayoutWidth / 3
        holder.binding.tripleBottomPictureLayout.firstSmallPictureLayout.layoutParams.height = floatingGalleryModel.floatingLayoutWidth / 3
        holder.binding.tripleBottomPictureLayout.firstSmallPicturePlaceholder.layoutParams.width = floatingGalleryModel.floatingLayoutWidth / 3
        holder.binding.tripleBottomPictureLayout.firstSmallPicturePlaceholder.layoutParams.height = floatingGalleryModel.floatingLayoutWidth / 3

        holder.binding.tripleBottomPictureLayout.thirdSmallPictureView.layoutParams.width = floatingGalleryModel.floatingLayoutWidth / 3
        holder.binding.tripleBottomPictureLayout.thirdSmallPictureView.layoutParams.height = floatingGalleryModel.floatingLayoutWidth / 3
        holder.binding.tripleBottomPictureLayout.thirdSmallPictureLayout.layoutParams.width = floatingGalleryModel.floatingLayoutWidth / 3
        holder.binding.tripleBottomPictureLayout.thirdSmallPictureLayout.layoutParams.height = floatingGalleryModel.floatingLayoutWidth / 3
        holder.binding.tripleBottomPictureLayout.thirdSmallPicturePlaceholder.layoutParams.width = floatingGalleryModel.floatingLayoutWidth / 3
        holder.binding.tripleBottomPictureLayout.thirdSmallPicturePlaceholder.layoutParams.height = floatingGalleryModel.floatingLayoutWidth / 3

        holder.binding.tripleBottomPictureLayout.secondSmallPictureView.layoutParams.width = floatingGalleryModel.floatingLayoutWidth / 3
        holder.binding.tripleBottomPictureLayout.secondSmallPictureView.layoutParams.height = floatingGalleryModel.floatingLayoutWidth / 3
        holder.binding.tripleBottomPictureLayout.secondSmallPictureLayout.layoutParams.width = floatingGalleryModel.floatingLayoutWidth / 3
        holder.binding.tripleBottomPictureLayout.secondSmallPictureLayout.layoutParams.height = floatingGalleryModel.floatingLayoutWidth / 3
        holder.binding.tripleBottomPictureLayout.secondSmallPicturePlaceholder.layoutParams.width = floatingGalleryModel.floatingLayoutWidth / 3
        holder.binding.tripleBottomPictureLayout.secondSmallPicturePlaceholder.layoutParams.height = floatingGalleryModel.floatingLayoutWidth / 3

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


