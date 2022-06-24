package com.example.looploader

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.lib.LoopLoader
import com.example.looploader.databinding.ActivityMainBinding
import com.google.android.material.slider.Slider

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loopLoader1.apply {
            numberOfSegments = 5 // wont work
            rotationDirection = LoopLoader.RotationDirection.CLOCKWISE
            segmentPaintWidth = 80F
            shadowPaintWidth = 80f
            setBackgroundColor(Color.BLACK)
            startAnimation()
        }
        binding.loopLoader2.startAnimation()
        binding.loopLoader3.startAnimation()

        binding.numberOfSegmentsSlider.apply {
            valueFrom = 2f
            valueTo = 12f
            stepSize = 2f
            addOnSliderTouchListener(getOnSliderTouchListener { slider ->
                binding.loopLoader1.apply {
                    endAnimation()
                    numberOfSegments = slider.value.toInt()
                    startAnimation()
                }
            })
        }

        binding.transformationSpeedSlider.apply {
            valueFrom = 50f
            valueTo = 1000f
            addOnSliderTouchListener(getOnSliderTouchListener { slider ->
                binding.loopLoader1.apply {
                    endAnimation()
                    segmentTransformationDuration = slider.value.toLong()
                    startAnimation()
                }
            })
        }

        binding.rotationSpeedSlider.apply {
            valueFrom = 50f
            valueTo = 5000f
            addOnSliderTouchListener(getOnSliderTouchListener { slider ->
                binding.loopLoader1.apply {
                    endAnimation()
                    segmentRotationDuration = slider.value.toLong()
                    startAnimation()
                }
            })
        }

    }

    private fun getOnSliderTouchListener(block : (Slider) -> Unit): Slider.OnSliderTouchListener {
        return object : Slider.OnSliderTouchListener {

            @SuppressLint("RestrictedApi")
            override fun onStartTrackingTouch(slider: Slider) {}

            @SuppressLint("RestrictedApi")
            override fun onStopTrackingTouch(slider: Slider) {
                block(slider)
            }
        }
    }
}