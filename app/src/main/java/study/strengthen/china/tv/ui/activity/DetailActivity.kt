package study.strengthen.china.tv.ui.activity

import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.util.Rational
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.AbsCallback
import com.owen.tvrecyclerview.widget.TvRecyclerView
import com.owen.tvrecyclerview.widget.TvRecyclerView.OnItemListener
import com.owen.tvrecyclerview.widget.V7GridLayoutManager
import com.owen.tvrecyclerview.widget.V7LinearLayoutManager
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_detail_phone.*
import kotlinx.android.synthetic.main.fragment_play_pop.*
import kotlinx.android.synthetic.main.play_video_title.*
import kotlinx.android.synthetic.main.pop_common_title.*
import okhttp3.Response
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import study.strengthen.china.tv.R
import study.strengthen.china.tv.api.ApiConfig
import study.strengthen.china.tv.base.BaseActivity
import study.strengthen.china.tv.bean.AbsXml
import study.strengthen.china.tv.bean.Movie
import study.strengthen.china.tv.bean.SourceBean
import study.strengthen.china.tv.bean.VodInfo
import study.strengthen.china.tv.cache.RoomDataManger
import study.strengthen.china.tv.event.RefreshEvent
import study.strengthen.china.tv.ui.adapter.SeriesAdapter
import study.strengthen.china.tv.ui.adapter.SeriesFlagAdapter
import study.strengthen.china.tv.util.DefaultConfig
import study.strengthen.china.tv.util.FastClickCheckUtil
import study.strengthen.china.tv.viewmodel.SourceViewModel
import java.net.URLEncoder
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * @author pj567
 * @date :2020/12/22
 * @description:
 */
class DetailActivity : BaseActivity() {
    private var llLayout: LinearLayout? = null
//    private var ivThumb: ImageView? = null
    private var tvName: TextView? = null
    private var tvYear: TextView? = null
    private var tvSite: TextView? = null
    private var tvArea: TextView? = null
    private var tvNote: TextView? = null
//    private var tvLang: TextView? = null
//    private var tvType: TextView? = null
    private var tvActor: TextView? = null
//    private var tvDirector: TextView? = null
//    private var tvDes: TextView? = null
//    private var tvPlay: TextView? = null
//    private var tvSort: TextView? = null
//    private var tvQuickSearch: TextView? = null
//    private var tvCollect: TextView? = null
    private var mGridViewFlag: TvRecyclerView? = null
    private var mGridView: TvRecyclerView? = null
    private var mEmptyPlayList: LinearLayout? = null
    private var sourceViewModel: SourceViewModel? = null
    private var mVideo: Movie.Video? = null
    var vodInfo: VodInfo? = null
    private var seriesFlagAdapter: SeriesFlagAdapter? = null
    private var seriesAdapter: SeriesAdapter? = null
    var vodId: String? = null
    var sourceKey: String? = null
    var seriesSelect = false
    private var seriesFlagFocus: View? = null
    var mPlayFragment : PlayFragment? = null

    override fun getLayoutResID(): Int {
        return R.layout.activity_detail_phone
    }

    override fun init() {
        EventBus.getDefault().register(this)
        initView()
        initViewModel()
        initData()
    }

