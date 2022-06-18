package com.example.lib

import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import androidx.core.content.res.ResourcesCompat
import kotlin.math.min

class LoopLoader: View {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    private lateinit var extraCanvas: Canvas
    private lateinit var extraBitmap: Bitmap

    private val ovalSpace = RectF()

    private val backgroundColor = ResourcesCompat.getColor(resources, R.color.colorBackground, null)

    private val activeSegmentColor = context?.resources?.getColor(R.color.white, null) ?: Color.GRAY
    private val passiveSegmentColor = context?.resources?.getColor(R.color.light_gray, null) ?: Color.GRAY

    private val segmentPaint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
        color = activeSegmentColor
        strokeWidth = 100f
    }

    var numberOfSegments = 6
        set(value) {
            if (value % 2 != 0) return
            segmentWidth = 360f / value
            field = value
        }
    private var segmentWidth = 360f / numberOfSegments

    private var offsetRotationModifier = 0f

    private var isAnimatingEvenSegments = true

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)

        if (::extraBitmap.isInitialized) extraBitmap.recycle()
        extraBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        extraCanvas = Canvas(extraBitmap)
        extraCanvas.drawColor(backgroundColor)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(extraBitmap, 0f, 0f, null)

        setSpace()

        var evenOffset = 270f
        var oddOffset = 270f + segmentWidth

        //change order of drawing so animated segments are on top
        if (isAnimatingEvenSegments) {

            evenOffset += offsetRotationModifier

            segmentPaint.color = passiveSegmentColor
            drawOddSegments(canvas, oddOffset)
            segmentPaint.color = activeSegmentColor
            drawEvenSegments(canvas, evenOffset)
        } else {

            oddOffset += offsetRotationModifier

            segmentPaint.color = passiveSegmentColor
            drawEvenSegments(canvas, evenOffset)
            segmentPaint.color = activeSegmentColor
            drawOddSegments(canvas, oddOffset)
        }

        if (offsetRotationModifier == segmentWidth * 2)
            isAnimatingEvenSegments = !isAnimatingEvenSegments

    }

    private fun drawEvenSegments(canvas: Canvas,  offset: Float) {
        var evenOffset = offset
        for (i in 0 .. numberOfSegments step 2) {
            canvas.drawArc(ovalSpace, evenOffset, segmentWidth - 2.5f, false, segmentPaint)
            evenOffset += segmentWidth * 2
        }
    }

    private fun drawOddSegments(canvas: Canvas,  offset: Float) {
        var oddOffset = offset
        for (i in 1 .. numberOfSegments step 2) {
            canvas.drawArc(ovalSpace, oddOffset, segmentWidth - 2.5f, false, segmentPaint)
            oddOffset += segmentWidth * 2
        }
    }

    private fun setSpace() {
        val horizontalCenter = (width.div(2)).toFloat()
        val verticalCenter = (height.div(2)).toFloat()
        val ovalSize = min(width,height) / 4
        ovalSpace.set(
            horizontalCenter - ovalSize,
            verticalCenter - ovalSize,
            horizontalCenter + ovalSize,
            verticalCenter + ovalSize
        )
    }

    fun animateRotation() {
        val valuesHolder = PropertyValuesHolder.ofFloat(
            "rotation",
            0f,
            segmentWidth * 2
        )

        val animator = ValueAnimator().apply {
            setValues(valuesHolder)
            duration = 700
            interpolator = AccelerateInterpolator()
            repeatCount = ValueAnimator.INFINITE

            addUpdateListener {
                offsetRotationModifier = it.getAnimatedValue("rotation") as Float

                invalidate()
            }

        }
        animator.start()
    }

}