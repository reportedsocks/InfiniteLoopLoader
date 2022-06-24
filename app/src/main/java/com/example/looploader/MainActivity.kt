package com.example.looploader

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.lib.LoopLoader
import com.example.looploader.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loopLoader1.apply {
            numberOfSegments = 5 // wont work
            rotationDirection = LoopLoader.RotationDirection.CLOCKWISE
            segmentPaintWidth = 80f
            shadowPaintWidth = 80f
            setBackgroundColor(Color.BLACK)
            startAnimation()
        }

        binding.loopLoader2.startAnimation()

        binding.loopLoader3.startAnimation()
    }
}