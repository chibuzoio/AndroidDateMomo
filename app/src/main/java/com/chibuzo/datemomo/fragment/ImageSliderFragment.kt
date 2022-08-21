package com.chibuzo.datemomo.fragment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.chibuzo.datemomo.R
import com.chibuzo.datemomo.activity.UserProfileActivity
import com.chibuzo.datemomo.databinding.FragmentImageSliderBinding
import com.chibuzo.datemomo.model.request.ChangeProfilePictureRequest
import com.chibuzo.datemomo.model.request.DeletePictureRequest
import com.chibuzo.datemomo.model.request.UserLikerRequest
import com.chibuzo.datemomo.model.response.CommittedResponse
import com.chibuzo.datemomo.model.response.PictureOwnerResponse
import com.chibuzo.datemomo.utility.Utility
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.*
import java.io.IOException


class ImageSliderFragment : Fragment() {
    private lateinit var binding: FragmentImageSliderBinding
    private lateinit var fragmentObject: ImageSliderFragment
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var deletePictureRequest: DeletePictureRequest
    private lateinit var pictureOwnerResponse: PictureOwnerResponse
    private lateinit var sharedPreferencesEditor: SharedPreferences.Editor
    private lateinit var changeProfilePictureRequest: ChangeProfilePictureRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fragmentObject = this

        sharedPreferences =
            requireActivity().getSharedPreferences(getString(R.string.shared_preferences), Context.MODE_PRIVATE)
        sharedPreferencesEditor = sharedPreferences.edit()

        deletePictureRequest =
            DeletePictureRequest(requireArguments().getString("imageName").toString())

