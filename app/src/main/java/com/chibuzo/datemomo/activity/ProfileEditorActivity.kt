package com.chibuzo.datemomo.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.chibuzo.datemomo.R
import com.chibuzo.datemomo.databinding.ActivityProfileEditorBinding
import com.chibuzo.datemomo.model.ActivityInstanceModel
import com.chibuzo.datemomo.model.instance.ActivitySavedInstance
import com.chibuzo.datemomo.model.instance.HomeDisplayInstance
import com.chibuzo.datemomo.model.instance.UserProfileInstance
import com.chibuzo.datemomo.model.request.*
import com.chibuzo.datemomo.model.response.CommittedResponse
import com.chibuzo.datemomo.model.response.OuterHomeDisplayResponse
import com.chibuzo.datemomo.model.response.PictureUpdateResponse
import com.chibuzo.datemomo.model.response.UserLikerResponse
import com.chibuzo.datemomo.utility.Utility
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.*
import java.io.File
import java.io.IOException
import java.util.*

class ProfileEditorActivity : AppCompatActivity() {
    private var photoFile: File? = null
    private val PICK_IMAGE_REQUEST = 200
    private var theBitmap: Bitmap? = null
    private var updatedStatus: String = ""
    private val CAPTURE_IMAGE_REQUEST = 100
    private var requestProcess: String = ""
    private var leastRootViewHeight: Int = 0
    private lateinit var buttonClickEffect: AlphaAnimation
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var viewRootHeightArray: MutableSet<Int>
    private lateinit var binding: ActivityProfileEditorBinding
    private lateinit var activitySavedInstance: ActivitySavedInstance
    private lateinit var updateSexualityRequest: UpdateSexualityRequest
    private lateinit var sharedPreferencesEditor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hideSystemUI()

        viewRootHeightArray = mutableSetOf()

        buttonClickEffect = AlphaAnimation(1f, 0f)
        sharedPreferences =
            getSharedPreferences(getString(R.string.shared_preferences), Context.MODE_PRIVATE)
        sharedPreferencesEditor = sharedPreferences.edit()

        binding.profileEditorCompleteButton.blueButtonText.text = "Done"

        binding.userGay.hollowButtonText.text = "Gay"
        binding.userToyBoy.hollowButtonText.text = "Toy Boy"
        binding.userLesbian.hollowButtonText.text = "Lesbian"
        binding.userToyGirl.hollowButtonText.text = "Toy Girl"
        binding.userBisexual.hollowButtonText.text = "Bisexual"
        binding.userStraight.hollowButtonText.text = "Straight"
        binding.userSugarDaddy.hollowButtonText.text = "Sugar Daddy"
        binding.userSugarMommy.hollowButtonText.text = "Sugar Mommy"

        binding.gayInterest.hollowButtonText.text = "Gay"
        binding.toyBoyInterest.hollowButtonText.text = "Toy Boy"
        binding.lesbianInterest.hollowButtonText.text = "Lesbian"
        binding.toyGirlInterest.hollowButtonText.text = "Toy Girl"
        binding.bisexualInterest.hollowButtonText.text = "Bisexual"
        binding.straightInterest.hollowButtonText.text = "Straight"
        binding.friendshipInterest.hollowButtonText.text = "Friendship"
        binding.sugarDaddyInterest.hollowButtonText.text = "Sugar Daddy"
        binding.sugarMommyInterest.hollowButtonText.text = "Sugar Mommy"
        binding.relationshipInterest.hollowButtonText.text = "Relationship"

        binding.sixtyNineExperience.hollowButtonText.text = "69"
        binding.analSexExperience.hollowButtonText.text = "Anal Sex"
        binding.orgySexExperience.hollowButtonText.text = "Orgy Sex"
        binding.poolSexExperience.hollowButtonText.text = "Pool Sex"
        binding.carSexExperience.hollowButtonText.text = "Sexed In Car"
        binding.threesomeExperience.hollowButtonText.text = "Threesome"
        binding.givenHeadExperience.hollowButtonText.text = "Given Head"
        binding.sexToyExperience.hollowButtonText.text = "Used Sex Toys"
        binding.missionaryExperience.hollowButtonText.text = "Missionary"
        binding.videoSexExperience.hollowButtonText.text = "Video Sex Chat"
        binding.publicSexExperience.hollowButtonText.text = "Sexed In Public"
        binding.receivedHeadExperience.hollowButtonText.text = "Received Head"
        binding.cameraSexExperience.hollowButtonText.text = "Sexed With Camera"
        binding.oneNightStandExperience.hollowButtonText.text = "One-night Stand"

