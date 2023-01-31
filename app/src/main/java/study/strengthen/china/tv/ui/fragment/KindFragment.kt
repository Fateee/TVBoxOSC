package study.strengthen.china.tv.ui.fragment

import android.os.Bundle
import android.view.View
import android.view.animation.BounceInterpolator
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.owen.tvrecyclerview.widget.TvRecyclerView
import com.owen.tvrecyclerview.widget.TvRecyclerView.OnInBorderKeyEventListener
import com.owen.tvrecyclerview.widget.TvRecyclerView.OnItemListener
import com.owen.tvrecyclerview.widget.V7GridLayoutManager
import kotlinx.android.synthetic.main.fragment_grid.*
import study.strengthen.china.tv.R
import study.strengthen.china.tv.base.BaseLazyFragment
import study.strengthen.china.tv.bean.MovieSort.SortData
import study.strengthen.china.tv.ui.activity.DetailActivity
import study.strengthen.china.tv.ui.adapter.GridAdapter
import study.strengthen.china.tv.ui.dialog.GridFilterDialog
import study.strengthen.china.tv.ui.tv.widget.LoadMoreView
import study.strengthen.china.tv.util.DensityUtil
import study.strengthen.china.tv.util.FastClickCheckUtil
import study.strengthen.china.tv.util.GridSpaceItemDecoration
import study.strengthen.china.tv.viewmodel.SourceViewModel

/**
 * @author pj567
 * @date :2020/12/21
 * @description:
 */
class KindFragment : BaseLazyFragment() {
    private var sortData: SortData? = null
//    private var mGridView: TvRecyclerView? = null
    private var sourceViewModel: SourceViewModel? = null
    private var gridFilterDialog: GridFilterDialog? = null
    private var gridAdapter: GridAdapter? = null
    private var page = 1
    private var maxPage = 1
    var isLoad = false
        private set
    var isTop = true
        private set

    fun setArguments(sortData: SortData?): KindFragment {
        this.sortData = sortData
        return this
    }

    override fun getLayoutResID(): Int {
        return R.layout.fragment_grid
    }

    override fun init() {
        initView()
        initViewModel()
        initData()
    }

    private fun initView() {
//        mGridView?.setHasFixedSize(true)
        gridAdapter = GridAdapter()
        mGridView?.adapter = gridAdapter
        mGridView?.layoutManager = GridLayoutManager(context,3)
        mGridView?.addItemDecoration(GridSpaceItemDecoration(3, DensityUtil.dip2px(6f), DensityUtil.dip2px(6f)))
        gridAdapter!!.setOnLoadMoreListener({
            gridAdapter!!.setEnableLoadMore(true)
            sourceViewModel!!.getList(sortData, page)
        }, mGridView)
        gridAdapter!!.setOnItemClickListener { adapter, view, position ->
            FastClickCheckUtil.check(view)
            val video = gridAdapter!!.data[position]
            if (video != null) {
                val bundle = Bundle()
                bundle.putString("id", video.id)
                bundle.putString("sourceKey", video.sourceKey)
                jumpActivity(DetailActivity::class.java, bundle)
            }
        }
        gridAdapter!!.setLoadMoreView(LoadMoreView())
        setLoadSir(mGridView)
    }

    private fun initViewModel() {
        sourceViewModel = ViewModelProvider(this).get(SourceViewModel::class.java)
        sourceViewModel!!.listResult.observe(this, Observer { absXml ->
            if (absXml?.movie != null && absXml.movie.videoList != null && absXml.movie.videoList.size > 0) {
                if (page == 1) {
                    showSuccess()
                    isLoad = true
                    gridAdapter!!.setNewData(absXml.movie.videoList)
                } else {
                    gridAdapter!!.addData(absXml.movie.videoList)
                }
                page++
                maxPage = absXml.movie.pagecount
            } else {
                if (page == 1) {
                    showEmpty()
                }
            }
            if (page > maxPage) {
                gridAdapter!!.loadMoreEnd()
            } else {
                gridAdapter!!.loadMoreComplete()
            }
        })
    }

    private fun initData() {
        showLoading()
        isLoad = false
        sourceViewModel!!.getList(sortData, page)
    }

    fun scrollTop() {
        isTop = true
        mGridView!!.scrollToPosition(0)
    }

    fun showFilter() {
        if (!sortData!!.filters.isEmpty() && gridFilterDialog == null) {
            gridFilterDialog = GridFilterDialog(mContext)
            gridFilterDialog!!.setData(sortData)
            gridFilterDialog!!.setOnDismiss {
                page = 1
                initData()
            }
        }
        if (gridFilterDialog != null) gridFilterDialog!!.show()
    }

    companion object {
        fun newInstance(sortData: SortData?): KindFragment {
            return KindFragment().setArguments(sortData)
        }
    }
}