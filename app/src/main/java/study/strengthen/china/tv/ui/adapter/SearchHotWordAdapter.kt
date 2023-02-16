package study.strengthen.china.tv.ui.adapter

import android.graphics.Color
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import kotlinx.android.synthetic.main.item_search_hot.view.*
import study.strengthen.china.tv.R

class SearchHotWordAdapter : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_search_hot,ArrayList<String>()) {
    override fun convert(helper: BaseViewHolder?, item: String?) {
        if (helper?.layoutPosition == 0)
            helper.itemView.tv_num?.setBackgroundColor(Color.parseColor("#F44336"));
        if (helper?.layoutPosition == 1)
            helper.itemView.tv_num?.setBackgroundColor(Color.parseColor("#F57C00"));
        if (helper?.layoutPosition == 2)
            helper.itemView.tv_num?.setBackgroundColor(Color.parseColor("#FFC107"));
        val rankNo = (helper?.layoutPosition?:0) + 1
        helper?.itemView?.tv_num?.text = rankNo.toString()
        helper?.itemView?.tvSearchWord?.text = item
    }
}