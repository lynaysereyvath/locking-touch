package com.example.floatinglock.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.FrameLayout
import kotlin.math.hypot
import kotlin.math.roundToInt

@SuppressLint("ViewConstructor")
class DragTouchLayout(
    context: Context,
    private val listener: Listener,
) : FrameLayout(context) {

    interface Listener {
        fun onDown()
        fun onDrag(totalDx: Int, totalDy: Int)
        fun onUp(wasDrag: Boolean)
    }

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private var downRawX = 0f
    private var downRawY = 0f
    private var dragging = false

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean = true

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downRawX = event.rawX
                downRawY = event.rawY
                dragging = false
                listener.onDown()
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.rawX - downRawX
                val dy = event.rawY - downRawY
                if (!dragging && hypot(dx, dy) > touchSlop) dragging = true
                if (dragging) listener.onDrag(dx.roundToInt(), dy.roundToInt())
            }
            MotionEvent.ACTION_UP -> {
                listener.onUp(dragging)
                if (!dragging) performClick()
            }
            MotionEvent.ACTION_CANCEL -> listener.onUp(dragging)
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}
