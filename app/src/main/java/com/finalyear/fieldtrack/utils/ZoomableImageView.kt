package com.finalyear.fieldtrack.utils

import android.content.Context
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView

/**
 * ImageView that supports multi-touch pinch-to-zoom on the captured/site photo.
 * Demonstrates the "Using Multi-touch" guideline feature.
 */
class ZoomableImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

    private val matrixT = Matrix()
    private var scaleFactor = 1f
    private val minScale = 1f
    private val maxScale = 5f

    private val scaleDetector = ScaleGestureDetector(context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                scaleFactor *= detector.scaleFactor
                scaleFactor = scaleFactor.coerceIn(minScale, maxScale)
                matrixT.setScale(scaleFactor, scaleFactor, width / 2f, height / 2f)
                imageMatrix = matrixT
                return true
            }
        })

    init {
        scaleType = ScaleType.MATRIX
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        return true
    }
}
