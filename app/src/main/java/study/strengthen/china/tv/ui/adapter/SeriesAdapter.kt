package study.strengthen.china.tv.ui.adapter

import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Typeface
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import study.strengthen.china.tv.R
import study.strengthen.china.tv.bean.VodInfo.VodSeries
import study.strengthen.china.tv.util.DensityUtil
import study.strengthen.china.tv.util.setVisible
import java.util.*

/**
 * @author pj567
 * @date :2020/12/22
 * @description:
 */
class SeriesAdapter :
    BaseQuickAdapter<VodSeries, BaseViewHolder>(R.layout.item_series, ArrayList()) {
    override fun convert(helper: BaseViewHolder, item: VodSeries) {
        var isLand = false
        if (mContext is Activity) {
            isLand =
                (mContext as Activity).requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
        helper.itemView.layoutParams.apply {
            height = if (isLand) DensityUtil.dip2px(25f) else DensityUtil.dip2px(52f)
        }
        val tvSeries = helper.getView<TextView>(R.id.tvSeries)
        val playingIcon = helper.getView<ImageView>(R.id.playingIcon)
        setTextSize(isLand, tvSeries)
        if (item.selected) {
            tvSeries.setTextColor(mContext.resources.getColor(R.color.main_color))
            //            tvSeries.setTextColor(Color.parseColor("#d0021b"));
            tvSeries.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
            playingIcon.setVisible(!isLand)
        } else {
            tvSeries.setTextColor(mContext.resources.getColor(R.color.c161616))
            tvSeries.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
            playingIcon.visibility = View.GONE
        }
        helper.setText(R.id.tvSeries, item.name)
    }

    private fun setTextSize(isLand: Boolean, tvSeries: TextView) {
        if (isLand) {
//            tvSeries.setTextColor(mContext.getResources().getColor(R.color.white));
            tvSeries.textSize = 9f
        } else {
//            tvSeries.setTextColor(mContext.getResources().getColor(R.color.c161616));
            tvSeries.textSize = 18f
        }
    }
}