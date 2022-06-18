package com.example.lib

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import androidx.core.content.res.ResourcesCompat
import kotlin.math.min

class LoopLoader: View {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    companion object {
        private const val CIRCLE_TOP = 270f
        private const val TOTAL_DEGREES = 360f
    }

    private lateinit var extraCanvas: Canvas
    private lateinit var extraBitmap: Bitmap

    private val rect = RectF()

    private val backgroundColor = ResourcesCompat.getColor(resources, R.color.colorBackground, context.theme)

    private val activeSegmentColor = context?.resources?.getColor(R.color.white, context.theme) ?: Color.GRAY
    private val passiveSegmentColor = context?.resources?.getColor(R.color.light_gray, context.theme) ?: Color.GRAY

    private var actualActiveColor = activeSegmentColor
    private var actualPassiveColor = activeSegmentColor

    private val evenSegmentPaint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeWidth = 100f
    }

    private val oddSegmentPaint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeWidth = 100f
    }

    var numberOfSegments = 6
        set(value) { // check for even number, then update all involved constants
            if (value % 2 != 0) return
            segmentWidth = TOTAL_DEGREES / value
            field = value
        }
    private var segmentWidth = TOTAL_DEGREES / numberOfSegments
        set(value) {
            oddOffset = CIRCLE_TOP + value
            field = value
        }

    private var evenOffset = CIRCLE_TOP
    private var oddOffset = CIRCLE_TOP + segmentWidth

    private var offsetRotationModifier = 0f

    private var isAnimatingEvenSegments = true

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)

        if (::extraBitmap.isInitialized) extraBitmap.recycle()
        extraBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        extraCanvas = Canvas(extraBitmap)
        extraCanvas.drawColor(backgroundColor)
        calculateRect()
    }

    private fun calculateRect() {
        val horizontalCenter = (width.div(2)).toFloat()
        val verticalCenter = (height.div(2)).toFloat()
        val rectSize = min(width,height) / 4
        rect.set(
            horizontalCenter - rectSize,
            verticalCenter - rectSize,
            horizontalCenter + rectSize,
            verticalCenter + rectSize
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(extraBitmap, 0f, 0f, null)


        if (isAnimatingEvenSegments) {

            evenSegmentPaint.color = actualActiveColor
            oddSegmentPaint.color = actualPassiveColor

            //change order of drawing so rotating segments are on top
            drawOddSegments(canvas, oddOffset)
            drawEvenSegments(canvas, evenOffset + offsetRotationModifier)
        } else {

            evenSegmentPaint.color = actualPassiveColor
            oddSegmentPaint.color = actualActiveColor

            drawEvenSegments(canvas, evenOffset)
            drawOddSegments(canvas, oddOffset + offsetRotationModifier)
        }

    }

    private fun drawEvenSegments(canvas: Canvas,  offset: Float) {
        drawSegments(canvas, 0, offset, evenSegmentPaint)
    }

    private fun drawOddSegments(canvas: Canvas,  offset: Float) {
        drawSegments(canvas, 1, offset, oddSegmentPaint)
    }

    private fun drawSegments(canvas: Canvas, startIndex: Int, startOffset: Float, paint: Paint) {
        var offset = startOffset
        for (i in startIndex .. numberOfSegments step 2) {
            canvas.drawArc(rect, offset, segmentWidth - 2.5f, false, paint)
            offset += segmentWidth * 2
        }
    }

    fun startAnimation() {

        val rotationAnimator = ValueAnimator.ofFloat(0f, segmentWidth * 2).apply {
            duration = 700
            interpolator = AccelerateInterpolator()
            addUpdateListener {
                offsetRotationModifier = it.animatedValue as Float
                invalidate()
            }
        }

        val passiveToActiveColorAnimator = ValueAnimator.ofArgb(passiveSegmentColor, activeSegmentColor).apply {
            duration = 200
            addUpdateListener {
                actualActiveColor = it.animatedValue as Int
                invalidate()
            }
        }

        val activeToPassiveColorAnimator = ValueAnimator.ofArgb(activeSegmentColor, passiveSegmentColor).apply {
            duration = 200
            addUpdateListener {
                actualPassiveColor = it.animatedValue as Int
                invalidate()
            }
        }

        val animatorSet = AnimatorSet().apply {
            play(passiveToActiveColorAnimator).with(activeToPassiveColorAnimator)
            play(rotationAnimator).after(passiveToActiveColorAnimator)

            addListener(object: Animator.AnimatorListener {
                override fun onAnimationStart(animator: Animator?) {}
                override fun onAnimationEnd(animator: Animator?) {
                    isAnimatingEvenSegments = !isAnimatingEvenSegments
                    animator?.start()
                }
                override fun onAnimationCancel(animator: Animator?) {}

                override fun onAnimationRepeat(animator: Animator?) {

                }
            })

            start()
        }
    }

}