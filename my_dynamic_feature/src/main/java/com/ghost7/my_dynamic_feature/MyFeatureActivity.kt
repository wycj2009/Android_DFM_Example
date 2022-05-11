package com.ghost7.my_dynamic_feature

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ghost7.android_dfm_example.BuildConfig
import com.ghost7.my_dynamic_feature.databinding.ActivityMyFeatureBinding
import com.google.android.play.core.splitcompat.SplitCompat

class MyFeatureActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyFeatureBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SplitCompat.install(this)
        binding = ActivityMyFeatureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tv.text = "My Dynamic Feature Module\nVERSION_NAME: ${BuildConfig.VERSION_NAME}"
    }

}