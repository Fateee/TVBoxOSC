package study.strengthen.china.tv.ui.fragment

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.AbsCallback
import com.lzy.okgo.model.Response
import com.orhanobut.hawk.Hawk
import kotlinx.android.synthetic.main.recommend_fragment.*
import study.strengthen.china.tv.R
import study.strengthen.china.tv.api.ApiConfig
import study.strengthen.china.tv.base.BaseLazyFragment
import study.strengthen.china.tv.bean.Movie
import study.strengthen.china.tv.ui.activity.DetailActivity
import study.strengthen.china.tv.ui.activity.SearchActivity
import study.strengthen.china.tv.ui.adapter.GridAdapter
import study.strengthen.china.tv.util.DensityUtil
import study.strengthen.china.tv.util.GridSpaceItemDecoration
import study.strengthen.china.tv.util.HawkConfig
import java.util.*

class HotFragment : BaseLazyFragment() {

    companion object {
        fun newInstance(recVod: List<Movie.Video?>?): HotFragment {
            return HotFragment().setArguments(recVod)
        }
    }
    private var homeSourceRec: List<Movie.Video?>? = null
    private var homeHotVodAdapter: GridAdapter? = null

    fun setArguments(recVod: List<Movie.Video?>?): HotFragment {
        homeSourceRec = recVod
        return this
    }

    override fun init() {
        homeHotVodAdapter = GridAdapter()
        homeHotVodAdapter?.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            if (ApiConfig.get().sourceBeanList.isEmpty()) return@OnItemClickListener
            val vod = adapter.getItem(position) as Movie.Video?
            if (vod!!.id != null && !vod.id.isEmpty()) {
                val bundle = Bundle()
                bundle.putString("id", vod.id)
                bundle.putString("sourceKey", vod.sourceKey)
                jumpActivity(DetailActivity::class.java, bundle)
            } else {
                val newIntent = Intent(mContext, SearchActivity::class.java)
                newIntent.putExtra("title", vod.name)
                newIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                mActivity.startActivity(newIntent)
            }
        }
        tvHotList?.layoutManager = GridLayoutManager(context,3)
        tvHotList?.addItemDecoration(GridSpaceItemDecoration(3, DensityUtil.dip2px(6f), DensityUtil.dip2px(6f)))
        tvHotList?.setAdapter(homeHotVodAdapter)
        homeHotVodAdapter?.let {
            initHomeHotVod(it)
        }
    }

    override fun getLayoutResID() = R.layout.recommend_fragment

    private fun initHomeHotVod(adapter: GridAdapter) {
        if (Hawk.get(HawkConfig.HOME_REC, 0) == 1) {
            if (homeSourceRec != null) {
                adapter.setNewData(homeSourceRec)
            }
            return
        } else if (Hawk.get(HawkConfig.HOME_REC, 0) == 2) {
            return
        }
        try {
            val cal = Calendar.getInstance()
            val year = cal[Calendar.YEAR]
            val month = cal[Calendar.MONTH] + 1
            val day = cal[Calendar.DATE]
            val today = String.format("%d%d%d", year, month, day)
            val requestDay = Hawk.get("home_hot_day", "")
            if (requestDay == today) {
                val json = Hawk.get("home_hot", "")
                if (!json.isEmpty()) {
                    adapter.setNewData(loadHots(json))
                    return
                }
            }
            OkGo.get<String>("https://movie.douban.com/j/new_search_subjects?sort=U&range=0,10&tags=&playable=1&start=0&year_range=$year,$year").execute(object : AbsCallback<String?>() {
                override fun onSuccess(response: Response<String?>?) {
                    val netJson = response?.body()
                    Hawk.put("home_hot_day", today)
                    Hawk.put("home_hot", netJson)
                    mActivity.runOnUiThread { adapter.setNewData(netJson?.let { loadHots(it) }) }
                }

                @Throws(Throwable::class)
                override fun convertResponse(response: okhttp3.Response): String {
                    return response.body()!!.string()
                }

            })
        } catch (th: Throwable) {
            th.printStackTrace()
        }
    }

    private fun loadHots(json: String): ArrayList<Movie.Video>? {
        val result = ArrayList<Movie.Video>()
        try {
            val infoJson = Gson().fromJson(json, JsonObject::class.java)
            val array = infoJson.getAsJsonArray("data")
            for (ele in array) {
                val obj = ele as JsonObject
                val vod = Movie.Video()
                vod.name = obj["title"].asString
                vod.note = obj["rate"].asString
                vod.pic = obj["cover"].asString
                result.add(vod)
            }
        } catch (th: Throwable) {
        }
        return result
    }
}