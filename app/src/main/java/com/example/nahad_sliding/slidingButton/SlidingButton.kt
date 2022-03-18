package com.example.nahad_sliding.slidingButton

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.*
import android.view.animation.*
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.*
import androidx.core.content.ContextCompat
import com.example.nahad_sliding.R
import com.example.nahad_sliding.databinding.LayoutButtonBinding
import kotlin.math.max

class SlidingButton : FrameLayout {

    private var binding: LayoutButtonBinding

    init {
        val inflater = LayoutInflater.from(context)
        binding = LayoutButtonBinding.inflate(inflater, this, true)
    }

    var isRtl: Boolean = false
    private var mStateListener: OnStateChangeListener? = null
    private var slidingListener: OnSlidingListener? = null

    private var statusActive = false
        set(value) {
            field = false
//            if (value && showIndicator) animatedIndicator()
            mStateListener?.onChange(value)
        }

    private var startOfButton = 0F
    private var endOfButton = 0F

    private var showIndicator = true
    private var enableTextAlpha = true

    private val stateListIconTint: ColorStateList

    private var buttonBackground: Drawable? = null
        set(value) {
            field = value
            binding.apply {
                rlSliding.background = value
            }
        }

    private var buttonIcon: Drawable? = null

    private var iconScaleType: ImageView.ScaleType = ImageView.ScaleType.CENTER_INSIDE
        set(value) {
            when (value) {
                ImageView.ScaleType.CENTER_INSIDE,
                ImageView.ScaleType.CENTER_CROP,
                ImageView.ScaleType.CENTER,
                ImageView.ScaleType.FIT_CENTER,
                ImageView.ScaleType.FIT_XY -> {
                    field = value
                }
                else -> throw IllegalArgumentException("ScaleType $value aren't allowed, please use CENTER, CENTER_CROP, CENTER_INSIDE,FIT_CENTER, or FIT_XY")
            }
        }

    private var textBackgroundLogin: Drawable? = null
        set(value) {
            field = value
            binding.tvSlidingLogin.background = value
        }

    private var textBackgroundSwipe: Drawable? = null
        set(value) {
            field = value
            binding.tvSlidingSwipe.background = value
        }

    private var mTextSizeLogin: Float = 0F
        private set(value) {
            field = value
            binding.tvSlidingLogin.setTextSize(TypedValue.COMPLEX_UNIT_PX, value)
        }

    private var mTextSizeSwipe: Float = 0F
        private set(value) {
            field = value
            binding.tvSlidingSwipe.setTextSize(TypedValue.COMPLEX_UNIT_PX, value)
        }

    /**
     * [textPaddings]
     * index of the array mean [0] start,[1] top,[2] end,[3] bottom
     */
    private val textPaddings = intArrayOf(0, 0, 0, 0)

    private var textColorsLogin: ColorStateList? = null
        set(value) {
            field = value
            binding.tvSlidingLogin.setTextColor(value)
        }

    private var textColorsSwipe: ColorStateList? = null
        set(value) {
            field = value
            binding.tvSlidingSwipe.setTextColor(value)
        }

    private var mTextLogin: String? = null
        set(value) {
            field = value
            binding.tvSlidingLogin.text = value
        }

    private var mTextSwipe: String? = null
        set(value) {
            field = value
            binding.tvSlidingSwipe.text = value
        }

    private var buttonWidth: Int = 0
        set(value) {
            field = value
            binding.rlSliding.layoutParams.width = value
        }

    private var buttonHeight: Int = 0
        set(value) {
            field = value
            binding.rlSliding.layoutParams.height = value
        }

    /**
     * [buttonMargins],[buttonPaddings]
     * index of the array mean [0] start,[1] top,[2] end,[3] bottom
     */
    private val buttonMargins = intArrayOf(0, 0, 0, 0)
    private val buttonPaddings = intArrayOf(0, 0, 0, 0)

    private var trackExtendedTo = TrackExtended.BUTTON

    private var cornerRadius: Float = 0F
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        set(value) {
            field = value
            RoundedOutlineProvider(value).also {
                this.outlineProvider = it
                binding.apply {
                    rlSliding.outlineProvider = it
                    tvSlidingLogin.outlineProvider = it
                    tvSlidingSwipe.outlineProvider = it
                }
            }
        }

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(
        context,
        attrs,
        R.attr.slidingButtonStyle
    )

