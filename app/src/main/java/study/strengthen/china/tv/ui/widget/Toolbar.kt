package study.strengthen.china.tv.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.custom_common_title.view.*
import study.strengthen.china.tv.R
import study.strengthen.china.tv.util.AppManager

/**
 */
class Toolbar : LinearLayout {

    private var mRightEvent: (() -> Unit)? = null

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        LayoutInflater.from(context).inflate(R.layout.custom_common_title,this)
        // Load attributes
        val a = context.obtainStyledAttributes(
                attrs, R.styleable.Toolbar, defStyle, 0)

        val titleString = a.getString(
                R.styleable.Toolbar_middleTitle)
        val rightTitleString = a.getString(
                R.styleable.Toolbar_rightTitle)
        iv_back?.setOnClickListener {
            AppManager.getInstance().currentActivity().finish()
        }
        titleString?.let {
            tv_common_title?.text = it
        }
        rightTitleString?.let {
            tv_custom?.visibility = View.VISIBLE
            tv_custom?.text = it
            tv_custom?.setOnClickListener {
                mRightEvent?.invoke()
            }
        }
        a.recycle()
    }

    fun setRightClickEvent(event : (() -> Unit)?) {
        mRightEvent = event
    }
}