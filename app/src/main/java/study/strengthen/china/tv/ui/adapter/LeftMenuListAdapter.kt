package study.strengthen.china.tv.ui.adapter

import android.content.Context
import android.view.View
import kotlinx.android.synthetic.main.item_left_menu.view.*
import me.zhouzhuo.zzsecondarylinkage.adapter.LeftMenuBaseListAdapter
import me.zhouzhuo.zzsecondarylinkage.viewholder.BaseListViewHolder
import study.strengthen.china.tv.R
import study.strengthen.china.tv.bean.Movie
import study.strengthen.china.tv.bean.SourceBean

/**
 * Created by zz on 2016/8/19.
 */
class LeftMenuListAdapter(private val ctx: Context?, list: List<Movie?>?) : LeftMenuBaseListAdapter<BaseListViewHolder, Movie?>(ctx, list) {
    override fun getViewHolder(): BaseListViewHolder {
        return BaseListViewHolder()
    }

    override fun bindView(leftListViewHolder: BaseListViewHolder, itemView: View) {
//        ViewUtil.scaleContentView((ViewGroup) itemView.findViewById(R.id.root));
//        leftListViewHolder.tvMacName = (TextView) itemView.findViewById(R.id.tv_menu);
//        leftListViewHolder.tvMacId = (TextView) itemView.findViewById(R.id.tv_id);
    }

    override fun bindData(leftListViewHolder: BaseListViewHolder, position: Int) {
//        leftListViewHolder.tvMacName.setText(list.get(position).getMacName());
//        leftListViewHolder.tvMacId.setText(list.get(position).getMacId());
        leftListViewHolder.rootView?.tv_item_text?.text = list[position]?.sourceKey
        if (list[position]?.isSelected == true) {
            leftListViewHolder.rootView?.icon_selected?.visibility = View.VISIBLE
            ctx?.resources?.getColor(R.color.main_color)?.let { leftListViewHolder.rootView?.tv_item_text?.setTextColor(it) }
        } else {
            leftListViewHolder.rootView?.icon_selected?.visibility = View.GONE
            ctx?.resources?.getColor(R.color.common_font_color)?.let { leftListViewHolder.rootView?.tv_item_text?.setTextColor(it) }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.item_left_menu
    }

    //9-patch drawable
    override fun getIndicatorResId(): Int {
        return R.drawable.shape_white_bg
    }
}