    constructor(
        _context: Context,
        attrs: AttributeSet?,
        defStyleInt: Int = R.attr.slidingButtonStyle
    ) : super(
        _context,
        attrs,
        defStyleInt
    ) {

        val colorPrimary = TypedValue()
        context.theme.resolveAttribute(R.attr.colorPrimary, colorPrimary, true)

        val ex by lazy {
            TypedValue().apply {
                context.theme.resolveAttribute(
                    R.attr.colorAccent,
                    this,
                    true
                )
            }.data
        }
        val colorAccent = TypedValue()
        context.theme.resolveAttribute(R.attr.colorAccent, colorAccent, true)
        val arr = context.obtainStyledAttributes(
            attrs,
            R.styleable.SlidingButton,
            defStyleInt,
            R.style.SlidingButton
        )

        /**
         * TextView attrs configuration
         */
        mTextSizeLogin =
            arr.getDimension(R.styleable.SlidingButton_sliding_text_size_login, 18f)
        mTextSizeSwipe =
            arr.getDimension(R.styleable.SlidingButton_sliding_text_size_swipe, 18f)

        textColorsLogin = arr.getColorStateList(R.styleable.SlidingButton_sliding_text_color_login)
            ?: ColorStateList.valueOf(ex)

        textColorsSwipe = arr.getColorStateList(R.styleable.SlidingButton_sliding_text_color_swipe)
            ?: ColorStateList.valueOf(ex)

        mTextLogin = arr.getString(R.styleable.SlidingButton_sliding_text_login)
        mTextSwipe = arr.getString(R.styleable.SlidingButton_sliding_text_swipe)
        textBackgroundLogin = arr.getDrawable(R.styleable.SlidingButton_sliding_text_background)

        textPaddings[0] = arr.getDimensionPixelSize(
            R.styleable.SlidingButton_sliding_text_paddingStart,
            0
        )
        textPaddings[1] = arr.getDimensionPixelSize(
            R.styleable.SlidingButton_sliding_text_paddingTop,
            0
        )
        textPaddings[2] = arr.getDimensionPixelSize(
            R.styleable.SlidingButton_sliding_text_paddingEnd,
            0
        )
        textPaddings[3] = arr.getDimensionPixelSize(
            R.styleable.SlidingButton_sliding_text_paddingBottom,
            0
        )

        /**
         * ImageView attrs configuration
         */
        val defaultButtonDrawable =
            ContextCompat.getDrawable(context, R.drawable.ic_close)
        buttonIcon = arr.getDrawable(R.styleable.SlidingButton_sliding_button_icon)
            ?: defaultButtonDrawable

        stateListIconTint = arr.getColorStateList(
            R.styleable.SlidingButton_sliding_button_icon_tint
        ) ?: ColorStateList.valueOf(colorAccent.data)

        buttonBackground = arr.getDrawable(R.styleable.SlidingButton_sliding_button_background)

        buttonWidth = arr.getDimensionPixelSize(
            R.styleable.SlidingButton_sliding_button_width,
            46
        )
        buttonHeight = arr.getDimensionPixelSize(
            R.styleable.SlidingButton_sliding_button_height,
            46
        )

        val scaleName = arr.getInteger(R.styleable.SlidingButton_sliding_icon_scaleType, 7).let {
            when (it) {
                1 -> "FIT_XY"
                3 -> "FIT_CENTER"
                5 -> "CENTER"
                6 -> "CENTER_CROP"
                else -> "CENTER_INSIDE"
            }
        }
        iconScaleType = ImageView.ScaleType.valueOf(scaleName)

        buttonMargins[0] = arr.getDimensionPixelSize(
            R.styleable.SlidingButton_sliding_button_marginStart,
            0
        )
        buttonMargins[1] = arr.getDimensionPixelSize(
            R.styleable.SlidingButton_sliding_button_marginTop,
            0
        )
        buttonMargins[2] = arr.getDimensionPixelSize(
            R.styleable.SlidingButton_sliding_button_marginEnd,
            0
        )
        buttonMargins[3] = arr.getDimensionPixelSize(
            R.styleable.SlidingButton_sliding_button_marginBottom,
            0
        )

        buttonPaddings[0] = arr.getDimensionPixelSize(
            R.styleable.SlidingButton_sliding_button_paddingStart,
            0
        )
        buttonPaddings[1] = arr.getDimensionPixelSize(
            R.styleable.SlidingButton_sliding_button_paddingTop,
            0
        )
        buttonPaddings[2] = arr.getDimensionPixelSize(
            R.styleable.SlidingButton_sliding_button_paddingEnd,
            0
        )
        buttonPaddings[3] = arr.getDimensionPixelSize(
            R.styleable.SlidingButton_sliding_button_paddingBottom,
            0
        )

        enableTextAlpha = arr.getBoolean(R.styleable.SlidingButton_sliding_enabledTextAlpha, true)
        showIndicator = arr.getBoolean(R.styleable.SlidingButton_sliding_showTrack, false)

        val index = arr.getInteger(R.styleable.SlidingButton_sliding_trackExtendTo, 1)
        trackExtendedTo = arrayOf(TrackExtended.CONTAINER, TrackExtended.BUTTON)[index]

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cornerRadius = arr.getDimension(R.styleable.SlidingButton_sliding_corner_radius, 0F)
            buttonIcon?.setTintList(stateListIconTint)
//            trackBackground?.setTintList(trackBackgroundTint)
        } else {
            buttonIcon?.colorFilter = PorterDuffColorFilter(
                stateListIconTint.defaultColor,
                PorterDuff.Mode.SRC_IN
            )
        }

