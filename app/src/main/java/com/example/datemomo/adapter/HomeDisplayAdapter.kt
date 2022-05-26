package com.example.datemomo.adapter

import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.datemomo.R
import com.example.datemomo.databinding.RecyclerHomeDisplayBinding
import com.example.datemomo.model.HomeDisplayModel
import com.example.datemomo.model.request.LikeUserRequest
import com.example.datemomo.model.response.CommittedResponse
import com.example.datemomo.model.response.HomeDisplayResponse
import com.example.datemomo.utility.Utility
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.android.synthetic.main.activity_home_display.view.*
import okhttp3.*
import java.io.IOException

class HomeDisplayAdapter(private val homeDisplayResponses: Array<HomeDisplayResponse>, private val homeDisplayModel: HomeDisplayModel) :
    RecyclerView.Adapter<HomeDisplayAdapter.MyViewHolder>() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferencesEditor: SharedPreferences.Editor

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = RecyclerHomeDisplayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        sharedPreferences =
            holder.itemView.context.getSharedPreferences(holder
                .itemView.context.getString(R.string.shared_preferences), Context.MODE_PRIVATE)
        sharedPreferencesEditor = sharedPreferences.edit()

        val allImageWidth = homeDisplayModel.deviceWidth - Utility.dimen(holder.itemView.context, 23f)
        val allImageHeight = (/* imageHeight */ 788 * (homeDisplayModel.deviceWidth -
                Utility.dimen(holder.itemView.context, 23f))) / /* imageWidth */ 788

        holder.binding.userImage.layoutParams.width = allImageWidth
        holder.binding.userImage.layoutParams.height = allImageHeight
        holder.binding.userImageBack.layoutParams.width = allImageWidth
        holder.binding.userImageBack.layoutParams.height = allImageHeight
        holder.binding.userImageLayout.layoutParams.width = allImageWidth
        holder.binding.userImageLayout.layoutParams.height = allImageHeight

        Glide.with(holder.itemView.context)
            .asGif()
            .load(R.drawable.motion_placeholder)
            .transform(RoundedCorners(15))
            .into(holder.binding.userImageBack)

        Log.e("AdapterClass", "Image properties here are ${homeDisplayResponses[position].userPictureModels[0].imageName}")

        // Add profilePicture properties to homeDisplayResponses
        // Then, add collection of all user images, with their properties to homeDisplayResponses

        holder.binding.userImage.setOnClickListener {
            processUserDataView(holder.itemView.context, position)
        }

        holder.binding.userImageBack.setOnClickListener {
            processUserDataView(holder.itemView.context, position)
        }

        holder.binding.userImageLayout.setOnClickListener {
            processUserDataView(holder.itemView.context, position)
        }

        holder.binding.userFullNameLayout.setOnClickListener {
            processUserDataView(holder.itemView.context, position)
        }

