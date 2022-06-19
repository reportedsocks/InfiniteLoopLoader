package com.example.lib

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import androidx.core.graphics.withScale
import kotlin.math.min

class LoopLoader: View {

    companion object {
        private const val CIRCLE_TOP = 270f
        private const val TOTAL_DEGREES = 360f
        private const val DEFAULT_SCALE = 1f
        private const val GAP_BETWEEN_SEGMENTS = 2.5f
    }

    var segmentPaintWidth = 100f
        set(value) {
            evenSegmentPaint.strokeWidth = value
            oddSegmentPaint.strokeWidth = value
            field = value
        }
    var segmentRotationDuration = 700L
    var segmentTransformationDuration = 200L
    var activeSegmentColor = context?.resources?.getColor(R.color.white, context.theme) ?: Color.GRAY
    var passiveSegmentColor = context?.resources?.getColor(R.color.light_gray, context.theme) ?: Color.GRAY
    var numberOfSegments = 6
        set(value) {
            if (value % 2 != 0) return  // check for even number
            segmentWidth = TOTAL_DEGREES / value
            field = value
        }
    var maxScale = 1.05f
        set(value) {
            maxScaleOverhead = value - DEFAULT_SCALE
            field = value
        }
    var rotationDirection = RotationDirection.CLOCKWISE

    private val rect = RectF()

    private var actualActiveColor = activeSegmentColor
    private var actualPassiveColor = activeSegmentColor

