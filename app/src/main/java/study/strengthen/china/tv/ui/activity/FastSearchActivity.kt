package study.strengthen.china.tv.ui.activity

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.AbsCallback
import com.lzy.okgo.model.Response
import kotlinx.android.synthetic.main.layout_search_title.*
import me.zhouzhuo.zzsecondarylinkage.ZzSecondaryLinkage
import me.zhouzhuo.zzsecondarylinkage.model.ILinkage
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import study.strengthen.china.tv.R
import study.strengthen.china.tv.api.ApiConfig
import study.strengthen.china.tv.base.BaseActivity
import study.strengthen.china.tv.bean.AbsXml
import study.strengthen.china.tv.bean.Movie
import study.strengthen.china.tv.bean.SourceBean
import study.strengthen.china.tv.event.ServerEvent
import study.strengthen.china.tv.ui.adapter.LeftMenuListAdapter
import study.strengthen.china.tv.ui.adapter.RightContentListAdapter
import study.strengthen.china.tv.util.FastClickCheckUtil
import study.strengthen.china.tv.viewmodel.SourceViewModel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.ArrayList

/**
 * @author pj567
 * @date :2020/12/23
 * @description:
 */
class FastSearchActivity : BaseActivity() {
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
                rightAdapter.setList(mSearchRetList?.get(position)?.videoList)
            }

            override fun onRightClick(view: View?, position: Int) {
//                FastClickCheckUtil.check(view)
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
            performSearch()
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
//        EventBus.getDefault().register(this)
//        llLayout = findViewById(R.id.llLayout)
//        etSearch = findViewById(R.id.etSearch)
//        tvSearch = findViewById(R.id.tvSearch)
//        tvClear = findViewById(R.id.tvClear)
//        tvAddress = findViewById(R.id.tvAddress)
//        ivQRCode = findViewById(R.id.ivQRCode)
//        mGridView = findViewById(R.id.mGridView)
//        keyboard = findViewById(R.id.keyBoardRoot)
//        mGridViewWord = findViewById(R.id.mGridViewWord)
//        mGridViewWord.setHasFixedSize(true)
//        mGridViewWord.setLayoutManager(V7LinearLayoutManager(mContext, 1, false))
//        wordAdapter = PinyinAdapter()
//        mGridViewWord.setAdapter(wordAdapter)
//        wordAdapter!!.setOnItemClickListener(BaseQuickAdapter.OnItemClickListener { adapter, view, position -> search(wordAdapter!!.getItem(position)) })
//        mGridView.setHasFixedSize(true)
//        // lite
//        if (Hawk.get(HawkConfig.SEARCH_VIEW, 0) == 0) mGridView.setLayoutManager(V7LinearLayoutManager(mContext, 1, false)) else mGridView.setLayoutManager(V7GridLayoutManager(mContext, 3))
//        searchAdapter = SearchAdapter()
//        mGridView.setAdapter(searchAdapter)
//        searchAdapter!!.setOnItemClickListener(object : BaseQuickAdapter.OnItemClickListener {
//            override fun onItemClick(adapter: BaseQuickAdapter<*, *>?, view: View, position: Int) {
//                FastClickCheckUtil.check(view)
//                val video = searchAdapter!!.data[position]
//                if (video != null) {
//                    try {
//                        if (searchExecutorService != null) {
//                            pauseRunnable = searchExecutorService!!.shutdownNow()
//                            searchExecutorService = null
//                        }
//                    } catch (th: Throwable) {
//                        th.printStackTrace()
//                    }
//                    val bundle = Bundle()
//                    bundle.putString("id", video.id)
//                    bundle.putString("sourceKey", video.sourceKey)
//                    jumpActivity(DetailActivity::class.java, bundle)
//                }
//            }
//        })
//        tvSearch.setOnClickListener(object : View.OnClickListener {
//            override fun onClick(v: View) {
//                FastClickCheckUtil.check(v)
//                val wd = etSearch.getText().toString().trim { it <= ' ' }
//                if (!TextUtils.isEmpty(wd)) {
//                    search(wd)
//                } else {
//                    Toast.makeText(mContext, "输入内容不能为空", Toast.LENGTH_SHORT).show()
//                }
//            }
//        })
//        tvClear.setOnClickListener(object : View.OnClickListener {
//            override fun onClick(v: View) {
//                FastClickCheckUtil.check(v)
//                etSearch.setText("")
//            }
//        })
//        keyboard.setOnSearchKeyListener(object : OnSearchKeyListener {
//            override fun onSearchKey(pos: Int, key: String) {
//                if (pos > 1) {
//                    var text = etSearch.getText().toString().trim { it <= ' ' }
//                    text += key
//                    etSearch.setText(text)
//                    if (text.length > 0) {
//                        loadRec(text)
//                    }
//                } else if (pos == 1) {
//                    var text = etSearch.getText().toString().trim { it <= ' ' }
//                    if (text.length > 0) {
//                        text = text.substring(0, text.length - 1)
//                        etSearch.setText(text)
//                    }
//                    if (text.length > 0) {
//                        loadRec(text)
//                    }
//                } else if (pos == 0) {
//                    val remoteDialog = RemoteDialog(mContext)
//                    remoteDialog.show()
//                }
//            }
//        })
//        setLoadSir(llLayout)
    }

    private fun performSearch() {
        val str = et_search?.text?.toString()?:"".trim()
        if (!TextUtils.isEmpty(str)) {
            search(str)
        } else {
            Toast.makeText(mContext, "输入内容不能为空", Toast.LENGTH_SHORT).show()
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
        OkGo.get<String>("https://s.video.qq.com/smartbox")
                .params("plat", 2)
                .params("ver", 0)
                .params("num", 10)
                .params("otype", "json")
                .params("query", key)
                .execute(object : AbsCallback<String?>() {
                    override fun onSuccess(response: Response<String?>) {
                        try {
                            val hots = ArrayList<String>()
                            val result = response.body()
                            val json = JsonParser.parseString(result?.substring(result.indexOf("{"), result.lastIndexOf("}") + 1)).asJsonObject
                            val itemList = json["item"].asJsonArray
                            for (ele: JsonElement in itemList) {
                                val obj = ele as JsonObject
                                hots.add(obj["word"].asString.trim { it <= ' ' })
                            }
//                            wordAdapter!!.setNewData(hots)
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

    private fun initData() {
        val intent = intent
        if (intent != null && intent.hasExtra("title")) {
            val title = intent.getStringExtra("title")
            showLoading()
            search(title)
        }
        // 加载热词
        OkGo.get<String>("https://node.video.qq.com/x/api/hot_mobilesearch")
                .params("channdlId", "0")
                .params("_", System.currentTimeMillis())
                .execute(object : AbsCallback<String?>() {
                    override fun onSuccess(response: Response<String?>) {
                        try {
                            val hots = ArrayList<String>()
                            val itemList = JsonParser.parseString(response.body()).asJsonObject["data"].asJsonObject["itemList"].asJsonArray
                            for (ele: JsonElement in itemList) {
                                val obj = ele as JsonObject
                                hots.add(obj["title"].asString.trim { it <= ' ' }.replace("<|>|《|》|-".toRegex(), "").split(" ").toTypedArray()[0])
                            }
//                            wordAdapter!!.setNewData(hots)
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


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun server(event: ServerEvent) {
        if (event.type == ServerEvent.SERVER_SEARCH) {
            val title = event.obj as String
            showLoading()
            search(title)
        }
    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun refresh(event: RefreshEvent) {
//        if (event.type == RefreshEvent.TYPE_SEARCH_RESULT) {
//            try {
//                searchData(if (event.obj == null) null else event.obj as AbsXml)
//            } catch (e: Exception) {
//                searchData(null)
//            }
//        }
//    }

    private fun search(title: String?) {
        mSearchRetList?.clear()
        cancel()
        showLoading()
        searchTitle = title
//        mGridView!!.visibility = View.INVISIBLE
//        searchAdapter!!.setNewData(ArrayList())
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
//            searchAdapter!!.setNewData(ArrayList())
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
            zzLinkage?.updateData(mSearchRetList)
//            if (searchAdapter!!.data.size > 0) {
//                searchAdapter!!.addData(data)
//            } else {
//                showSuccess()
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
}