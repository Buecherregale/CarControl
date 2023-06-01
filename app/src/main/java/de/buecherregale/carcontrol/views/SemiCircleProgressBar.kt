package de.buecherregale.carcontrol.views

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import de.buecherregale.carcontrol.R
import java.lang.Integer.min

class SemiCircleProgressBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0):
    View(context, attrs, defStyleAttr, defStyleRes) {

    var min: Int = 10
        set(value) {    // remove check
            field = value
            if(progress < value) {
                progress = value
            }
            invalidate()
        }
    var max = 1000
        set(value) {
            field = value
            if(progress > value) {
                progress = value
            }
            invalidate()
        }
    var progress: Int = min
        set(value) {
            if(value in min..max) {
                field = value
                invalidate()
            } else {
                field = min
            }
        }

    // visuals
    // don't realloc, reuse
    private val paint = Paint()
    private val textPaint = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.STROKE
        textSize = 48f
        textAlign = Paint.Align.CENTER
    }
    private val rectF = RectF()

    private var backgroundColor = Color.GRAY
    private var foregroundColor = Color.GREEN

    private var backStrokeWidth = 10f
    private var foreStrokeWidth = 8f

    init {
        // get xml attributes
        if(attrs != null) {
            val typedArray: TypedArray =
                context.obtainStyledAttributes(attrs, R.styleable.SemiCircleProgressBar)

            min =
                typedArray.getInt(R.styleable.SemiCircleProgressBar_Minimum, min)
            max =
                typedArray.getInt(R.styleable.SemiCircleProgressBar_Maximum, max)
            progress =
                typedArray.getInt(R.styleable.SemiCircleProgressBar_Progress, progress)
            // ui
            backgroundColor =
                typedArray.getColor(R.styleable.SemiCircleProgressBar_BackgroundColor, backgroundColor)
            foregroundColor =
                typedArray.getColor(R.styleable.SemiCircleProgressBar_ForegroundColor, foregroundColor)
            backStrokeWidth =
                typedArray.getDimension(R.styleable.SemiCircleProgressBar_BackStrokeWidth, backStrokeWidth)
            foreStrokeWidth =
                typedArray.getDimension(R.styleable.SemiCircleProgressBar_ForeStrokeWidth, foreStrokeWidth)

            typedArray.recycle()
        }
    }

    @Override
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // background
        updatePaint(backgroundColor, backStrokeWidth)

        val radius = min(width, height) / 2 - paint.strokeWidth / 2
        updateRectF(radius)
        // start at 180f for upper circle half
        canvas.drawArc(rectF, 180f, 180f, false, paint)

        // foreground
        updatePaint(foregroundColor, foreStrokeWidth)
        updateRectF(radius)

        canvas.drawArc(rectF, 180f, getSweepAngle(), false, paint)

        val textX = width.toFloat() / 2
        val textY = height.toFloat() / 2 + textPaint.textSize / 4
        canvas.drawText(progress.toString(), textX, textY, textPaint)
    }

    private fun getSweepAngle() : Float {
        // 1100 - 1100
        return (progress - min).toFloat() / (max - min) * 180f
    }

    private fun updatePaint(color: Int, strokeWidth: Float) {
        paint.color = color
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth
        paint.isAntiAlias = true
        paint.strokeCap = Paint.Cap.ROUND
    }

    private fun updateRectF(radius: Float) {
        val centerX = width / 2
        val centerY = height / 2

        rectF.left = centerX - radius
        rectF.top = centerY - radius
        rectF.right = centerX + radius
        rectF.bottom = centerY + radius
    }
}