package com.example.datemomo.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.example.datemomo.R
import com.example.datemomo.databinding.FragmentImageSliderBinding

class ImageSliderFragment : Fragment() {
    private lateinit var binding: FragmentImageSliderBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentImageSliderBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.pictureCompositeCounter.text = getString(R.string.picture_composite_counter,
            requireArguments().getInt("itemPosition") + 1,
            requireArguments().getInt("itemCount"))

        Glide.with(this)
            .load(getString(R.string.date_momo_api) + getString(R.string.api_image)
                    + requireArguments().getString("imageName"))
            .transform(FitCenter())
            .into(binding.genericImageSlider)
    }

    companion object {
        const val TAG = "ImageSliderFragment"
    }
}


