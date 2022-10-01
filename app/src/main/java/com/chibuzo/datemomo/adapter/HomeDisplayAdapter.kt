package com.chibuzo.datemomo.adapter

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
import com.chibuzo.datemomo.R
import com.chibuzo.datemomo.databinding.RecyclerHomeDisplayBinding
import com.chibuzo.datemomo.model.HomeDisplayModel
import com.chibuzo.datemomo.model.request.*
import com.chibuzo.datemomo.model.response.CommittedResponse
import com.chibuzo.datemomo.model.response.HomeDisplayResponse
import com.chibuzo.datemomo.utility.Utility
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.*
import java.io.IOException

class HomeDisplayAdapter(private val homeDisplayResponses: ArrayList<HomeDisplayResponse>, private val homeDisplayModel: HomeDisplayModel) :
    RecyclerView.Adapter<HomeDisplayAdapter.MyViewHolder>() {
    private lateinit var messageRequest: MessageRequest
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var userInformationRequest: UserInformationRequest
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

        // Add profilePicture properties to homeDisplayResponses
        // Then, add collection of all user images, with their properties to homeDisplayResponses

        homeDisplayModel.binding.userImageContainer.setOnClickListener {
            homeDisplayModel.requestProcess = holder.itemView.context.getString(R.string.request_fetch_user_pictures)
            val userPictureRequest = UserPictureRequest(this.messageRequest.receiverId)
            Log.e(TAG, "ReceiverId value here is ${this.messageRequest.receiverId}")
            homeDisplayModel.homeDisplayActivity.fetchUserPictures(userPictureRequest)
        }

        homeDisplayModel.binding.profileDisplayButton.iconHollowButtonLayout.setOnClickListener {
            homeDisplayModel.binding.profileDisplayButton.iconHollowButtonLayout.startAnimation(homeDisplayModel.buttonClickEffect)
            homeDisplayModel.requestProcess = holder.itemView.context.getString(R.string.request_fetch_user_information)
            homeDisplayModel.homeDisplayActivity.fetchUserInformation(userInformationRequest)
        }

        homeDisplayModel.binding.userMessageButton.iconHollowButtonLayout.setOnClickListener {
            homeDisplayModel.binding.userMessageButton.iconHollowButtonLayout.startAnimation(homeDisplayModel.buttonClickEffect)
            homeDisplayModel.requestProcess = holder.itemView.context.getString(R.string.request_fetch_user_messages)
            homeDisplayModel.homeDisplayActivity.fetchUserMessages(messageRequest)
        }

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
            holder.binding.loveUserIcon.startAnimation(homeDisplayModel.bounceAnimation)

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
                    + holder.itemView.context.getString(R.string.api_image)
                    + homeDisplayResponses[position].profilePicture)
            .transform(CenterCrop(), RoundedCorners(33))
            .into(holder.binding.userImage)

        holder.binding.userLocation.text =
            homeDisplayResponses[position].currentLocation.ifEmpty { "Location Not Set" }

        if (homeDisplayResponses[position].currentLocation.isEmpty()) {
            holder.binding.userLocation.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.edit_text_hint))
        } else {
            holder.binding.userLocation.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.edit_text))
        }

        if (homeDisplayResponses[position].fullName.isEmpty()) {
            holder.binding.userFullName.text =
                holder.itemView.context.getString(R.string.name_and_age_text,
                    homeDisplayResponses[position].userName.replaceFirstChar { it.uppercase() },
                    homeDisplayResponses[position].age)
        } else {
            holder.binding.userFullName.text =
                holder.itemView.context.getString(R.string.name_and_age_text,
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
        this.messageRequest = MessageRequest(
            sharedPreferences.getInt("memberId", 0),
            homeDisplayResponses[position].memberId,
            homeDisplayResponses[position].fullName,
            homeDisplayResponses[position].userName, "",
            homeDisplayResponses[position].profilePicture,
            homeDisplayResponses[position].userBlockedStatus)
        this.userInformationRequest = UserInformationRequest(
            homeDisplayResponses[position].memberId
        )

        for ((index, userPictureModel) in homeDisplayResponses[position].userPictureModels.withIndex()) {
            if (userPictureModel.imageName == homeDisplayResponses[position].profilePicture) {
                userPicturePosition = index
            }
        }

        sharedPreferencesEditor.putBoolean(context.getString(R.string.user_information_layout_visible), true)
        sharedPreferencesEditor.apply()

        var imageHeight = 0
        val imageWidth = homeDisplayModel.deviceWidth - Utility.dimen(context, 23f)

        try {
            imageHeight =
                (homeDisplayResponses[position].userPictureModels[userPicturePosition].imageHeight *
                        (homeDisplayModel.deviceWidth - Utility.dimen(context, 23f))) /
                        homeDisplayResponses[position].userPictureModels[userPicturePosition].imageWidth
        } catch (exception: IndexOutOfBoundsException) {
            exception.printStackTrace()
            Log.e(TAG, "Error message from selecting from array here is ${exception.message}")
        }

        homeDisplayModel.binding.userImageContainer.layoutParams.width = imageWidth
        homeDisplayModel.binding.userImageContainer.layoutParams.height = imageHeight
        homeDisplayModel.binding.userImagePlaceholder.layoutParams.width = imageWidth
        homeDisplayModel.binding.userInformationImage.layoutParams.width = imageWidth
        homeDisplayModel.binding.userImagePlaceholder.layoutParams.height = imageHeight
        homeDisplayModel.binding.userInformationImage.layoutParams.height = imageHeight

        homeDisplayModel.binding.userStatusText.text = homeDisplayResponses[position].userStatus
        homeDisplayModel.binding.userLocation.text =
            homeDisplayResponses[position].currentLocation.ifEmpty { "Location Not Set" }

        val userFullName = homeDisplayResponses[position].fullName.ifEmpty {
            homeDisplayResponses[position].userName.replaceFirstChar { it.uppercase() }
        }

        homeDisplayModel.binding.userFullName.text =
            context.getString(R.string.name_and_age_text, userFullName, homeDisplayResponses[position].age)
        homeDisplayModel.binding.userInterestTitle.text = context.getString(R.string.title_interest, userFullName)
        homeDisplayModel.binding.userSexualityTitle.text = context.getString(R.string.title_sexuality, userFullName)
        homeDisplayModel.binding.userExperienceTitle.text = context.getString(R.string.title_experience, userFullName)

        Glide.with(context)
            .load(ColorDrawable(ContextCompat.getColor(context, R.color.grey_picture_placeholder)))
            .transform(RoundedCorners(37))
            .into(homeDisplayModel.binding.userImagePlaceholder)

        Glide.with(context)
            .load(context.getString(R.string.date_momo_api)
                    + context.getString(R.string.api_image)
                    + homeDisplayResponses[position].profilePicture)
            .transform(RoundedCorners(37))
            .into(homeDisplayModel.binding.userInformationImage)

        homeDisplayModel.binding.userBisexual.blueLabelLayout.visibility =
            if (homeDisplayResponses[position].bisexualCategory > 0) { View.VISIBLE } else { View.GONE }

        homeDisplayModel.binding.userGay.blueLabelLayout.visibility =
            if (homeDisplayResponses[position].gayCategory > 0) { View.VISIBLE } else { View.GONE }

        homeDisplayModel.binding.userLesbian.blueLabelLayout.visibility =
            if (homeDisplayResponses[position].lesbianCategory > 0) { View.VISIBLE } else { View.GONE }

        homeDisplayModel.binding.userStraight.blueLabelLayout.visibility =
            if (homeDisplayResponses[position].straightCategory > 0) { View.VISIBLE } else { View.GONE }

        homeDisplayModel.binding.userSugarDaddy.blueLabelLayout.visibility =
            if (homeDisplayResponses[position].sugarDaddyCategory > 0) { View.VISIBLE } else { View.GONE }

        homeDisplayModel.binding.userSugarMommy.blueLabelLayout.visibility =
            if (homeDisplayResponses[position].sugarMommyCategory > 0) { View.VISIBLE } else { View.GONE }

        homeDisplayModel.binding.userToyBoy.blueLabelLayout.visibility =
            if (homeDisplayResponses[position].toyBoyCategory > 0) { View.VISIBLE } else { View.GONE }

        homeDisplayModel.binding.userToyGirl.blueLabelLayout.visibility =
            if (homeDisplayResponses[position].toyGirlCategory > 0) { View.VISIBLE } else { View.GONE }

        homeDisplayModel.binding.bisexualInterest.blueLabelLayout.visibility =
            if (homeDisplayResponses[position].bisexualInterest > 0) { View.VISIBLE } else { View.GONE }

        homeDisplayModel.binding.friendshipInterest.blueLabelLayout.visibility =
            if (homeDisplayResponses[position].friendshipInterest > 0) { View.VISIBLE } else { View.GONE }

        homeDisplayModel.binding.gayInterest.blueLabelLayout.visibility =
            if (homeDisplayResponses[position].gayInterest > 0) { View.VISIBLE } else { View.GONE }

        homeDisplayModel.binding.straightInterest.blueLabelLayout.visibility =
            if (homeDisplayResponses[position].straightInterest > 0) { View.VISIBLE } else { View.GONE }

        homeDisplayModel.binding.relationshipInterest.blueLabelLayout.visibility =
            if (homeDisplayResponses[position].relationshipInterest > 0) { View.VISIBLE } else { View.GONE }

        homeDisplayModel.binding.lesbianInterest.blueLabelLayout.visibility =
            if (homeDisplayResponses[position].lesbianInterest > 0) { View.VISIBLE } else { View.GONE }

        homeDisplayModel.binding.sugarDaddyInterest.blueLabelLayout.visibility =
            if (homeDisplayResponses[position].sugarDaddyInterest > 0) { View.VISIBLE } else { View.GONE }

        homeDisplayModel.binding.sugarMommyInterest.blueLabelLayout.visibility =
            if (homeDisplayResponses[position].sugarMommyInterest > 0) { View.VISIBLE } else { View.GONE }

        homeDisplayModel.binding.toyBoyInterest.blueLabelLayout.visibility =
            if (homeDisplayResponses[position].toyBoyInterest > 0) { View.VISIBLE } else { View.GONE }

        homeDisplayModel.binding.toyGirlInterest.blueLabelLayout.visibility =
            if (homeDisplayResponses[position].toyGirlInterest > 0) { View.VISIBLE } else { View.GONE }

        homeDisplayModel.binding.analSexExperience.blueLabelLayout.visibility =
            if (homeDisplayResponses[position].analSexExperience > 0) { View.VISIBLE } else { View.GONE }

        homeDisplayModel.binding.missionaryExperience.blueLabelLayout.visibility =
            if (homeDisplayResponses[position].missionaryExperience > 0) { View.VISIBLE } else { View.GONE }

        homeDisplayModel.binding.sixtyNineExperience.blueLabelLayout.visibility =
            if (homeDisplayResponses[position].sixtyNineExperience > 0) { View.VISIBLE } else { View.GONE }

        homeDisplayModel.binding.cameraSexExperience.blueLabelLayout.visibility =
            if (homeDisplayResponses[position].cameraSexExperience > 0) { View.VISIBLE } else { View.GONE }

        homeDisplayModel.binding.carSexExperience.blueLabelLayout.visibility =
            if (homeDisplayResponses[position].carSexExperience > 0) { View.VISIBLE } else { View.GONE }

        homeDisplayModel.binding.threesomeExperience.blueLabelLayout.visibility =
            if (homeDisplayResponses[position].threesomeExperience > 0) { View.VISIBLE } else { View.GONE }

        homeDisplayModel.binding.givenHeadExperience.blueLabelLayout.visibility =
            if (homeDisplayResponses[position].givenHeadExperience > 0) { View.VISIBLE } else { View.GONE }

        homeDisplayModel.binding.receivedHeadExperience.blueLabelLayout.visibility =
            if (homeDisplayResponses[position].receivedHeadExperience > 0) { View.VISIBLE } else { View.GONE }

        homeDisplayModel.binding.oneNightStandExperience.blueLabelLayout.visibility =
            if (homeDisplayResponses[position].oneNightStandExperience > 0) { View.VISIBLE } else { View.GONE }

        homeDisplayModel.binding.orgySexExperience.blueLabelLayout.visibility =
            if (homeDisplayResponses[position].orgySexExperience > 0) { View.VISIBLE } else { View.GONE }

        homeDisplayModel.binding.poolSexExperience.blueLabelLayout.visibility =
            if (homeDisplayResponses[position].poolSexExperience > 0) { View.VISIBLE } else { View.GONE }

        homeDisplayModel.binding.sexToyExperience.blueLabelLayout.visibility =
            if (homeDisplayResponses[position].sexToyExperience > 0) { View.VISIBLE } else { View.GONE }

        homeDisplayModel.binding.videoSexExperience.blueLabelLayout.visibility =
            if (homeDisplayResponses[position].videoSexExperience > 0) { View.VISIBLE } else { View.GONE }

        homeDisplayModel.binding.publicSexExperience.blueLabelLayout.visibility =
            if (homeDisplayResponses[position].publicSexExperience > 0) { View.VISIBLE } else { View.GONE }

        homeDisplayModel.binding.userInformationLayout.visibility = View.VISIBLE
    }

    @Throws(IOException::class)
    private fun notifyLikedUser(context: Context, position: Int) {
        val notifierName =
            sharedPreferences.getString(context.getString(R.string.full_name), "").toString().ifEmpty {
                sharedPreferences.getString(context.getString(R.string.user_name), "")
            }

        val genericNotification = "{$notifierName} reacted to your profile picture"

        val mapper = jacksonObjectMapper()
        val notifyUserRequest = NotifyUserRequest(
            context.getString(R.string.profile_picture_like),
            homeDisplayResponses[position].memberId,
            genericNotification,
            sharedPreferences.getInt(context.getString(R.string.member_id), 0),
            "")

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        val jsonObjectString = mapper.writeValueAsString(notifyUserRequest)
        val requestBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            jsonObjectString
        )

        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(context.getString(R.string.date_momo_api) +
                    context.getString(R.string.api_notify_profile_picture_owner))
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
                    exception.printStackTrace()
                    Log.e(TAG, "Exception from processLikeUser is ${exception.message}")
                }
            }
        })
    }

    @Throws(IOException::class)
    private fun processUserLike(context: Context, position: Int) {
        val mapper = jacksonObjectMapper()
        val likeUserRequest = LikeUserRequest(
            sharedPreferences.getInt(context.getString(R.string.member_id), 0),
            homeDisplayResponses[position].liked,
            homeDisplayResponses[position].memberId)

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        val jsonObjectString = mapper.writeValueAsString(likeUserRequest)
        val requestBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            jsonObjectString
        )

        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(context.getString(R.string.date_momo_api) + context.getString(R.string.api_like_user))
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

                    if (committedResponse.committed) {
                        notifyLikedUser(context, position)
                    }
                } catch (exception: IOException) {
                    exception.printStackTrace()
                    Log.e(TAG, "Exception from processLikeUser is ${exception.message}")
                }
            }
        })
    }

    companion object {
        const val TAG = "HomeDisplayAdapter"
    }
}


