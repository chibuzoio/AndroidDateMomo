package com.example.datemomo.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.datemomo.fragment.ImageSliderFragment
import com.example.datemomo.model.UserPictureModel

class ImageSliderAdapter(fragmentActivity: FragmentActivity,
                         var userPictureComposite: ArrayList<UserPictureModel>) :
    FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return userPictureComposite.size
    }

    override fun createFragment(position: Int): Fragment {
        val imageSliderFragment = ImageSliderFragment()
        val bundle = Bundle()
        bundle.putInt("imageId", userPictureComposite[position].imageId)
        bundle.putInt("imageWidth", userPictureComposite[position].imageWidth)
        bundle.putString("imageName", userPictureComposite[position].imageName)
        bundle.putInt("imageHeight", userPictureComposite[position].imageHeight)
        imageSliderFragment.arguments = bundle
        return imageSliderFragment
    }
}


