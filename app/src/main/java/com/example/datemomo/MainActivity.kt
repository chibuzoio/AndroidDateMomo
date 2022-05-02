package com.example.datemomo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.example.datemomo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

/*        binding.nameInput.genericInputField.setOnFocusChangeListener {view, focused ->
            if (focused) {
                view.background = ContextCompat.getDrawable(this, R.drawable.focused_edit_text)
            } else {
                view.background = ContextCompat.getDrawable(this, R.drawable.normal_edit_text)
            }
        }

        binding.sexInput.genericInputField.setOnFocusChangeListener {view, focused ->
            if (focused) {
                view.background = ContextCompat.getDrawable(this, R.drawable.focused_edit_text)
            } else {
                view.background = ContextCompat.getDrawable(this, R.drawable.normal_edit_text)
            }
        }*/
    }
}