        updateSexualityRequest = UpdateSexualityRequest(
            memberId = sharedPreferences.getInt("memberId", 0),
            bisexualCategory = sharedPreferences.getInt(getString(R.string.bisexual_category), 0),
            gayCategory = sharedPreferences.getInt(getString(R.string.gay_category), 0),
            lesbianCategory = sharedPreferences.getInt(getString(R.string.lesbian_category), 0),
            straightCategory = sharedPreferences.getInt(getString(R.string.straight_category), 0),
            sugarDaddyCategory = sharedPreferences.getInt(getString(R.string.sugar_daddy_category), 0),
            sugarMommyCategory = sharedPreferences.getInt(getString(R.string.sugar_mommy_category), 0),
            toyBoyCategory = sharedPreferences.getInt(getString(R.string.toy_boy_category), 0),
            toyGirlCategory = sharedPreferences.getInt(getString(R.string.toy_girl_category), 0),
            bisexualInterest = sharedPreferences.getInt(getString(R.string.bisexual_interest), 0),
            gayInterest = sharedPreferences.getInt(getString(R.string.gay_interest), 0),
            lesbianInterest = sharedPreferences.getInt(getString(R.string.lesbian_interest), 0),
            straightInterest = sharedPreferences.getInt(getString(R.string.straight_interest), 0),
            friendshipInterest = sharedPreferences.getInt(getString(R.string.friendship_interest), 0),
            sugarDaddyInterest = sharedPreferences.getInt(getString(R.string.sugar_daddy_interest), 0),
            sugarMommyInterest = sharedPreferences.getInt(getString(R.string.sugar_mommy_interest), 0),
            relationshipInterest = sharedPreferences.getInt(getString(R.string.relationship_interest), 0),
            toyBoyInterest = sharedPreferences.getInt(getString(R.string.toy_boy_interest), 0),
            toyGirlInterest = sharedPreferences.getInt(getString(R.string.toy_girl_interest), 0),
            sixtyNineExperience = sharedPreferences.getInt(getString(R.string.sixty_nine_experience), 0),
            analSexExperience = sharedPreferences.getInt(getString(R.string.anal_sex_experience), 0),
            givenHeadExperience = sharedPreferences.getInt(getString(R.string.given_head_experience), 0),
            missionaryExperience = sharedPreferences.getInt(getString(R.string.missionary_experience), 0),
            oneNightStandExperience = sharedPreferences.getInt(getString(R.string.one_night_stand_experience), 0),
            orgySexExperience = sharedPreferences.getInt(getString(R.string.orgy_experience), 0),
            poolSexExperience = sharedPreferences.getInt(getString(R.string.pool_sex_experience), 0),
            receivedHeadExperience = sharedPreferences.getInt(getString(R.string.received_head_experience), 0),
            carSexExperience = sharedPreferences.getInt(getString(R.string.car_sex_experience), 0),
            publicSexExperience = sharedPreferences.getInt(getString(R.string.public_sex_experience), 0),
            cameraSexExperience = sharedPreferences.getInt(getString(R.string.camera_sex_experience), 0),
            threesomeExperience = sharedPreferences.getInt(getString(R.string.threesome_experience), 0),
            sexToyExperience = sharedPreferences.getInt(getString(R.string.sex_toy_experience), 0),
            videoSexExperience = sharedPreferences.getInt(getString(R.string.video_sex_experience), 0)
        )

        Glide.with(this)
            .load(getString(R.string.date_momo_api) + getString(R.string.api_image)
                    + sharedPreferences.getString(getString(R.string.profile_picture), ""))
            .transform(CircleCrop(), CenterCrop())
            .into(binding.accountProfilePicture)

        binding.root.viewTreeObserver.addOnGlobalLayoutListener {
            viewRootHeightArray.add(binding.root.height)

            if (viewRootHeightArray.size >= 3) {
                leastRootViewHeight = viewRootHeightArray.elementAt(0)

                for (viewRootHeight in viewRootHeightArray) {
                    if (viewRootHeight < leastRootViewHeight) {
                        leastRootViewHeight = viewRootHeight
                    }
                }
            }

            if (binding.root.height > leastRootViewHeight && leastRootViewHeight > 0) {
                binding.userStatusUpdater.clearFocus()
                viewRootHeightArray = mutableSetOf()
                leastRootViewHeight = 0
                hideSystemUI()
            }
        }

        binding.userStatusUpdater.setOnFocusChangeListener { _, focused ->
            if (focused) {
                showSystemUI()
            } else {
                hideSystemUI()
            }
        }

        if ((sharedPreferences.getString(getString(R.string.updated_location), "")
                .equals(sharedPreferences.getString(getString(R.string.current_location), "")))
            && sharedPreferences.getString(getString(R.string.current_location), "")!!.isNotEmpty()) {
            binding.locationUpdaterSeparator.visibility = View.GONE
            binding.currentLocationHeader.visibility = View.GONE
            binding.currentLocationValue.visibility = View.GONE
            binding.userLocationHeader.visibility = View.GONE
            binding.userLocationValue.visibility = View.GONE
        } else if (sharedPreferences.getString(getString(R.string.updated_location), "")!!.isEmpty()) {
            binding.locationUpdaterSeparator.visibility = View.GONE
            binding.currentLocationHeader.visibility = View.GONE
            binding.currentLocationValue.visibility = View.GONE
            binding.userLocationHeader.visibility = View.GONE
            binding.userLocationValue.visibility = View.GONE
        } else {
            binding.locationUpdaterSeparator.visibility = View.VISIBLE
            binding.currentLocationHeader.visibility = View.VISIBLE
            binding.currentLocationValue.visibility = View.VISIBLE
            binding.userLocationHeader.visibility = View.VISIBLE
            binding.userLocationValue.visibility = View.VISIBLE
        }

        binding.userStatusUpdater.setText(sharedPreferences.getString(getString(R.string.user_status), ""))
        binding.userLocationValue.text = sharedPreferences.getString(getString(R.string.current_location), "")
        binding.currentLocationValue.text = sharedPreferences.getString(getString(R.string.updated_location), "")
        binding.currentUserStatus.text = sharedPreferences.getString(getString(R.string.user_status), "")
            .toString().ifEmpty { getString(R.string.status_placeholder) }

        if (binding.currentUserStatus.text.toString() == getString(R.string.status_placeholder)) {
            binding.currentUserStatus.setTextColor(ContextCompat.getColor(this, R.color.blue))
            binding.statusEditorButton.visibility = View.GONE
        }

        binding.currentUserStatus.setOnClickListener {
            binding.statusUpdaterButton.visibility = View.VISIBLE
            binding.userStatusUpdater.visibility = View.VISIBLE
            binding.statusEditorButton.visibility = View.GONE
            binding.currentUserStatus.visibility = View.GONE
        }

        binding.locationUpdaterButton.setOnClickListener {
            updateCurrentLocation()
        }

        binding.profileEditorCompleteButton.blueButtonLayout.setOnClickListener {
            onBackPressed()
        }

        binding.statusUpdaterButton.setOnClickListener {
            updatedStatus = binding.userStatusUpdater.text.toString().trim()

            binding.currentUserStatus.visibility = View.VISIBLE
            binding.statusUpdaterButton.visibility = View.GONE
            binding.userStatusUpdater.visibility = View.GONE

            if (updatedStatus.isNotEmpty()) {
                binding.currentUserStatus.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.edit_text
                    )
                )
                binding.currentUserStatus.text = updatedStatus

                binding.statusEditorButton.visibility = View.VISIBLE

