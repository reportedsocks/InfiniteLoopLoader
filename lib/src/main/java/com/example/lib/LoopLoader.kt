package com.example.lib

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import androidx.core.content.ContextCompat
import androidx.core.graphics.withScale
import kotlin.math.min
import androidx.core.content.res.use

class LoopLoader: View {

    //region Constants
    private val CIRCLE_TOP = 270f
    private val TOTAL_DEGREES = 360f
    //endregion Constants

    //region styleable variables
    private val DEFAULT_SCALE = 1f
    private val GAP_BETWEEN_SEGMENTS = 2.5f
    private val SHADOW_OFFSET = 6f
    private val NEGATIVE_SHADOW_OFFSET = 2f
    private val BLUR_RADIUS = 4f
    //endregion styleable variables

    var innerCircleColor = ContextCompat.getColor(context, R.color.dark_gray)
    set(value) {
        circlePaint.color = value
        field = value
    }
    var segmentPaintWidth = 100f
        set(value) {
            evenSegmentPaint.strokeWidth = value
            oddSegmentPaint.strokeWidth = value
            field = value
        }
    var shadowPaintWidth = segmentPaintWidth - BLUR_RADIUS * 2
        set(value) {
            shadowPaint.strokeWidth = value
            field = value
        }
    var segmentRotationDuration = 700L
    var segmentTransformationDuration = 200L
    var activeSegmentColor = ContextCompat.getColor(context, R.color.white)
    var passiveSegmentColor = ContextCompat.getColor(context, R.color.light_gray)
    var shadowColor = ContextCompat.getColor(context, R.color.dark_gray)
        set(value) {
            shadowPaint.color = value
            field = value
        }
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
    private val shadowPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = shadowPaintWidth
        color = shadowColor
        isAntiAlias = true
        maskFilter = BlurMaskFilter(BLUR_RADIUS, BlurMaskFilter.Blur.NORMAL)
    }
    private val circlePaint = Paint().apply {
        color = innerCircleColor
        isAntiAlias = true
    }

    private var segmentWidth = TOTAL_DEGREES / numberOfSegments
        set(value) {
            oddOffset = CIRCLE_TOP + value
            field = value
        }

    private var evenOffset = CIRCLE_TOP
    private var oddOffset = CIRCLE_TOP + segmentWidth

    private val evenActiveOffset get() =
        if (rotationDirection == RotationDirection.CLOCKWISE)
            evenOffset + offsetRotationModifier else evenOffset - offsetRotationModifier

    private val oddActiveOffset get() =
        if (rotationDirection == RotationDirection.CLOCKWISE)
            oddOffset + offsetRotationModifier else oddOffset - offsetRotationModifier

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

    private val innerCircleRadius get() = width / 2 - segmentPaintWidth

    private val outerBoundPath = Path()
    private val outerCircleRadius get() = width / 2f - (segmentPaintWidth * maxScale - segmentPaintWidth)

    private var animationEndWasCalled = false

    private val rotationAnimator get() = ValueAnimator.ofFloat(0f, segmentWidth * 2).apply {
        duration = segmentRotationDuration
        interpolator = AccelerateInterpolator()
        addUpdateListener {
            offsetRotationModifier = it.animatedValue as Float
            invalidate()
        }
    }

    private val passiveToActiveColorAnimator get() = ValueAnimator.ofArgb(passiveSegmentColor, activeSegmentColor).apply {
        duration = segmentTransformationDuration
        addUpdateListener {
            actualActiveColor = it.animatedValue as Int
            invalidate()
        }
    }

    private val activeToPassiveColorAnimator get() = ValueAnimator.ofArgb(activeSegmentColor, passiveSegmentColor).apply {
        duration = segmentTransformationDuration
        addUpdateListener {
            actualPassiveColor = it.animatedValue as Int
            invalidate()
        }
    }

    private val scaleAnimator get() = ValueAnimator.ofFloat(0f, maxScale - DEFAULT_SCALE ).apply {
        duration = segmentTransformationDuration
        addUpdateListener {
            maxScaleOverhead = it.animatedValue as Float
            invalidate()
        }
    }

    private var animatorSet: AnimatorSet? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        context.obtainStyledAttributes(attrs, R.styleable.LoopLoader).use {

            segmentPaintWidth = it.getDimensionPixelSize(R.styleable.LoopLoader_ll_segmentPaintWidth, segmentPaintWidth.toInt()).toFloat()
            shadowPaintWidth = it.getDimensionPixelSize(R.styleable.LoopLoader_ll_shadowPaintWidth, shadowPaintWidth.toInt()).toFloat()
            segmentRotationDuration = it.getInt(R.styleable.LoopLoader_ll_segmentRotationDuration, segmentRotationDuration.toInt()).toLong()
            segmentTransformationDuration = it.getInt(R.styleable.LoopLoader_ll_segmentTransformationDuration, segmentTransformationDuration.toInt()).toLong()
            activeSegmentColor = it.getColor(R.styleable.LoopLoader_ll_activeSegmentColor, activeSegmentColor)
            passiveSegmentColor = it.getColor(R.styleable.LoopLoader_ll_passiveSegmentColor, passiveSegmentColor)
            shadowColor = it.getColor(R.styleable.LoopLoader_ll_shadowColor, shadowColor)
            numberOfSegments = it.getInt(R.styleable.LoopLoader_ll_numberOfSegments, numberOfSegments)
            maxScale = it.getFloat(R.styleable.LoopLoader_ll_maxScale, maxScale)
            rotationDirection = RotationDirection.values()[it.getInt(R.styleable.LoopLoader_ll_rotationDirection, 0)]
            innerCircleColor = it.getColor(R.styleable.LoopLoader_android_background, innerCircleColor)
            if (it.hasValue(R.styleable.LoopLoader_ll_innerCircleColor))
                innerCircleColor = it.getColor(R.styleable.LoopLoader_ll_innerCircleColor, innerCircleColor)
        }
    }
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    override fun setBackgroundColor(color: Int) {
        super.setBackgroundColor(color)
        innerCircleColor = color
    }

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

        canvas.clipPath(outerBoundPath.apply {
            reset()
            addCircle(rect.centerX(), rect.centerY(), outerCircleRadius, Path.Direction.CW)
        })

        scaleAndDraw(
            canvas,
            if (isAnimatingEvenSegments) { {drawOddSegments(canvas, oddOffset)} } else { {drawEvenSegments(canvas, evenOffset)} },
            if (isAnimatingEvenSegments) { {drawEvenSegments(canvas, evenActiveOffset)} } else { {drawOddSegments(canvas, oddActiveOffset)} }
        )

        canvas.drawCircle(rect.centerX(), rect.centerY(), innerCircleRadius, circlePaint)

    }

    private fun scaleAndDraw(canvas: Canvas, passiveSegments: () -> Unit, activeSegments: () -> Unit) {

        val downScale = DEFAULT_SCALE + minScaleOverhead
        val upScale = DEFAULT_SCALE + maxScaleOverhead

        with(canvas) {
            withScale(
                downScale,
                downScale,
                horizontalCenter,
                verticalCenter) {
                calculateRect(downScale)
                passiveSegments()
            }
            withScale(
                upScale,
                upScale,
                horizontalCenter,
                verticalCenter) {
                calculateRect(upScale)
                activeSegments()
            }
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
        drawSegments(canvas, 0, offset, evenSegmentPaint, isAnimatingEvenSegments)
    }

    private fun drawOddSegments(canvas: Canvas,  offset: Float) {
        drawSegments(canvas, 1, offset, oddSegmentPaint, !isAnimatingEvenSegments)
    }

    private fun drawSegments(canvas: Canvas, startIndex: Int, startOffset: Float, paint: Paint, withShadow: Boolean) {
        var offset = startOffset
        for (i in startIndex .. numberOfSegments step 2) {
            if (withShadow)
                canvas.drawArc(rect, offset - NEGATIVE_SHADOW_OFFSET, segmentWidth - GAP_BETWEEN_SEGMENTS + SHADOW_OFFSET, false, shadowPaint)
            else
                setShader(paint)
            canvas.drawArc(rect, offset, segmentWidth - GAP_BETWEEN_SEGMENTS, false, paint)
            offset += segmentWidth * 2
            paint.shader = null
        }
    }

    private fun setShader(paint: Paint) {
        paint.shader = RadialGradient(rect.centerX(), rect.centerY(), rect.width(), intArrayOf(paint.color, shadowColor), floatArrayOf(0.6f, 0.8f), Shader.TileMode.CLAMP)
    }

    fun startAnimation() {
        animatorSet = AnimatorSet().apply {
            play(passiveToActiveColorAnimator)
                .with(activeToPassiveColorAnimator)
                .with(scaleAnimator)
                .before(rotationAnimator)

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

    fun endAnimation() {
        animationEndWasCalled = true
        animatorSet?.end()
    }

    enum class RotationDirection {
        CLOCKWISE,
        COUNTERCLOCKWISE
    }
}