    private fun initView() {
//        tabLayout?.apply {
//            addTab(newTab().setText("详情"))
//            addTab(newTab().setText("简介"))
//        }
//        pageAdapter = HomePageAdapter(supportFragmentManager, fragments)
//        mViewPager?.adapter = pageAdapter
//        mViewPager?.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
//        tabLayout?.setOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
//            override fun onTabSelected(tab: TabLayout.Tab?) {
//                tab?.position?.let {
//                    mViewPager?.currentItem = it
//                }
//            }
//
//            override fun onTabUnselected(tab: TabLayout.Tab?) {}
//
//            override fun onTabReselected(tab: TabLayout.Tab?) {}
//        })


        llLayout = findViewById(R.id.llLayout)
//        ivThumb = findViewById(R.id.ivThumb)
        tvName = findViewById(R.id.tvName)
        tvYear = findViewById(R.id.tvYear)
        tvSite = findViewById(R.id.tvSite)
        tvArea = findViewById(R.id.tvArea)
        tvNote = findViewById(R.id.tvNote)
//        tvLang = findViewById(R.id.tvLang)
//        tvType = findViewById(R.id.tvType)
        tvActor = findViewById(R.id.tvActor)
//        tvDirector = findViewById(R.id.tv_director)
//        tvDes = findViewById(R.id.tvDes)
//        tvPlay = findViewById(R.id.tvPlay)
//        tvSort = findViewById(R.id.tvSort)
//        tvCollect = findViewById(R.id.tvCollect)
//        tvQuickSearch = findViewById(R.id.tvQuickSearch)
        mEmptyPlayList = findViewById(R.id.mEmptyPlaylist)
        mGridView = findViewById(R.id.mGridView)
        mGridView?.setHasFixedSize(true)
        mGridView?.setLayoutManager(V7GridLayoutManager(mContext, if (isBaseOnWidth) 4 else 5))
        seriesAdapter = SeriesAdapter()
        mGridView?.setAdapter(seriesAdapter)
        mGridViewFlag = findViewById(R.id.mGridViewFlag)
        mGridViewFlag?.setHasFixedSize(true)
        mGridViewFlag?.setLayoutManager(V7LinearLayoutManager(mContext, 0, false))
        seriesFlagAdapter = SeriesFlagAdapter()
        mGridViewFlag?.setAdapter(seriesFlagAdapter)
        ivInfo?.setOnClickListener {
            playInfoPop?.visibility = View.VISIBLE
        }
        iv_back?.setOnClickListener {
            playInfoPop?.visibility = View.GONE
        }
        cardCollect?.setOnClickListener {
            val ret = RoomDataManger.insertVodCollect(sourceKey, vodInfo)
            if (ret != null) {
                RoomDataManger.deleteVodCollect(ret.getId())
                ivCollect?.setImageResource(R.drawable.collect)
            } else {
                //成功
                ivCollect?.setImageResource(R.drawable.is_collect)
            }

        }
//        tvSort.setOnClickListener(View.OnClickListener {
//            if (vodInfo != null && vodInfo!!.seriesMap.size > 0) {
//                vodInfo!!.reverseSort = !vodInfo!!.reverseSort
//                vodInfo!!.reverse()
//                insertVod(sourceKey, vodInfo)
//                seriesAdapter!!.notifyDataSetChanged()
//            }
//        })
//        tvPlay.setOnClickListener(View.OnClickListener { v ->
//            FastClickCheckUtil.check(v)
//            jumpToPlay()
//        })
//        tvQuickSearch.setOnClickListener(View.OnClickListener {
//            startQuickSearch()
//            val quickSearchDialog = QuickSearchDialog(this@DetailActivity)
//            EventBus.getDefault().post(RefreshEvent(RefreshEvent.TYPE_QUICK_SEARCH, quickSearchData))
//            EventBus.getDefault().post(RefreshEvent(RefreshEvent.TYPE_QUICK_SEARCH_WORD, quickSearchWord))
//            quickSearchDialog.show()
//            if (pauseRunnable != null && pauseRunnable!!.size > 0) {
//                searchExecutorService = Executors.newFixedThreadPool(5)
//                for (runnable in pauseRunnable!!) {
//                    searchExecutorService.execute(runnable)
//                }
//                pauseRunnable!!.clear()
//                pauseRunnable = null
//            }
//            quickSearchDialog.setOnDismissListener {
//                try {
//                    if (searchExecutorService != null) {
//                        pauseRunnable = searchExecutorService!!.shutdownNow()
//                        searchExecutorService = null
//                    }
//                } catch (th: Throwable) {
//                    th.printStackTrace()
//                }
//            }
//        })
//        tvCollect.setOnClickListener(View.OnClickListener {
//            RoomDataManger.insertVodCollect(sourceKey, vodInfo)
//            Toast.makeText(this@DetailActivity, "已加入收藏夹", Toast.LENGTH_SHORT).show()
//        })
        mGridView?.setOnItemListener(object : OnItemListener {
            override fun onItemPreSelected(parent: TvRecyclerView, itemView: View, position: Int) {
                seriesSelect = false
            }

            override fun onItemSelected(parent: TvRecyclerView, itemView: View, position: Int) {
                seriesSelect = true
            }

            override fun onItemClick(parent: TvRecyclerView, itemView: View, position: Int) {}
        })
        mGridViewFlag?.setOnItemListener(object : OnItemListener {
            private fun refresh(itemView: View, position: Int) {
                val newFlag = seriesFlagAdapter!!.data[position].name
                if (vodInfo != null && vodInfo?.playFlag != newFlag) {
                    for (i in vodInfo!!.seriesFlags.indices) {
                        val flag = vodInfo!!.seriesFlags[i]
                        if (flag.name == vodInfo!!.playFlag) {
                            flag.selected = false
                            seriesFlagAdapter!!.notifyItemChanged(i)
                            break
                        }
                    }
                    val flag = vodInfo!!.seriesFlags[position]
                    flag.selected = true
                    vodInfo!!.playFlag = newFlag
                    seriesFlagAdapter!!.notifyItemChanged(position)
                    refreshList()
                }
                seriesFlagFocus = itemView
            }

            override fun onItemPreSelected(parent: TvRecyclerView, itemView: View, position: Int) {}
            override fun onItemSelected(parent: TvRecyclerView, itemView: View, position: Int) {
                refresh(itemView, position)
            }

            override fun onItemClick(parent: TvRecyclerView, itemView: View, position: Int) {
                refresh(itemView, position)
            }
        })
        seriesAdapter?.setOnItemClickListener { adapter, view, position ->
//            FastClickCheckUtil.check(view)
            if (vodInfo != null && vodInfo!!.seriesMap[vodInfo!!.playFlag]!!.size > 0) {
                if (vodInfo!!.playIndex != position) {
                    seriesAdapter!!.data[vodInfo!!.playIndex].selected = false
                    seriesAdapter!!.notifyItemChanged(vodInfo!!.playIndex)
                    seriesAdapter!!.data[position].selected = true
                    seriesAdapter!!.notifyItemChanged(position)
                    vodInfo!!.playIndex = position
                }
                seriesAdapter!!.data[vodInfo!!.playIndex].selected = true
                seriesAdapter!!.notifyItemChanged(vodInfo!!.playIndex)
                jumpToPlay()
            }
        }
        setLoadSir(llLayout)
        mPlayFragment = PlayFragment().newInstance(this)
        supportFragmentManager.beginTransaction().add(R.id.playerContainer, mPlayFragment!!).commit()
        supportFragmentManager.beginTransaction().show(mPlayFragment!!).commitAllowingStateLoss()
    }

