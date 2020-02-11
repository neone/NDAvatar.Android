


/*
 * Copyright 2014 - 2020 Henning Dodenhof
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.neone.android.ndavatar

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.graphics.drawable.toBitmap
import kotlin.math.min
import kotlin.math.pow


open class CircleImageView:ImageView {
    private val paintbrush = Paint()
    // Constructors for ImageView
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.CircleImageView, defStyle, 0)

        avatarBorderStrokeWidth = a.getDimensionPixelSize(R.styleable.CircleImageView_civ_border_width, DEFAULT_BORDER_WIDTH)
        avatarBorderColor = a.getColor(R.styleable.CircleImageView_civ_border_color, DEFAULT_BORDER_COLOR)
        mBorderOverlay = a.getBoolean(R.styleable.CircleImageView_civ_border_overlay, DEFAULT_BORDER_OVERLAY)
        avatarBackgroundColor = a.getColor(R.styleable.CircleImageView_civ_circle_background_color, DEFAULT_AVATAR_BACKGROUND_COLOR)
        initializeBitmap()


        a.recycle()
    }

    // Equivalence of 'private static final' in java
    companion object {
        val SCALE_TYPE = ScaleType.CENTER_CROP
        val BITMAP_CONFIG = Bitmap.Config.ARGB_8888
        val COLORDRAWABLE_DIMENSION = 2
        val DEFAULT_BORDER_WIDTH = 5
        val DEFAULT_BORDER_COLOR = Color.BLUE
        val DEFAULT_AVATAR_BACKGROUND_COLOR = Color.GREEN
        val DEFAULT_BORDER_OVERLAY = false
        /**
         * Defaults for initials-based avatar
         */
        val DEFAULT_INITIALS = "MA"
        val DEFAULT_TEXTCOLOR = Color.WHITE
        val DEFAULT_FONT_INITIALSAVATAR = Typeface.DEFAULT_BOLD
        val DEFAULT_FONT_SIZE = 200f
        val DEFAULT_LETTER_SCALE_FACTOR = 1f
    }


    /**
     * Variables specific to generation of initials-based avatar
     */
    /**
     * textColor is an Int (@ColorInt) that defines the color of the text to be drawn
     */
    var textColor = DEFAULT_TEXTCOLOR
        set(newColor) {
            if (newColor == field) return
            field = newColor
            generateInitialsTextDraw()
        }
    /**
     * stringToRender is a string that defines what text will be rendered into the avatar
     */
    var stringToRender = DEFAULT_INITIALS
        set(newString) {
            if (newString == field) return
            field = newString
            avatarUsingInitialsBuilder = generateInitialsTextDraw()!!
            initializeBitmap()
        }
    /**
     * fontForInitials is a Typeface(Font) that will be used to style the string to be rendered
     */
    var fontForInitials = DEFAULT_FONT_INITIALSAVATAR
        set(newTypeface) {
            if (newTypeface == field) return
            field = newTypeface
            generateInitialsTextDraw()
        }
    /**
     * fontSize is a Int that defines the size in pixels of the letters to be drawn in the avatar
     */
    var fontSize = DEFAULT_FONT_SIZE
        set(newFontSize) {
            if (newFontSize == field) return
            field = newFontSize
            generateInitialsTextDraw()
        }
    /**
     * fontScaleFactor is percentage <Float> that defines how much the text in the avatar will be reduced
     * (0,1]
     */
    var fontScaleFactor = DEFAULT_LETTER_SCALE_FACTOR
        set(newFontScaleFactor) {
            if (newFontScaleFactor == field) return
            field = newFontScaleFactor
            generateInitialsTextDraw()
        }
    /**
     * useInitialsForAvatar is a flag used to programmatically switch between
     */
    var useInitialsForAvatar = false
        set(value) {
            if (field == value) return
            field = value
            initializeBitmap()
        }

    private var avatarUsingInitialsBuilder: NDTextDraw? = generateInitialsTextDraw()



    /**
     * END INITIALS-BASED AVATAR GENERATION SPECIFIC VARS
     */

    private val mDrawableRect = RectF()
    private val mRectBackground = RectF()
    private val mBorderRect = RectF()

    private val mShaderMatrix = Matrix()
    private val mBitmapPaint = Paint()
    private val mBorderPaint = Paint()
    private val mCircleBackgroundPaint = Paint()

    var avatarBorderColor = DEFAULT_BORDER_COLOR
    set(value) {
        if (value == field) {
            return
        }

        field = value
        mBorderPaint.color = avatarBorderColor
        invalidate()
    }
    var avatarBorderStrokeWidth = DEFAULT_BORDER_WIDTH
    set(value) {
        if (value == field) {
            return
        }

        field = value
        calcAvatarBoundsAndSetPaintbrushes()
    }
    var avatarBackgroundColor = DEFAULT_AVATAR_BACKGROUND_COLOR
    set(value) {
        if (value == field) {
            return
        }

        field = value
        calcAvatarBoundsAndSetPaintbrushes()
    }

    private lateinit var mBitmap: Bitmap
    private lateinit var mBitmapShader: BitmapShader
    private var mBitmapWidth: Int = 0
    private var mBitmapHeight: Int = 0

    private var mDrawableRadius = 0f
    private var mBorderRadius = 0f

    private var mColorFilter: ColorFilter? = null
    set(value) {
        if (value == field) {
            return
        }

        field = value
        applyColorFilter()
        invalidate()
    }

    private var mReady = false
    private var mSetupPending = false
    private var mBorderOverlay = false
        set(value) {
            if (value == field) {
                return
            }

            field = value
            calcAvatarBoundsAndSetPaintbrushes()
        }
    var applyCircularMask = false
    set(value) {
        if (field == value) {
        return
        }

        field = value
        initializeBitmap()
    }

    init {
        super.setScaleType(SCALE_TYPE)
        mReady = true

        if (mSetupPending) {
            calcAvatarBoundsAndSetPaintbrushes()
            mSetupPending = false
        }
    }

    override fun getScaleType(): ScaleType {
        return SCALE_TYPE
    }


    override fun setScaleType(scaleType: ScaleType?) {
        if (scaleType != SCALE_TYPE) {
            throw IllegalArgumentException("ScaleType $scaleType not supported.")
        }
    }


    override fun setAdjustViewBounds(adjustViewBounds: Boolean) {
        if (adjustViewBounds) {
            throw IllegalArgumentException("adjustViewBounds not supported.")
        }
    }

    override fun onDraw(canvas: Canvas) {
        //Circular mask?
        when (applyCircularMask) {
            true -> {
                // Paint Border
                if (mDrawableRect.width() > 0 && mDrawableRect.height() > 0) {
                    canvas.drawCircle(mBorderRect.centerX(), mBorderRect.centerY(), mBorderRadius, mBorderPaint)
                }

                //Paint background circle
                canvas.drawCircle(mDrawableRect.centerX(), mDrawableRect.centerY(), mDrawableRadius, mCircleBackgroundPaint)

                // Paint the avatar bitmap
                canvas.drawCircle(mDrawableRect.centerX(), mDrawableRect.centerY(), mDrawableRadius, mBitmapPaint)
            }
            false -> {
                // Paint the border
                canvas.drawRect(mBorderRect, mBorderPaint)

                // Paint the background
                canvas.drawRect(mRectBackground, mCircleBackgroundPaint)

                // Paint the avatar bitmap (image or initials)
                canvas.drawRect(mDrawableRect, mBitmapPaint)
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        calcAvatarBoundsAndSetPaintbrushes()
    }


    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        super.setPadding(left, top, right, bottom)
        calcAvatarBoundsAndSetPaintbrushes()
    }


    override fun setPaddingRelative(start: Int, top: Int, end: Int, bottom: Int) {
        super.setPaddingRelative(start, top, end, bottom)
        calcAvatarBoundsAndSetPaintbrushes()
    }

    override fun setImageBitmap(bm: Bitmap) {
        super.setImageBitmap(bm)
        initializeBitmap()
    }


    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        initializeBitmap()
    }


    override fun setImageResource(@DrawableRes resId: Int) {
        super.setImageResource(resId)
        initializeBitmap()
    }

    override fun setImageURI(uri: Uri?) {
        super.setImageURI(uri)
        initializeBitmap()
    }


    override fun setColorFilter(cf: ColorFilter) {
        if (cf == mColorFilter) {
            return
        }

        mColorFilter = cf
        applyColorFilter()
        invalidate()
    }

    private fun applyColorFilter() {
        // This might be called from setColorFilter during ImageView construction
        // before member initialization has finished on API level <= 19.
        mBitmapPaint.colorFilter = mColorFilter
    }

    private fun getBitmapFromDrawable(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }

        try {
            val bitmap = if (drawable is ColorDrawable) {
                Bitmap.createBitmap(COLORDRAWABLE_DIMENSION, COLORDRAWABLE_DIMENSION, BITMAP_CONFIG)
            } else {
                Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, BITMAP_CONFIG)
            }

            val canvas = Canvas(bitmap)
            drawable.run {
                setBounds(0, 0, canvas.width, canvas.height)
                draw(canvas)
            }
            return bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            throw Error("Failed to generate bitmap from drawable")
        }
    }

    /**
     * Sets bitmap reference from drawable OR a default drawable to avoid null.
     */
    private fun initializeBitmap() {
        mBitmap = if (useInitialsForAvatar) {
            //Translation for line below: If generateInitialsBitmap returns null, use the default avatar image from library assets
            generateInitialsBitmap() ?: getBitmapFromDrawable( drawable ?: resources.getDrawable(R.drawable.default_avatar, null))
        } else {
            // Translation for line below: if 'drawable' item is null use the default avatar image in the library assets
            getBitmapFromDrawable( drawable ?: resources.getDrawable(R.drawable.default_avatar, null))
        }

        calcAvatarBoundsAndSetPaintbrushes()
    }

    /**
     * After changing a setting such as font color, font, upper/lower-case, etc, rebuild the text draw
     * object
     */
    private fun generateInitialsTextDraw(): NDTextDraw? {
        val newfontSize:Int = if (mDrawableRect != null) {
            mDrawableRect.height().toInt()
        } else {
            (DEFAULT_FONT_SIZE * 1.5f).toInt()
        }
        val configuredBuilder = NDTextDraw.builder().beginConfig()
            ?.textColor(textColor)
            ?.useFont(fontForInitials)
            ?.fontSize(newfontSize)/* size in px */
            ?.bold()
            ?.toUpperCase()
            ?.endConfig()

        if (applyCircularMask) {
            return configuredBuilder?.buildRound(stringToRender, avatarBackgroundColor)
        } else {
            return configuredBuilder?.buildRect(stringToRender, avatarBackgroundColor)
        }
    }

    /**
     * Uses the pre-existing ndTextDraw instance to generate a bitmap of native drawable size.
     */
    private fun generateInitialsBitmap(): Bitmap? {
        var width = if (mDrawableRect.width() <= 0 ) 1 else mDrawableRect.width().toInt()
        var height = if (mDrawableRect.height() <= 0) 1 else mDrawableRect.height().toInt()
        return avatarUsingInitialsBuilder?.toBitmap(width, height)
    }

    private fun calcAvatarBoundsAndSetPaintbrushes() {
        // Wait until avatar data is ready
        if (!mReady) {
            mSetupPending = true
            return
        }

        // Skip if view has no area
        if (width <= 0 && height <= 0) {
            return
        }

        //LAYER: Image as bitmap
        // mBitmap contains the avatar image to be painted, if null, paint initials instead
        mBitmapShader = BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        mBitmapPaint.isAntiAlias = true
        mBitmapPaint.isDither = true
        mBitmapPaint.isFilterBitmap = true
        mBitmapPaint.shader = mBitmapShader

        //LAYER: Border Paint as Stroke
        mBorderPaint.style = Paint.Style.FILL_AND_STROKE
        mBorderPaint.isAntiAlias = true
        mBorderPaint.color = avatarBorderColor
        mBorderPaint.strokeWidth = avatarBorderStrokeWidth.toFloat()

        //LAYER: Background fill
        mCircleBackgroundPaint.style = Paint.Style.FILL
        mCircleBackgroundPaint.isAntiAlias = true
        mCircleBackgroundPaint.color = avatarBackgroundColor

        mBitmapHeight = mBitmap.height
        mBitmapWidth = mBitmap.width


        mBorderRect.set(calcOuterBoundsOfAvatarsDrawableArea())
        mRectBackground.set(mBorderRect)
        mRectBackground.inset(avatarBorderStrokeWidth.toFloat(), avatarBorderStrokeWidth.toFloat())
        mBorderRadius = min(
            (mBorderRect.height() - avatarBorderStrokeWidth) / 2.0f,
            (mBorderRect.width() - avatarBorderStrokeWidth) / 2.0f
        )

        mDrawableRect.set(mBorderRect)
        // Scale the bitmap layer that will be drawn on mDrawableRect rectangle.
        // This scales the image to be inside of the border as the border width increases
        if (!mBorderOverlay && avatarBorderStrokeWidth > 0) {
            mDrawableRect.inset(
                avatarBorderStrokeWidth - 1.0f,
                avatarBorderStrokeWidth - 1.0f
            )
        }
        generateInitialsTextDraw()

        mDrawableRadius = min(
            mDrawableRect.height() / 2.0f,
            mDrawableRect.width() / 2.0f
        )

        applyColorFilter()
        updateShaderMatrix()
        invalidate()
    }

    /**
     * Calculate outer-most pixel dimensions of screen space available for rendering avatar.
     * 'width' and 'height' are raw pixels drawn on target device at runtime.
     * This may be different than XML-specified values based on specific devices screen size.
     */
    private fun calcOuterBoundsOfAvatarsDrawableArea(): RectF {
        val availableWidth  = width - paddingLeft - paddingRight
        val availableHeight = height - paddingTop - paddingBottom

        val sideLength = min(availableWidth, availableHeight)

        val left = paddingLeft + (availableWidth - sideLength) / 2f
        val top = paddingTop + (availableHeight - sideLength) / 2f

        return RectF(left, top, left + sideLength, top + sideLength)
    }

    /**
     * Scale image to fit using largest edge
     */
    private fun updateShaderMatrix() {
        val scale: Float
        var dx = 0f
        var dy = 0f

        mShaderMatrix.set(null)

        if (mBitmapWidth * mDrawableRect.height() > mDrawableRect.width() * mBitmapHeight) {
            // scale is drawable area/height of bitmap
            scale = mDrawableRect.height() / mBitmapHeight
            // width of ((drawable area - width of bitmap) * scale factor from height) * 0.5
            dx = (mDrawableRect.width() - mBitmapWidth * scale) * 0.5f
        } else {
            scale = mDrawableRect.width() / mBitmapWidth
            dy = (mDrawableRect.height() - mBitmapHeight * scale) * 0.5f
        }

        mShaderMatrix.setScale(scale, scale)
        mShaderMatrix.postTranslate((dx + 0.5f).toInt() + mDrawableRect.left, (dy + 0.5f).toInt() + mDrawableRect.top)

        mBitmapShader.setLocalMatrix(mShaderMatrix)
    }

    override fun onTouchEvent(event: MotionEvent):Boolean {
        if (!applyCircularMask) {
            return super.onTouchEvent(event)
        }

        return inTouchableArea(event.x, event.y) && super.onTouchEvent(event)
    }

    private fun inTouchableArea(x: Float, y: Float):Boolean {
        if (mBorderRect.isEmpty) {
            return true
        }

        return  (x - mBorderRect.centerX()).toDouble().pow(2.0) +
                (y - mBorderRect.centerY()).toDouble().pow(2.0) <=
                mBorderRadius.toDouble().pow(2.0)
    }
}
