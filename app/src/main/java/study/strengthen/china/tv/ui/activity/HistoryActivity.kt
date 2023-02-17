package study.strengthen.china.tv.ui.activity

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.BounceInterpolator
import android.widget.TextView
import com.owen.tvrecyclerview.widget.TvRecyclerView
import com.owen.tvrecyclerview.widget.TvRecyclerView.OnInBorderKeyEventListener
import com.owen.tvrecyclerview.widget.TvRecyclerView.OnItemListener
import com.owen.tvrecyclerview.widget.V7GridLayoutManager
import kotlinx.android.synthetic.main.activity_history.*
import kotlinx.android.synthetic.main.custom_common_title.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import study.strengthen.china.tv.R
import study.strengthen.china.tv.base.BaseActivity
import study.strengthen.china.tv.bean.VodInfo
import study.strengthen.china.tv.cache.RoomDataManger
import study.strengthen.china.tv.event.RefreshEvent
import study.strengthen.china.tv.ui.adapter.HistoryAdapter
import study.strengthen.china.tv.util.FastClickCheckUtil
import java.util.*

/**
 * @author pj567
 * @date :2021/1/7
 * @description:
 */
class HistoryActivity : BaseActivity() {
//    private var tvDel: TextView? = null
//    private var tvDelTip: TextView? = null
    private var mGridView: TvRecyclerView? = null
    private var historyAdapter: HistoryAdapter? = null
    private var delMode = false
    override fun getLayoutResID(): Int {
        return R.layout.activity_history
    }

    override fun init() {
        initView()
        initData()
    }

    private fun toggleDelMode() {
        delMode = !delMode
//        tvDelTip!!.visibility = if (delMode) View.VISIBLE else View.GONE
//        tvDel!!.setTextColor(if (delMode) resources.getColor(R.color.color_FF0057) else Color.WHITE)
    }

    private fun initView() {
        toolbar?.setRightClickEvent {
            toggleDelMode()
        }
//        tvDel = findViewById(R.id.tvDel)
//        tvDelTip = findViewById(R.id.tvDelTip)
        mGridView = findViewById(R.id.mGridView)
        mGridView?.setHasFixedSize(true)
        mGridView?.setLayoutManager(V7GridLayoutManager(mContext, 3))
        historyAdapter = HistoryAdapter()
        mGridView?.setAdapter(historyAdapter)
//        tvDel?.setOnClickListener(View.OnClickListener { toggleDelMode() })
//        mGridView?.setOnInBorderKeyEventListener(OnInBorderKeyEventListener { direction, focused ->
//            if (direction == View.FOCUS_UP) {
//                tvDel?.setFocusable(true)
//                tvDel?.requestFocus()
//            }
//            false
//        })
//        mGridView?.setOnItemListener(object : OnItemListener {
//            override fun onItemPreSelected(parent: TvRecyclerView, itemView: View, position: Int) {
//                itemView.animate().scaleX(1.0f).scaleY(1.0f).setDuration(300).setInterpolator(BounceInterpolator()).start()
//            }
//
//            override fun onItemSelected(parent: TvRecyclerView, itemView: View, position: Int) {
//                itemView.animate().scaleX(1.05f).scaleY(1.05f).setDuration(300).setInterpolator(BounceInterpolator()).start()
//            }
//
//            override fun onItemClick(parent: TvRecyclerView, itemView: View, position: Int) {}
//        })
        historyAdapter?.setOnItemClickListener { adapter, view, position ->
//            FastClickCheckUtil.check(view)
            val vodInfo = historyAdapter!!.data[position]

            //HistoryDialog historyDialog = new HistoryDialog().build(mContext, vodInfo).setOnHistoryListener(new HistoryDialog.OnHistoryListener() {
            //    @Override
            //    public void onLook(VodInfo vodInfo) {
            //        if (vodInfo != null) {
            //            Bundle bundle = new Bundle();
            //            bundle.putInt("id", vodInfo.id);
            //            bundle.putString("sourceKey", vodInfo.sourceKey);
            //            jumpActivity(DetailActivity.class, bundle);
            //        }
            //    }

            //    @Override
            //    public void onDelete(VodInfo vodInfo) {
            //        if (vodInfo != null) {
            //               for (int i = 0; i < historyAdapter.getData().size(); i++) {
            //                    if (vodInfo.id == historyAdapter.getData().get(i).id) {
            //                        historyAdapter.remove(i);
            //                        break;
            //                    }
            //                }
            //                RoomDataManger.deleteVodRecord(vodInfo.sourceKey, vodInfo);
            //        }
            //    }
            //});
            //historyDialog.show();
            if (vodInfo != null) {
                if (delMode) {
                    historyAdapter!!.remove(position)
                    RoomDataManger.deleteVodRecord(vodInfo.sourceKey, vodInfo)
                } else {
                    val bundle = Bundle()
                    bundle.putString("id", vodInfo.id)
                    bundle.putString("sourceKey", vodInfo.sourceKey)
                    jumpActivity(DetailActivity::class.java, bundle)
                }
            }
        }
    }

    private fun initData() {
        val allVodRecord = RoomDataManger.getAllVodRecord(100)
        val vodInfoList: MutableList<VodInfo> = ArrayList()
        for (vodInfo in allVodRecord) {
            vodInfoList.add(vodInfo)
        }
        historyAdapter!!.setNewData(vodInfoList)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun refresh(event: RefreshEvent) {
        if (event.type == RefreshEvent.TYPE_HISTORY_REFRESH) {
            initData()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
//        EventBus.getDefault().unregister(this)
    }

    override fun onBackPressed() {
        if (delMode) {
            toggleDelMode()
            return
        }
        super.onBackPressed()
    }
}