    private var pauseRunnable: MutableList<Runnable>? = null
    private fun jumpToPlay() {
        if (vodInfo != null && vodInfo!!.seriesMap[vodInfo!!.playFlag]!!.size > 0) {
            val bundle = Bundle()
            //保存历史
            insertVod(sourceKey, vodInfo)
            bundle.putString("sourceKey", sourceKey)
            bundle.putSerializable("VodInfo", vodInfo)
            mPlayFragment?.initData()
//            jumpActivity(PlayActivity::class.java, bundle)
        }
    }

    fun refreshList() {
        if (vodInfo == null) return
        if (vodInfo!!.seriesMap[vodInfo?.playFlag]!!.size <= vodInfo!!.playIndex) {
            vodInfo!!.playIndex = 0
        }
        if (vodInfo!!.seriesMap[vodInfo?.playFlag] != null) {
            vodInfo!!.seriesMap[vodInfo?.playFlag]!![vodInfo!!.playIndex].selected = true
        }
        seriesAdapter!!.setNewData(vodInfo!!.seriesMap[vodInfo?.playFlag])
        mGridView!!.postDelayed({ mGridView!!.scrollToPosition(vodInfo!!.playIndex) }, 100)
    }

    private fun setTextShow(view: TextView?, tag: String, info: String?) {
        if (info == null || info.trim { it <= ' ' }.isEmpty()) {
            view!!.visibility = View.GONE
            return
        }
        view!!.visibility = View.VISIBLE
        view.text = Html.fromHtml(getHtml(tag, info))
    }

    private fun removeHtmlTag(info: String?): String {
        return info?.replace("\\<.*?\\>".toRegex(), "")?.replace("\\s".toRegex(), "") ?: ""
    }

