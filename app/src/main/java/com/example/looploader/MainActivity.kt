package com.example.looploader

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.lib.LoopLoader

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val loopLoader = findViewById<LoopLoader>(R.id.loop_loader)
        loopLoader.animateRotation()
        loopLoader.numberOfSegments = 5
    }
}