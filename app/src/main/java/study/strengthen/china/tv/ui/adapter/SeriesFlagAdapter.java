package study.strengthen.china.tv.ui.adapter;

import android.app.Activity;
import android.content.pm.ActivityInfo;
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
        boolean isLand = false;
        if (mContext instanceof Activity) {
            isLand = ((Activity)mContext).getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        }
        setTextSize(isLand,tvSeries);
        if (item.selected) {
            tvSeries.setTextColor(mContext.getResources().getColor(R.color.main_color));
            tvSeries.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            select.setVisibility(View.VISIBLE);
        } else {
            tvSeries.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            select.setVisibility(View.INVISIBLE);
        }
        helper.setText(R.id.tvSeriesFlag, item.name);
    }

    private void setTextSize(boolean isLand, TextView tvSeries) {
        if (isLand) {
            tvSeries.setTextColor(mContext.getResources().getColor(R.color.white));
            tvSeries.setTextSize(13f);
        } else {
            tvSeries.setTextColor(mContext.getResources().getColor(R.color.c161616));
            tvSeries.setTextSize(20f);
        }
    }
}