        profilePictureOwnerId()
    }

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

        binding.deletePictureMenu.setOnClickListener {
            binding.makeProfilePictureMenu.visibility = View.GONE
            binding.pictureMenuLayout.visibility = View.GONE

            deletePictureRequest =
                DeletePictureRequest(requireArguments().getString("imageName").toString())

            deleteProfilePicture()
        }

        binding.savePictureMenu.setOnClickListener {

        }

        binding.makeProfilePictureMenu.setOnClickListener {
            changeProfilePictureRequest = ChangeProfilePictureRequest(
                sharedPreferences.getInt(getString(R.string.member_id), 0),
                requireArguments().getString("imageName").toString()
            )

            changeProfilePicture()
        }

        binding.pictureMenuCancel.setOnClickListener {
            binding.makeProfilePictureMenu.visibility = View.GONE
            binding.pictureMenuLayout.visibility = View.GONE
        }

        binding.pictureMenuLayout.setOnClickListener {
            binding.makeProfilePictureMenu.visibility = View.GONE
            binding.pictureMenuLayout.visibility = View.GONE
        }

        binding.photoMenuIconLayout.setOnClickListener {
            if (sharedPreferences.getInt(getString(R.string.member_id), 0) ==
                pictureOwnerResponse.memberId) {
                binding.pictureMenuLayout.visibility = View.VISIBLE

                binding.makeProfilePictureMenu.visibility =
                    if (requireArguments().getInt("itemPosition") > 0) {
                        View.VISIBLE } else { View.GONE }
            }
        }

        binding.pictureCompositeCounter.text = getString(R.string.picture_composite_counter,
            requireArguments().getInt("itemPosition") + 1,
            requireArguments().getInt("itemCount"))

        Glide.with(this)
            .load(getString(R.string.date_momo_api) + getString(R.string.api_image)
                    + requireArguments().getString("imageName"))
            .transform(FitCenter())
            .into(binding.genericImageSlider)
    }

    @Throws(IOException::class)
    fun profilePictureOwnerId() {
        val mapper = jacksonObjectMapper()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        val jsonObjectString = mapper.writeValueAsString(deletePictureRequest)
        val requestBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            jsonObjectString
        )

        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(getString(R.string.date_momo_api) + getString(R.string.api_picture_owner))
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()
            }

            override fun onResponse(call: Call, response: Response) {
                val myResponse: String = response.body()!!.string()
                pictureOwnerResponse = mapper.readValue(myResponse)
            }
        })
    }

    @Throws(IOException::class)
    fun deleteProfilePicture() {
        val mapper = jacksonObjectMapper()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        val jsonObjectString = mapper.writeValueAsString(deletePictureRequest)
        val requestBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            jsonObjectString
        )

        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(getString(R.string.date_momo_api) + getString(R.string.api_delete_profile_picture))
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()
            }

            override fun onResponse(call: Call, response: Response) {
                val myResponse: String = response.body()!!.string()
                val committedResponse: CommittedResponse = mapper.readValue(myResponse)

                if (committedResponse.committed) {
                    if (requireArguments().getInt("itemPosition") == 0) {
                        changeProfilePictureRequest = ChangeProfilePictureRequest(
                            sharedPreferences.getInt(getString(R.string.member_id), 0),
                            requireArguments().getString("secondPicture").toString()
                        )

                        changeProfilePicture()
                    } else {
                        // For here, try and find out way you will efficiently remove
                        // fragment from back stack

//                        val fragmentManager = requireActivity().supportFragmentManager
//                        val fragmentTransaction = fragmentManager.beginTransaction()
//                        fragmentTransaction.remove(fragmentObject)
//                        fragmentTransaction.commit()
//                        fragmentManager.popBackStack()

                        fetchUserLikers()
                    }
                }
            }
        })
    }

    @Throws(IOException::class)
    fun changeProfilePicture() {
        val mapper = jacksonObjectMapper()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        val jsonObjectString = mapper.writeValueAsString(changeProfilePictureRequest)
        val requestBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            jsonObjectString
        )

        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(getString(R.string.date_momo_api) + getString(R.string.api_change_profile_picture))
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()
            }

            override fun onResponse(call: Call, response: Response) {
                val myResponse: String = response.body()!!.string()
                val committedResponse: CommittedResponse = mapper.readValue(myResponse)

                if (committedResponse.committed) {
                    sharedPreferencesEditor.putString(getString(R.string.profile_picture),
                        changeProfilePictureRequest.profilePicture)
                    sharedPreferencesEditor.apply()

                    fetchUserLikers()
                }
            }
        })
    }

    @Throws(IOException::class)
    fun fetchUserLikers() {
        val mapper = jacksonObjectMapper()
        val userLikerRequest = UserLikerRequest(sharedPreferences.getInt(getString(R.string.member_id), 0))

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        val jsonObjectString = mapper.writeValueAsString(userLikerRequest)
        val requestBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            jsonObjectString
        )

        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(getString(R.string.date_momo_api) + getString(R.string.api_user_likers_data))
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()

                if (!Utility.isConnected(requireActivity().baseContext)) {
                    displayDoubleButtonDialog()
                } else if (e.message!!.contains("after")) {
                    displaySingleButtonDialog(getString(R.string.poor_internet_title), getString(R.string.poor_internet_message))
                } else {
                    displaySingleButtonDialog(getString(R.string.server_error_title), getString(R.string.server_error_message))
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val myResponse: String = response.body()!!.string()
                val intent = Intent(requireActivity().baseContext, UserProfileActivity::class.java)
                intent.putExtra("jsonResponse", myResponse)
                startActivity(intent)
            }
        })
    }

    fun displayDoubleButtonDialog() {
        requireActivity().runOnUiThread {
            binding.doubleButtonDialog.doubleButtonTitle.text = getString(R.string.network_error_title)
            binding.doubleButtonDialog.doubleButtonMessage.text = getString(R.string.network_error_message)
            binding.doubleButtonDialog.doubleButtonLayout.visibility = View.VISIBLE
        }
    }

    fun displaySingleButtonDialog(title: String, message: String) {
        requireActivity().runOnUiThread {
            binding.singleButtonDialog.singleButtonTitle.text = title
            binding.singleButtonDialog.singleButtonMessage.text = message
            binding.singleButtonDialog.singleButtonLayout.visibility = View.VISIBLE
        }
    }

    companion object {
        const val TAG = "ImageSliderFragment"
    }
}


