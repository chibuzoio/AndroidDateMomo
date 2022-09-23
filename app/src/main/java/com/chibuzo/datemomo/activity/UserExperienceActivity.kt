package com.chibuzo.datemomo.activity

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.chibuzo.datemomo.R
import com.chibuzo.datemomo.databinding.ActivityUserExperienceBinding

class UserExperienceActivity : AppCompatActivity() {
    private lateinit var bundle: Bundle
    private var requestProcess: String = ""
    private var leastRootViewHeight: Int = 0
    private lateinit var buttonClickEffect: AlphaAnimation
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var viewRootHeightArray: MutableSet<Int>
    private lateinit var binding: ActivityUserExperienceBinding
    private lateinit var sharedPreferencesEditor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserExperienceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hideSystemUI()

        bundle = intent.extras!!

        viewRootHeightArray = mutableSetOf()

        buttonClickEffect = AlphaAnimation(1f, 0f)

        sharedPreferences =
            getSharedPreferences(getString(R.string.shared_preferences), Context.MODE_PRIVATE)
        sharedPreferencesEditor = sharedPreferences.edit()

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
                binding.userReportingEditor.clearFocus()
                viewRootHeightArray = mutableSetOf()
                leastRootViewHeight = 0
                hideSystemUI()
            }
        }

        binding.userReportingEditor.setOnFocusChangeListener { _, focused ->
            if (focused) {
                showSystemUI()
            } else {
                hideSystemUI()
            }
        }

        val userFullName = bundle.getString("fullName")!!.ifEmpty {
            bundle.getString("userName")!!.replaceFirstChar { it.uppercase() }
        }

        binding.abusiveCheckbox.text = getString(R.string.report_abusive_words, userFullName)
        binding.userReportingHeader.text = getString(R.string.report_user_header, userFullName)
        binding.harassmentCheckbox.text = getString(R.string.report_sexual_harassment, userFullName)
        binding.impersonationCheckbox.text = getString(R.string.report_user_impersonation, userFullName)

        binding.submitReportButton.blueButtonText.text = "Submit Report"

        binding.submitReportButton.blueButtonLayout.setOnClickListener {
            binding.submitReportButton.blueButtonLayout.startAnimation(buttonClickEffect)

        }
    }

    override fun onStart() {
        super.onStart()
        hideSystemUI()
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

    companion object {
        const val TAG = "UserExperienceActivity"
    }
}