                updateDateMomoStatus()
            }
        }

        binding.statusEditorButton.setOnClickListener {
            binding.statusUpdaterButton.visibility = View.VISIBLE
            binding.userStatusUpdater.visibility = View.VISIBLE
            binding.statusEditorButton.visibility = View.GONE
            binding.currentUserStatus.visibility = View.GONE
        }

        binding.profilePictureChanger.setOnClickListener {
            pickImageFromGallery()
        }

        binding.userGay.hollowButtonLayout.setOnClickListener {
            binding.userGay.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.userGay.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.userGay.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.userGay.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                updateSexualityRequest.gayCategory = 1
            } else {
                binding.userGay.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.userGay.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                updateSexualityRequest.gayCategory = 0
            }
        }

        binding.userToyBoy.hollowButtonLayout.setOnClickListener {
            binding.userToyBoy.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.userToyBoy.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.userToyBoy.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.userToyBoy.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                updateSexualityRequest.toyBoyCategory = 1
            } else {
                binding.userToyBoy.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.userToyBoy.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                updateSexualityRequest.toyBoyCategory = 0
            }
        }

        binding.userBisexual.hollowButtonLayout.setOnClickListener {
            binding.userBisexual.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.userBisexual.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.userBisexual.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.userBisexual.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                updateSexualityRequest.bisexualCategory = 1
            } else {
                binding.userBisexual.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.userBisexual.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                updateSexualityRequest.bisexualCategory = 0
            }
        }

        binding.userStraight.hollowButtonLayout.setOnClickListener {
            binding.userStraight.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.userStraight.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.userStraight.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.userStraight.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                updateSexualityRequest.straightCategory = 1
            } else {
                binding.userStraight.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.userStraight.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                updateSexualityRequest.straightCategory = 0
            }
        }

        binding.userSugarDaddy.hollowButtonLayout.setOnClickListener {
            binding.userSugarDaddy.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.userSugarDaddy.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.userSugarDaddy.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.userSugarDaddy.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                updateSexualityRequest.sugarDaddyCategory = 1
            } else {
                binding.userSugarDaddy.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.userSugarDaddy.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                updateSexualityRequest.sugarDaddyCategory = 0
            }
        }

        binding.userLesbian.hollowButtonLayout.setOnClickListener {
            binding.userLesbian.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.userLesbian.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.userLesbian.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.userLesbian.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                updateSexualityRequest.lesbianCategory = 1
            } else {
                binding.userLesbian.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.userLesbian.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                updateSexualityRequest.lesbianCategory = 0
            }
        }

        binding.userToyGirl.hollowButtonLayout.setOnClickListener {
            binding.userToyGirl.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.userToyGirl.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.userToyGirl.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.userToyGirl.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                updateSexualityRequest.toyGirlCategory = 1
            } else {
                binding.userToyGirl.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.userToyGirl.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                updateSexualityRequest.toyGirlCategory = 0
            }
        }

        binding.userSugarMommy.hollowButtonLayout.setOnClickListener {
            binding.userSugarMommy.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.userSugarMommy.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.userSugarMommy.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.userSugarMommy.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                updateSexualityRequest.sugarMommyCategory = 1
            } else {
                binding.userSugarMommy.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.userSugarMommy.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                updateSexualityRequest.sugarMommyCategory = 0
            }
        }

        binding.gayInterest.hollowButtonLayout.setOnClickListener {
            binding.gayInterest.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.gayInterest.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.gayInterest.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.gayInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                updateSexualityRequest.gayInterest = 1
            } else {
                binding.gayInterest.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.gayInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                updateSexualityRequest.gayInterest = 0
            }
        }

        binding.friendshipInterest.hollowButtonLayout.setOnClickListener {
            binding.friendshipInterest.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.friendshipInterest.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.friendshipInterest.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.friendshipInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                updateSexualityRequest.friendshipInterest = 1
            } else {
                binding.friendshipInterest.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.friendshipInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                updateSexualityRequest.friendshipInterest = 0
            }
        }

        binding.toyBoyInterest.hollowButtonLayout.setOnClickListener {
            binding.toyBoyInterest.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.toyBoyInterest.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.toyBoyInterest.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.toyBoyInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                updateSexualityRequest.toyBoyInterest = 1
            } else {
                binding.toyBoyInterest.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.toyBoyInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                updateSexualityRequest.toyBoyInterest = 0
            }
        }

        binding.lesbianInterest.hollowButtonLayout.setOnClickListener {
            binding.lesbianInterest.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.lesbianInterest.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.lesbianInterest.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.lesbianInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                updateSexualityRequest.lesbianInterest = 1
            } else {
                binding.lesbianInterest.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.lesbianInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                updateSexualityRequest.lesbianInterest = 0
            }
        }

        binding.relationshipInterest.hollowButtonLayout.setOnClickListener {
            binding.relationshipInterest.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.relationshipInterest.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.relationshipInterest.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.relationshipInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                updateSexualityRequest.relationshipInterest = 1
            } else {
                binding.relationshipInterest.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.relationshipInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                updateSexualityRequest.relationshipInterest = 0
            }
        }

        binding.toyGirlInterest.hollowButtonLayout.setOnClickListener {
            binding.toyGirlInterest.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.toyGirlInterest.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.toyGirlInterest.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.toyGirlInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                updateSexualityRequest.toyGirlInterest = 1
            } else {
                binding.toyGirlInterest.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.toyGirlInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                updateSexualityRequest.toyGirlInterest = 0
            }
        }

        binding.bisexualInterest.hollowButtonLayout.setOnClickListener {
            binding.bisexualInterest.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.bisexualInterest.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.bisexualInterest.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.bisexualInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                updateSexualityRequest.bisexualInterest = 1
            } else {
                binding.bisexualInterest.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.bisexualInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                updateSexualityRequest.bisexualInterest = 0
            }
        }

        binding.straightInterest.hollowButtonLayout.setOnClickListener {
            binding.straightInterest.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.straightInterest.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.straightInterest.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.straightInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                updateSexualityRequest.straightInterest = 1
            } else {
                binding.straightInterest.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.straightInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                updateSexualityRequest.straightInterest = 0
            }
        }

        binding.sugarDaddyInterest.hollowButtonLayout.setOnClickListener {
            binding.sugarDaddyInterest.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.sugarDaddyInterest.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.sugarDaddyInterest.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.sugarDaddyInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                updateSexualityRequest.sugarDaddyInterest = 1
            } else {
                binding.sugarDaddyInterest.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.sugarDaddyInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                updateSexualityRequest.sugarDaddyInterest = 0
            }
        }

        binding.sugarMommyInterest.hollowButtonLayout.setOnClickListener {
            binding.sugarMommyInterest.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.sugarMommyInterest.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.sugarMommyInterest.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.sugarMommyInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                updateSexualityRequest.sugarMommyInterest = 1
            } else {
                binding.sugarMommyInterest.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.sugarMommyInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                updateSexualityRequest.sugarMommyInterest = 0
            }
        }

        binding.sixtyNineExperience.hollowButtonLayout.setOnClickListener {
            binding.sixtyNineExperience.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.sixtyNineExperience.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.sixtyNineExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.sixtyNineExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                updateSexualityRequest.sixtyNineExperience = 1
            } else {
                binding.sixtyNineExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.sixtyNineExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                updateSexualityRequest.sixtyNineExperience = 0
            }
        }

        binding.analSexExperience.hollowButtonLayout.setOnClickListener {
            binding.analSexExperience.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.analSexExperience.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.analSexExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.analSexExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                updateSexualityRequest.analSexExperience = 1
            } else {
                binding.analSexExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.analSexExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                updateSexualityRequest.analSexExperience = 0
            }
        }

        binding.missionaryExperience.hollowButtonLayout.setOnClickListener {
            binding.missionaryExperience.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.missionaryExperience.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.missionaryExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.missionaryExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                updateSexualityRequest.missionaryExperience = 1
            } else {
                binding.missionaryExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.missionaryExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                updateSexualityRequest.missionaryExperience = 0
            }
        }

        binding.orgySexExperience.hollowButtonLayout.setOnClickListener {
            binding.orgySexExperience.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.orgySexExperience.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.orgySexExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.orgySexExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                updateSexualityRequest.orgySexExperience = 1
            } else {
                binding.orgySexExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.orgySexExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                updateSexualityRequest.orgySexExperience = 0
            }
        }

        binding.poolSexExperience.hollowButtonLayout.setOnClickListener {
            binding.poolSexExperience.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.poolSexExperience.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.poolSexExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.poolSexExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                updateSexualityRequest.poolSexExperience = 1
            } else {
                binding.poolSexExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.poolSexExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                updateSexualityRequest.poolSexExperience = 0
            }
        }

        binding.carSexExperience.hollowButtonLayout.setOnClickListener {
            binding.carSexExperience.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.carSexExperience.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.carSexExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.carSexExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                updateSexualityRequest.carSexExperience = 1
            } else {
                binding.carSexExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.carSexExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                updateSexualityRequest.carSexExperience = 0
            }
        }

        binding.threesomeExperience.hollowButtonLayout.setOnClickListener {
            binding.threesomeExperience.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.threesomeExperience.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.threesomeExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.threesomeExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                updateSexualityRequest.threesomeExperience = 1
            } else {
                binding.threesomeExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.threesomeExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                updateSexualityRequest.threesomeExperience = 0
            }
        }

        binding.givenHeadExperience.hollowButtonLayout.setOnClickListener {
            binding.givenHeadExperience.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.givenHeadExperience.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.givenHeadExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.givenHeadExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                updateSexualityRequest.givenHeadExperience = 1
            } else {
                binding.givenHeadExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.givenHeadExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                updateSexualityRequest.givenHeadExperience = 0
            }
        }

        binding.sexToyExperience.hollowButtonLayout.setOnClickListener {
            binding.sexToyExperience.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.sexToyExperience.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.sexToyExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.sexToyExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                updateSexualityRequest.sexToyExperience = 1
            } else {
                binding.sexToyExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.sexToyExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                updateSexualityRequest.sexToyExperience = 0
            }
        }

        binding.videoSexExperience.hollowButtonLayout.setOnClickListener {
            binding.videoSexExperience.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.videoSexExperience.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.videoSexExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.videoSexExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                updateSexualityRequest.videoSexExperience = 1
            } else {
                binding.videoSexExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.videoSexExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                updateSexualityRequest.videoSexExperience = 0
            }
        }

        binding.publicSexExperience.hollowButtonLayout.setOnClickListener {
            binding.publicSexExperience.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.publicSexExperience.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.publicSexExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.publicSexExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                updateSexualityRequest.publicSexExperience = 1
            } else {
                binding.publicSexExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.publicSexExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                updateSexualityRequest.publicSexExperience = 0
            }
        }

        binding.receivedHeadExperience.hollowButtonLayout.setOnClickListener {
            binding.receivedHeadExperience.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.receivedHeadExperience.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.receivedHeadExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.receivedHeadExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                updateSexualityRequest.receivedHeadExperience = 1
            } else {
                binding.receivedHeadExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.receivedHeadExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                updateSexualityRequest.receivedHeadExperience = 0
            }
        }

        binding.cameraSexExperience.hollowButtonLayout.setOnClickListener {
            binding.cameraSexExperience.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.cameraSexExperience.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.cameraSexExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.cameraSexExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                updateSexualityRequest.cameraSexExperience = 1
            } else {
                binding.cameraSexExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.cameraSexExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                updateSexualityRequest.cameraSexExperience = 0
            }
        }

        binding.oneNightStandExperience.hollowButtonLayout.setOnClickListener {
            binding.oneNightStandExperience.hollowButtonLayout.startAnimation(buttonClickEffect)

            if (binding.oneNightStandExperience.hollowButtonText.currentTextColor ==
                ContextCompat.getColor(this, R.color.blue)) {
                binding.oneNightStandExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.oneNightStandExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
                updateSexualityRequest.oneNightStandExperience = 1
            } else {
                binding.oneNightStandExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.blue))
                binding.oneNightStandExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.hollow_blue_button)
                updateSexualityRequest.oneNightStandExperience = 0
            }
        }

        if (sharedPreferences.getInt(getString(R.string.bisexual_category), 0) > 0) {
            binding.userBisexual.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
            binding.userBisexual.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
        }

        if (sharedPreferences.getInt(getString(R.string.gay_category), 0) > 0) {
            binding.userGay.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
            binding.userGay.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
        }

        if (sharedPreferences.getInt(getString(R.string.lesbian_category), 0) > 0) {
            binding.userLesbian.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
            binding.userLesbian.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
        }

        if (sharedPreferences.getInt(getString(R.string.straight_category), 0) > 0) {
            binding.userStraight.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
            binding.userStraight.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
        }

        if (sharedPreferences.getInt(getString(R.string.sugar_daddy_category), 0) > 0) {
            binding.userSugarDaddy.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
            binding.userSugarDaddy.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
        }

        if (sharedPreferences.getInt(getString(R.string.sugar_mommy_category), 0) > 0) {
            binding.userSugarMommy.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
            binding.userSugarMommy.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
        }

        if (sharedPreferences.getInt(getString(R.string.toy_boy_category), 0) > 0) {
            binding.userToyBoy.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
            binding.userToyBoy.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
        }

        if (sharedPreferences.getInt(getString(R.string.toy_girl_category), 0) > 0) {
            binding.userToyGirl.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
            binding.userToyGirl.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
        }

        if (sharedPreferences.getInt(getString(R.string.bisexual_interest), 0) > 0) {
            binding.bisexualInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
            binding.bisexualInterest.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
        }

        if (sharedPreferences.getInt(getString(R.string.friendship_interest), 0) > 0) {
            binding.friendshipInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
            binding.friendshipInterest.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
        }

        if (sharedPreferences.getInt(getString(R.string.gay_interest), 0) > 0) {
            binding.gayInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
            binding.gayInterest.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
        }

        if (sharedPreferences.getInt(getString(R.string.straight_interest), 0) > 0) {
            binding.straightInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
            binding.straightInterest.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
        }

        if (sharedPreferences.getInt(getString(R.string.relationship_interest), 0) > 0) {
            binding.relationshipInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
            binding.relationshipInterest.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
        }

        if (sharedPreferences.getInt(getString(R.string.lesbian_interest), 0) > 0) {
            binding.lesbianInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
            binding.lesbianInterest.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
        }

        if (sharedPreferences.getInt(getString(R.string.sugar_daddy_interest), 0) > 0) {
            binding.sugarDaddyInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
            binding.sugarDaddyInterest.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
        }

        if (sharedPreferences.getInt(getString(R.string.sugar_mommy_interest), 0) > 0) {
            binding.sugarMommyInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
            binding.sugarMommyInterest.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
        }

        if (sharedPreferences.getInt(getString(R.string.toy_boy_interest), 0) > 0) {
            binding.toyBoyInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
            binding.toyBoyInterest.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
        }

        if (sharedPreferences.getInt(getString(R.string.toy_girl_interest), 0) > 0) {
            binding.toyGirlInterest.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
            binding.toyGirlInterest.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
        }

        if (sharedPreferences.getInt(getString(R.string.anal_sex_experience), 0) > 0) {
            binding.analSexExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
            binding.analSexExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
        }

        if (sharedPreferences.getInt(getString(R.string.sixty_nine_experience), 0) > 0) {
            binding.sixtyNineExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
            binding.sixtyNineExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
        }

        if (sharedPreferences.getInt(getString(R.string.camera_sex_experience), 0) > 0) {
            binding.cameraSexExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
            binding.cameraSexExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
        }

        if (sharedPreferences.getInt(getString(R.string.missionary_experience), 0) > 0) {
            binding.missionaryExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
            binding.missionaryExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
        }

        if (sharedPreferences.getInt(getString(R.string.car_sex_experience), 0) > 0) {
            binding.carSexExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
            binding.carSexExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
        }

        if (sharedPreferences.getInt(getString(R.string.threesome_experience), 0) > 0) {
            binding.threesomeExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
            binding.threesomeExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
        }

        if (sharedPreferences.getInt(getString(R.string.given_head_experience), 0) > 0) {
            binding.givenHeadExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
            binding.givenHeadExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
        }

        if (sharedPreferences.getInt(getString(R.string.received_head_experience), 0) > 0) {
            binding.receivedHeadExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
            binding.receivedHeadExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
        }

        if (sharedPreferences.getInt(getString(R.string.one_night_stand_experience), 0) > 0) {
            binding.oneNightStandExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
            binding.oneNightStandExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
        }

        if (sharedPreferences.getInt(getString(R.string.orgy_experience), 0) > 0) {
            binding.orgySexExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
            binding.orgySexExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
        }

        if (sharedPreferences.getInt(getString(R.string.pool_sex_experience), 0) > 0) {
            binding.poolSexExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
            binding.poolSexExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
        }

        if (sharedPreferences.getInt(getString(R.string.sex_toy_experience), 0) > 0) {
            binding.sexToyExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
            binding.sexToyExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
        }

        if (sharedPreferences.getInt(getString(R.string.video_sex_experience), 0) > 0) {
            binding.videoSexExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
            binding.videoSexExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
        }

        if (sharedPreferences.getInt(getString(R.string.public_sex_experience), 0) > 0) {
            binding.publicSexExperience.hollowButtonLayout.background = ContextCompat.getDrawable(this, R.drawable.blue_button)
            binding.publicSexExperience.hollowButtonText.setTextColor(ContextCompat.getColor(this, R.color.white))
        }
    }

    override fun onStart() {
        super.onStart()
        hideSystemUI()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        updateSexualityQualities()

        val mapper = jacksonObjectMapper()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        val activityInstanceModel: ActivityInstanceModel =
            mapper.readValue(sharedPreferences.getString(getString(R.string.activity_instance_model), "")!!)

        try {
            when (activityInstanceModel.activityInstanceStack.peek().activity) {
                getString(R.string.activity_user_profile) -> {
                    requestProcess = getString(R.string.request_fetch_user_likers)
                    fetchUserLikers()
                }
                getString(R.string.activity_home_display) -> {
                    requestProcess = getString(R.string.request_fetch_matched_users)
                    fetchMatchedUsers()
                }
                else -> super.onBackPressed()
            }
        } catch (exception: EmptyStackException) {
            exception.printStackTrace()
            Log.e(TAG, "Exception from trying to peek activityStack here is ${exception.message}")
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAPTURE_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            theBitmap = BitmapFactory.decodeFile(photoFile!!.absolutePath)

            Glide.with(this)
                .load(theBitmap)
                .transform(CircleCrop(), CenterCrop())
                .into(binding.accountProfilePicture)
        } else if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            Glide.with(this)
                .load(data?.data)
                .transform(CircleCrop(), CenterCrop())
                .into(binding.accountProfilePicture)

            theBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(contentResolver, data?.data!!)
                ImageDecoder.decodeBitmap(source)
            } else{
                MediaStore.Images.Media.getBitmap(contentResolver, data?.data)
            }
        }

        if (theBitmap != null) {
            updateProfilePicture()
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    @Throws(IOException::class)
    fun updateCurrentLocation() {
        val mapper = jacksonObjectMapper()
        val updateLocationRequest =
            UpdateLocationRequest(sharedPreferences.getInt(getString(R.string.member_id), 0),
                sharedPreferences.getString(getString(R.string.updated_location), "").toString())

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        val jsonObjectString = mapper.writeValueAsString(updateLocationRequest)
        val requestBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            jsonObjectString
        )

        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(getString(R.string.date_momo_api) + getString(R.string.api_update_location))
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()

                runOnUiThread {

                }

                if (!Utility.isConnected(baseContext)) {
                    displayDoubleButtonDialog()
                } else if (e.message!!.contains("after")) {
                    displaySingleButtonDialog(getString(R.string.poor_internet_title), getString(R.string.poor_internet_message))
                } else {
                    displaySingleButtonDialog(getString(R.string.server_error_title), getString(R.string.server_error_message))
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val myResponse: String = response.body()!!.string()
                val committedResponse: CommittedResponse = mapper.readValue(myResponse)

                if (committedResponse.committed) {
                    sharedPreferencesEditor.putString(getString(R.string.current_location),
                        sharedPreferences.getString(getString(R.string.updated_location), "").toString())
                    sharedPreferencesEditor.apply()

                    runOnUiThread {
                        binding.locationUpdaterButton.visibility = View.GONE
                        binding.userLocationHeader.visibility = View.GONE
                        binding.userLocationValue.visibility = View.GONE
                    }
                }
            }
        })
    }

    @Throws(IOException::class)
    fun fetchMatchedUsers() {
        val mapper = jacksonObjectMapper()
        val homeDisplayRequest = OuterHomeDisplayRequest(
            sharedPreferences.getInt(getString(R.string.member_id), 0),
            sharedPreferences.getInt(getString(R.string.age), 0),
            sharedPreferences.getString(getString(R.string.sex), "")!!,
            sharedPreferences.getString(getString(R.string.registration_date), "")!!,
            sharedPreferences.getInt(getString(R.string.bisexual_category), 0),
            sharedPreferences.getInt(getString(R.string.gay_category), 0),
            sharedPreferences.getInt(getString(R.string.lesbian_category), 0),
            sharedPreferences.getInt(getString(R.string.straight_category), 0),
            sharedPreferences.getInt(getString(R.string.sugar_daddy_category), 0),
            sharedPreferences.getInt(getString(R.string.sugar_mommy_category), 0),
            sharedPreferences.getInt(getString(R.string.toy_boy_category), 0),
            sharedPreferences.getInt(getString(R.string.toy_girl_category), 0),
            sharedPreferences.getInt(getString(R.string.bisexual_interest), 0),
            sharedPreferences.getInt(getString(R.string.gay_interest), 0),
            sharedPreferences.getInt(getString(R.string.lesbian_interest), 0),
            sharedPreferences.getInt(getString(R.string.straight_interest), 0),
            sharedPreferences.getInt(getString(R.string.friendship_interest), 0),
            sharedPreferences.getInt(getString(R.string.sugar_daddy_interest), 0),
            sharedPreferences.getInt(getString(R.string.sugar_mommy_interest), 0),
            sharedPreferences.getInt(getString(R.string.relationship_interest), 0),
            sharedPreferences.getInt(getString(R.string.toy_boy_interest), 0),
            sharedPreferences.getInt(getString(R.string.toy_girl_interest), 0),
            sharedPreferences.getInt(getString(R.string.sixty_nine_experience), 0),
            sharedPreferences.getInt(getString(R.string.anal_sex_experience), 0),
            sharedPreferences.getInt(getString(R.string.given_head_experience), 0),
            sharedPreferences.getInt(getString(R.string.missionary_experience), 0),
            sharedPreferences.getInt(getString(R.string.one_night_stand_experience), 0),
            sharedPreferences.getInt(getString(R.string.orgy_experience), 0),
            sharedPreferences.getInt(getString(R.string.pool_sex_experience), 0),
            sharedPreferences.getInt(getString(R.string.received_head_experience), 0),
            sharedPreferences.getInt(getString(R.string.car_sex_experience), 0),
            sharedPreferences.getInt(getString(R.string.public_sex_experience), 0),
            sharedPreferences.getInt(getString(R.string.camera_sex_experience), 0),
            sharedPreferences.getInt(getString(R.string.threesome_experience), 0),
            sharedPreferences.getInt(getString(R.string.sex_toy_experience), 0),
            sharedPreferences.getInt(getString(R.string.video_sex_experience), 0))

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        val jsonObjectString = mapper.writeValueAsString(homeDisplayRequest)
        val requestBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            jsonObjectString
        )

        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(getString(R.string.date_momo_api) + getString(R.string.api_matched_user_data))
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()

                if (!Utility.isConnected(baseContext)) {
                    displayDoubleButtonDialog()
                } else if (e.message!!.contains("after")) {
                    displaySingleButtonDialog(getString(R.string.poor_internet_title), getString(R.string.poor_internet_message))
                } else {
                    displaySingleButtonDialog(getString(R.string.server_error_title), getString(R.string.server_error_message))
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val myResponse: String = response.body()!!.string()
                val outerHomeDisplayResponse: OuterHomeDisplayResponse = mapper.readValue(myResponse)
                var homeDisplayInstance = HomeDisplayInstance(
                    scrollToPosition = 0,
                    outerHomeDisplayResponse = outerHomeDisplayResponse)

                val activityInstanceModel: ActivityInstanceModel =
                    mapper.readValue(sharedPreferences.getString(getString(R.string.activity_instance_model), "")!!)

                try {
                    if (activityInstanceModel.activityInstanceStack.peek().activity ==
                        getString(R.string.activity_home_display)) {
                        activitySavedInstance = activityInstanceModel.activityInstanceStack.peek()
                        homeDisplayInstance = mapper.readValue(activitySavedInstance.activityStateData)
                    }

                    val activityStateData = mapper.writeValueAsString(homeDisplayInstance)

                    activitySavedInstance = ActivitySavedInstance(
                        activity = getString(R.string.activity_home_display),
                        activityStateData = activityStateData)

                    if (activityInstanceModel.activityInstanceStack.peek().activity != getString(
                            R.string.activity_home_display
                        )) {
                        activityInstanceModel.activityInstanceStack.push(activitySavedInstance)
                    } else {
                        activityInstanceModel.activityInstanceStack.pop()
                        activityInstanceModel.activityInstanceStack.push(activitySavedInstance)
                    }

                    commitInstanceModel(mapper, activityInstanceModel)
                } catch (exception: EmptyStackException) {
                    exception.printStackTrace()
                    Log.e(TAG, "Exception from trying to peek and pop activityInstanceStack here is ${exception.message}")
                }

                val activitySavedInstanceString = mapper.writeValueAsString(activitySavedInstance)
                val intent = Intent(this@ProfileEditorActivity, HomeDisplayActivity::class.java)
                intent.putExtra(getString(R.string.activity_saved_instance), activitySavedInstanceString)
                startActivity(intent)
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

                if (!Utility.isConnected(baseContext)) {
                    displayDoubleButtonDialog()
                } else if (e.message!!.contains("after")) {
                    displaySingleButtonDialog(getString(R.string.poor_internet_title), getString(R.string.poor_internet_message))
                } else {
                    displaySingleButtonDialog(getString(R.string.server_error_title), getString(R.string.server_error_message))
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val myResponse: String = response.body()!!.string()
                val userLikerResponses: ArrayList<UserLikerResponse> = mapper.readValue(myResponse)
                val userProfileInstance = UserProfileInstance(userLikerResponses)

                val activityStateData = mapper.writeValueAsString(userProfileInstance)

                val activityInstanceModel: ActivityInstanceModel =
                    mapper.readValue(sharedPreferences.getString(getString(R.string.activity_instance_model), "")!!)

                try {
                    activitySavedInstance = ActivitySavedInstance(
                        activity = getString(R.string.activity_user_profile),
                        activityStateData = activityStateData)

                    if (activityInstanceModel.activityInstanceStack.peek().activity != getString(
                            R.string.activity_user_profile
                        )) {
                        activityInstanceModel.activityInstanceStack.push(activitySavedInstance)
                    } else {
                        activityInstanceModel.activityInstanceStack.pop()
                        activityInstanceModel.activityInstanceStack.push(activitySavedInstance)
                    }

                    commitInstanceModel(mapper, activityInstanceModel)
                } catch (exception: EmptyStackException) {
                    exception.printStackTrace()
                    Log.e(TAG, "Exception from trying to peek and pop activityInstanceStack here is ${exception.message}")
                }

                val activitySavedInstanceString = mapper.writeValueAsString(activitySavedInstance)
                val intent = Intent(this@ProfileEditorActivity, UserProfileActivity::class.java)
                intent.putExtra(getString(R.string.activity_saved_instance), activitySavedInstanceString)
                startActivity(intent)
            }
        })
    }

    @Throws(IOException::class)
    fun updateSexualityQualities() {
        val mapper = jacksonObjectMapper()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        val jsonObjectString = mapper.writeValueAsString(updateSexualityRequest)
        val requestBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            jsonObjectString
        )

        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(getString(R.string.date_momo_api) + getString(R.string.api_update_sexuality))
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
                    sharedPreferencesEditor.putInt(getString(R.string.bisexual_category), updateSexualityRequest.bisexualCategory)
                    sharedPreferencesEditor.putInt(getString(R.string.gay_category), updateSexualityRequest.gayCategory)
                    sharedPreferencesEditor.putInt(getString(R.string.lesbian_category), updateSexualityRequest.lesbianCategory)
                    sharedPreferencesEditor.putInt(getString(R.string.straight_category), updateSexualityRequest.straightCategory)
                    sharedPreferencesEditor.putInt(getString(R.string.sugar_daddy_category), updateSexualityRequest.sugarDaddyCategory)
                    sharedPreferencesEditor.putInt(getString(R.string.sugar_mommy_category), updateSexualityRequest.sugarMommyCategory)
                    sharedPreferencesEditor.putInt(getString(R.string.toy_boy_category), updateSexualityRequest.toyBoyCategory)
                    sharedPreferencesEditor.putInt(getString(R.string.toy_girl_category), updateSexualityRequest.toyGirlCategory)
                    sharedPreferencesEditor.putInt(getString(R.string.bisexual_interest), updateSexualityRequest.bisexualInterest)
                    sharedPreferencesEditor.putInt(getString(R.string.gay_interest), updateSexualityRequest.gayInterest)
                    sharedPreferencesEditor.putInt(getString(R.string.straight_interest), updateSexualityRequest.straightInterest)
                    sharedPreferencesEditor.putInt(getString(R.string.friendship_interest), updateSexualityRequest.friendshipInterest)
                    sharedPreferencesEditor.putInt(getString(R.string.relationship_interest), updateSexualityRequest.relationshipInterest)
                    sharedPreferencesEditor.putInt(getString(R.string.lesbian_interest), updateSexualityRequest.lesbianInterest)
                    sharedPreferencesEditor.putInt(getString(R.string.sugar_daddy_interest), updateSexualityRequest.sugarDaddyInterest)
                    sharedPreferencesEditor.putInt(getString(R.string.sugar_mommy_interest), updateSexualityRequest.sugarMommyInterest)
                    sharedPreferencesEditor.putInt(getString(R.string.toy_boy_interest), updateSexualityRequest.toyBoyInterest)
                    sharedPreferencesEditor.putInt(getString(R.string.toy_girl_interest), updateSexualityRequest.toyGirlInterest)
                    sharedPreferencesEditor.putInt(getString(R.string.anal_sex_experience), updateSexualityRequest.analSexExperience)
                    sharedPreferencesEditor.putInt(getString(R.string.sixty_nine_experience), updateSexualityRequest.sixtyNineExperience)
                    sharedPreferencesEditor.putInt(getString(R.string.camera_sex_experience), updateSexualityRequest.cameraSexExperience)
                    sharedPreferencesEditor.putInt(getString(R.string.car_sex_experience), updateSexualityRequest.carSexExperience)
                    sharedPreferencesEditor.putInt(getString(R.string.threesome_experience), updateSexualityRequest.threesomeExperience)
                    sharedPreferencesEditor.putInt(getString(R.string.given_head_experience), updateSexualityRequest.givenHeadExperience)
                    sharedPreferencesEditor.putInt(getString(R.string.missionary_experience), updateSexualityRequest.missionaryExperience)
                    sharedPreferencesEditor.putInt(getString(R.string.received_head_experience), updateSexualityRequest.receivedHeadExperience)
                    sharedPreferencesEditor.putInt(getString(R.string.one_night_stand_experience), updateSexualityRequest.oneNightStandExperience)
                    sharedPreferencesEditor.putInt(getString(R.string.orgy_experience), updateSexualityRequest.orgySexExperience)
                    sharedPreferencesEditor.putInt(getString(R.string.pool_sex_experience), updateSexualityRequest.poolSexExperience)
                    sharedPreferencesEditor.putInt(getString(R.string.sex_toy_experience), updateSexualityRequest.sexToyExperience)
                    sharedPreferencesEditor.putInt(getString(R.string.video_sex_experience), updateSexualityRequest.videoSexExperience)
                    sharedPreferencesEditor.putInt(getString(R.string.public_sex_experience), updateSexualityRequest.publicSexExperience)
                    sharedPreferencesEditor.apply()
                }
            }
        })
    }

    @Throws(IOException::class)
    fun updateDateMomoStatus() {
        val statusUpdateRequest = StatusUpdateRequest(
            sharedPreferences.getInt(getString(R.string.member_id), 0),
            updatedStatus
        )

        val mapper = jacksonObjectMapper()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        val jsonObjectString = mapper.writeValueAsString(statusUpdateRequest)
        val requestBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            jsonObjectString
        )

        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(getString(R.string.date_momo_api) + getString(R.string.api_update_status))
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
                    sharedPreferencesEditor.putString(getString(R.string.user_status), updatedStatus)
                    sharedPreferencesEditor.apply()
                }
            }
        })
    }

    @Throws(IOException::class)
    fun updateProfilePicture() {
        val imageWidth = theBitmap!!.width
        val imageHeight = theBitmap!!.height

        val base64Picture = Utility.encodeUploadImage(theBitmap!!)
        
        val mapper = jacksonObjectMapper()
        val pictureUpdateRequest = PictureUpdateRequest(
            sharedPreferences.getInt(getString(R.string.member_id), 0),
            imageWidth,
            imageHeight,
            base64Picture
        )

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        val jsonObjectString = mapper.writeValueAsString(pictureUpdateRequest)
        val requestBody: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            jsonObjectString
        )

        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(getString(R.string.date_momo_api) + getString(R.string.api_update_picture))
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()

                if (!Utility.isConnected(baseContext)) {
                    displayDoubleButtonDialog()
                } else if (e.message!!.contains("after")) {
                    displaySingleButtonDialog(
                        getString(R.string.poor_internet_title),
                        getString(R.string.poor_internet_message)
                    )
                } else {
                    displaySingleButtonDialog(
                        getString(R.string.server_error_title),
                        getString(R.string.server_error_message)
                    )
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val myResponse: String = response.body()!!.string()
                val pictureUpdateResponse: PictureUpdateResponse

                try {
                    pictureUpdateResponse = mapper.readValue(myResponse)
                    sharedPreferencesEditor.putString(getString(R.string.profile_picture),
                        pictureUpdateResponse.profilePicture)
                    sharedPreferencesEditor.apply()
                } catch (exception: IOException) {
                    exception.printStackTrace()
                    displaySingleButtonDialog(
                        getString(R.string.server_error_title),
                        getString(R.string.server_error_message)
                    )
                }
            }
        })
    }

    private fun commitInstanceModel(mapper: ObjectMapper, activityInstanceModel: ActivityInstanceModel) {
        val activityInstanceModelString =
            mapper.writeValueAsString(activityInstanceModel)
        sharedPreferencesEditor.putString(
            getString(R.string.activity_instance_model),
            activityInstanceModelString
        )
        sharedPreferencesEditor.apply()
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.root).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun showSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, binding.root).show(WindowInsetsCompat.Type.systemBars())
    }

    fun displayDoubleButtonDialog() {
        runOnUiThread {
            binding.doubleButtonDialog.doubleButtonTitle.text = getString(R.string.network_error_title)
            binding.doubleButtonDialog.doubleButtonMessage.text = getString(R.string.network_error_message)
            binding.doubleButtonDialog.doubleButtonLayout.visibility = View.VISIBLE
        }
    }

    fun displaySingleButtonDialog(title: String, message: String) {
        runOnUiThread {
            binding.singleButtonDialog.singleButtonTitle.text = title
            binding.singleButtonDialog.singleButtonMessage.text = message
            binding.singleButtonDialog.singleButtonLayout.visibility = View.VISIBLE
        }
    }

    companion object {
        const val TAG = "ProfileEditorActivity"
    }
}


