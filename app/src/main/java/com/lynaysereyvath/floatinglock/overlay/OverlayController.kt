package com.example.floatinglock.overlay

import android.accessibilityservice.AccessibilityService
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import com.example.floatinglock.R
import com.example.floatinglock.data.BubbleSettings
import com.example.floatinglock.ui.theme.AppTheme
import kotlin.math.roundToInt

class OverlayController(
    private val service: AccessibilityService,
    private val onLockRequested: () -> Unit,
    private val onScreenshotRequested: () -> Unit,
) {
    private val windowManager: WindowManager = checkNotNull(service.getSystemService(WindowManager::class.java))
    private val handler = Handler(Looper.getMainLooper())

    private val density = service.resources.displayMetrics.density
    private val bubbleSizePx = (BUBBLE_SIZE_DP * density).roundToInt()
    private val edgeMarginPx = (EDGE_MARGIN_DP * density).roundToInt()

    private var owner: OverlayLifecycleOwner? = null
    private var bubbleRoot: DragTouchLayout? = null
    private var menuRoot: ComposeView? = null
    private var snapAnimator: ValueAnimator? = null
    private val removeMenuRunnable = Runnable { removeMenu() }

    private var pressed by mutableStateOf(false)
    private var interactionTick by mutableIntStateOf(0)
    private var menuVisible by mutableStateOf(false)

    private val bubbleParams = WindowManager.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        PixelFormat.TRANSLUCENT,
    ).apply { gravity = Gravity.TOP or Gravity.START }

    fun show() {
        if (bubbleRoot != null) return

        val lifecycleOwner = OverlayLifecycleOwner().also { it.start() }
        owner = lifecycleOwner

        val compose = ComposeView(service).apply {
            setContent {
                AppTheme(darkTheme = true) {
                    FloatingBubble(
                        size = BUBBLE_SIZE_DP.dp,
                        pressed = pressed,
                        interactionTick = interactionTick,
                    )
                }
            }
        }

        val root = DragTouchLayout(service, touchListener).apply {
            contentDescription = service.getString(R.string.bubble_content_description)
            addView(compose, FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            lifecycleOwner.attachTo(this)
            setOnClickListener { showMenu() }
        }

        applySavedAnchor()
        windowManager.addView(root, bubbleParams)
        bubbleRoot = root
    }

    fun hide() {
        handler.removeCallbacksAndMessages(null)
        snapAnimator?.cancel()
        snapAnimator = null

        menuRoot?.let { runCatching { windowManager.removeView(it) } }
        menuRoot = null
        bubbleRoot?.let { runCatching { windowManager.removeView(it) } }
        bubbleRoot = null

        owner?.destroy()
        owner = null

        menuVisible = false
        pressed = false
    }

    fun onScreenChanged() {
        handler.post {
            if (bubbleRoot == null) return@post
            snapAnimator?.cancel()
            applySavedAnchor()
            bubbleRoot?.let { windowManager.updateViewLayout(it, bubbleParams) }
        }
    }

    // ── Touch handling ───────────────────────────────────────────────────────

    private val touchListener = object : DragTouchLayout.Listener {
        private var startX = 0
        private var startY = 0

        override fun onDown() {
            snapAnimator?.cancel()
            startX = bubbleParams.x
            startY = bubbleParams.y
            pressed = true
            interactionTick++
        }

        override fun onDrag(totalDx: Int, totalDy: Int) {
            moveBubbleTo(startX + totalDx, startY + totalDy)
        }

        override fun onUp(wasDrag: Boolean) {
            pressed = false
            interactionTick++
            if (wasDrag) snapToEdge()
        }
    }

    private fun screenWidth() = service.resources.displayMetrics.widthPixels
    private fun screenHeight() = service.resources.displayMetrics.heightPixels
    private fun maxX() = (screenWidth() - bubbleSizePx).coerceAtLeast(0)
    private fun maxY() = (screenHeight() - bubbleSizePx).coerceAtLeast(0)

    private fun moveBubbleTo(x: Int, y: Int) {
        val root = bubbleRoot ?: return
        bubbleParams.x = x.coerceIn(0, maxX())
        bubbleParams.y = y.coerceIn(0, maxY())
        windowManager.updateViewLayout(root, bubbleParams)
    }

    private fun applySavedAnchor() {
        val anchor = BubbleSettings.anchor(service)
        bubbleParams.x = if (anchor.onRight) maxX() - edgeMarginPx else edgeMarginPx
        bubbleParams.y = (anchor.yFraction * screenHeight()).roundToInt().coerceIn(0, maxY())
    }

    private fun snapToEdge() {
        val centreX = bubbleParams.x + bubbleSizePx / 2
        val onRight = centreX >= screenWidth() / 2
        val targetX = if (onRight) maxX() - edgeMarginPx else edgeMarginPx

        snapAnimator?.cancel()
        snapAnimator = ValueAnimator.ofInt(bubbleParams.x, targetX).apply {
            duration = 220
            interpolator = DecelerateInterpolator()
            addUpdateListener { moveBubbleTo(it.animatedValue as Int, bubbleParams.y) }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    BubbleSettings.saveAnchor(
                        service,
                        BubbleSettings.Anchor(
                            onRight = onRight,
                            yFraction = bubbleParams.y / screenHeight().toFloat(),
                        ),
                    )
                }
            })
            start()
        }
    }

    // ── Menu window ──────────────────────────────────────────────────────────

    private fun showMenu() {
        if (menuRoot != null) return
        val lifecycleOwner = owner ?: return

        val view = ComposeView(service).apply {
            lifecycleOwner.attachTo(this)
            setContent {
                AppTheme(darkTheme = true) {
                    ActionMenu(
                        visible = menuVisible,
                        onLock = ::lockNow,
                        onScreenshot = ::screenshotNow,
                        onDismiss = ::dismissMenu,
                    )
                }
            }
        }

        val params = WindowManager.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT,
        )

        menuVisible = true
        windowManager.addView(view, params)
        menuRoot = view
    }

    private fun dismissMenu() {
        if (menuRoot == null) return
        menuVisible = false
        handler.postDelayed(removeMenuRunnable, MENU_EXIT_MS)
    }

    private fun lockNow() {
        handler.removeCallbacks(removeMenuRunnable)
        removeMenu()
        onLockRequested()
    }

    private fun screenshotNow() {
        handler.removeCallbacks(removeMenuRunnable)
        removeMenu()
        bubbleRoot?.visibility = View.INVISIBLE

        handler.postDelayed({
            onScreenshotRequested()
            handler.postDelayed(
                { bubbleRoot?.visibility = View.VISIBLE },
                BUBBLE_RESTORE_MS,
            )
        }, SCREENSHOT_DELAY_MS)
    }

    private fun removeMenu() {
        menuRoot?.let { runCatching { windowManager.removeView(it) } }
        menuRoot = null
        menuVisible = false
    }

    private companion object {
        const val BUBBLE_SIZE_DP = 52
        const val EDGE_MARGIN_DP = 4
        const val MENU_EXIT_MS = 160L
        const val SCREENSHOT_DELAY_MS = 300L
        const val BUBBLE_RESTORE_MS = 1_200L
    }
}