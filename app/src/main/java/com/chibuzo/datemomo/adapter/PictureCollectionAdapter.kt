package com.chibuzo.datemomo.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.recyclerview.widget.RecyclerView
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
        val bottomPictureBigPictureHeight = ((60 / 100F) * floatingLayoutWidth.toFloat()).toInt()
        val leftRightBigPictureWidth = floatingLayoutWidth - leftRightPictureWidthHeight
        val leftRightBigPictureHeight = leftRightPictureWidthHeight * 2

        holder.binding.doubleLeftPictureLayout.bigPictureView.layoutParams.width = leftRightBigPictureWidth
        holder.binding.doubleLeftPictureLayout.bigPictureView.layoutParams.height = leftRightBigPictureHeight
        holder.binding.doubleLeftPictureLayout.firstSmallPictureView.layoutParams.width = leftRightPictureWidthHeight
        holder.binding.doubleLeftPictureLayout.firstSmallPictureView.layoutParams.height = leftRightPictureWidthHeight
        holder.binding.doubleLeftPictureLayout.secondSmallPictureView.layoutParams.width = leftRightPictureWidthHeight
        holder.binding.doubleLeftPictureLayout.secondSmallPictureView.layoutParams.height = leftRightPictureWidthHeight

        holder.binding.doubleRightPictureLayout.bigPictureView.layoutParams.width = leftRightBigPictureWidth
        holder.binding.doubleRightPictureLayout.bigPictureView.layoutParams.height = leftRightBigPictureHeight
        holder.binding.doubleRightPictureLayout.firstSmallPictureView.layoutParams.width = leftRightPictureWidthHeight
        holder.binding.doubleRightPictureLayout.firstSmallPictureView.layoutParams.height = leftRightPictureWidthHeight
        holder.binding.doubleRightPictureLayout.secondSmallPictureView.layoutParams.width = leftRightPictureWidthHeight
        holder.binding.doubleRightPictureLayout.secondSmallPictureView.layoutParams.height = leftRightPictureWidthHeight

        holder.binding.tripleBottomPictureLayout.bigPictureView.layoutParams.width = floatingLayoutWidth
        holder.binding.tripleBottomPictureLayout.bigPictureView.layoutParams.height = bottomPictureBigPictureHeight
        holder.binding.tripleBottomPictureLayout.firstSmallPictureView.layoutParams.width = floatingLayoutWidth / 3
        holder.binding.tripleBottomPictureLayout.firstSmallPictureView.layoutParams.height = floatingLayoutWidth / 3
        holder.binding.tripleBottomPictureLayout.thirdSmallPictureView.layoutParams.width = floatingLayoutWidth / 3
        holder.binding.tripleBottomPictureLayout.thirdSmallPictureView.layoutParams.height = floatingLayoutWidth / 3
        holder.binding.tripleBottomPictureLayout.secondSmallPictureView.layoutParams.width = floatingLayoutWidth / 3
        holder.binding.tripleBottomPictureLayout.secondSmallPictureView.layoutParams.height = floatingLayoutWidth / 3

        // if picture is remaining only one, use single picture layout
        // if picture is remaining two, use single picture layout for the two pictures
        // if the picture is remaining only three, shuffle between double left and double right
        // Create an array that will be storing the

    }

    override fun getItemCount(): Int {
        return pictureCollectionModels.size
    }

    class MyViewHolder(val binding: RecyclerPictureCollectionBinding) :
        RecyclerView.ViewHolder(binding.root)
}


