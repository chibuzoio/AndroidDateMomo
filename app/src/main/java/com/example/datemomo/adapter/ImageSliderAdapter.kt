package com.example.datemomo.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.datemomo.fragment.ImageSliderFragment
import com.example.datemomo.model.response.UserPictureResponse

class ImageSliderAdapter(fragmentActivity: FragmentActivity,
                         var userPictureComposite: ArrayList<UserPictureResponse>) :
    FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return userPictureComposite.size
    }

    override fun createFragment(position: Int): Fragment {
        val imageSliderFragment = ImageSliderFragment()
        val bundle = Bundle()
        bundle.putInt("itemCount", itemCount)
        bundle.putInt("itemPosition", position)
        bundle.putInt("imageId", userPictureComposite[position].imageId)
        bundle.putInt("imageWidth", userPictureComposite[position].imageWidth)
        bundle.putString("imageName", userPictureComposite[position].imageName)
        bundle.putInt("imageHeight", userPictureComposite[position].imageHeight)

        val secondPicture = if (itemCount > 1) { userPictureComposite[1].imageName } else { "" }

        bundle.putString("secondPicture", secondPicture)

        imageSliderFragment.arguments = bundle
        return imageSliderFragment
    }
}


