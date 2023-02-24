package study.strengthen.china.tv.ui.activity

import android.content.Intent
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
import kotlinx.android.synthetic.main.custom_common_title.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import study.strengthen.china.tv.R
import study.strengthen.china.tv.api.ApiConfig
import study.strengthen.china.tv.base.BaseActivity
import study.strengthen.china.tv.cache.RoomDataManger
import study.strengthen.china.tv.cache.VodCollect
import study.strengthen.china.tv.event.RefreshEvent
import study.strengthen.china.tv.ui.adapter.CollectAdapter
import study.strengthen.china.tv.ui.adapter.HistoryAdapter
import study.strengthen.china.tv.util.FastClickCheckUtil
import java.util.*

class CollectActivity : BaseActivity() {
//    private var tvDel: TextView? = null
//    private var tvDelTip: TextView? = null
    private var mGridView: TvRecyclerView? = null
    private var collectAdapter: CollectAdapter? = null
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
        toolbar?.tv_common_title?.text = "我的收藏"
//        EventBus.getDefault().register(this)
//        tvDel = findViewById(R.id.tvDel)
//        tvDelTip = findViewById(R.id.tvDelTip)
        mGridView = findViewById(R.id.mGridView)
        mGridView?.setHasFixedSize(true)
        mGridView?.setLayoutManager(V7GridLayoutManager(mContext, 3))
        collectAdapter = CollectAdapter()
        mGridView?.setAdapter(collectAdapter)
        collectAdapter?.setOnItemClickListener { adapter, view, position ->
            val vodInfo = collectAdapter!!.data[position]
            if (vodInfo != null) {
                if (delMode) {
                    collectAdapter!!.remove(position)
                    RoomDataManger.deleteVodCollect(vodInfo.id)
                } else {
                    if (ApiConfig.get().getSource(vodInfo.sourceKey) != null) {
                        val bundle = Bundle()
                        bundle.putString("id", vodInfo.vodId)
                        bundle.putString("sourceKey", vodInfo.sourceKey)
                        jumpActivity(DetailActivity::class.java, bundle)
                    } else {
                        val newIntent = Intent(mContext, SearchActivity::class.java)
                        newIntent.putExtra("title", vodInfo.name)
                        newIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(newIntent)
                    }
                }
            }
        }
    }

    private fun initData() {
        val allVodRecord = RoomDataManger.getAllVodCollect()
        val vodInfoList: MutableList<VodCollect> = ArrayList()
        for (vodInfo in allVodRecord) {
            vodInfoList.add(vodInfo)
        }
        collectAdapter!!.setNewData(vodInfoList)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun refresh(event: RefreshEvent) {
        if (event.type == RefreshEvent.TYPE_HISTORY_REFRESH) {
            initData()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onBackPressed() {
        if (delMode) {
            toggleDelMode()
            return
        }
        super.onBackPressed()
    }
}