    private fun initViewModel() {
        sourceViewModel = ViewModelProvider(this).get(SourceViewModel::class.java)
        sourceViewModel!!.detailResult.observe(this, Observer { absXml ->
            if (absXml?.movie != null && absXml.movie.videoList != null && absXml.movie.videoList.size > 0) {
                showSuccess()
                mVideo = absXml.movie.videoList[0]
                vodInfo = VodInfo()
                vodInfo?.setVideo(mVideo)
                vodInfo?.sourceKey = mVideo?.sourceKey
                initInfoPop()
                tvName?.text = mVideo?.name
                setTextShow(tvSite, "来源：", ApiConfig.get().getSource(mVideo?.sourceKey).name)
                setTextShow(tvYear, "", if (mVideo?.year == 0) "" else mVideo?.year.toString())
                setTextShow(tvArea, "", mVideo?.area)
                setTextShow(tvNote, "", mVideo?.note)
//                setTextShow(tvLang, "语言：", mVideo?.lang)
//                setTextShow(tvType, "类型：", mVideo?.type)
                setTextShow(tvActor, "", mVideo?.actor)
//                setTextShow(tvDirector, "导演：", mVideo?.director)
//                setTextShow(tvDes, "内容简介：", removeHtmlTag(mVideo?.des))
                if (vodInfo?.seriesMap != null && vodInfo?.seriesMap?.size ?:0 > 0) {
                    mGridViewFlag!!.visibility = View.VISIBLE
                    mGridView!!.visibility = View.VISIBLE
//                    tvPlay!!.visibility = View.VISIBLE
                    mEmptyPlayList!!.visibility = View.GONE
                    val vodInfoRecord = RoomDataManger.getVodInfo(sourceKey, vodId)
                    // 读取历史记录
                    if (vodInfoRecord != null) {
                        vodInfo?.playIndex = Math.max(vodInfoRecord.playIndex, 0)
                        vodInfo?.playFlag = vodInfoRecord.playFlag
                        vodInfo?.playerCfg = vodInfoRecord.playerCfg
                        vodInfo?.reverseSort = vodInfoRecord.reverseSort
                    } else {
                        vodInfo?.playIndex = 0
                        vodInfo?.playFlag = null
                        vodInfo?.playerCfg = ""
                        vodInfo?.reverseSort = false
                    }
                    if (vodInfo != null && vodInfo!!.reverseSort) {
                        vodInfo?.reverse()
                    }
                    if (vodInfo?.playFlag == null || !vodInfo!!.seriesMap.containsKey(vodInfo!!.playFlag)) vodInfo!!.playFlag = vodInfo!!.seriesMap.keys.toTypedArray()[0] as String
                    var flagScrollTo = 0
                    for (j in vodInfo!!.seriesFlags.indices) {
                        val flag = vodInfo!!.seriesFlags[j]
                        if (flag.name == vodInfo!!.playFlag) {
                            flagScrollTo = j
                            flag.selected = true
                        } else flag.selected = false
                    }
                    seriesFlagAdapter!!.setNewData(vodInfo?.seriesFlags)
                    mGridViewFlag!!.scrollToPosition(flagScrollTo)
                    refreshList()
                    jumpToPlay()
                    // startQuickSearch();
                } else {
                    mGridViewFlag!!.visibility = View.GONE
                    mGridView!!.visibility = View.GONE
//                    tvPlay!!.visibility = View.GONE
                    mEmptyPlayList!!.visibility = View.VISIBLE
                }
            } else {
                showEmpty()
            }
        })
    }

    private fun getHtml(label: String, content: String): String {
        var content: String? = content
        if (content == null) {
            content = ""
        }
//        return "$label<font color=\"#FFFFFF\">$content</font>"
        return "$label$content"
    }

    private fun initData() {
        val intent = intent
        if (intent != null && intent.extras != null) {
            val bundle = intent.extras
            loadDetail(bundle!!.getString("id", null), bundle.getString("sourceKey", ""))
        }
    }

