package study.strengthen.china.tv.ui.adapter

import android.content.Context
import android.text.TextUtils
import android.view.View
import com.squareup.picasso.Picasso
import me.zhouzhuo.zzsecondarylinkage.adapter.RightMenuBaseListAdapter
import me.zhouzhuo.zzsecondarylinkage.viewholder.BaseListViewHolder
import study.strengthen.china.tv.R
import study.strengthen.china.tv.bean.Movie
import kotlinx.android.synthetic.main.item_search_fast.view.*
import study.strengthen.china.tv.util.DefaultConfig

/**
 * Created by zz on 2016/8/20.
 */
class RightContentListAdapter(ctx: Context?, list: List<Movie.Video?>?) : RightMenuBaseListAdapter<BaseListViewHolder, Movie.Video?>(ctx, list) {
    override fun getViewHolder(): BaseListViewHolder {
        return BaseListViewHolder()
    }

    override fun bindView(rightListViewHolder: BaseListViewHolder, itemView: View) {
//        ViewUtil.scaleContentView((ViewGroup) itemView.findViewById(R.id.root));
//        rightListViewHolder.ivPic = (ImageView) itemView.findViewById(R.id.iv_pic);
//        rightListViewHolder.tvProductName = (TextView) itemView.findViewById(R.id.tv_product_name);
//        rightListViewHolder.tvMacName = (TextView) itemView.findViewById(R.id.mac_name);
//        rightListViewHolder.tvTaskNum = (TextView) itemView.findViewById(R.id.tv_task_number);
//        rightListViewHolder.tvTaskId = (TextView) itemView.findViewById(R.id.tv_task_id);
//        rightListViewHolder.tvStartTime = (TextView) itemView.findViewById(R.id.tv_start_time);
    }

    override fun bindData(rightListViewHolder: BaseListViewHolder?, position: Int) {
//        Glide.with(context).load(Constants.BASE_URL + getItem(position).getPicUrl()).into(rightListViewHolder.ivPic);
//        rightListViewHolder.tvProductName.setText(getItem(position).getProductName());
//        rightListViewHolder.tvMacName.setText(getItem(position).getMachineName());
//        rightListViewHolder.tvTaskNum.setText(getItem(position).getTaskNo());
//        rightListViewHolder.tvTaskId.setText(getItem(position).getTaskId());
//        rightListViewHolder.tvStartTime.setText(getItem(position).getStartTime());
        val item = list[position]
        rightListViewHolder?.rootView?.tvName?.text = item?.name
        rightListViewHolder?.rootView?.tvSite?.text = "来源:"+item?.sourceKey
        if (item?.year?:0 <= 0) {
            rightListViewHolder?.rootView?.tvYear?.setVisibility(View.GONE)
        } else {
            rightListViewHolder?.rootView?.tvYear?.setText(item?.year.toString());
            rightListViewHolder?.rootView?.tvYear?.setVisibility(View.VISIBLE);
        }
        if (TextUtils.isEmpty(item?.note)) {
            rightListViewHolder?.rootView?.tvNote?.setVisibility(View.INVISIBLE)
        } else {
            rightListViewHolder?.rootView?.tvNote?.setVisibility(View.VISIBLE)
            rightListViewHolder?.rootView?.tvNote?.setText(item?.note)
        }
        if (TextUtils.isEmpty(item?.actor)) {
            rightListViewHolder?.rootView?.tvActor?.setVisibility(View.INVISIBLE)
        } else {
            rightListViewHolder?.rootView?.tvActor?.setVisibility(View.VISIBLE)
            rightListViewHolder?.rootView?.tvActor?.setText(item?.actor)
        }
        //由于部分电视机使用glide报错
        if (!item?.pic.isNullOrEmpty()) {
            Picasso.get()
                    .load(DefaultConfig.checkReplaceProxy(item?.pic))
//                    .transform(new RoundTransformation(MD5.string2MD5(item.pic + "position=" + helper.getLayoutPosition()))
//                            .centerCorp(true)
//                            .roundRadius(DensityUtil.dip2px( 14f), RoundTransformation.RoundType.ALL))
//                    .centerCrop()
                    .placeholder(R.drawable.img_loading_placeholder)
                    .error(R.drawable.img_loading_placeholder)
                    .into(rightListViewHolder?.rootView?.ivThumb)
        } else {
            rightListViewHolder?.rootView?.ivThumb?.setImageResource(R.drawable.img_loading_placeholder);
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.item_search_fast
    }
}