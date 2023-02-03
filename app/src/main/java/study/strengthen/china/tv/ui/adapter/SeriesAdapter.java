package study.strengthen.china.tv.ui.adapter;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import study.strengthen.china.tv.R;
import study.strengthen.china.tv.bean.VodInfo;

import java.util.ArrayList;

/**
 * @author pj567
 * @date :2020/12/22
 * @description:
 */
public class SeriesAdapter extends BaseQuickAdapter<VodInfo.VodSeries, BaseViewHolder> {
    public SeriesAdapter() {
        super(R.layout.item_series, new ArrayList<>());
    }

    @Override
    protected void convert(BaseViewHolder helper, VodInfo.VodSeries item) {
        TextView tvSeries = helper.getView(R.id.tvSeries);
        ImageView playingIcon = helper.getView(R.id.playingIcon);
        if (item.selected) {
            tvSeries.setTextColor(mContext.getResources().getColor(R.color.cC50723));
//            tvSeries.setTextColor(Color.parseColor("#d0021b"));
            tvSeries.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            playingIcon.setVisibility(View.VISIBLE);
        } else {
            tvSeries.setTextColor(mContext.getResources().getColor(R.color.c161616));
            tvSeries.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            playingIcon.setVisibility(View.GONE);
        }
        helper.setText(R.id.tvSeries, item.name);
    }
}