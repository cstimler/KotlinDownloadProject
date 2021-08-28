package com.udacity

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.renderscript.Sampler
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.withStyledAttributes
import kotlinx.android.synthetic.main.content_main.view.*
import java.lang.Math.PI
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 100
    private var heightSize = 100
    private var backgroundColoring = 0
    private var mainTextColor = 0

 //   private val valueAnimator = ValueAnimator()
    private var sweepAngle: Float = 90f

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->
    }
    var v2 = getRootView()
    var rv = v2.custom_button
    private var paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 50.0f
       // color = Color.BLACK
        color = backgroundColoring
    }

    init {
        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            backgroundColoring = getColor(R.styleable.LoadingButton_backgroundColoring, 0)
            mainTextColor = getColor(R.styleable.LoadingButton_mainTextColor, 0)

        }
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        paint.color = backgroundColoring
        canvas?.drawRect(30f, 30f, 1200f, 800f, paint)
      //  paint.color = Color.RED
        paint.color = mainTextColor
        canvas?.drawText("TESTING", 500f, 130f, paint)
        canvas?.drawArc(650f, 60f, 750f, 160f, 0f, sweepAngle, true, paint)
        Log.i("CHARLES in onDraw:", sweepAngle.toString())
        Log.i("CHARLES IN onDraw - update regular sweepAngle", sweepAngle.toString())
        Log.i("CHARLES GENERIC ONDRAW","THIS JUST HAS TO BE CALLED")
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }
// stackoverflow.com/questions/4946295/android-expand-collapse-animation
    companion object fun animateView(v:View, initialAngle: Float, finalAngle: Float) {
        Log.i("CHARLES", "Got into animate View")
        val valueAnimator = ValueAnimator.ofFloat(0f, 360.0f)
        valueAnimator.addUpdateListener { animation ->
            sweepAngle = animation.animatedValue as Float
           // requestLayout()
            invalidate()
            setBackgroundColor(Color.BLUE)
            Log.i("CHARLES", rv.sweepAngle.toString())
            Log.i("CHARLES animateView's sweepAngle", sweepAngle.toString())
        }
        valueAnimator.addListener(object: Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {

            }

            override fun onAnimationEnd(p0: Animator?) {
                sweepAngle = 360f
                Log.i("CHARLES", "Gets to onAnimationEnd")
                setBackgroundColor(Color.YELLOW)

            }

            override fun onAnimationCancel(p0: Animator?) {

            }

            override fun onAnimationRepeat(p0: Animator?) {

            }

        })
    valueAnimator.duration = 1000
    Log.i("CHARLES", "Gets down to start")
    valueAnimator.start()
    }

}