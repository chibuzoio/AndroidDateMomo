package com.chibuzo.datemomo.adapter

import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.chibuzo.datemomo.R
import com.chibuzo.datemomo.databinding.RecyclerHomeDisplayBinding
import com.chibuzo.datemomo.model.GalleryPictureModel
import com.chibuzo.datemomo.model.HomeDisplayModel
import com.chibuzo.datemomo.model.PictureCollectionModel
import com.chibuzo.datemomo.model.request.*
import com.chibuzo.datemomo.model.response.CommittedResponse
import com.chibuzo.datemomo.model.response.HomeDisplayResponse
import com.chibuzo.datemomo.model.response.UserPictureResponse
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

        holder.binding.profilePictureCounter.text = homeDisplayResponses[position].userPictureResponses.size.toString()

        Glide.with(holder.itemView.context)
            .asGif()
            .load(R.drawable.motion_placeholder)
            .transform(RoundedCorners(15))
            .into(holder.binding.userImageBack)

        // Add profilePicture properties to homeDisplayResponses
        // Then, add collection of all user images, with their properties to homeDisplayResponses

        homeDisplayModel.binding.userImageContainer.setOnClickListener {
            homeDisplayModel.requestProcess = holder.itemView.context.getString(R.string.request_fetch_user_pictures)
            val userPictureRequest = UserPictureRequest(
                memberId = this.messageRequest.receiverId,
                currentPosition = 0)

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
            * notifyItemRangeChanged(fromIndex, itemCount)
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

        for ((index, userPictureModel) in homeDisplayResponses[position].userPictureResponses.withIndex()) {
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
                (homeDisplayResponses[position].userPictureResponses[userPicturePosition].imageHeight *
                        (homeDisplayModel.deviceWidth - Utility.dimen(context, 23f))) /
                        homeDisplayResponses[position].userPictureResponses[userPicturePosition].imageWidth
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

        var galleryLayoutHeight = 0
        val userPictureResponses = arrayListOf<UserPictureResponse>()
        userPictureResponses.addAll(homeDisplayResponses[position].userPictureResponses)

        val pictureCollectionModels = slicePictureResponses(context, userPictureResponses)

        for (pictureCollectionModel in pictureCollectionModels) {
            when (pictureCollectionModel.pictureLayoutType) {
                context.getString(R.string.picture_layout_single) ->
                    galleryLayoutHeight += homeDisplayModel.floatingGalleryModel.singlePictureLayoutHeight
                context.getString(R.string.picture_layout_double_left) ->
                    galleryLayoutHeight += homeDisplayModel.floatingGalleryModel.doubleLeftRightLayoutHeight
                context.getString(R.string.picture_layout_double_right) ->
                    galleryLayoutHeight += homeDisplayModel.floatingGalleryModel.doubleLeftRightLayoutHeight
                context.getString(R.string.picture_layout_triple_bottom) ->
                    galleryLayoutHeight += homeDisplayModel.floatingGalleryModel.tripleBottomLayoutHeight
            }
        }

        homeDisplayModel.floatingGalleryModel.floatingGalleryLayoutHeight = galleryLayoutHeight
        homeDisplayModel.floatingGalleryModel.profileOwnerId = homeDisplayResponses[position].memberId

        val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        homeDisplayModel.binding.pictureCollectionRecycler.layoutManager = layoutManager
        homeDisplayModel.binding.pictureCollectionRecycler.itemAnimator = DefaultItemAnimator()

        val pictureCollectionAdapter = PictureCollectionAdapter(pictureCollectionModels, homeDisplayModel.floatingGalleryModel)
        homeDisplayModel.binding.pictureCollectionRecycler.adapter = pictureCollectionAdapter
        homeDisplayModel.binding.pictureCollectionRecycler.layoutParams.height = galleryLayoutHeight
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
                val committedResponse: CommittedResponse

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

    private fun slicePictureResponses(context: Context, userPictureResponses: ArrayList<UserPictureResponse>):
            ArrayList<PictureCollectionModel> {
        val imagePositionCollection = arrayListOf<Int>()
        val pictureLayoutTypes: ArrayList<String> = arrayListOf()
        val pictureCollectionModels: ArrayList<PictureCollectionModel> = arrayListOf()

        for ((index, _) in userPictureResponses.withIndex()) {
            imagePositionCollection.add(index)
        }

        userPictureResponses.reverse()

        recurSlicePictureResponses(context, pictureLayoutTypes,
            userPictureResponses, pictureCollectionModels, imagePositionCollection)

        return pictureCollectionModels
    }

    private fun recurSlicePictureResponses(context: Context, pictureLayoutTypes: ArrayList<String>,
                                           userPictureResponses: ArrayList<UserPictureResponse>,
                                           pictureCollectionModels: ArrayList<PictureCollectionModel>,
                                           imagePositionCollection: ArrayList<Int>) {
        try {
            if (userPictureResponses.size == 1) {
                val galleryPictureModel =
                    GalleryPictureModel(imagePositionCollection[0], userPictureResponses[0])
                val pictureCollectionModel = PictureCollectionModel(
                    pictureLayoutType = context.getString(R.string.picture_layout_single),
                    galleryPictureModels = arrayListOf(galleryPictureModel)
                )

                pictureLayoutTypes.add(context.getString(R.string.picture_layout_single))
                pictureCollectionModels.add(pictureCollectionModel)
            }

            if (userPictureResponses.size == 2) {
                var galleryPictureModel =
                    GalleryPictureModel(imagePositionCollection[0], userPictureResponses[0])
                var pictureCollectionModel = PictureCollectionModel(
                    pictureLayoutType = context.getString(R.string.picture_layout_single),
                    galleryPictureModels = arrayListOf(galleryPictureModel)
                )

                pictureLayoutTypes.add(context.getString(R.string.picture_layout_single))
                pictureCollectionModels.add(pictureCollectionModel)

                galleryPictureModel =
                    GalleryPictureModel(imagePositionCollection[1], userPictureResponses[1])
                pictureCollectionModel = PictureCollectionModel(
                    pictureLayoutType = context.getString(R.string.picture_layout_single),
                    galleryPictureModels = arrayListOf(galleryPictureModel)
                )

                pictureLayoutTypes.add(context.getString(R.string.picture_layout_single))
                pictureCollectionModels.add(pictureCollectionModel)
            }

            if (userPictureResponses.size == 3) {
                var pictureDisplayLayoutType = ""

                if (pictureLayoutTypes.isNotEmpty()) {
                    if (pictureLayoutTypes.size > 1) {
                        var containsLeftPictureLayout = false
                        var containsRightPictureLayout = false
                        val lastTwoPictureLayouts: ArrayList<String> = arrayListOf()
                        lastTwoPictureLayouts.add(pictureLayoutTypes[pictureLayoutTypes.size - 1])
                        lastTwoPictureLayouts.add(pictureLayoutTypes[pictureLayoutTypes.size - 2])

                        if (lastTwoPictureLayouts.contains(context.getString(R.string.picture_layout_double_right))) {
                            containsRightPictureLayout = true
                        }

                        if (lastTwoPictureLayouts.contains(context.getString(R.string.picture_layout_double_left))) {
                            containsLeftPictureLayout = true
                        }

                        if (containsLeftPictureLayout || containsRightPictureLayout) {
                            if (containsLeftPictureLayout && containsRightPictureLayout) {
                                if (pictureLayoutTypes[pictureLayoutTypes.size - 1] == context.getString(R.string.picture_layout_double_left)) {
                                    pictureDisplayLayoutType = context.getString(R.string.picture_layout_double_right)
                                }

                                if (pictureLayoutTypes[pictureLayoutTypes.size - 1] == context.getString(R.string.picture_layout_double_right)) {
                                    pictureDisplayLayoutType = context.getString(R.string.picture_layout_double_left)
                                }
                            } else {
                                if (!containsRightPictureLayout) {
                                    pictureDisplayLayoutType = context.getString(R.string.picture_layout_double_right)
                                }

                                if (!containsLeftPictureLayout) {
                                    pictureDisplayLayoutType = context.getString(R.string.picture_layout_double_left)
                                }
                            }
                        }

                        if (!containsLeftPictureLayout && !containsRightPictureLayout) {
                            pictureDisplayLayoutType = context.getString(R.string.picture_layout_double_left)
                        }
                    } else {
                        pictureDisplayLayoutType = if (pictureLayoutTypes[0] == context.getString(R.string.picture_layout_double_left)) {
                            context.getString(R.string.picture_layout_double_right)
                        } else if (pictureLayoutTypes[0] == context.getString(R.string.picture_layout_double_right)) {
                            context.getString(R.string.picture_layout_double_left)
                        } else {
                            context.getString(R.string.picture_layout_double_left)
                        }
                    }
                } else {
                    pictureDisplayLayoutType = context.getString(R.string.picture_layout_double_left)
                }

                pictureLayoutTypes.add(pictureDisplayLayoutType)

                val galleryPictureModels = arrayListOf(
                    GalleryPictureModel(imagePositionCollection[0], userPictureResponses[0]),
                    GalleryPictureModel(imagePositionCollection[1], userPictureResponses[1]),
                    GalleryPictureModel(imagePositionCollection[2], userPictureResponses[2]))
                val pictureCollectionModel = PictureCollectionModel(
                    pictureLayoutType = pictureDisplayLayoutType.ifEmpty { context.getString(R.string.picture_layout_double_left) },
                    galleryPictureModels = galleryPictureModels
                )

                pictureCollectionModels.add(pictureCollectionModel)
            }

            if (userPictureResponses.size == 4) {
                var pictureDisplayLayoutType = ""

                if (pictureLayoutTypes.isNotEmpty()) {
                    if (pictureLayoutTypes.size > 1) {
                        val lastTwoPictureLayouts: ArrayList<String> = arrayListOf()
                        lastTwoPictureLayouts.add(pictureLayoutTypes[pictureLayoutTypes.size - 1])
                        lastTwoPictureLayouts.add(pictureLayoutTypes[pictureLayoutTypes.size - 2])

                        pictureDisplayLayoutType = if (lastTwoPictureLayouts.contains(context.getString(R.string.picture_layout_triple_bottom))) {
                            if (lastTwoPictureLayouts.contains(context.getString(R.string.picture_layout_double_right))) {
                                context.getString(R.string.picture_layout_double_left)
                            } else {
                                context.getString(R.string.picture_layout_double_right)
                            }
                        } else {
                            context.getString(R.string.picture_layout_triple_bottom)
                        }
                    } else {
                        pictureDisplayLayoutType = if (pictureLayoutTypes[0] == context.getString(R.string.picture_layout_triple_bottom)) {
                            context.getString(R.string.picture_layout_double_left)
                        } else {
                            context.getString(R.string.picture_layout_triple_bottom)
                        }
                    }
                } else {
                    pictureDisplayLayoutType = context.getString(R.string.picture_layout_triple_bottom)
                }

                pictureLayoutTypes.add(pictureDisplayLayoutType)

                if (pictureDisplayLayoutType == context.getString(R.string.picture_layout_triple_bottom)) {
                    val galleryPictureModels = arrayListOf(
                        GalleryPictureModel(imagePositionCollection[0], userPictureResponses[0]),
                        GalleryPictureModel(imagePositionCollection[1], userPictureResponses[1]),
                        GalleryPictureModel(imagePositionCollection[2], userPictureResponses[2]),
                        GalleryPictureModel(imagePositionCollection[3], userPictureResponses[3]))
                    val pictureCollectionModel = PictureCollectionModel(
                        pictureLayoutType = context.getString(R.string.picture_layout_triple_bottom),
                        galleryPictureModels = galleryPictureModels
                    )

                    pictureCollectionModels.add(pictureCollectionModel)
                } else {
                    val galleryPictureModels = arrayListOf(
                        GalleryPictureModel(imagePositionCollection[0], userPictureResponses[0]),
                        GalleryPictureModel(imagePositionCollection[1], userPictureResponses[1]),
                        GalleryPictureModel(imagePositionCollection[2], userPictureResponses[2]))
                    val pictureCollectionModel = PictureCollectionModel(
                        pictureLayoutType = pictureDisplayLayoutType,
                        galleryPictureModels = galleryPictureModels)

                    pictureCollectionModels.add(pictureCollectionModel)

                    userPictureResponses.removeAt(2)
                    userPictureResponses.removeAt(1)
                    userPictureResponses.removeAt(0)

                    imagePositionCollection.removeAt(2)
                    imagePositionCollection.removeAt(1)
                    imagePositionCollection.removeAt(0)

                    recurSlicePictureResponses(context, pictureLayoutTypes,
                        userPictureResponses, pictureCollectionModels, imagePositionCollection)
                }
            }

            if (userPictureResponses.size > 4) {
                var pictureDisplayLayoutType = ""

                if (pictureLayoutTypes.isNotEmpty()) {
                    var containsLeftPictureLayout = false
                    var containsRightPictureLayout = false
                    var containsTripleBottomPictureLayout = false

                    if (pictureLayoutTypes.size > 1) {
                        val lastTwoPictureLayouts: ArrayList<String> = arrayListOf()
                        lastTwoPictureLayouts.add(pictureLayoutTypes[pictureLayoutTypes.size - 1])
                        lastTwoPictureLayouts.add(pictureLayoutTypes[pictureLayoutTypes.size - 2])

                        if (lastTwoPictureLayouts.contains(context.getString(R.string.picture_layout_double_left))) {
                            containsLeftPictureLayout = true
                        }

                        if (lastTwoPictureLayouts.contains(context.getString(R.string.picture_layout_double_right))) {
                            containsRightPictureLayout = true
                        }

                        if (lastTwoPictureLayouts.contains(context.getString(R.string.picture_layout_triple_bottom))) {
                            containsTripleBottomPictureLayout = true
                        }

                        if (containsLeftPictureLayout && containsRightPictureLayout) {
                            pictureDisplayLayoutType = context.getString(R.string.picture_layout_triple_bottom)
                        }

                        if (containsLeftPictureLayout && containsTripleBottomPictureLayout) {
                            pictureDisplayLayoutType = context.getString(R.string.picture_layout_double_right)
                        }

                        if (containsRightPictureLayout && containsTripleBottomPictureLayout) {
                            pictureDisplayLayoutType = context.getString(R.string.picture_layout_double_left)
                        }
                    } else {
                        if (pictureLayoutTypes[0] == context.getString(R.string.picture_layout_triple_bottom)) {
                            pictureDisplayLayoutType = context.getString(R.string.picture_layout_double_left)
                        }

                        if (pictureLayoutTypes[0] == context.getString(R.string.picture_layout_double_right)) {
                            pictureDisplayLayoutType = context.getString(R.string.picture_layout_triple_bottom)
                        }

                        if (pictureLayoutTypes[0] == context.getString(R.string.picture_layout_double_left)) {
                            pictureDisplayLayoutType = context.getString(R.string.picture_layout_triple_bottom)
                        }
                    }
                } else {
                    pictureDisplayLayoutType = context.getString(R.string.picture_layout_triple_bottom)
                }

                pictureLayoutTypes.add(pictureDisplayLayoutType)

                if (pictureDisplayLayoutType == context.getString(R.string.picture_layout_triple_bottom)) {
                    val galleryPictureModels = arrayListOf(
                        GalleryPictureModel(imagePositionCollection[0], userPictureResponses[0]),
                        GalleryPictureModel(imagePositionCollection[1], userPictureResponses[1]),
                        GalleryPictureModel(imagePositionCollection[2], userPictureResponses[2]),
                        GalleryPictureModel(imagePositionCollection[3], userPictureResponses[3]))
                    val pictureCollectionModel = PictureCollectionModel(
                        pictureLayoutType = context.getString(R.string.picture_layout_triple_bottom),
                        galleryPictureModels = galleryPictureModels
                    )

                    pictureCollectionModels.add(pictureCollectionModel)

                    userPictureResponses.removeAt(3)
                    userPictureResponses.removeAt(2)
                    userPictureResponses.removeAt(1)
                    userPictureResponses.removeAt(0)

                    imagePositionCollection.removeAt(3)
                    imagePositionCollection.removeAt(2)
                    imagePositionCollection.removeAt(1)
                    imagePositionCollection.removeAt(0)
                } else {
                    val galleryPictureModels = arrayListOf(
                        GalleryPictureModel(imagePositionCollection[0], userPictureResponses[0]),
                        GalleryPictureModel(imagePositionCollection[1], userPictureResponses[1]),
                        GalleryPictureModel(imagePositionCollection[2], userPictureResponses[2]))
                    val pictureCollectionModel = PictureCollectionModel(
                        pictureLayoutType = pictureDisplayLayoutType,
                        galleryPictureModels = galleryPictureModels
                    )

                    pictureCollectionModels.add(pictureCollectionModel)

                    userPictureResponses.removeAt(2)
                    userPictureResponses.removeAt(1)
                    userPictureResponses.removeAt(0)

                    imagePositionCollection.removeAt(2)
                    imagePositionCollection.removeAt(1)
                    imagePositionCollection.removeAt(0)
                }

                recurSlicePictureResponses(context, pictureLayoutTypes,
                    userPictureResponses, pictureCollectionModels, imagePositionCollection)
            }
        } catch (exception: IndexOutOfBoundsException) {
            exception.printStackTrace()
            Log.e(TAG, "IndexOutOfBoundsException was caught on line 714 in recurSlicePictureResponses method, with message = ${exception.message}")
        }
    }

    companion object {
        const val TAG = "HomeDisplayAdapter"
    }
}