        arr.recycle()

    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        configureTextView()
        configureImageView()

        //Apply Rounded corner to views
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            RoundedOutlineProvider(cornerRadius).also {
                this.outlineProvider = it
                binding.apply {
                    tvSlidingLogin.outlineProvider = it
                    tvSlidingSwipe.outlineProvider = it
                    rlSliding.outlineProvider = it
                }
            }
        }

        configureTouch(true)
    }

    private fun configureImageView() {
        binding.apply {
            rlSliding.background = buttonBackground
            rlSliding.layoutParams.let { it as LayoutParams }.also {
                it.width = buttonWidth
                it.height = buttonHeight
                it.marginStart = buttonMargins[0]
                it.topMargin = buttonMargins[1]
                it.marginEnd = buttonMargins[2]
                it.bottomMargin = buttonMargins[3]
                rlSliding.layoutParams = it
            }
            rlSliding.setPaddingRelative(
                buttonPaddings[0],
                buttonPaddings[1],
                buttonPaddings[2],
                buttonPaddings[3]
            )
        }
    }

    private fun configureTextView() {
        binding.apply {
            tvSlidingLogin.background = textBackgroundLogin
            tvSlidingLogin.setPaddingRelative(
                textPaddings[0],
                textPaddings[1],
                textPaddings[2],
                textPaddings[3]
            )
            tvSlidingLogin.text = mTextLogin
            tvSlidingLogin.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSizeLogin)
            tvSlidingLogin.setTextColor(textColorsLogin)

            tvSlidingSwipe.background = textBackgroundSwipe
            tvSlidingSwipe.setPaddingRelative(
                textPaddings[0],
                textPaddings[1],
                textPaddings[2],
                textPaddings[3]
            )
            tvSlidingSwipe.text = mTextSwipe
            tvSlidingSwipe.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSizeSwipe)
            tvSlidingSwipe.setTextColor(textColorsSwipe)
            tvSlidingSwipe.gravity = Gravity.CENTER
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        startOfButton = buttonMargins[0].toFloat()
        endOfButton =
            w.toFloat() - (buttonWidth.toFloat() + buttonMargins[2].toFloat() + paddingEnd.toFloat() + paddingStart.toFloat())
    }

    override fun removeAllViews() = throw IllegalStateException("This method isn't allowed ")

    override fun removeView(view: View?) = throw IllegalStateException("This method isn't allowed ")

    override fun removeViewAt(index: Int) =
        throw IllegalStateException("This method isn't allowed ")

    @SuppressLint("ClickableViewAccessibility") //todo added
    fun configureTouch(isRtl: Boolean) {
        this.setOnTouchListener { _, event ->

            val startTouch = (paddingStart + buttonMargins[0]).toFloat()
            val maxStartTouch = startTouch + buttonWidth
            val isStartTouch = event.y in startOfButton..maxStartTouch

            val endTouch = (paddingEnd + buttonMargins[2]).toFloat()
            val maxEndTouch = (this.width - paddingEnd - buttonMargins[2]).toFloat()
            val isEndTouch = event.y in endTouch..maxEndTouch

            when (event.action) {
                MotionEvent.ACTION_DOWN -> (isStartTouch && !statusActive) || (isEndTouch && statusActive)
                MotionEvent.ACTION_MOVE -> onMove(event, startTouch, endOfButton + buttonWidth)
                MotionEvent.ACTION_UP -> onUp(isRtl)
                else -> true
            }
        }
    }

    private fun onUp(isRtl: Boolean): Boolean {
        if (isRtl) {
            return when {
                binding.rlSliding.x > this.width * 0.45F -> {
                    animatedToStart()
                    true
                }
                binding.rlSliding.x <= this.width * 0.45F -> {
                    animatedToEnd()
                    true
                }
                else -> false
            }
        } else {
            return when {
                binding.rlSliding.x + buttonWidth < this.width * 0.55F && binding.rlSliding.x > startOfButton -> {
                    animatedToStart()
                    true
                }
                binding.rlSliding.x + buttonWidth >= this.width * 0.55F -> {
                    animatedToEnd()
                    true
                }
                else -> false
            }
        }
    }

    fun setGravity(isRtl: Boolean) {
        binding.apply {
            if (isRtl) {
                rlSliding.gravity = Gravity.START or Gravity.CENTER
            } else {
                rlSliding.gravity = Gravity.END or Gravity.CENTER
            }
        }
    }

    fun changeState(active: Boolean, animated: Boolean = false) {
        if (animated && active) {
            statusActive = true
            animatedToEnd()
        } else if (animated && !active) {
            statusActive = false
            animatedToStart()
        } else if (active) {
            binding.rlSliding.x = endOfButton
            statusActive = true
        } else {
            binding.rlSliding.x = startOfButton
            statusActive = false
        }
    }

    fun animatedToStart() {
        if (isActivated) isActivated = false

        val floatAnimator = ValueAnimator.ofFloat(binding.rlSliding.x, if (isRtl) endOfButton else startOfButton)
        floatAnimator.addUpdateListener {
            updateSlidingXPosition(it.animatedValue as Float)
        }
        floatAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}

            override fun onAnimationEnd(animation: Animator?) {
                isActivated = false
                if (statusActive) statusActive = false
            }

            override fun onAnimationCancel(animation: Animator?) {}

            override fun onAnimationStart(animation: Animator?) {
                isActivated = false
                if (statusActive) statusActive = false
            }
        })
        floatAnimator.duration = 115L
        floatAnimator.interpolator = AccelerateDecelerateInterpolator()
        floatAnimator.start()
    }

    fun animatedToEnd() {
        val floatAnimator = ValueAnimator.ofFloat(binding.rlSliding.x,  if (isRtl) startOfButton else endOfButton)
        floatAnimator.addUpdateListener {
            updateSlidingXPosition(it.animatedValue as Float)
        }
        floatAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}

            override fun onAnimationEnd(animation: Animator?) {
                isActivated = true
                if (!statusActive) statusActive = true

            }

            override fun onAnimationCancel(animation: Animator?) {}

            override fun onAnimationStart(animation: Animator?) {

            }
        })
        floatAnimator.duration = 115L
        floatAnimator.interpolator = AccelerateDecelerateInterpolator()
        floatAnimator.start()
    }

    private fun onMove(event: MotionEvent, start: Float, end: Float): Boolean {
        if (isActivated) isActivated = false
        if (isRtl){
            if (event.x > ((startOfButton + buttonWidth) + 100)) {
                updateSlidingXPosition(endOfButton)
                return true
            }
            if (event.x in start..end) {
                updateSlidingXPosition(event.x)
                return true
            }
        }else {
            if (event.x < startOfButton + buttonWidth) {
                updateSlidingXPosition(startOfButton)
                return true
            }
            if (event.x in start..end) {
                updateSlidingXPosition(event.x - buttonWidth)
                return true
            }
        }
        return false
    }

    private fun updateSlidingXPosition(x: Float) {
        binding.rlSliding.x = x
        val realX = x - startOfButton
        val percent = realX / (endOfButton - startOfButton)
        if (enableTextAlpha) {
            binding.apply {
                if (!isRtl) {
                    tvSlidingLogin.alpha =
                        if (percent < 0.2F) 1F - percent else max(0F, 1F - (percent + 0.3F))
                    tvSlidingSwipe.alpha =
                        if (percent < 0.2F) 1F - percent else max(0F, 1F - (percent + 0.3F))
                }
                else {
                    tvSlidingLogin.alpha =
                        if (percent < 0.2F) 0F - percent else max(1F, 0F - (percent + 0.3F))
                    tvSlidingSwipe.alpha =
                        if (percent < 0.2F) 0F - percent else max(1F, 0F - (percent + 0.3F))
                }
            }
        }

        slidingListener?.onSliding(percent)
    }

    private fun translateAnimation() {
        binding.rlSliding.clearAnimation()
        val animation = TranslateAnimation(0F, endOfButton, 0F, 0F)
        animation.interpolator = AccelerateDecelerateInterpolator()
        animation.duration = 350L
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                binding.rlSliding.clearAnimation()
                val anim = ScaleAnimation(
                    0.35F,
                    1F,
                    0.35F,
                    1F,
                    binding.rlSliding.width.toFloat() / 2F,
                    binding.rlSliding.height.toFloat() / 2F
                )
                anim.interpolator = DecelerateInterpolator()
                anim.duration = 225L
                anim.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationRepeat(animation: Animation?) {}

                    override fun onAnimationEnd(animation: Animation?) {
                        binding.apply {
                            rlSliding.clearAnimation()
                            rlSliding.scaleX = 1F
                            rlSliding.scaleY = 1F
                        }
                    }

                    override fun onAnimationStart(animation: Animation?) {}
                })
                binding.rlSliding.startAnimation(anim)
            }

            override fun onAnimationStart(animation: Animation?) {

            }
        })
        binding.rlSliding.startAnimation(animation)
    }

    private fun translateAnimationRtl() {
        binding.rlSliding.clearAnimation()
        val animation = TranslateAnimation(endOfButton, startOfButton, 0F, 0F)
        animation.interpolator = AccelerateDecelerateInterpolator()
        animation.duration = 350L
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                binding.rlSliding.clearAnimation()
                val anim = ScaleAnimation(
                    0.35F,
                    1F,
                    0.35F,
                    1F,
                    binding.rlSliding.width.toFloat() / 2F,
                    binding.rlSliding.height.toFloat() / 2F
                )
                anim.interpolator = DecelerateInterpolator()
                anim.duration = 225L
                anim.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationRepeat(animation: Animation?) {}

                    override fun onAnimationEnd(animation: Animation?) {
                        binding.apply {
                            rlSliding.clearAnimation()
                            rlSliding.scaleX = 1F
                            rlSliding.scaleY = 1F
                        }
                    }

                    override fun onAnimationStart(animation: Animation?) {}
                })
                binding.rlSliding.startAnimation(anim)
            }

            override fun onAnimationStart(animation: Animation?) {

            }
        })
        binding.rlSliding.startAnimation(animation)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        binding.apply {
            tvSlidingLogin.isEnabled = enabled
            tvSlidingSwipe.isEnabled = enabled
            rlSliding.isEnabled = enabled
        }
    }

    fun setTextColorLogin(@ColorInt color: Int) {
        textColorsLogin = ColorStateList.valueOf(color)
    }

    fun setButtonIcon(@DrawableRes resId: Int) {
        buttonIcon = ContextCompat.getDrawable(context, resId)
    }

    fun setButtonBackground(@DrawableRes resId: Int) {
        buttonBackground = ContextCompat.getDrawable(context, resId)
    }

    fun setButtonBackgroundColor(@ColorInt color: Int) {
        when (buttonBackground) {
            is ColorDrawable -> (binding.rlSliding.background as ColorDrawable).color = color
            else -> buttonBackground = ColorDrawable(color)
        }
    }

    fun setButtonPadding(start: Int, top: Int, end: Int, bottom: Int) {
        buttonPaddings[0] = start
        buttonPaddings[1] = top
        buttonPaddings[2] = end
        buttonPaddings[3] = bottom
        binding.rlSliding.setPadding(
            buttonPaddings[0],
            buttonPaddings[1],
            buttonPaddings[2],
            buttonPaddings[3]
        )
    }

    fun setButtonMargin(start: Int, top: Int, end: Int, bottom: Int) {
        buttonMargins[0] = start
        buttonMargins[1] = top
        buttonMargins[2] = end
        buttonMargins[3] = bottom
        binding.rlSliding.layoutParams.let { it as LayoutParams }.setMargins(
            buttonMargins[0],
            buttonMargins[1],
            buttonMargins[2],
            buttonMargins[3]
        )
    }

    fun setOnStateChangeListener(listener: OnStateChangeListener?) {
        mStateListener = listener
    }

    fun setOnStateChangeListener(l: (active: Boolean) -> Unit) {
        this.setOnStateChangeListener(object : OnStateChangeListener {
            override fun onChange(active: Boolean) {
                l.invoke(active)
            }
        })
    }

    fun setOnSlidingListener(listener: OnSlidingListener?) {
        slidingListener = listener
    }

    fun setOnSlidingListener(l: (progress: Float) -> Unit) {
        this.setOnSlidingListener(object : OnSlidingListener {
            override fun onSliding(progress: Float) {
                l.invoke(progress)
            }
        })
    }

    interface OnStateChangeListener {
        fun onChange(active: Boolean)
    }

    interface OnSlidingListener {
        fun onSliding(progress: Float)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private class RoundedOutlineProvider(val radius: Float) : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            val rect = Rect(0, 0, view.width, view.height)
            outline.setRoundRect(rect, radius)
            view.clipToOutline = true
        }
    }

    @Keep
    enum class TrackExtended constructor(val value: Int) {
        CONTAINER(0),
        BUTTON(1)
    }

}