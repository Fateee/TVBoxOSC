package study.strengthen.china.tv.ui.activity

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.AbsCallback
import com.lzy.okgo.model.Response
import com.orhanobut.hawk.Hawk
import com.owen.tvrecyclerview.widget.V7GridLayoutManager
import kotlinx.android.synthetic.main.activity_fast_search.*
import kotlinx.android.synthetic.main.layout_search_title.*
import me.zhouzhuo.zzsecondarylinkage.ZzSecondaryLinkage
import me.zhouzhuo.zzsecondarylinkage.model.ILinkage
import org.greenrobot.eventbus.EventBus
import study.strengthen.china.tv.R
import study.strengthen.china.tv.api.ApiConfig
import study.strengthen.china.tv.base.BaseActivity
import study.strengthen.china.tv.bean.AbsXml
import study.strengthen.china.tv.bean.Movie
import study.strengthen.china.tv.bean.SourceBean
import study.strengthen.china.tv.ui.adapter.LeftMenuListAdapter
import study.strengthen.china.tv.ui.adapter.RightContentListAdapter
import study.strengthen.china.tv.ui.adapter.SearchHotWordAdapter
import study.strengthen.china.tv.util.DensityUtil
import study.strengthen.china.tv.viewmodel.SourceViewModel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author pj567
 * @date :2020/12/23
 * @description:
 */
class FastSearchActivity : BaseActivity() {
    private var mHistorySearch: java.util.ArrayList<String>? = null

    //    private var llLayout: LinearLayout? = null
//    private var mGridView: TvRecyclerView? = null
//    private var mGridViewWord: TvRecyclerView? = null
    var sourceViewModel: SourceViewModel? = null
//    private var etSearch: EditText? = null
//    private var tvSearch: TextView? = null
//    private var tvClear: TextView? = null
//    private var tvAddress: TextView? = null
//    private var ivQRCode: ImageView? = null
//    private var searchAdapter: SearchAdapter? = null
//    private var wordAdapter: PinyinAdapter? = null
    private var zzLinkage : ZzSecondaryLinkage<Movie>?=null
    private var searchTitle: String? = ""
//    private var mAllMovie : Movie? = null
    private var mSearchRetList: MutableList<Movie?>? = ArrayList()
    private var mHotWordAdapter : SearchHotWordAdapter? = null
//    private var mSearchRetList: MutableList<MutableList<Movie.Video>>? = ArrayList()
    override fun getLayoutResID(): Int {
        return R.layout.activity_fast_search
    }

    override fun init() {
        initView()
        initViewModel()
        initData()
    }

    private var pauseRunnable: MutableList<Runnable>? = null
    override fun onResume() {
        super.onResume()
//        if (pauseRunnable != null && pauseRunnable!!.size > 0) {
//            searchExecutorService = Executors.newFixedThreadPool(5)
//            allRunCount.set(pauseRunnable!!.size)
//            for (runnable: Runnable? in pauseRunnable!!) {
//                searchExecutorService.execute(runnable)
//            }
//            pauseRunnable!!.clear()
//            pauseRunnable = null
//        }
    }

    private fun initView() {
        iv_back?.setOnClickListener {
            finish()
        }

        zzLinkage = findViewById(R.id.listLinkage)
        zzLinkage?.setLeftMenuAdapter(LeftMenuListAdapter(this, mSearchRetList))
        val rightAdapter = RightContentListAdapter(this, ArrayList<Movie.Video>())
        zzLinkage?.setRightContentAdapter(rightAdapter)
        zzLinkage?.setOnItemClickListener(object : ILinkage.OnItemClickListener{
            override fun onLeftClick(itemView: View?, position: Int) {
//                if (itemView == null && position == 0 && rightAdapter.count != 0) return
                rightAdapter.setList(mSearchRetList?.get(position)?.videoList)
            }

            override fun onRightClick(view: View?, position: Int) {
                val video = rightAdapter.getItem(position)
                if (video != null) {
                    try {
                        if (searchExecutorService != null) {
                            pauseRunnable = searchExecutorService!!.shutdownNow()
                            searchExecutorService = null
                        }
                    } catch (th: Throwable) {
                        th.printStackTrace()
                    }
                    val bundle = Bundle()
                    bundle.putString("id", video.id)
                    bundle.putString("sourceKey", video.sourceKey)
                    jumpActivity(DetailActivity::class.java, bundle)
                }
            }
        })
        et_search?.setOnTextChangedCallback{paramEditable : Editable ->
            val str = et_search?.text?.toString()?:""
            if (str.isEmpty()) {
                searchBox?.visibility = View.GONE
                recordBox?.visibility = View.VISIBLE
            }
        }
        et_search?.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
        iv_search?.setOnClickListener {
            performSearch()
        }
        mHistorySearch = Hawk.get("search_history", ArrayList())
        fl_record?.removeAllViews()
        mHistorySearch?.forEach {
            addHistorySearchItemView(it,false)
        }
        record_clear?.setOnClickListener {
            fl_record?.removeAllViews()
            Hawk.delete("search_history")
        }
        mGridViewHot?.layoutManager = V7GridLayoutManager(this, 2)
        mHotWordAdapter = SearchHotWordAdapter()
        mGridViewHot?.adapter = mHotWordAdapter
        mHotWordAdapter?.setOnItemClickListener { adapter, view, position ->
            val value = adapter?.getItem(position)
            et_search?.setText(value as String)
            performSearch()
        }
        setLoadSir(searchBox)
    }

