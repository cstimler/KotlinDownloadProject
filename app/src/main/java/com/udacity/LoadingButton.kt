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
import android.widget.Toast
import androidx.core.content.withStyledAttributes
import kotlinx.android.synthetic.main.content_main.view.*
import java.lang.Math.PI
import kotlin.math.roundToInt
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    // all the below variables will be set by paint, init, or the onDraw() method below:
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
            // the button loads until it reaches "Completed" (success) or "Indeterminate" (failure):
            ButtonState.Loading -> animateView()
            ButtonState.Completed -> stopAnimation()
            ButtonState.Indeterminate -> stopAnimationAndNotify()
            else -> Log.i("LoadingButton statement problem", "got to else")
        }

    }
    private var paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        val desiredSPSize = 20f // trial and error
        textSize = scaleForSP(desiredSPSize)
        color = backgroundColoring
    }

        // the init assignments use the attributes in the attrs.xml file to populate:
    init {
        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            backgroundColoring = getColor(R.styleable.LoadingButton_backgroundColoring, 0)
            mainTextColor = getColor(R.styleable.LoadingButton_mainTextColor, 0)
            darkOverlayColor = getColor(R.styleable.LoadingButton_darkOverlayColor, 0)
            circleColor = getColor(R.styleable.LoadingButton_circleColor, 0)
        }
    }


    fun stopAnimation() {
        // this represents a successful download; stopAnimatingNow directs button to stop loading animation.
        stopAnimatingNow = true
        Toast.makeText(rootView.context, "Your file has been downloaded! Check notifications.", Toast.LENGTH_SHORT).show()
    }
        // this represents a failed download; stopAnimatingNow directs button to stop loading animation.
    fun stopAnimationAndNotify() {
        stopAnimatingNow = true
        Toast.makeText(rootView.context, "Problem with downloading, please try again later.", Toast.LENGTH_SHORT).show()
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        // Udacity knowledge: https://knowledge.udacity.com/questions/672428
        // helped with next few lines
        var z = Rect()
        canvas?.getClipBounds(z)
        heightSize = z.height().toFloat()
        widthSize = z.width().toFloat()
        // draw the background rectangle:
        paint.color = backgroundColoring
        // using 20f below because the layout_margin is "20" in the xml layout file:
        canvas?.drawRect(20f, 20f, widthSize, heightSize, paint)
        // draw the moving rectangle that sweeps over the background:
        paint.color = darkOverlayColor
        canvas?.drawRect(20f, 20f, rightSideOfMovingRectangle, heightSize, paint)
        // draw the text, centering by basing location on widthSize and heightSize:
        paint.color = mainTextColor
        canvas?.drawText(buttonText, widthSize / 2, heightSize / 2 + 10, paint)
        // circle is centered vertically, and anchored to the right side of the rectangle given
        // a space that is aesthetically pleasing, moved one heightSize to the left:
        paint.color = circleColor
        canvas?.drawArc(widthSize-heightSize, heightSize/4, widthSize-heightSize/2, 3*heightSize/4, 0f, sweepAngle, true, paint)
    }

// stackoverflow.com/questions/4946295/android-expand-collapse-animation
    companion object fun animateView() {
        // animator must take circle arc from 0 to 360 degrees:
        val valueAnimator = ValueAnimator.ofFloat(0f, 360.0f)
        valueAnimator.addUpdateListener { animation ->
            sweepAngle = animation.animatedValue as Float
            // the provided math adjustments take the animator from left to right side:
            rightSideOfMovingRectangle =
                (animation.animatedValue as Float) * ((widthSize - 30f) / 360) + 30f
      // absolutely must call invalidate() here or the animation will not work!!!
            invalidate()
        }
        valueAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {
                // once the animation starts the text must change and the button must be disabled:
                buttonText = "We are loading"
                custom_button.isEnabled = false
                // set flag to keep animating by default unless button state changes:
                stopAnimatingNow = false
                //lets get the coordinates for the button:
                //Based on: https://knowledge.udacity.com/questions/672428
            }

            override fun onAnimationEnd(p0: Animator?) {
                // stopAnimatingNow will be true if button state is either Completed or Indeterminate:
                if (stopAnimatingNow) {
                    sweepAngle = 0f
                    buttonText = "Download"
                    rightSideOfMovingRectangle = 20f
                    custom_button.isEnabled = true
                    Log.i("CHARLES", "Gets to onAnimationEnd")
                } else {
                    // if button state is still Loading then loading animation will continue:
                    animateView()
                }
            }

            override fun onAnimationCancel(p0: Animator?) {

            }

            override fun onAnimationRepeat(p0: Animator?) {

            }

        })
        valueAnimator.duration = 1000  // one second
        valueAnimator.start()
    }


    // https://stackoverflow.com/questions/3061930/how-to-set-unit-for-paint-settextsize
    fun scaleForSP(spValue: Float) : Float {
     val pixels =  TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, Resources.getSystem().displayMetrics)
        return pixels
    }

}