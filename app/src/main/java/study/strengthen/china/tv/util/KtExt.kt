package study.strengthen.china.tv.util

import android.content.Context
import android.content.res.Resources
import android.graphics.Typeface
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import study.strengthen.china.tv.BuildConfig
import java.math.BigDecimal
import kotlin.math.roundToInt

/**
 * Author : Ray
 * Time : 2019-09-29 11:07
 * Description :
 */

fun View.inflate(id: Int, root: ViewGroup? = null, attachToRoot: Boolean = root != null): View {
    return LayoutInflater.from(context).inflate(id, root, attachToRoot)
}

fun View.setVisible(visible: Boolean, isGone: Boolean = false) {
    visibility = if (visible) {
        View.VISIBLE
    } else {
        if (isGone) {
            View.GONE
        } else {
            View.INVISIBLE
        }
    }
}

fun Float.dp2Px(context: Context? = null) =
    TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        Resources.getSystem().displayMetrics
    )

fun Float.sp2Px(context: Context? = null) =
    TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP,
        this,
        Resources.getSystem().displayMetrics
    )

//fun String.toast() {
//    Toast(SingleContainer.getApplicationContext()).apply {
//        setGravity(Gravity.CENTER, 0, 0)
//        view = LayoutInflater.from(SingleContainer.getApplicationContext())
//            .inflate(R.layout.layout_toast, null)
//        setText(this@toast)
//        show()
//    }
//}

//fun ImageView.load(url: String, placeHolder: Int = 0, cacheDisk: Boolean = true) {
//    Glide.with(context).load(url).apply(
//        RequestOptions()
//            .diskCacheStrategy(if (cacheDisk) DiskCacheStrategy.ALL else DiskCacheStrategy.NONE)
//            .dontAnimate()
//            .placeholder(placeHolder)
//    ).into(this)
//}

val Int.dpInt: Int
    get() {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            Resources.getSystem().displayMetrics
        ).roundToInt()
    }

val Float.dpInt: Int
    get() {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this,
            Resources.getSystem().displayMetrics
        ).roundToInt()
    }

val Int.dpFloat: Float
    get() {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            Resources.getSystem().displayMetrics
        )
    }

/**
 * double类型的减法
 */
fun doubleSub(v1: Double, v2: Double): Double {
    val b1 = BigDecimal(v1.toString())
    val b2 = BigDecimal(v2.toString())

    return b1.subtract(b2).toDouble()
}

/**
 * double类型的加法
 */
fun doubleAdd(v1: Double, v2: Double): Double {
    val b1 = BigDecimal(v1.toString())
    val b2 = BigDecimal(v2.toString())

    return b1.add(b2).toDouble()
}

fun String?.getValueOrBlank(): String {
    return if (isNullOrBlank()) "" else this!!
}

fun Int.color(context: Context): Int {
    return context.resources.getColor(this)
}

fun Any.log(prefix: String? = null) {
    if (BuildConfig.DEBUG)
        Log.d("taia", "$prefix ${toString()}")
}

val View.isVisible: Boolean
    get() = visibility == View.VISIBLE


///**
// * ArrayList深度拷贝,需要实现DeepCloneable接口
// */
//fun <T : DeepCloneable> ArrayList<T>.deepCopy(): ArrayList<T> {
//    val newList = ArrayList<T>(size)
//    forEach { item ->
//        newList.add(
//            when (item) {
//                is DeepCloneable -> item.copy()
//                else -> item
//            } as T
//        )
//    }
//    return newList
//}


///**
// * 设置为Din字体
// */
//fun TextView.dinFont(){
//    val mFont = Typeface.createFromAsset(SingleContainer.getApplicationContext().assets, "fonts/DINCondensedBold.ttf")
//    this.typeface = mFont
//}

fun TextView.setTextWithVisible(txt : String?) {
    text = txt
    visibility = if (text.isNullOrEmpty()) View.GONE else View.VISIBLE
}
