package ch.famoser.mensa.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.cardview.widget.CardView


class InterceptTouchCardView: CardView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attributes: AttributeSet) : super(context, attributes)

    constructor(context: Context, attributes: AttributeSet, defStyleAttr: Int) : super(context, attributes, defStyleAttr)

    /**
     * Intercept touch event so that inner views cannot receive it.
     *
     * If a ViewGroup contains a RecyclerView and has an OnTouchListener or something like that,
     * touch events will be directly delivered to inner RecyclerView and handled by it. As a result,
     * parent ViewGroup won't receive the touch event any longer.
     */
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return true
    }
}