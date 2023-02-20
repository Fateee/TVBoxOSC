package study.strengthen.china.tv.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
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
import study.strengthen.china.tv.ui.activity.FastSearchActivity
import study.strengthen.china.tv.ui.adapter.GridAdapter
import study.strengthen.china.tv.util.DensityUtil
import study.strengthen.china.tv.util.GridSpaceItemDecoration
import study.strengthen.china.tv.util.HawkConfig
import java.util.*

class HotFragment : BaseLazyFragment() {

    companion object {
        fun newInstance(recVod: List<Movie.Video?>?, type : Int = 0): HotFragment {
            return HotFragment().setArguments(recVod,type)
        }
    }
    private var homeSourceRec: List<Movie.Video?>? = null
    private var homeHotVodAdapter: GridAdapter? = null
    private var type = 0

    fun setArguments(recVod: List<Movie.Video?>?, type : Int = 0): HotFragment {
        homeSourceRec = recVod
        this.type = type
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
                val newIntent = Intent(mContext, FastSearchActivity::class.java)
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
        if (homeSourceRec != null) {
            adapter.setNewData(homeSourceRec)
            return
        } else if (Hawk.get(HawkConfig.HOME_REC, 0) == 2) {
            return
        }
        when(type) {
            0->{
                loadDbHot(adapter)
            }
            1->{
                loadOtherHot(adapter)
            }
            else -> {
                loadIQYHot(adapter)
            }
        }
    }

    private fun loadDbHot(adapter: GridAdapter) {
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

    private fun loadOtherHot(adapter: GridAdapter) {
        // 加载热词
        OkGo.get<String>("https://api.web.360kan.com/v1/rank")
                .params("cat", "1")
                .execute(object : AbsCallback<String?>() {
                    override fun onSuccess(response: Response<String?>) {
                        try {
                            val hots = ArrayList<Movie.Video>()
                            val itemList = JsonParser.parseString(response.body()).getAsJsonObject().get("data").getAsJsonArray()
                            for (ele: JsonElement in itemList) {
                                val obj = ele as JsonObject
                                val vod = Movie.Video()
                                vod.name = obj["title"].asString.trim { it <= ' ' }.replace("<|>|《|》|-".toRegex(), "").split(" ").toTypedArray()[0]
                                vod.pic = obj["cover"].asString
                                hots.add(vod)
                            }
                            if (!hots.isNullOrEmpty()) {
                                adapter.setNewData(hots)
                            }
                        } catch (th: Throwable) {
                            th.printStackTrace()
                        }
                    }

                    @Throws(Throwable::class)
                    override fun convertResponse(response: okhttp3.Response): String {
                        return response.body()!!.string()
                    }
                })
    }

    private fun loadIQYHot(adapter: GridAdapter) {
        // 加载热词
        OkGo.get<String>("https://pcw-api.iqiyi.com/strategy/pcw/data/topReboBlock")
                .params("cid", "-1")
                .params("dim", "hour")
                .params("type", "realTime")
                .params("len", "50")
                .params("pageNumber", "1")
                .params("QC005", "32d538fe89954eb39765c24331ddc1bd")
                .execute(object : AbsCallback<String?>() {
                    override fun onSuccess(response: Response<String?>) {
                        try {
                            val hots = ArrayList<Movie.Video>()
                            val itemList = JsonParser.parseString(response.body()).getAsJsonObject().get("data")?.asJsonObject?.get("formatData")?.asJsonObject?.get("list")?.asJsonArray
                            if (itemList != null) {
                                for (ele: JsonElement in itemList) {
                                    val obj = ele as JsonObject
                                    val vod = Movie.Video()
                                    vod.name = obj["name"].asString.trim { it <= ' ' }.replace("<|>|《|》|-".toRegex(), "").split(" ").toTypedArray()[0]
                                    vod.pic = obj["imageUrl"].asString
                                    vod.note = obj["score"].asString
                                    hots.add(vod)
                                }
                                if (!hots.isNullOrEmpty()) {
                                    adapter.setNewData(hots)
                                }
                            }
                        } catch (th: Throwable) {
                            th.printStackTrace()
                        }
                    }

                    @Throws(Throwable::class)
                    override fun convertResponse(response: okhttp3.Response): String {
                        return response.body()!!.string()
                    }
                })
    }
}