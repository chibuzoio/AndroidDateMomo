package com.chibuzo.datemomo.adapter

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.chibuzo.datemomo.R
import com.chibuzo.datemomo.activity.ImageSliderActivity
import com.chibuzo.datemomo.databinding.RecyclerPictureCollectionBinding
import com.chibuzo.datemomo.model.ActivityInstanceModel
import com.chibuzo.datemomo.model.FloatingGalleryModel
import com.chibuzo.datemomo.model.PictureCollectionModel
import com.chibuzo.datemomo.model.instance.ActivitySavedInstance
import com.chibuzo.datemomo.model.instance.ImageSliderInstance
import com.chibuzo.datemomo.model.request.UserPictureRequest
import com.chibuzo.datemomo.model.response.UserPictureResponse
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.*
import java.io.IOException
import java.util.*

class PictureCollectionAdapter(private val pictureCollectionModels: ArrayList<PictureCollectionModel>,
                               private val floatingGalleryModel: FloatingGalleryModel) :
    RecyclerView.Adapter<PictureCollectionAdapter.MyViewHolder>() {
    private val buttonClickEffect = AlphaAnimation(1f, 0f)
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var activitySavedInstance: ActivitySavedInstance
    private lateinit var sharedPreferencesEditor: SharedPreferences.Editor

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
                        + pictureCollectionModels[position].galleryPictureModels[0].userPictureResponse.imageName)
                .transform(CenterCrop())
                .into(holder.binding.singlePictureOuterLayout.singlePictureView)

            holder.binding.singlePictureOuterLayout.singlePictureLayout.setOnClickListener {
                holder.binding.singlePictureOuterLayout.singlePictureLayout.startAnimation(buttonClickEffect)
                fetchUserPictures(holder.itemView.context, pictureCollectionModels[position].galleryPictureModels[0].imagePosition)
            }
        }

        if (pictureCollectionModels[position].pictureLayoutType == holder.itemView.context.getString(R.string.picture_layout_double_left)) {
            holder.binding.tripleBottomPictureLayout.tripleBottomPictureLayout.visibility = View.GONE
            holder.binding.doubleLeftPictureLayout.doubleLeftPictureLayout.visibility = View.VISIBLE
            holder.binding.singlePictureOuterLayout.singlePictureOuterLayout.visibility = View.GONE
            holder.binding.doubleRightPictureLayout.doubleRightPictureLayout.visibility = View.GONE

            Glide.with(holder.itemView.context)
                .load(holder.itemView.context.getString(R.string.date_momo_api)
                        + holder.itemView.context.getString(R.string.api_image)
                        + pictureCollectionModels[position].galleryPictureModels[0].userPictureResponse.imageName)
                .transform(CenterCrop())
                .into(holder.binding.doubleLeftPictureLayout.firstSmallPictureView)

            holder.binding.doubleLeftPictureLayout.firstSmallPictureLayout.setOnClickListener {
                holder.binding.doubleLeftPictureLayout.firstSmallPictureLayout.startAnimation(buttonClickEffect)
                fetchUserPictures(holder.itemView.context, pictureCollectionModels[position].galleryPictureModels[0].imagePosition)
            }

            Glide.with(holder.itemView.context)
                .load(holder.itemView.context.getString(R.string.date_momo_api)
                        + holder.itemView.context.getString(R.string.api_image)
                        + pictureCollectionModels[position].galleryPictureModels[1].userPictureResponse.imageName)
                .transform(CenterCrop())
                .into(holder.binding.doubleLeftPictureLayout.secondSmallPictureView)

            holder.binding.doubleLeftPictureLayout.secondSmallPictureLayout.setOnClickListener {
                holder.binding.doubleLeftPictureLayout.secondSmallPictureLayout.startAnimation(buttonClickEffect)
                fetchUserPictures(holder.itemView.context, pictureCollectionModels[position].galleryPictureModels[1].imagePosition)
            }

            Glide.with(holder.itemView.context)
                .load(holder.itemView.context.getString(R.string.date_momo_api)
                        + holder.itemView.context.getString(R.string.api_image)
                        + pictureCollectionModels[position].galleryPictureModels[2].userPictureResponse.imageName)
                .transform(CenterCrop())
                .into(holder.binding.doubleLeftPictureLayout.bigPictureView)

            holder.binding.doubleLeftPictureLayout.bigPictureLayout.setOnClickListener {
                holder.binding.doubleLeftPictureLayout.bigPictureLayout.startAnimation(buttonClickEffect)
                fetchUserPictures(holder.itemView.context, pictureCollectionModels[position].galleryPictureModels[2].imagePosition)
            }
        }

        if (pictureCollectionModels[position].pictureLayoutType == holder.itemView.context.getString(R.string.picture_layout_double_right)) {
            holder.binding.doubleRightPictureLayout.doubleRightPictureLayout.visibility = View.VISIBLE
            holder.binding.tripleBottomPictureLayout.tripleBottomPictureLayout.visibility = View.GONE
            holder.binding.singlePictureOuterLayout.singlePictureOuterLayout.visibility = View.GONE
            holder.binding.doubleLeftPictureLayout.doubleLeftPictureLayout.visibility = View.GONE

            Glide.with(holder.itemView.context)
                .load(holder.itemView.context.getString(R.string.date_momo_api)
                        + holder.itemView.context.getString(R.string.api_image)
                        + pictureCollectionModels[position].galleryPictureModels[0].userPictureResponse.imageName)
                .transform(CenterCrop())
                .into(holder.binding.doubleRightPictureLayout.bigPictureView)

            holder.binding.doubleRightPictureLayout.bigPictureLayout.setOnClickListener {
                holder.binding.doubleRightPictureLayout.bigPictureLayout.startAnimation(buttonClickEffect)
                fetchUserPictures(holder.itemView.context, pictureCollectionModels[position].galleryPictureModels[0].imagePosition)
            }

            Glide.with(holder.itemView.context)
                .load(holder.itemView.context.getString(R.string.date_momo_api)
                        + holder.itemView.context.getString(R.string.api_image)
                        + pictureCollectionModels[position].galleryPictureModels[1].userPictureResponse.imageName)
                .transform(CenterCrop())
                .into(holder.binding.doubleRightPictureLayout.firstSmallPictureView)

            holder.binding.doubleRightPictureLayout.firstSmallPictureLayout.setOnClickListener {
                holder.binding.doubleRightPictureLayout.firstSmallPictureLayout.startAnimation(buttonClickEffect)
                fetchUserPictures(holder.itemView.context, pictureCollectionModels[position].galleryPictureModels[1].imagePosition)
            }

            Glide.with(holder.itemView.context)
                .load(holder.itemView.context.getString(R.string.date_momo_api)
                        + holder.itemView.context.getString(R.string.api_image)
                        + pictureCollectionModels[position].galleryPictureModels[2].userPictureResponse.imageName)
                .transform(CenterCrop())
                .into(holder.binding.doubleRightPictureLayout.secondSmallPictureView)

            holder.binding.doubleRightPictureLayout.secondSmallPictureLayout.setOnClickListener {
                holder.binding.doubleRightPictureLayout.secondSmallPictureLayout.startAnimation(buttonClickEffect)
                fetchUserPictures(holder.itemView.context, pictureCollectionModels[position].galleryPictureModels[2].imagePosition)
            }
        }

        if (pictureCollectionModels[position].pictureLayoutType == holder.itemView.context.getString(R.string.picture_layout_triple_bottom)) {
            holder.binding.tripleBottomPictureLayout.tripleBottomPictureLayout.visibility = View.VISIBLE
            holder.binding.doubleRightPictureLayout.doubleRightPictureLayout.visibility = View.GONE
            holder.binding.singlePictureOuterLayout.singlePictureOuterLayout.visibility = View.GONE
            holder.binding.doubleLeftPictureLayout.doubleLeftPictureLayout.visibility = View.GONE

            Glide.with(holder.itemView.context)
                .load(holder.itemView.context.getString(R.string.date_momo_api)
                        + holder.itemView.context.getString(R.string.api_image)
                        + pictureCollectionModels[position].galleryPictureModels[0].userPictureResponse.imageName)
                .transform(CenterCrop())
                .into(holder.binding.tripleBottomPictureLayout.bigPictureView)

            holder.binding.tripleBottomPictureLayout.bigPictureLayout.setOnClickListener {
                holder.binding.tripleBottomPictureLayout.bigPictureLayout.startAnimation(buttonClickEffect)
                fetchUserPictures(holder.itemView.context, pictureCollectionModels[position].galleryPictureModels[0].imagePosition)
            }

            Glide.with(holder.itemView.context)
                .load(holder.itemView.context.getString(R.string.date_momo_api)
                        + holder.itemView.context.getString(R.string.api_image)
                        + pictureCollectionModels[position].galleryPictureModels[1].userPictureResponse.imageName)
                .transform(CenterCrop())
                .into(holder.binding.tripleBottomPictureLayout.firstSmallPictureView)

            holder.binding.tripleBottomPictureLayout.firstSmallPictureLayout.setOnClickListener {
                holder.binding.tripleBottomPictureLayout.firstSmallPictureLayout.startAnimation(buttonClickEffect)
                fetchUserPictures(holder.itemView.context, pictureCollectionModels[position].galleryPictureModels[1].imagePosition)
            }

            Glide.with(holder.itemView.context)
                .load(holder.itemView.context.getString(R.string.date_momo_api)
                        + holder.itemView.context.getString(R.string.api_image)
                        + pictureCollectionModels[position].galleryPictureModels[2].userPictureResponse.imageName)
                .transform(CenterCrop())
                .into(holder.binding.tripleBottomPictureLayout.secondSmallPictureView)

            holder.binding.tripleBottomPictureLayout.secondSmallPictureLayout.setOnClickListener {
                holder.binding.tripleBottomPictureLayout.secondSmallPictureLayout.startAnimation(buttonClickEffect)
                fetchUserPictures(holder.itemView.context, pictureCollectionModels[position].galleryPictureModels[2].imagePosition)
            }

            Glide.with(holder.itemView.context)
                .load(holder.itemView.context.getString(R.string.date_momo_api)
                        + holder.itemView.context.getString(R.string.api_image)
                        + pictureCollectionModels[position].galleryPictureModels[3].userPictureResponse.imageName)
                .transform(CenterCrop())
                .into(holder.binding.tripleBottomPictureLayout.thirdSmallPictureView)

            holder.binding.tripleBottomPictureLayout.thirdSmallPictureLayout.setOnClickListener {
                holder.binding.tripleBottomPictureLayout.thirdSmallPictureLayout.startAnimation(buttonClickEffect)
                fetchUserPictures(holder.itemView.context, pictureCollectionModels[position].galleryPictureModels[3].imagePosition)
            }
        }
    }

    override fun getItemCount(): Int {
        return pictureCollectionModels.size
    }

    class MyViewHolder(val binding: RecyclerPictureCollectionBinding) :
        RecyclerView.ViewHolder(binding.root)

    @Throws(IOException::class)
    fun fetchUserPictures(context: Context, currentPosition: Int) {
        val mapper = jacksonObjectMapper()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        val userPictureRequest = UserPictureRequest(
            memberId = floatingGalleryModel.profileOwnerId,
            currentPosition = currentPosition
        )

        val jsonObjectString = mapper.writeValueAsString(userPictureRequest)
        val requestBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            jsonObjectString
        )

        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(context.getString(R.string.date_momo_api) + context.getString(R.string.api_user_picture))
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val myResponse: String = response.body()!!.string()
                val userPictureResponses: java.util.ArrayList<UserPictureResponse> = mapper.readValue(myResponse)
                var imageSliderInstance = ImageSliderInstance(
                    memberId = userPictureRequest.memberId,
                    currentPosition = userPictureRequest.currentPosition,
                    userPictureResponses = userPictureResponses)

                val activityInstanceModel: ActivityInstanceModel =
                    mapper.readValue(sharedPreferences.getString(context.getString(R.string.activity_instance_model), "")!!)

                try {
                    if (activityInstanceModel.activityInstanceStack.peek().activity ==
                        context.getString(R.string.activity_image_slider)) {
                        activitySavedInstance = activityInstanceModel.activityInstanceStack.peek()
                        imageSliderInstance = mapper.readValue(activitySavedInstance.activityStateData)
                    }

                    val activityStateData = mapper.writeValueAsString(imageSliderInstance)

                    // Always do this below the method above, updateHomeDisplayInstance
                    activitySavedInstance = ActivitySavedInstance(
                        activity = context.getString(R.string.activity_image_slider),
                        activityStateData = activityStateData)

                    if (activityInstanceModel.activityInstanceStack.peek().activity != context.getString(
                            R.string.activity_image_slider
                        )) {
                        activityInstanceModel.activityInstanceStack.push(activitySavedInstance)
                    } else {
                        activityInstanceModel.activityInstanceStack.pop()
                        activityInstanceModel.activityInstanceStack.push(activitySavedInstance)
                    }

                    commitInstanceModel(context, mapper, activityInstanceModel)
                } catch (exception: EmptyStackException) {
                    exception.printStackTrace()
                    Log.e(TAG, "Exception from trying to peek and pop activityInstanceStack here is ${exception.message}")
                }

                Log.e(TAG, "The number of activities on the stack here is ${activityInstanceModel.activityInstanceStack.size}")

                val activitySavedInstanceString = mapper.writeValueAsString(activitySavedInstance)
                val intent = Intent(context, ImageSliderActivity::class.java)
                intent.putExtra(context.getString(R.string.activity_saved_instance), activitySavedInstanceString)
                context.startActivity(intent)
            }
        })
    }

    private fun commitInstanceModel(context: Context, mapper: ObjectMapper,
                                    activityInstanceModel: ActivityInstanceModel) {
        val activityInstanceModelString =
            mapper.writeValueAsString(activityInstanceModel)
        sharedPreferencesEditor.putString(
            context.getString(R.string.activity_instance_model),
            activityInstanceModelString
        )
        sharedPreferencesEditor.apply()
    }

    companion object {
        const val TAG = "PictureCollAdapter"
    }
}


