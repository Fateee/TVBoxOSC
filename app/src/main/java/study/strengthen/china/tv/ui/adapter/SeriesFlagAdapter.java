package study.strengthen.china.tv.ui.adapter;

import android.graphics.Typeface;
import android.view.View;
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
public class SeriesFlagAdapter extends BaseQuickAdapter<VodInfo.VodSeriesFlag, BaseViewHolder> {
    public SeriesFlagAdapter() {
        super(R.layout.item_series_flag, new ArrayList<>());
    }

    @Override
    protected void convert(BaseViewHolder helper, VodInfo.VodSeriesFlag item) {
        TextView tvSeries = helper.getView(R.id.tvSeriesFlag);
        View select = helper.getView(R.id.tvSeriesFlagSelect);
        if (item.selected) {
            tvSeries.setTextColor(mContext.getResources().getColor(R.color.cC50723));
            tvSeries.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            select.setVisibility(View.VISIBLE);
        } else {
            tvSeries.setTextColor(mContext.getResources().getColor(R.color.c161616));
            tvSeries.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            select.setVisibility(View.GONE);
        }
        helper.setText(R.id.tvSeriesFlag, item.name);
    }
}