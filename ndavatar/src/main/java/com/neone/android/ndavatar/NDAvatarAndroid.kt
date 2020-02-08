


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
import kotlin.math.min
import kotlin.math.pow


open class CircleImageView:ImageView {
    // Constructors for ImageView
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.CircleImageView, defStyle, 0)

        mBorderWidth = a.getDimensionPixelSize(R.styleable.CircleImageView_civ_border_width, DEFAULT_BORDER_WIDTH)
        mBorderColor = a.getColor(R.styleable.CircleImageView_civ_border_color, DEFAULT_BORDER_COLOR)
        mBorderOverlay = a.getBoolean(R.styleable.CircleImageView_civ_border_overlay, DEFAULT_BORDER_OVERLAY)
        mCircleBackgroundColor = a.getColor(R.styleable.CircleImageView_civ_circle_background_color, DEFAULT_CIRCLE_BACKGROUND_COLOR)

        a.recycle()
    }

    // Equivalence of 'private static final' in java
    companion object {
        val SCALE_TYPE = ScaleType.CENTER_CROP
        val BITMAP_CONFIG = Bitmap.Config.ARGB_8888
        val COLORDRAWABLE_DIMENSION = 2
        val DEFAULT_BORDER_WIDTH = 5
        val DEFAULT_BORDER_COLOR = Color.BLUE
        val DEFAULT_CIRCLE_BACKGROUND_COLOR = Color.GREEN
        val DEFAULT_BORDER_OVERLAY = false
    }

    private val mDrawableRect = RectF()
    private val mBorderRect = RectF()

    private val mShaderMatrix = Matrix()
    private val mBitmapPaint = Paint()
    private val mBorderPaint = Paint()
    private val mCircleBackgroundPaint = Paint()

    private var mBorderColor = DEFAULT_BORDER_COLOR
    set(value) {
        if (value == field) {
            return
        }

        field = value
        mBorderPaint.color = mBorderColor
        invalidate()
    }
    private var mBorderWidth = DEFAULT_BORDER_WIDTH
    set(value) {
        if (value == field) {
            return
        }

        field = value
        setup()
    }
    private var mCircleBackgroundColor = DEFAULT_CIRCLE_BACKGROUND_COLOR
    set(value) {
        if (value == field) {
            return
        }

        field = value
        mCircleBackgroundPaint.color = value
        invalidate()
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
            setup()
        }
    private var mDisableCircularTransformation = false
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
            setup()
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
        if (mDisableCircularTransformation) {
            super.onDraw(canvas)
            return
        }

        if (mCircleBackgroundColor != Color.TRANSPARENT) {
            canvas.drawCircle(mDrawableRect.centerX(), mDrawableRect.centerY(), mDrawableRadius, mCircleBackgroundPaint)
        }
        canvas.drawCircle(mDrawableRect.centerX(), mDrawableRect.centerY(), mDrawableRadius, mBitmapPaint)
        if (mBorderWidth > 0) {
            canvas.drawCircle(mBorderRect.centerX(), mBorderRect.centerY(), mBorderRadius, mBorderPaint)
        }
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        setup()
    }


    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        super.setPadding(left, top, right, bottom)
        setup()
    }


    override fun setPaddingRelative(start: Int, top: Int, end: Int, bottom: Int) {
        super.setPaddingRelative(start, top, end, bottom)
        setup()
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
        mBitmap = if (!mDisableCircularTransformation) {
            getBitmapFromDrawable(drawable)
        } else {
            getBitmapFromDrawable(resources.getDrawable(R.drawable.default_avatar, null))
        }
        setup()
    }

    private fun setup() {
        if (!mReady) {
            mSetupPending = true
            return
        }

        if (width == 0 && height == 0) {
            return
        }

        mBitmapShader = BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)

        mBitmapPaint.isAntiAlias = true
        mBitmapPaint.isDither = true
        mBitmapPaint.isFilterBitmap = true
        mBitmapPaint.shader = mBitmapShader

        mBorderPaint.style = Paint.Style.STROKE
        mBorderPaint.isAntiAlias = true
        mBorderPaint.color = mBorderColor
        mBorderPaint.strokeWidth = mBorderWidth.toFloat()

        mCircleBackgroundPaint.style = Paint.Style.FILL
        mCircleBackgroundPaint.isAntiAlias = true
        mCircleBackgroundPaint.color = mCircleBackgroundColor

        mBitmapHeight = mBitmap.height
        mBitmapWidth = mBitmap.width

        mBorderRect.set(calculateBounds())
        mBorderRadius = min(
            (mBorderRect.height() - mBorderWidth) / 2.0f,
            (mBorderRect.width() - mBorderWidth) / 2.0f
        )

        mDrawableRect.set(mBorderRect)
        if (!mBorderOverlay && mBorderWidth > 0) {
            mDrawableRect.inset(
                mBorderWidth - 1.0f,
                mBorderWidth - 1.0f
            )
        }
        mDrawableRadius = min(
            mDrawableRect.height() / 2.0f,
            mDrawableRect.width() / 2.0f
        )

        applyColorFilter()
        updateShaderMatrix()
        invalidate()
    }

    private fun calculateBounds(): RectF {
        val availableWidth  = width - paddingLeft - paddingRight
        val availableHeight = height - paddingTop - paddingBottom

        val sideLength = min(availableWidth, availableHeight)

        val left = paddingLeft + (availableWidth - sideLength) / 2f
        val top = paddingTop + (availableHeight - sideLength) / 2f

        return RectF(left, top, left + sideLength, top + sideLength)
    }

    private fun updateShaderMatrix() {
        val scale: Float
        var dx = 0f
        var dy = 0f

        mShaderMatrix.set(null)

        if (mBitmapWidth * mDrawableRect.height() > mDrawableRect.width() * mBitmapHeight) {
            scale = mDrawableRect.height() / mBitmapHeight
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
        if (mDisableCircularTransformation) {
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

    /**
     * Accessor to set border width. Will cause a
     * redraw of the circleview with the new border width.
     * @param newSize Int
     */
    fun setBorderWidth(newSize: Int) {
        mBorderWidth = newSize
    }

    /**
     * Getter to get current border width.
     * @return The width setting of the border as an Int.B
     */
    fun getBorderWidth(): Int {
        return mBorderWidth
    }
}