    private fun performSearch() {
        val str = et_search?.text?.toString()?:"".trim()
        if (!TextUtils.isEmpty(str)) {
            search(str)
            addHistory(str)
        } else {
            Toast.makeText(mContext, "请输入影片名", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initViewModel() {
        sourceViewModel = ViewModelProvider(this).get(SourceViewModel::class.java)
        sourceViewModel?.searchResult?.observe(this, androidx.lifecycle.Observer {
            searchData(it ?:null)
        })
    }

    /**
     * 拼音联想
     */
    private fun loadRec(key: String) {
//        OkGo.get<String>("https://s.video.qq.com/smartbox")
//                .params("plat", 2)
//                .params("ver", 0)
//                .params("num", 10)
//                .params("otype", "json")
//                .params("query", key)
//                .execute(object : AbsCallback<String?>() {
//                    override fun onSuccess(response: Response<String?>) {
//                        try {
//                            val hots = ArrayList<String>()
//                            val result = response.body()
//                            val json = JsonParser.parseString(result?.substring(result.indexOf("{"), result.lastIndexOf("}") + 1)).asJsonObject
//                            val itemList = json["item"].asJsonArray
//                            for (ele: JsonElement in itemList) {
//                                val obj = ele as JsonObject
//                                hots.add(obj["word"].asString.trim { it <= ' ' })
//                            }
////                            wordAdapter!!.setNewData(hots)
//                        } catch (th: Throwable) {
//                            th.printStackTrace()
//                        }
//                    }
//
//                    @Throws(Throwable::class)
//                    override fun convertResponse(response: okhttp3.Response): String {
//                        return response.body()!!.string()
//                    }
//                })
//        OkGo.get<String>("https://suggest.video.iqiyi.com/")
//                .params("if", "mobile")
//                .params("key", key)
//                .execute(object : AbsCallback<String?>() {
//                    override fun onSuccess(response: Response<String?>) {
//                        try {
//                            val hots: ArrayList<String> = ArrayList()
//                            val result = response.body()
//                            val json = JsonParser.parseString(result).asJsonObject
//                            val itemList: JsonArray = json["data"].asJsonArray
//                            for (ele in itemList) {
//                                val obj = ele as JsonObject
//                                hots.add(obj["name"].asString.trim { it <= ' ' }.replace("<|>|《|》|-".toRegex(), ""))
//                            }
////                            wordAdapter.setNewData(hots)
//                        } catch (th: Throwable) {
//                            th.printStackTrace()
//                        }
//                    }
//
//                    @Throws(Throwable::class)
//                    override fun convertResponse(response: okhttp3.Response): String {
//                        return response.body()!!.string()
//                    }
//                })
    }

    private fun initData() {
        val intent = intent
        if (intent != null && intent.hasExtra("title")) {
            val title = intent.getStringExtra("title")
//            showLoading()
            search(title)
            recordBox?.visibility = View.GONE
        } else {
            recordBox?.visibility = View.VISIBLE
        }
        // 加载热词
        OkGo.get<String>("https://node.video.qq.com/x/api/hot_search")
                .params("channdlId", "0")
                .params("_", System.currentTimeMillis())
                .execute(object : AbsCallback<String?>() {
                    override fun onSuccess(response: Response<String?>) {
                        try {
                            val hots = ArrayList<String>()
                            val itemList = JsonParser.parseString(response.body()).asJsonObject["data"].asJsonObject["mapResult"].asJsonObject.get("0").asJsonObject.get("listInfo").asJsonArray
                            for (ele: JsonElement in itemList) {
                                val obj = ele as JsonObject
                                hots.add(obj["title"].asString.trim { it <= ' ' }.replace("<|>|《|》|-".toRegex(), "").split(" ").toTypedArray()[0])
                            }
                            if (!hots.isNullOrEmpty()) {
                                mHotWordAdapter?.setNewData(hots)
                                hotBox?.visibility = View.VISIBLE
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

    private fun search(title: String?) {
        mSearchRetList?.clear()
        if (searchBox?.visibility == View.GONE) searchBox?.visibility = View.VISIBLE
        if (recordBox?.visibility == View.VISIBLE) recordBox?.visibility = View.GONE
        hideKeyboard()
        cancel()
        showLoading()
        searchTitle = title
        et_search?.setText(title)
        title?.length?.let { et_search?.setSelection(it) }
        searchResult()
    }

    private var searchExecutorService: ExecutorService? = null
    private val allRunCount = AtomicInteger(0)
    private fun searchResult() {
        try {
            if (searchExecutorService != null) {
                searchExecutorService!!.shutdownNow()
                searchExecutorService = null
            }
        } catch (th: Throwable) {
            th.printStackTrace()
        } finally {
            allRunCount.set(0)
        }
        searchExecutorService = Executors.newFixedThreadPool(5)
        val searchRequestList: MutableList<SourceBean> = ArrayList()
        searchRequestList.addAll(ApiConfig.get().sourceBeanList)
        val home = ApiConfig.get().homeSourceBean
        searchRequestList.remove(home)
        searchRequestList.add(0, home)
        val siteKey = ArrayList<String>()
        for (bean: SourceBean in searchRequestList) {
            if (!bean.isSearchable) {
                continue
            }
            siteKey.add(bean.key)
            allRunCount.incrementAndGet()
        }
        for (key: String in siteKey) {
            searchExecutorService?.execute(object : Runnable {
                override fun run() {
                    sourceViewModel!!.getSearch(key, searchTitle)
                }
            })
        }
    }

    private fun searchData(absXml: AbsXml?) {
        if ((absXml != null) && (absXml.movie != null) && (absXml.movie.videoList != null) && (absXml.movie.videoList.size > 0)) {
            val data: MutableList<Movie.Video> = ArrayList()
            for (video: Movie.Video in absXml.movie.videoList) {
                if (video.name.contains((searchTitle.toString()))) data.add(video)
            }
            mSearchRetList?.add(absXml.movie)
            showSuccess()
            zzLinkage?.updateData(mSearchRetList)
//            if (searchAdapter!!.data.size > 0) {
//                searchAdapter!!.addData(data)
//            } else {
//                mGridView!!.visibility = View.VISIBLE
//                searchAdapter!!.setNewData(data)
//            }
        }
        val count = allRunCount.decrementAndGet()
        if (count <= 0) {
//            if (searchAdapter!!.data.size <= 0) {
//                showEmpty()
//            }
            cancel()
        }
    }

    private fun cancel() {
        OkGo.getInstance().cancelTag("search")
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
        try {
            if (searchExecutorService != null) {
                searchExecutorService!!.shutdownNow()
                searchExecutorService = null
            }
        } catch (th: Throwable) {
            th.printStackTrace()
        }
        EventBus.getDefault().unregister(this)
    }

    private fun addHistorySearchItemView(paramString: String?, paramBoolean: Boolean) {
        if (searchRecordBox?.visibility == View.GONE) searchRecordBox?.visibility = View.VISIBLE

        val marginLayoutParams: ViewGroup.MarginLayoutParams = ViewGroup.MarginLayoutParams(-2, DensityUtil.dip2px(38.0f))
        marginLayoutParams.setMargins(DensityUtil.dip2px(10.0f), 0, DensityUtil.dip2px(10.0f), 0)
        val textView = TextView(this)
        textView.setPadding(DensityUtil.dip2px(15.0f), 0, DensityUtil.dip2px(15.0f), 0)
        textView.setTextColor(resources.getColor(R.color.text_gray))
        textView.setTextSize(2, 18.0f)
        textView.setText(paramString)
        textView.tag = paramString
        textView.setGravity(16)
        textView.setLines(1)
        textView.setBackgroundResource(R.drawable.search_record_tag_bg)
        textView.setOnClickListener {
            et_search?.setText(it.tag as String)
            performSearch()
        }
//        textView.setOnLongClickListener(gj0(this, paramString, textView) as View.OnLongClickListener?)
        if (paramBoolean) {
            fl_record?.addView(textView as View, 0, marginLayoutParams as ViewGroup.LayoutParams)
        } else {
            fl_record?.addView(textView as View, marginLayoutParams as ViewGroup.LayoutParams)
        }
    }

    private fun addHistory(paramString : String) {
        if (!TextUtils.isEmpty(paramString) && mHistorySearch?.contains(paramString) == false) {
            mHistorySearch?.add(0, paramString);
            if (mHistorySearch?.size?:0 > 30)
                mHistorySearch?.removeAt(30);
            Hawk.put("search_history", mHistorySearch)
            addHistorySearchItemView(paramString, true);
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(et_search?.windowToken, 0)
    }
}