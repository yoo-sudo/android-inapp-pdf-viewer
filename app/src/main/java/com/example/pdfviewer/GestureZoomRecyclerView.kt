package com.example.pdfviewer

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log.i
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.OnScaleGestureListener
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign


private const val TAG = "ZoomLayout"
private const val MIN_ZOOM = 1.0f
private const val MAX_ZOOM = 3.0f

class GestureZoomRecyclerView : RecyclerView, OnScaleGestureListener,
    GestureDetector.OnGestureListener,
    GestureDetector.OnDoubleTapListener {
    private enum class Mode {
        NONE, DRAG, ZOOM
    }

    private var mGestureDetector: GestureDetector? = null
    private var scaleDetector: ScaleGestureDetector? = null

    private var mode = Mode.NONE
    private var scale = 1.0f
    private var lastScaleFactor = 0f

    // Where the finger first  touches the screen
    private var startX = 0f
    private var startY = 0f

    // How much to translate the canvas
    private var dx = 0f
    private var dy = 0f
    private var prevDx = 0f
    private var prevDy = 0f

    // Custom vars to handle double tap
    private var restore = false

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(context)
    }

    override fun onTouchEvent(motionEvent: MotionEvent): Boolean {
        super.onTouchEvent(motionEvent)
        mGestureDetector?.onTouchEvent(motionEvent)
        when (motionEvent.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> if (scale > MIN_ZOOM) {
                mode = Mode.DRAG
                startX = motionEvent.x - prevDx
                startY = motionEvent.y - prevDy
            } else {
                mode = Mode.NONE
            }
            MotionEvent.ACTION_MOVE -> if (mode == Mode.DRAG) {
                dx = motionEvent.x - startX
                dy = motionEvent.y - startY
            }
            MotionEvent.ACTION_POINTER_DOWN -> mode = Mode.ZOOM
            MotionEvent.ACTION_POINTER_UP -> mode = Mode.ZOOM
            MotionEvent.ACTION_UP -> {
                i(TAG, "UP")
                mode = Mode.NONE
                prevDx = dx
                prevDy = dy
            }
        }
        scaleDetector?.onTouchEvent(motionEvent)
        calculateZoomLevel()
        return true
    }

    private fun calculateZoomLevel() {
        if (mode == Mode.DRAG && scale >= MIN_ZOOM || mode == Mode.ZOOM) {
            parent.requestDisallowInterceptTouchEvent(true)
            val maxDx: Float =
                (width - (width / scale)) / 2 * scale
            val maxDy: Float =
                (height - (height / scale)) / 2 * scale
            dx = dx.coerceAtLeast(-maxDx).coerceAtMost(maxDx)
            dy = dy.coerceAtLeast(-maxDy).coerceAtMost(maxDy)
            i(
                TAG,
                "Width: " + width
                    .toString() + ", scale " + scale.toString() + ", dx " + dx
                    .toString() + ", max " + maxDx
            )
        }
    }

    private fun init(context: Context) {
        scaleDetector = ScaleGestureDetector(context, this)
        mGestureDetector = GestureDetector(context, this)
        mGestureDetector?.setOnDoubleTapListener(this)
    }

    // ScaleGestureDetector
    override fun onScaleBegin(scaleDetector: ScaleGestureDetector): Boolean = true

    override fun onScale(scaleDetector: ScaleGestureDetector): Boolean {
        val scaleFactor = scaleDetector.scaleFactor
        i(TAG, "onScale$scaleFactor")
        if (lastScaleFactor == 0f || sign(scaleFactor) == sign(
                lastScaleFactor
            )
        ) {
            scale *= scaleFactor
            scale = max(MIN_ZOOM, min(scale, MAX_ZOOM))
            lastScaleFactor = scaleFactor
        } else {
            lastScaleFactor = 0f
        }
        return true
    }

    override fun onScaleEnd(scaleDetector: ScaleGestureDetector) {}

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        canvas.translate(dx, dy)
        canvas.scale(scale, scale)
        canvas.restore()
    }

    override fun dispatchDraw(canvas: Canvas) {
        canvas.save()
        if (scale == 1.0f) {
            dx = 0.0f
            dy = 0.0f
        }
        canvas.translate(dx, dy)
        canvas.scale(scale, scale, canvas.width /2.0f, canvas.height /2.0f)
        super.dispatchDraw(canvas)
        canvas.restore()
        invalidate()
    }

    override fun onDoubleTap(p0: MotionEvent?): Boolean {
        if (restore) {
            scale = 1.0f
            restore = false
        } else {
            scale *= 2.0f
            restore = true
        }
        mode = Mode.ZOOM
        calculateZoomLevel()
        return true
    }

    override fun onDoubleTapEvent(p0: MotionEvent?): Boolean = false

    override fun onSingleTapConfirmed(p0: MotionEvent?): Boolean = false

    override fun onShowPress(p0: MotionEvent?) {}

    override fun onSingleTapUp(p0: MotionEvent?): Boolean = false

    override fun onDown(p0: MotionEvent?): Boolean = false

    override fun onFling(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean = false

    override fun onScroll(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean = false

    override fun onLongPress(p0: MotionEvent?) {}
}