    private fun loadDetail(vid: String?, key: String) {
        if (vid != null) {
            vodId = vid
            sourceKey = key
            showLoading()
            sourceViewModel!!.getDetail(sourceKey, vodId)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun refresh(event: RefreshEvent) {
        if (event.type == RefreshEvent.TYPE_REFRESH) {
            if (event.obj != null) {
                if (event.obj is Int) {
                    val index = event.obj as Int
                    if (index != vodInfo!!.playIndex) {
                        seriesAdapter!!.data[vodInfo!!.playIndex].selected = false
                        seriesAdapter!!.notifyItemChanged(vodInfo!!.playIndex)
                        seriesAdapter!!.data[index].selected = true
                        seriesAdapter!!.notifyItemChanged(index)
                        mGridView!!.setSelection(index)
                        vodInfo?.playIndex = index
                        //保存历史
                        insertVod(sourceKey, vodInfo)
                    }
                } else if (event.obj is JSONObject) {
                    vodInfo?.playerCfg = (event.obj as JSONObject).toString()
                    //保存历史
                    insertVod(sourceKey, vodInfo)
                }
            }
        } else if (event.type == RefreshEvent.TYPE_QUICK_SEARCH_SELECT) {
            if (event.obj != null) {
                val video = event.obj as Movie.Video
                loadDetail(video.id, video.sourceKey)
            }
        } else if (event.type == RefreshEvent.TYPE_QUICK_SEARCH_WORD_CHANGE) {
            if (event.obj != null) {
                val word = event.obj as String
                switchSearchWord(word)
            }
        } else if (event.type == RefreshEvent.TYPE_QUICK_SEARCH_RESULT) {
            try {
                searchData(if (event.obj == null) null else event.obj as AbsXml)
            } catch (e: Exception) {
                searchData(null)
            }
        }
    }

    private var searchTitle = ""
    private var hadQuickStart = false
    private val quickSearchData: MutableList<Movie.Video> = ArrayList()
    private val quickSearchWord: MutableList<String> = ArrayList()
    private var searchExecutorService: ExecutorService? = null
    private fun switchSearchWord(word: String) {
        OkGo.getInstance().cancelTag("quick_search")
        quickSearchData.clear()
        searchTitle = word
        searchResult()
    }

    private fun startQuickSearch() {
        if (hadQuickStart) return
        hadQuickStart = true
        OkGo.getInstance().cancelTag("quick_search")
        quickSearchWord.clear()
        searchTitle = mVideo!!.name
        quickSearchData.clear()
        quickSearchWord.add(searchTitle)
        // 分词
        OkGo.get<String>("http://api.pullword.com/get.php?source=" + URLEncoder.encode(searchTitle) + "&param1=0&param2=0&json=1")
                .tag("fenci")
                .execute(object : AbsCallback<String?>() {
                    @Throws(Throwable::class)
                    override fun convertResponse(response: Response): String {
                        return if (response.body() != null) {
                            response.body()!!.string()
                        } else {
                            throw IllegalStateException("网络请求错误")
                        }
                    }

                    override fun onSuccess(response: com.lzy.okgo.model.Response<String?>) {
                        val json = response.body()
                        quickSearchWord.clear()
                        try {
                            for (je in Gson().fromJson(json, JsonArray::class.java)) {
                                quickSearchWord.add(je.asJsonObject["t"].asString)
                            }
                        } catch (th: Throwable) {
                            th.printStackTrace()
                        }
                        quickSearchWord.add(searchTitle)
                        EventBus.getDefault().post(RefreshEvent(RefreshEvent.TYPE_QUICK_SEARCH_WORD, quickSearchWord))
                    }

                    override fun onError(response: com.lzy.okgo.model.Response<String?>) {
                        super.onError(response)
                    }
                })
        searchResult()
    }

    private fun searchResult() {
        try {
            if (searchExecutorService != null) {
                searchExecutorService!!.shutdownNow()
                searchExecutorService = null
            }
        } catch (th: Throwable) {
            th.printStackTrace()
        }
        searchExecutorService = Executors.newFixedThreadPool(5)
        val searchRequestList: MutableList<SourceBean> = ArrayList()
        searchRequestList.addAll(ApiConfig.get().sourceBeanList)
        val home = ApiConfig.get().homeSourceBean
        searchRequestList.remove(home)
        searchRequestList.add(0, home)
        val siteKey = ArrayList<String>()
        for (bean in searchRequestList) {
            if (!bean.isSearchable || !bean.isQuickSearch) {
                continue
            }
            siteKey.add(bean.key)
        }
        for (key in siteKey) {
            searchExecutorService?.execute(Runnable { sourceViewModel!!.getQuickSearch(key, searchTitle) })
        }
    }

    private fun searchData(absXml: AbsXml?) {
        if (absXml?.movie != null && absXml.movie.videoList != null && absXml.movie.videoList.size > 0) {
            val data: MutableList<Movie.Video> = ArrayList()
            for (video in absXml.movie.videoList) {
                // 去除当前相同的影片
                if (video.sourceKey == sourceKey && video.id == vodId) continue
                data.add(video)
            }
            quickSearchData.addAll(data)
            EventBus.getDefault().post(RefreshEvent(RefreshEvent.TYPE_QUICK_SEARCH, data))
        }
    }

    private fun insertVod(sourceKey: String?, vodInfo: VodInfo?) {
        try {
            vodInfo!!.playNote = vodInfo.seriesMap[vodInfo.playFlag]!![vodInfo.playIndex].name
        } catch (th: Throwable) {
            vodInfo!!.playNote = ""
        }
        RoomDataManger.insertVodRecord(sourceKey, vodInfo)
        EventBus.getDefault().post(RefreshEvent(RefreshEvent.TYPE_HISTORY_REFRESH))
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (searchExecutorService != null) {
                searchExecutorService!!.shutdownNow()
                searchExecutorService = null
            }
        } catch (th: Throwable) {
            th.printStackTrace()
        }
        OkGo.getInstance().cancelTag("fenci")
        OkGo.getInstance().cancelTag("detail")
        OkGo.getInstance().cancelTag("quick_search")
        EventBus.getDefault().unregister(this)
    }

//    override fun onBackPressed() {
//        if (seriesSelect) {
//            if (seriesFlagFocus != null && !seriesFlagFocus!!.isFocused) {
//                seriesFlagFocus!!.requestFocus()
//                return
//            }
//        }
//        super.onBackPressed()
//    }

    private fun initInfoPop() {
        tv_name?.text = mVideo?.name
//        setTextShow(tvSite, "来源：", ApiConfig.get().getSource(mVideo.sourceKey).name)
        setTextShow(tv_vod_year, "", if (mVideo?.year == 0) "" else mVideo?.year.toString())
        setTextShow(tv_vod_area, "", mVideo?.area)
        setTextShow(tv_lang, "语言：", mVideo?.lang)
        setTextShow(tv_type, "类型：", mVideo?.type)
        setTextShow(tv_actor, "", mVideo?.actor)
        setTextShow(tv_director, "", mVideo?.director)
        setTextShow(tv_abstract, "", removeHtmlTag(mVideo?.des))
        setTextShow(tv_update_tag, "", mVideo?.note)

        if (!TextUtils.isEmpty(mVideo?.pic)) {
            Picasso.get()
                    .load(DefaultConfig.checkReplaceProxy(mVideo?.pic))
//                    .transform(RoundTransformation(MD5.string2MD5(mVideo?.pic + mVideo?.name))
//                            .centerCorp(true)
//                            .override(AutoSizeUtils.mm2px(mContext, 300f), AutoSizeUtils.mm2px(mContext, 400f))
//                            .roundRadius(AutoSizeUtils.mm2px(mContext, 10f), RoundTransformation.RoundType.ALL))
                    .placeholder(R.drawable.img_loading_placeholder)
                    .error(R.drawable.img_loading_placeholder)
                    .into(iv_thumb)
        } else {
            iv_thumb?.setImageResource(R.drawable.img_loading_placeholder)
        }
    }

    override fun initStatusBarColor() = Color.BLACK

    //判断是否可以进入画中画模式
    private fun isCanPipModel(): Boolean {
        return packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
    }

    fun enterPipModel() {
        if (!isCanPipModel()) {
            Toast.makeText(this, "无法进入PIP模式", Toast.LENGTH_SHORT).show();
            return
        }
        if (Build.VERSION.SDK_INT >= 26) {
            val builder = PictureInPictureParams.Builder()
            //设置Actions
            val pIntent = PendingIntent.getActivity(this,100,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT)
            val remoteAction = RemoteAction(Icon.createWithResource(this,
                    R.drawable.app_icon), "画中画","pip",pIntent)
            builder.setActions(arrayListOf(remoteAction))
            //设置宽高比例，第一个是分子，第二个是分母,指定宽高比，必须在 2.39:1或1:2.39 之间，否则会抛出IllegalArgumentException异常。
            val rational= Rational(16,9)
            builder.setAspectRatio(rational)

            //Android12下加入的画中画配置，对于非视频内容停用无缝大小调整
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            builder.setSeamlessResizeEnabled(false)
//            builder.setAutoEnterEnabled(true)
//        }

            enterPictureInPictureMode(builder.build())
        } else {

        }
    }

    override fun onPictureInPictureModeChanged(
            isInPictureInPictureMode: Boolean, configuration: Configuration?) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, configuration)
        if (isInPictureInPictureMode) {
            mPlayFragment?.mVideoView?.setVideoController(null)
        } else {
            mPlayFragment?.mVideoView?.setVideoController(mPlayFragment?.mController)
            mPlayFragment?.mVideoView?.requestLayout()
        }
    }

}