/*
        Glide.with(holder.itemView.context)
            .load(ContextCompat.getDrawable(holder.itemView.context, homeDisplayImages[position].imageId))
            .thumbnail(Glide.with(holder.itemView.context).load(R.drawable.motion_placeholder))
            .transform(FitCenter(), RoundedCorners(33))
            .into(holder.binding.userImage);
*/

        if (position == (itemCount - 1)) {
            val userDisplayLayoutParam = holder.binding.userDisplayLayout.layoutParams as ViewGroup.MarginLayoutParams
            userDisplayLayoutParam.bottomMargin = 25;
            holder.binding.userDisplayLayout.layoutParams = userDisplayLayoutParam
        }

        if (homeDisplayResponses[position].liked) {
            holder.binding.loveUserIcon.setImageDrawable(
                ContextCompat.getDrawable(
                    holder.itemView.context,
                    R.drawable.icon_heart_red
                )
            )
        } else {
            holder.binding.loveUserIcon.setImageDrawable(
                ContextCompat.getDrawable(
                    holder.itemView.context,
                    R.drawable.icon_heart_hollow
                )
            )
        }

        holder.binding.loveUserLayout.setOnClickListener {
            homeDisplayResponses[position].liked = !homeDisplayResponses[position].liked
            notifyItemChanged(position)

            processUserLike(holder.itemView.context, position)

            /*
            * notifyItemInserted(insertIndex)
            * notifyItemRangeInserted(insertIndex, items.size())
            * notifyItemChanged(updateIndex)
            * notifyItemRemoved(removeIndex)
            * notifyItemRangeRemoved(startIndex, count)
            * notifyItemMoved(fromPosition, toPosition)
            * notifyDataSetChanged()
            * */
        }

        Glide.with(holder.itemView.context)
            .load(holder.itemView.context.getString(R.string.date_momo_api)
                    + "client/image/" + homeDisplayResponses[position].profilePicture)
            .transform(CenterCrop(), RoundedCorners(33))
            .into(holder.binding.userImage)

        if (homeDisplayResponses[position].fullName.isEmpty()) {
            holder.binding.userFullName.text =
                holder.itemView.context.getString(R.string.nameAndAgeText,
                    homeDisplayResponses[position].userName, homeDisplayResponses[position].age)
        } else {
            holder.binding.userFullName.text =
                holder.itemView.context.getString(R.string.nameAndAgeText,
                    homeDisplayResponses[position].fullName, homeDisplayResponses[position].age)
        }
    }

    override fun getItemCount(): Int {
        return homeDisplayResponses.size
    }

    class MyViewHolder(val binding: RecyclerHomeDisplayBinding) :
        RecyclerView.ViewHolder(binding.root)

    private fun processUserDataView(context: Context, position: Int) {
        var userPicturePosition = 0

        for ((index, userPictureModel) in homeDisplayResponses[position].userPictureModels.withIndex()) {
            if (userPictureModel.imageName == homeDisplayResponses[position].profilePicture) {
                userPicturePosition = index
            }
        }

        homeDisplayResponses[position].userPictureModels

        val imageWidth = homeDisplayModel.deviceWidth - Utility.dimen(context, 23f)
        val imageHeight = (homeDisplayResponses[position].userPictureModels[userPicturePosition].imageHeight *
                (homeDisplayModel.deviceWidth - Utility.dimen(context, 23f))) /
                homeDisplayResponses[position].userPictureModels[userPicturePosition].imageWidth

        homeDisplayModel.binding.userImageContainer.layoutParams.width = imageWidth
        homeDisplayModel.binding.userImageContainer.layoutParams.height = imageHeight
        homeDisplayModel.binding.userImagePlaceholder.layoutParams.width = imageWidth
        homeDisplayModel.binding.userInformationImage.layoutParams.width = imageWidth
        homeDisplayModel.binding.userImagePlaceholder.layoutParams.height = imageHeight
        homeDisplayModel.binding.userInformationImage.layoutParams.height = imageHeight

        if (homeDisplayResponses[position].fullName.isEmpty()) {
            homeDisplayModel.binding.userFullName.text =
                context.getString(
                    R.string.nameAndAgeText,
                    homeDisplayResponses[position].userName,
                    homeDisplayResponses[position].age
                )
        } else {
            homeDisplayModel.binding.userFullName.text =
                context.getString(
                    R.string.nameAndAgeText,
                    homeDisplayResponses[position].fullName,
                    homeDisplayResponses[position].age
                )
        }

        Glide.with(context)
            .load(ColorDrawable(ContextCompat.getColor(context, R.color.grey_picture_placeholder)))
            .transform(RoundedCorners(37))
            .into(homeDisplayModel.binding.userImagePlaceholder)

        Glide.with(context)
            .load(context.getString(R.string.date_momo_api) + "client/image/" + homeDisplayResponses[position].profilePicture)
            .transform(RoundedCorners(37))
            .into(homeDisplayModel.binding.userInformationImage)

        if (homeDisplayResponses[position].bisexualCategory > 0) {
            homeDisplayModel.binding.userBisexual.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponses[position].gayCategory > 0) {
            homeDisplayModel.binding.userGay.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponses[position].lesbianCategory > 0) {
            homeDisplayModel.binding.userLesbian.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponses[position].straightCategory > 0) {
            homeDisplayModel.binding.userStraight.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponses[position].sugarDaddyCategory > 0) {
            homeDisplayModel.binding.userSugarDaddy.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponses[position].sugarMommyCategory > 0) {
            homeDisplayModel.binding.userSugarMommy.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponses[position].toyBoyCategory > 0) {
            homeDisplayModel.binding.userToyBoy.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponses[position].toyGirlCategory > 0) {
            homeDisplayModel.binding.userToyGirl.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponses[position].bisexualInterest > 0) {
            homeDisplayModel.binding.bisexualInterest.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponses[position].gayInterest > 0) {
            homeDisplayModel.binding.gayInterest.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponses[position].straightInterest > 0) {
            homeDisplayModel.binding.straightInterest.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponses[position].lesbianInterest > 0) {
            homeDisplayModel.binding.lesbianInterest.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponses[position].sugarDaddyInterest > 0) {
            homeDisplayModel.binding.sugarDaddyInterest.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponses[position].sugarMommyInterest > 0) {
            homeDisplayModel.binding.sugarMommyInterest.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponses[position].toyBoyInterest > 0) {
            homeDisplayModel.binding.toyBoyInterest.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponses[position].toyGirlInterest > 0) {
            homeDisplayModel.binding.toyGirlInterest.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponses[position].analSexExperience > 0) {
            homeDisplayModel.binding.analSexExperience.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponses[position].sixtyNineExperience > 0) {
            homeDisplayModel.binding.sixtyNineExperience.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponses[position].cameraSexExperience > 0) {
            homeDisplayModel.binding.cameraSexExperience.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponses[position].carSexExperience > 0) {
            homeDisplayModel.binding.carSexExperience.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponses[position].threesomeExperience > 0) {
            homeDisplayModel.binding.threesomeExperience.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponses[position].givenHeadExperience > 0) {
            homeDisplayModel.binding.givenHeadExperience.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponses[position].receivedHeadExperience > 0) {
            homeDisplayModel.binding.receivedHeadExperience.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponses[position].oneNightStandExperience > 0) {
            homeDisplayModel.binding.oneNightStandExperience.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponses[position].orgySexExperience > 0) {
            homeDisplayModel.binding.orgySexExperience.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponses[position].poolSexExperience > 0) {
            homeDisplayModel.binding.poolSexExperience.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponses[position].sexToyExperience > 0) {
            homeDisplayModel.binding.sexToyExperience.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponses[position].videoSexExperience > 0) {
            homeDisplayModel.binding.videoSexExperience.blueButtonLayout.visibility = View.VISIBLE
        }

        if (homeDisplayResponses[position].publicSexExperience > 0) {
            homeDisplayModel.binding.publicSexExperience.blueButtonLayout.visibility = View.VISIBLE
        }

        homeDisplayModel.binding.userInformationLayout.visibility = View.VISIBLE
    }

    @Throws(IOException::class)
    private fun processUserLike(context: Context, position: Int) {
        val mapper = jacksonObjectMapper()
        val likeUserRequest = LikeUserRequest(
            sharedPreferences.getInt("memberId", 0),
            homeDisplayResponses[position].liked,
            homeDisplayResponses[position].memberId)

        val jsonObjectString = mapper.writeValueAsString(likeUserRequest)
        val requestBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            jsonObjectString
        )

        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(context.getString(R.string.date_momo_api) + "service/likeuser.php")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()
            }

            override fun onResponse(call: Call, response: Response) {
                val myResponse: String = response.body()!!.string()
                var committedResponse = CommittedResponse(false)

                try {
                    committedResponse = mapper.readValue(myResponse)
                } catch (exception: IOException) {
                    Log.e(TAG, "Exception from processLikeUser is ${exception.message}")
                }
            }
        })
    }

    companion object {
        const val TAG = "HomeDisplayAdapter"
    }
}


