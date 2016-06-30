package com.dgmltn.metaball

import java.util.ArrayList

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

/**
 * Heavily inspired by:
 * https://raw.githubusercontent.com/dodola/MetaballLoading/master/app/src/main/java/com/dodola/animview/MetaballView.java
 */
class MetaballView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    /**
     * Radius of the cursor dot.
     */
    private val cursorRadiusPx by lazy { resources.getDimension(R.dimen.default_radius) }

    /**
     * Radius of the fixed dots.
     */
    private val fixedRadiusPx by lazy { cursorRadiusPx * 0.7f }

    /**
     * Number of fixed dots.
     */
    var dotCount = 3

    /**
     * Amount by which the thickness of the elastic band thins in the middle when it's stretched. 0f = no thinning.
     */
    private val bandThinning = 2f

    /**
     * Number of pixels by which to space apart the fixed circles.
     */
    private val spacingPx = cursorRadiusPx * 4f

    /**
     * Maximum length of the band, before it snaps
     */
    private val bandMaxLength = spacingPx

    /**
     * Rate at which to scale the fixed circle due to the proximity of the cursor.
     */
    private val scaleRate = 0.3f

    /**
     * Thickness of the elastic band (relative to the circle radius). [0f, 1f]
     */
    private val bandThickness = 0.5f

    /**
     * The fixed circle that is currently connected to the cursor.
     */
    var connectedIndex = 0

    /**
     * Whether or not the cursor is animated.
     */
    private val isAnimated = false

    /**
     * The scaled x-position of the cursor. 0f = centered on the first circle,
     * 1f = centered on the second circle, etc.
     */
    // Position the cursor
    var cursorPosition = 0f
        set(t) {
            field = t
            if (dots.size > 0) {
                cursor.x = spacingPx * t + dots[0].x
            }
            invalidate()
        }

    // Current state of the circles
    private val cursor = Circle()
    private val dots = ArrayList<Circle>()

    // Cached variables used during onDraw
    private val paint = Paint()
    private val paint2 = Paint()
    private val path1 = Path()

    init {
        // Paint for both the page dots, and the cursor
        paint.color = resources.getColor(R.color.dotSelected)
        paint.style = Paint.Style.FILL
        paint.isAntiAlias = true

        paint2.color = resources.getColor(R.color.dotUnselected)
        paint.style = Paint.Style.FILL
        paint.isAntiAlias = true

        setPaintMode(1)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // Initialize the cursor
        cursor.x = (w - paddingRight + paddingLeft - spacingPx * (dotCount - 1)) / 2f
        cursor.y = (h - paddingBottom + paddingTop) / 2f
        cursor.radius = cursorRadiusPx

        // Initialize/update the page dots
        while (dots.size < dotCount) {
            dots.add(Circle())
        }
        while (dots.size > dotCount) {
            dots.removeAt(0)
        }
        for (i in 0..dotCount - 1) {
            val c = dots[i]
            dots[i].x = spacingPx * i + cursor.x
            dots[i].y = cursor.y
            dots[i].radius = fixedRadiusPx
        }
    }

    fun setPaintMode(mode: Int) {
        paint.style = if (mode == 0) Paint.Style.STROKE else Paint.Style.FILL
        invalidate()
    }

    /**
     * @param canvas       canvas
     * @param paint        paint used to draw the circles and the band
     * @param fixedCircle
     * @param movingCircle
     */
    private fun metaball(canvas: Canvas, paint: Paint, fixedCircle: Circle?, movingCircle: Circle?) {

        if (movingCircle == null || fixedCircle == null) {
            return
        }

        val d = movingCircle.getDistanceTo(fixedCircle)

        var radius1 = movingCircle.radius
        var radius2 = fixedCircle.radius
        var scale2 = 1f

        //        Log.d("Metaball_radius", "radius1:" + radius1 + ",radius2:" + radius2);
        if (radius1 == 0f || radius2 == 0f) {
            return
        }

        // Scale up the fixed circle if they're close together
        if (d <= bandMaxLength) {
            scale2 = 1 + scaleRate * (1 - d / bandMaxLength)
            radius2 *= scale2
        }

        // Draw cursor
        movingCircle.draw(canvas, paint)

        // No need to draw the fixed circle if it's overlapped by the cursor
        if (d <= Math.abs(radius1 - radius2)) {
            return
        }

        // Draw fixed circle
        fixedCircle.draw(canvas, paint, scale2)

        // Nothing else to do if the circles are too far apart to draw the band
        if (d > bandMaxLength) {
            return
        }

        val u1: Float
        val u2: Float

        if (d < radius1 + radius2) {
            u1 = Math.acos(((radius1 * radius1 + d * d - radius2 * radius2) / (2f * radius1 * d)).toDouble()).toFloat()
            u2 = Math.acos(((radius2 * radius2 + d * d - radius1 * radius1) / (2f * radius2 * d)).toDouble()).toFloat()
        }
        else {
            u1 = 0f
            u2 = 0f
        }
        //        Log.d("Metaball", "center2:" + Arrays.toString(center2) + ",center1:" + Arrays.toString(center1));
        val centermin = floatArrayOf(fixedCircle.x - movingCircle.x, fixedCircle.y - movingCircle.y)

        val angle1 = Math.atan2(centermin[1].toDouble(), centermin[0].toDouble()).toFloat()
        val angle2 = Math.acos(((radius1 - radius2) / d).toDouble()).toFloat()
        val angle1a = angle1 + u1 + (angle2 - u1) * bandThickness
        val angle1b = angle1 - u1 - (angle2 - u1) * bandThickness
        val angle2a = (angle1 + Math.PI - u2.toDouble() - (Math.PI - u2.toDouble() - angle2.toDouble()) * bandThickness).toFloat()
        val angle2b = (angle1 - Math.PI + u2.toDouble() + (Math.PI - u2.toDouble() - angle2.toDouble()) * bandThickness).toFloat()

        //        Log.d("Metaball", "angle1:" + angle1 + ",angle2:" + angle2 + ",angle1a:" + angle1a + ",angle1b:" + angle1b + ",angle2a:" + angle2a + ",angle2b:" + angle2b);


        val p1a1 = getVector(angle1a, radius1)
        val p1b1 = getVector(angle1b, radius1)
        val p2a1 = getVector(angle2a, radius2)
        val p2b1 = getVector(angle2b, radius2)

        val p1a = floatArrayOf(p1a1[0] + movingCircle.x, p1a1[1] + movingCircle.y)
        val p1b = floatArrayOf(p1b1[0] + movingCircle.x, p1b1[1] + movingCircle.y)
        val p2a = floatArrayOf(p2a1[0] + fixedCircle.x, p2a1[1] + fixedCircle.y)
        val p2b = floatArrayOf(p2b1[0] + fixedCircle.x, p2b1[1] + fixedCircle.y)


        //        Log.d("Metaball", "p1a:" + Arrays.toString(p1a) + ",p1b:" + Arrays.toString(p1b) + ",p2a:" + Arrays.toString(p2a) + ",p2b:" + Arrays.toString(p2b));

        val p1_p2 = floatArrayOf(p1a[0] - p2a[0], p1a[1] - p2a[1])

        val totalRadius = radius1 + radius2
        var d2 = Math.min(bandThickness * bandThinning, getLength(p1_p2) / totalRadius)
        d2 *= Math.min(1f, d * 2 / (radius1 + radius2))
        //        Log.d("Metaball", "d2:" + d2);
        radius1 *= d2
        radius2 *= d2

        // The points along the edge of the circles from which to draw the elastic band
        val sp1 = getVector(angle1a - PI2, radius1)
        val sp2 = getVector(angle2a + PI2, radius2)
        val sp3 = getVector(angle2b - PI2, radius2)
        val sp4 = getVector(angle1b + PI2, radius1)
        //        Log.d("Metaball", "sp1:" + Arrays.toString(sp1) + ",sp2:" + Arrays.toString(sp2) + ",sp3:" + Arrays.toString(sp3) + ",sp4:" + Arrays.toString(sp4));


        path1.reset()
        path1.moveTo(p1a[0], p1a[1])
        path1.cubicTo(p1a[0] + sp1[0], p1a[1] + sp1[1], p2a[0] + sp2[0], p2a[1] + sp2[1], p2a[0], p2a[1])
        path1.lineTo(p2b[0], p2b[1])
        path1.cubicTo(p2b[0] + sp3[0], p2b[1] + sp3[1], p1b[0] + sp4[0], p1b[1] + sp4[1], p1b[0], p1b[1])
        path1.lineTo(p1a[0], p1a[1])
        path1.close()
        canvas.drawPath(path1, paint)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw the page dots
        var i = 0
        val l = dots.size
        while (i < l) {
            if (i == connectedIndex) {
                metaball(canvas, paint, dots[i], cursor)
            }
            else {
                dots[i].draw(canvas, paint2)
            }
            i++
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
                View.resolveSizeAndState((dotCount * (cursorRadiusPx * 2 + spacingPx)).toInt(), widthMeasureSpec, 0),
                View.resolveSizeAndState((2f * cursorRadiusPx * 1.4f).toInt(), heightMeasureSpec, 0))
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)

        if (visibility == View.GONE || visibility == View.INVISIBLE) {
            stopAnimation()
        }
        else if (isAnimated) {
            startAnimation()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isAnimated) {
            startAnimation()
        }
    }

    override fun onDetachedFromWindow() {
        stopAnimation()
        super.onDetachedFromWindow()
    }

    private fun stopAnimation() {
        clearAnimation()
        postInvalidate()
    }

    private fun startAnimation() {
        val anim = ObjectAnimator.ofFloat(this, "cursorPosition", 0f, dotCount - 1f).setDuration(2500)
        anim.repeatCount = ValueAnimator.INFINITE
        anim.repeatMode = ValueAnimator.REVERSE
        anim.start()
    }

    private class Circle(var x: Float = 0f, var y: Float = 0f, var radius: Float = 1f) {

        fun draw(canvas: Canvas, paint: Paint) {
            canvas.drawCircle(x, y, radius, paint)
        }

        fun draw(canvas: Canvas, paint: Paint, scale: Float) {
            canvas.drawCircle(x, y, radius * scale, paint)
        }

        fun getDistanceTo(other: Circle): Float {
            val dx = x - other.x
            val dy = y - other.y
            return Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
        }
    }

    companion object {

        private val PI2 = (Math.PI / 2.0).toFloat()

        ///////////////////////////////////////////////////////////////////////////
        // Utilities
        ///////////////////////////////////////////////////////////////////////////

        private fun getLength(b: FloatArray): Float {
            return Math.sqrt((b[0] * b[0] + b[1] * b[1]).toDouble()).toFloat()
        }

        private fun getVector(radians: Float, length: Float): FloatArray {
            val x = (Math.cos(radians.toDouble()) * length).toFloat()
            val y = (Math.sin(radians.toDouble()) * length).toFloat()
            return floatArrayOf(x, y)
        }
    }
}
