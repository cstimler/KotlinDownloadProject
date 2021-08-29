package com.udacity

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.renderscript.Sampler
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import androidx.core.content.withStyledAttributes
import kotlinx.android.synthetic.main.content_main.view.*
import java.lang.Math.PI
import kotlin.math.roundToInt
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0f
    private var heightSize = 0f
    private var backgroundColoring = 0
    private var mainTextColor = 0
    private var darkOverlayColor = 0
    private var circleColor = 0
    private var buttonText: String = "Download"
    private lateinit var globalCanvas: Canvas

    //   private val valueAnimator = ValueAnimator()
    private var sweepAngle: Float = 0f
    private var rightSideOfMovingRectangle: Float = 20f

    private var stopAnimatingNow: Boolean = false

    var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->
        // https://knowledge.udacity.com/questions/420421
        when(new) {
            ButtonState.Loading -> animateView()
            ButtonState.Completed -> stopAnimation()
            else -> Log.i("CHARLES when statement problem", "got to else")
        }

    }
    private var paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        val desiredSPSize = 20f
        textSize = scaleForSP(desiredSPSize)
        // color = Color.BLACK
        color = backgroundColoring
    }

    init {
        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            backgroundColoring = getColor(R.styleable.LoadingButton_backgroundColoring, 0)
            mainTextColor = getColor(R.styleable.LoadingButton_mainTextColor, 0)
            darkOverlayColor = getColor(R.styleable.LoadingButton_darkOverlayColor, 0)
            circleColor = getColor(R.styleable.LoadingButton_circleColor, 0)
        }
    }

    fun stopAnimation() {
        stopAnimatingNow = true
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        // Udacity knowledge: https://knowledge.udacity.com/questions/672428
        // helped with next few lines
        var z = Rect()
        canvas?.getClipBounds(z)
        heightSize = z.height().toFloat()
        widthSize = z.width().toFloat()
        paint.color = backgroundColoring
        canvas?.drawRect(20f, 20f, widthSize, heightSize, paint)
        paint.color = darkOverlayColor
        canvas?.drawRect(20f, 20f, rightSideOfMovingRectangle, heightSize, paint)
        //  paint.color = Color.RED
        paint.color = mainTextColor
        // paint.getTextBounds(buttonText, 0, buttonText.length, z)
        canvas?.drawText(buttonText, widthSize / 2, heightSize / 2 + 10, paint)
       // Log.i(
      //      "CHARLES: left, bottom",
      //      z.left.toFloat().toString() + " " + z.bottom.toFloat().toString()
     //   )
        paint.color = circleColor
        canvas?.drawArc(widthSize-heightSize, heightSize/4, widthSize-heightSize/2, 3*heightSize/4, 0f, sweepAngle, true, paint)
     //   Log.i("CHARLES in onDraw:", sweepAngle.toString())
     //   Log.i("CHARLES IN onDraw - update regular sweepAngle", sweepAngle.toString())
     //   Log.i("CHARLES GENERIC ONDRAW", "THIS JUST HAS TO BE CALLED")
    }



    /*
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
 */
// stackoverflow.com/questions/4946295/android-expand-collapse-animation
    companion object fun animateView() {
        Log.i("CHARLES", "Got into animate View")
        val valueAnimator = ValueAnimator.ofFloat(0f, 360.0f)
        valueAnimator.addUpdateListener { animation ->
            sweepAngle = animation.animatedValue as Float
            rightSideOfMovingRectangle =
                (animation.animatedValue as Float) * ((widthSize - 30f) / 360) + 30f
       //     Log.i("CHARLES - right side of moving rect is:", rightSideOfMovingRectangle.toString())
            // requestLayout()
            invalidate()
            //   setBackgroundColor(Color.BLUE)
      //      Log.i("CHARLES animateView's sweepAngle", sweepAngle.toString())
        }
        valueAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {
                buttonText = "We are loading"
                custom_button.isEnabled = false
                stopAnimatingNow = false
                //lets get the coordinates for the button:
                //Based on: https://knowledge.udacity.com/questions/672428
            }

            override fun onAnimationEnd(p0: Animator?) {
                if (stopAnimatingNow) {
                    sweepAngle = 0f
                    buttonText = "Download"
                    rightSideOfMovingRectangle = 20f
                    custom_button.isEnabled = true
                    Log.i("CHARLES", "Gets to onAnimationEnd")
                    //     setBackgroundColor(Color.YELLOW)
                    //  animateView()
                } else {
                    animateView()
                }
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


    // https://stackoverflow.com/questions/3061930/how-to-set-unit-for-paint-settextsize
    fun scaleForSP(spValue: Float) : Float {
     val pixels =  TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, Resources.getSystem().displayMetrics)
        return pixels
    }

}