    private val evenSegmentPaint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeWidth = segmentPaintWidth
    }
    private val oddSegmentPaint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeWidth = segmentPaintWidth
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

    private var horizontalCenter = 0f
    private var verticalCenter = 0f
    private var rectSize = 0f

    private var maxScaleOverhead = maxScale - DEFAULT_SCALE
        set(value) {
            minScaleOverhead = maxScale - DEFAULT_SCALE - value
            field = value
        }
    private var minScaleOverhead = maxScale - DEFAULT_SCALE - maxScaleOverhead

    private var animationEndWasCalled = false

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.LoopLoader)

        if (a.hasValue(R.styleable.LoopLoader_segmentPaintWidth))
            segmentPaintWidth = a.getFloat(R.styleable.LoopLoader_segmentPaintWidth, segmentPaintWidth)
        if (a.hasValue(R.styleable.LoopLoader_segmentRotationDuration))
            segmentRotationDuration = a.getInt(R.styleable.LoopLoader_segmentRotationDuration, segmentRotationDuration.toInt()).toLong()
        if (a.hasValue(R.styleable.LoopLoader_segmentTransformationDuration))
            segmentTransformationDuration = a.getInt(R.styleable.LoopLoader_segmentTransformationDuration, segmentTransformationDuration.toInt()).toLong()
        if (a.hasValue(R.styleable.LoopLoader_activeSegmentColor))
            activeSegmentColor = a.getColor(R.styleable.LoopLoader_activeSegmentColor, activeSegmentColor)
        if (a.hasValue(R.styleable.LoopLoader_passiveSegmentColor))
            passiveSegmentColor = a.getColor(R.styleable.LoopLoader_passiveSegmentColor, passiveSegmentColor)
        if (a.hasValue(R.styleable.LoopLoader_numberOfSegments))
            numberOfSegments = a.getInt(R.styleable.LoopLoader_numberOfSegments, numberOfSegments)
        if (a.hasValue(R.styleable.LoopLoader_maxScale))
            maxScale = a.getFloat(R.styleable.LoopLoader_maxScale, maxScale)
        if (a.hasValue(R.styleable.LoopLoader_rotationDirection))
            rotationDirection =
                RotationDirection.values()[a.getInt(R.styleable.LoopLoader_rotationDirection, 0)]
        a.recycle()
    }
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        horizontalCenter = width / 2f
        verticalCenter = height / 2f
        rectSize = min(horizontalCenter,verticalCenter) - (segmentPaintWidth / 2 * maxScale)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val size = min(measuredHeight, measuredWidth)
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        evenSegmentPaint.color = if (isAnimatingEvenSegments) actualActiveColor else actualPassiveColor
        oddSegmentPaint.color = if (isAnimatingEvenSegments) actualPassiveColor else actualActiveColor

        scaleAndDraw(
            canvas,
            if (isAnimatingEvenSegments) { {drawOddSegments(canvas, oddOffset)} } else { {drawEvenSegments(canvas, evenOffset)} },
            if (isAnimatingEvenSegments) { {drawEvenSegments(canvas, getEvenActiveOffset())} } else { {drawOddSegments(canvas, getOddActiveOffset())} }
        )

    }

    private fun getEvenActiveOffset() =
        if (rotationDirection == RotationDirection.CLOCKWISE)
            evenOffset + offsetRotationModifier else evenOffset - offsetRotationModifier

    private fun getOddActiveOffset() =
        if (rotationDirection == RotationDirection.CLOCKWISE)
            oddOffset + offsetRotationModifier else oddOffset - offsetRotationModifier


    private fun scaleAndDraw(canvas: Canvas, passiveSegments: () -> Unit, activeSegments: () -> Unit) {

        val downScale = DEFAULT_SCALE + minScaleOverhead
        val upScale = DEFAULT_SCALE + maxScaleOverhead

        canvas.withScale(
            downScale,
            downScale,
            horizontalCenter,
            verticalCenter) {
            calculateRect(downScale)
            passiveSegments()
        }
        canvas.withScale(
            upScale,
            upScale,
            horizontalCenter,
            verticalCenter) {
            calculateRect(upScale)
            activeSegments()
        }
    }

    private fun calculateRect(scale: Float = DEFAULT_SCALE) {
        val scaledRectSize = rectSize / scale
        rect.set(
            horizontalCenter - scaledRectSize,
            verticalCenter - scaledRectSize,
            horizontalCenter + scaledRectSize,
            verticalCenter + scaledRectSize
        )
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
            canvas.drawArc(rect, offset, segmentWidth - GAP_BETWEEN_SEGMENTS, false, paint)
            offset += segmentWidth * 2
        }
    }

    fun startAnimation() {
        AnimatorSet().apply {
            play(getPassiveToActiveColorAnimator())
                .with(getActiveToPassiveColorAnimator())
                .with(getScaleAnimator())
                .before(getRotationAnimator())

            addListener(object: Animator.AnimatorListener {
                override fun onAnimationStart(animator: Animator?) {}
                override fun onAnimationCancel(animator: Animator?) {}
                override fun onAnimationRepeat(animator: Animator?) {}

                override fun onAnimationEnd(animator: Animator?) {
                    if (animationEndWasCalled) {
                        animationEndWasCalled = false
                    } else {
                        isAnimatingEvenSegments = !isAnimatingEvenSegments
                        animator?.start()
                    }
                }
            })

            start()
        }
    }

    private fun getRotationAnimator() = ValueAnimator.ofFloat(0f, segmentWidth * 2)
        .apply {
            duration = segmentRotationDuration
            interpolator = AccelerateInterpolator()
            addUpdateListener {
                offsetRotationModifier = it.animatedValue as Float
                invalidate()
            }
        }

    private fun getPassiveToActiveColorAnimator() = ValueAnimator.ofArgb(passiveSegmentColor, activeSegmentColor)
        .apply {
            duration = segmentTransformationDuration
            addUpdateListener {
                actualActiveColor = it.animatedValue as Int
                invalidate()
            }
        }

    private fun getActiveToPassiveColorAnimator() = ValueAnimator.ofArgb(activeSegmentColor, passiveSegmentColor)
        .apply {
            duration = segmentTransformationDuration
            addUpdateListener {
                actualPassiveColor = it.animatedValue as Int
                invalidate()
            }
        }

    private fun getScaleAnimator() = ValueAnimator.ofFloat(0f, maxScale - DEFAULT_SCALE )
        .apply {
            duration = segmentTransformationDuration
            addUpdateListener {
                maxScaleOverhead = it.animatedValue as Float
                invalidate()
            }
        }


    fun endAnimation() {
        animationEndWasCalled = true
    }

    enum class RotationDirection {
        CLOCKWISE,
        COUNTERCLOCKWISE
    }
}

