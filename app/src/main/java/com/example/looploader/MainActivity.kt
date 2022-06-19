package com.example.looploader

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.lib.LoopLoader

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val loopLoader1 = findViewById<LoopLoader>(R.id.loop_loader1)
        loopLoader1.startAnimation()
        loopLoader1.numberOfSegments = 5 // wont work
        loopLoader1.rotationDirection = LoopLoader.RotationDirection.COUNTERCLOCKWISE
        loopLoader1.segmentPaintWidth = 20f
        loopLoader1.segmentRotationDuration = 500
        loopLoader1.setBackgroundColor(Color.BLACK)


        val loopLoader2 = findViewById<LoopLoader>(R.id.loop_loader2)
        loopLoader2.startAnimation()

        val loopLoader3 = findViewById<LoopLoader>(R.id.loop_loader3)
        loopLoader3.startAnimation()
    }
}