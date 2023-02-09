package study.strengthen.china.tv.ui.activity

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.net.http.SslError
import android.os.Build
import android.os.Handler
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.AbsCallback
import com.lzy.okgo.model.HttpHeaders
import com.orhanobut.hawk.Hawk
import me.jessyan.autosize.AutoSize
import okhttp3.Response
import org.greenrobot.eventbus.EventBus
import org.json.JSONException
import org.json.JSONObject
import org.xwalk.core.*
import study.strengthen.china.tv.R
import study.strengthen.china.tv.api.ApiConfig
import study.strengthen.china.tv.base.BaseLazyFragment
import study.strengthen.china.tv.bean.ParseBean
import study.strengthen.china.tv.bean.SourceBean
import study.strengthen.china.tv.bean.VodInfo
import study.strengthen.china.tv.cache.CacheManager
import study.strengthen.china.tv.event.RefreshEvent
import study.strengthen.china.tv.player.controller.VodController
import study.strengthen.china.tv.player.controller.VodController.VodControlListener
import study.strengthen.china.tv.player.thirdparty.MXPlayer
import study.strengthen.china.tv.player.thirdparty.ReexPlayer
import study.strengthen.china.tv.util.*
import study.strengthen.china.tv.util.XWalkUtils.XWalkState
import study.strengthen.china.tv.util.thunder.Thunder
import study.strengthen.china.tv.util.thunder.Thunder.ThunderCallback
import study.strengthen.china.tv.viewmodel.SourceViewModel
import xyz.doikki.videoplayer.player.ProgressManager
import xyz.doikki.videoplayer.player.VideoView
import java.io.ByteArrayInputStream
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class PlayFragment : BaseLazyFragment() {
    private var mVideoView: VideoView<*>? = null
    private var mPlayLoadTip: TextView? = null
    private var mPlayLoadErr: View? = null
    private var mPlayLoading: ProgressBar? = null
    private var mController: VodController? = null
    private var sourceViewModel: SourceViewModel? = null
    private var mDetailActivity : DetailActivity? = null
    private var mHandler = Handler(Handler.Callback { msg ->
        when (msg.what) {
            100 -> {
                stopParse()
                errorWithRetry("嗅探错误", false)
            }
        }
        false
    })

    fun newInstance(detailActivity : DetailActivity?): PlayFragment {
        this.mDetailActivity = detailActivity
        return this
    }

    override fun getLayoutResID(): Int {
        return R.layout.activity_play
    }

    override fun init() {
        initView()
        initViewModel()
        initData()
    }

    private fun initView() {
        mVideoView = findViewById(R.id.mVideoView)
        mPlayLoadTip = findViewById(R.id.play_load_tip)
        mPlayLoading = findViewById(R.id.play_loading)
        mPlayLoadErr = findViewById(R.id.play_load_error)
        mController = VodController(requireContext())
        mController!!.setCanChangePosition(true)
        mController!!.setEnableInNormal(true)
        mController!!.setGestureEnabled(true)
        val progressManager: ProgressManager = object : ProgressManager() {
            override fun saveProgress(url: String, progress: Long) {
                CacheManager.save(MD5.string2MD5(url), progress)
            }

            override fun getSavedProgress(url: String): Long {
                var st = 0
                try {
                    st = mVodPlayerCfg!!.getInt("st")
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                val skip = st * 1000.toLong()
                if (CacheManager.getCache(MD5.string2MD5(url)) == null) {
                    return skip
                }
                val rec = CacheManager.getCache(MD5.string2MD5(url)) as Long
                return if (rec < skip) skip else rec
            }
        }
        mVideoView?.setProgressManager(progressManager)
        mController!!.setListener(object : VodControlListener {
            override fun playNext(rmProgress: Boolean) {
                val preProgressKey = progressKey
                this@PlayFragment.playNext()
                if (rmProgress && preProgressKey != null) CacheManager.delete(MD5.string2MD5(preProgressKey), 0)
            }

            override fun playPre() {
                playPrevious()
            }

            override fun changeParse(pb: ParseBean?) {
                autoRetryCount = 0
                doParse(pb)
            }

            override fun updatePlayerCfg() {
                mVodInfo!!.playerCfg = mVodPlayerCfg.toString()
                EventBus.getDefault().post(RefreshEvent(RefreshEvent.TYPE_REFRESH, mVodPlayerCfg))
            }

            override fun replay() {
                autoRetryCount = 0
                play()
            }

            override fun errReplay() {
                errorWithRetry("视频播放出错", false)
            }
        })
        mVideoView?.setVideoController(mController)
    }

    fun setTip(msg: String?, loading: Boolean, err: Boolean) {
        mPlayLoadTip!!.text = msg
        mPlayLoadTip!!.visibility = View.VISIBLE
        mPlayLoading!!.visibility = if (loading) View.VISIBLE else View.GONE
        mPlayLoadErr!!.visibility = if (err) View.VISIBLE else View.GONE
    }

    fun hideTip() {
        mPlayLoadTip!!.visibility = View.GONE
        mPlayLoading!!.visibility = View.GONE
        mPlayLoadErr!!.visibility = View.GONE
    }

    fun errorWithRetry(err: String?, finish: Boolean) {
        if (!autoRetry()) {
            activity?.runOnUiThread {
                if (finish) {
                    Toast.makeText(mContext, err, Toast.LENGTH_SHORT).show()
//                    finish()
                } else {
                    setTip(err, false, true)
                }
            }
        }
    }

    fun playUrl(url: String?, headers: HashMap<String?, String?>?) {
        activity?.runOnUiThread(Runnable {
            stopParse()
            if (mVideoView != null) {
                mVideoView!!.release()
                if (url != null) {
                    try {
                        val playerType = mVodPlayerCfg!!.getInt("pl")
                        if (playerType >= 10) {
                            val vs = mVodInfo!!.seriesMap[mVodInfo!!.playFlag]!![mVodInfo!!.playIndex]
                            val playTitle = mVodInfo!!.name + " " + vs.name
                            setTip("调用外部播放器" + PlayerHelper.getPlayerName(playerType) + "进行播放", true, false)
                            var callResult = false
                            when (playerType) {
                                10 -> {
                                    callResult = MXPlayer.run(activity, url, playTitle, playSubtitle, headers)
                                }
                                11 -> {
                                    callResult = ReexPlayer.run(activity, url, playTitle, playSubtitle, headers)
                                }
                            }
                            setTip("调用外部播放器" + PlayerHelper.getPlayerName(playerType) + if (callResult) "成功" else "失败", callResult, !callResult)
                            return@Runnable
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    hideTip()
                    PlayerHelper.updateCfg(mVideoView, mVodPlayerCfg)
                    mVideoView!!.setProgressKey(progressKey)
                    if (headers != null) {
                        mVideoView!!.setUrl(url, headers)
                    } else {
                        mVideoView!!.setUrl(url)
                    }
                    mVideoView!!.start()
                    mController!!.resetSpeed()
                }
            }
        })
    }

    private fun initViewModel() {
        sourceViewModel = ViewModelProvider(this).get(SourceViewModel::class.java)
        sourceViewModel!!.playResult.observe(this, Observer { info ->
            if (info != null) {
                try {
                    progressKey = info.optString("proKey", null)
                    val parse = info.optString("parse", "1") == "1"
                    val jx = info.optString("jx", "0") == "1"
                    playSubtitle = info.optString("subt",  /*"https://dash.akamaized.net/akamai/test/caption_test/ElephantsDream/ElephantsDream_en.vtt"*/
                            "")
                    val playUrl = info.optString("playUrl", "")
                    val flag = info.optString("flag")
                    val url = info.getString("url")
                    var headers: HashMap<String?, String?>? = null
                    if (info.has("header")) {
                        try {
                            val hds = JSONObject(info.getString("header"))
                            val keys = hds.keys()
                            while (keys.hasNext()) {
                                val key = keys.next()
                                if (headers == null) {
                                    headers = HashMap()
                                }
                                headers[key] = hds.getString(key)
                            }
                        } catch (th: Throwable) {
                        }
                    }
                    if (parse || jx) {
                        val userJxList = playUrl.isEmpty() && ApiConfig.get().vipParseFlags.contains(flag) || jx
                        initParse(flag, userJxList, playUrl, url)
                    } else {
                        mController!!.showParse(false)
                        playUrl(playUrl + url, headers)
                    }
                } catch (th: Throwable) {
                    errorWithRetry("获取播放信息错误", true)
                }
            } else {
                errorWithRetry("获取播放信息错误", true)
            }
        })
    }

    fun initData() {
//        val intent = intent
//        if (intent != null && intent.extras != null) {
//            val bundle = intent.extras
//            mVodInfo = bundle!!.getSerializable("VodInfo") as VodInfo?
//            sourceKey = bundle.getString("sourceKey")
//            sourceBean = ApiConfig.get().getSource(sourceKey)
//            initPlayerCfg()
//            play()
//        }
        Log.e("huyi","playfragment initData")
        if (mDetailActivity?.vodInfo != null && !mDetailActivity?.sourceKey.isNullOrEmpty()) {
            Log.e("huyi","playfragment initData.........")
            mVodInfo = mDetailActivity?.vodInfo
            sourceKey = mDetailActivity?.sourceKey
            sourceBean = ApiConfig.get().getSource(sourceKey)
            initPlayerCfg()
            play()
        }
    }

    fun initPlayerCfg() {
        mVodPlayerCfg = try {
            JSONObject(mVodInfo!!.playerCfg)
        } catch (th: Throwable) {
            JSONObject()
        }
        try {
            if (!mVodPlayerCfg!!.has("pl")) {
                mVodPlayerCfg!!.put("pl", Hawk.get(HawkConfig.PLAY_TYPE, 1))
            }
            if (!mVodPlayerCfg!!.has("pr")) {
                mVodPlayerCfg!!.put("pr", Hawk.get(HawkConfig.PLAY_RENDER, 0))
            }
            if (!mVodPlayerCfg!!.has("ijk")) {
                mVodPlayerCfg!!.put("ijk", Hawk.get(HawkConfig.IJK_CODEC, ""))
            }
            if (!mVodPlayerCfg!!.has("sc")) {
                mVodPlayerCfg!!.put("sc", Hawk.get(HawkConfig.PLAY_SCALE, 0))
            }
            if (!mVodPlayerCfg!!.has("sp")) {
                mVodPlayerCfg!!.put("sp", 1.0)
            }
            if (!mVodPlayerCfg!!.has("st")) {
                mVodPlayerCfg!!.put("st", 0)
            }
            if (!mVodPlayerCfg!!.has("et")) {
                mVodPlayerCfg!!.put("et", 0)
            }
        } catch (th: Throwable) {
        }
        mController!!.setPlayerConfig(mVodPlayerCfg)
    }

//    override fun onBackPressed() {
//        if (mController!!.onBackPressed()) {
//            return
//        }
//        super.onBackPressed()
//    }
//
//    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
//        if (event != null) {
//            if (mController!!.onKeyEvent(event)) {
//                return true
//            }
//        }
//        return super.dispatchKeyEvent(event)
//    }

    override fun onResume() {
        super.onResume()
//        if (mVideoView != null) {
//            mVideoView!!.resume()
//        }
    }

    override fun onPause() {
        super.onPause()
//        if (mVideoView != null) {
//            mVideoView!!.pause()
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mVideoView != null) {
            mVideoView!!.release()
            mVideoView = null
        }
        stopLoadWebView(true)
        stopParse()
        mController?.clearHandlerMsgCallback()
    }

    private var mVodInfo: VodInfo? = null
    private var mVodPlayerCfg: JSONObject? = null
    private var sourceKey: String? = null
    private var sourceBean: SourceBean? = null
    private fun playNext() {
        var hasNext = true
        hasNext = if (mVodInfo == null || mVodInfo!!.seriesMap[mVodInfo!!.playFlag] == null) {
            false
        } else {
            mVodInfo!!.playIndex + 1 < mVodInfo!!.seriesMap[mVodInfo!!.playFlag]!!.size
        }
        if (!hasNext) {
            Toast.makeText(mContext, "已经是最后一集了!", Toast.LENGTH_SHORT).show()
            return
        }
        mVodInfo!!.playIndex++
        play()
    }

    private fun playPrevious() {
        var hasPre = true
        hasPre = if (mVodInfo == null || mVodInfo!!.seriesMap[mVodInfo!!.playFlag] == null) {
            false
        } else {
            mVodInfo!!.playIndex - 1 >= 0
        }
        if (!hasPre) {
            Toast.makeText(mContext, "已经是第一集了!", Toast.LENGTH_SHORT).show()
            return
        }
        mVodInfo!!.playIndex--
        play()
    }

    private var autoRetryCount = 0
    fun autoRetry(): Boolean {
        return if (autoRetryCount < 3) {
            autoRetryCount++
            play()
            true
        } else {
            autoRetryCount = 0
            false
        }
    }

    fun play() {
        if (mVodInfo == null) return
        val vs = mVodInfo!!.seriesMap[mVodInfo!!.playFlag]!![mVodInfo!!.playIndex]
        EventBus.getDefault().post(RefreshEvent(RefreshEvent.TYPE_REFRESH, mVodInfo!!.playIndex))
        setTip("正在获取播放信息", true, false)
        val playTitleInfo = mVodInfo!!.name + " " + vs.name
        mController!!.setTitle(playTitleInfo)
        playUrl(null, null)
        val progressKey = mVodInfo!!.sourceKey + mVodInfo!!.id + mVodInfo!!.playFlag + mVodInfo!!.playIndex
        if (Thunder.play(vs.url, object : ThunderCallback {
                    override fun status(code: Int, info: String) {
                        if (code < 0) {
                            setTip(info, false, true)
                        } else {
                            setTip(info, true, false)
                        }
                    }

                    override fun list(playList: String) {}
                    override fun play(url: String) {
                        playUrl(url, null)
                    }
                })) {
            mController!!.showParse(false)
            return
        }
        sourceViewModel!!.getPlay(sourceKey, mVodInfo!!.playFlag, progressKey, vs.url)
    }

    private var playSubtitle: String? = null
    private var progressKey: String? = null
    private var parseFlag: String? = null
    private var webUrl: String? = null
    private fun initParse(flag: String, useParse: Boolean, playUrl: String, url: String) {
        parseFlag = flag
        webUrl = url
        var parseBean: ParseBean? = null
        mController!!.showParse(useParse)
        if (useParse) {
            parseBean = ApiConfig.get().defaultParse
        } else {
            if (playUrl.startsWith("json:")) {
                parseBean = ParseBean()
                parseBean.type = 1
                parseBean.url = playUrl.substring(5)
            } else if (playUrl.startsWith("parse:")) {
                val parseRedirect = playUrl.substring(6)
                for (pb in ApiConfig.get().parseBeanList) {
                    if (pb.name == parseRedirect) {
                        parseBean = pb
                        break
                    }
                }
            }
            if (parseBean == null) {
                parseBean = ParseBean()
                parseBean.type = 0
                parseBean.url = playUrl
            }
        }
        loadFound = false
        doParse(parseBean)
    }

    @Throws(JSONException::class)
    fun jsonParse(input: String?, json: String?): JSONObject? {
        val jsonPlayData = JSONObject(json)
        var url = jsonPlayData.getString("url")
        val msg = jsonPlayData.optString("msg", "")
        if (url.startsWith("//")) {
            url = "https:$url"
        }
        if (!url.startsWith("http")) {
            return null
        }
        val headers = JSONObject()
        val ua = jsonPlayData.optString("user-agent", "")
        if (ua.trim { it <= ' ' }.length > 0) {
            headers.put("User-Agent", " $ua")
        }
        val referer = jsonPlayData.optString("referer", "")
        if (referer.trim { it <= ' ' }.length > 0) {
            headers.put("Referer", " $referer")
        }
        val taskResult = JSONObject()
        taskResult.put("header", headers)
        taskResult.put("url", url)
        return taskResult
    }

    fun stopParse() {
        mHandler.removeMessages(100)
        stopLoadWebView(false)
        loadFound = false
        OkGo.getInstance().cancelTag("json_jx")
        if (parseThreadPool != null) {
            try {
                parseThreadPool!!.shutdown()
                parseThreadPool = null
            } catch (th: Throwable) {
                th.printStackTrace()
            }
        }
    }

    var parseThreadPool: ExecutorService? = null
    private fun doParse(pb: ParseBean?) {
        stopParse()
        if (pb!!.type == 0) {
            setTip("正在嗅探播放地址", true, false)
            mHandler.removeMessages(100)
            mHandler.sendEmptyMessageDelayed(100, 20 * 1000.toLong())
            loadWebView(pb.url + webUrl)
        } else if (pb.type == 1) { // json 解析
            setTip("正在解析播放地址", true, false)
            // 解析ext
            val reqHeaders = HttpHeaders()
            try {
                val jsonObject = JSONObject(pb.ext)
                if (jsonObject.has("header")) {
                    val headerJson = jsonObject.optJSONObject("header")
                    val keys = headerJson.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        reqHeaders.put(key, headerJson.optString(key, ""))
                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            OkGo.get<String>(pb.url + webUrl)
                    .tag("json_jx")
                    .headers(reqHeaders)
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
                            try {
                                val rs = jsonParse(webUrl, json)
                                var headers: HashMap<String?, String?>? = null
                                if (rs!!.has("header")) {
                                    try {
                                        val hds = rs.getJSONObject("header")
                                        val keys = hds.keys()
                                        while (keys.hasNext()) {
                                            val key = keys.next()
                                            if (headers == null) {
                                                headers = HashMap()
                                            }
                                            headers[key] = hds.getString(key)
                                        }
                                    } catch (th: Throwable) {
                                    }
                                }
                                playUrl(rs.getString("url"), headers)
                            } catch (e: Throwable) {
                                e.printStackTrace()
                                errorWithRetry("解析错误", false)
                            }
                        }

                        override fun onError(response: com.lzy.okgo.model.Response<String?>) {
                            super.onError(response)
                            errorWithRetry("解析错误", false)
                        }
                    })
        } else if (pb.type == 2) { // json 扩展
            setTip("正在解析播放地址", true, false)
            parseThreadPool = Executors.newSingleThreadExecutor()
            val jxs = LinkedHashMap<String, String>()
            for (p in ApiConfig.get().parseBeanList) {
                if (p.type == 1) {
                    jxs[p.name] = p.mixUrl()
                }
            }
            parseThreadPool?.execute(Runnable {
                val rs = ApiConfig.get().jsonExt(pb.url, jxs, webUrl)
                if (rs == null || !rs.has("url")) {
                    errorWithRetry("解析错误", false)
                } else {
                    var headers: HashMap<String?, String?>? = null
                    if (rs.has("header")) {
                        try {
                            val hds = rs.getJSONObject("header")
                            val keys = hds.keys()
                            while (keys.hasNext()) {
                                val key = keys.next()
                                if (headers == null) {
                                    headers = HashMap()
                                }
                                headers!![key] = hds.getString(key)
                            }
                        } catch (th: Throwable) {
                        }
                    }
                    if (rs.has("jxFrom")) {
                        activity?.runOnUiThread { Toast.makeText(mContext, "解析来自:" + rs.optString("jxFrom"), Toast.LENGTH_SHORT).show() }
                    }
                    val parseWV = rs.optInt("parse", 0) == 1
                    if (parseWV) {
                        val wvUrl = DefaultConfig.checkReplaceProxy(rs.optString("url", ""))
                        loadUrl(wvUrl)
                    } else {
                        playUrl(rs.optString("url", ""), headers)
                    }
                }
            })
        } else if (pb.type == 3) { // json 聚合
            setTip("正在解析播放地址", true, false)
            parseThreadPool = Executors.newSingleThreadExecutor()
            val jxs = LinkedHashMap<String, HashMap<String, String>>()
            var extendName = ""
            for (p in ApiConfig.get().parseBeanList) {
                val data = HashMap<String, String>()
                data["url"] = p.url
                if (p.url == pb.url) {
                    extendName = p.name
                }
                data["type"] = p.type.toString() + ""
                data["ext"] = p.ext
                jxs[p.name] = data
            }
            val finalExtendName = extendName
            parseThreadPool?.execute(Runnable {
                val rs = ApiConfig.get().jsonExtMix(parseFlag + "111", pb.url, finalExtendName, jxs, webUrl)
                if (rs == null || !rs.has("url")) {
                    errorWithRetry("解析错误", false)
                } else {
                    if (rs.has("parse") && rs.optInt("parse", 0) == 1) {
                        activity?.runOnUiThread {
                            val mixParseUrl = DefaultConfig.checkReplaceProxy(rs.optString("url", ""))
                            stopParse()
                            setTip("正在嗅探播放地址", true, false)
                            mHandler.removeMessages(100)
                            mHandler.sendEmptyMessageDelayed(100, 20 * 1000.toLong())
                            loadWebView(mixParseUrl)
                        }
                    } else {
                        var headers: HashMap<String?, String?>? = null
                        if (rs.has("header")) {
                            try {
                                val hds = rs.getJSONObject("header")
                                val keys = hds.keys()
                                while (keys.hasNext()) {
                                    val key = keys.next()
                                    if (headers == null) {
                                        headers = HashMap()
                                    }
                                    headers!![key] = hds.getString(key)
                                }
                            } catch (th: Throwable) {
                            }
                        }
                        if (rs.has("jxFrom")) {
                            activity?.runOnUiThread { Toast.makeText(mContext, "解析来自:" + rs.optString("jxFrom"), Toast.LENGTH_SHORT).show() }
                        }
                        playUrl(rs.optString("url", ""), headers)
                    }
                }
            })
        }
    }

    // webview
    private var mXwalkWebView: XWalkView? = null
    private var mX5WebClient: XWalkWebClient? = null
    private var mSysWebView: WebView? = null
    private var mSysWebClient: SysWebClient? = null
    private val loadedUrls: MutableMap<String, Boolean?> = HashMap()
    private var loadFound = false
    fun loadWebView(url: String?) {
        if (mSysWebView == null && mXwalkWebView == null) {
            val useSystemWebView = Hawk.get(HawkConfig.PARSE_WEBVIEW, true)
            if (!useSystemWebView) {
                XWalkUtils.tryUseXWalk(mContext, object : XWalkState {
                    override fun success() {
                        initWebView(false)
                        loadUrl(url)
                    }

                    override fun fail() {
                        Toast.makeText(mContext, "XWalkView不兼容，已替换为系统自带WebView", Toast.LENGTH_SHORT).show()
                        initWebView(true)
                        loadUrl(url)
                    }

                    override fun ignore() {
                        Toast.makeText(mContext, "XWalkView运行组件未下载，已替换为系统自带WebView", Toast.LENGTH_SHORT).show()
                        initWebView(true)
                        loadUrl(url)
                    }
                })
            } else {
                initWebView(true)
                loadUrl(url)
            }
        } else {
            loadUrl(url)
        }
    }

    fun initWebView(useSystemWebView: Boolean) {
        if (useSystemWebView) {
            mSysWebView = MyWebView(mContext)
            configWebViewSys(mSysWebView)
        } else {
            mXwalkWebView = MyXWalkView(mContext)
            configWebViewX5(mXwalkWebView)
        }
    }

    fun loadUrl(url: String?) {
        activity?.runOnUiThread {
            if (mXwalkWebView != null) {
                mXwalkWebView!!.stopLoading()
                mXwalkWebView!!.clearCache(true)
                mXwalkWebView!!.loadUrl(url)
            }
            if (mSysWebView != null) {
                mSysWebView!!.stopLoading()
                mSysWebView!!.clearCache(true)
                mSysWebView!!.loadUrl(url)
            }
        }
    }

    fun stopLoadWebView(destroy: Boolean) {
        activity?.runOnUiThread {
            if (mXwalkWebView != null) {
                mXwalkWebView!!.stopLoading()
                mXwalkWebView!!.loadUrl("about:blank")
                if (destroy) {
                    mXwalkWebView!!.clearCache(true)
                    mXwalkWebView!!.removeAllViews()
                    mXwalkWebView!!.onDestroy()
                    mXwalkWebView = null
                }
            }
            if (mSysWebView != null) {
                mSysWebView!!.stopLoading()
                mSysWebView!!.loadUrl("about:blank")
                if (destroy) {
                    mSysWebView!!.clearCache(true)
                    mSysWebView!!.removeAllViews()
                    mSysWebView!!.destroy()
                    mSysWebView = null
                }
            }
        }
    }

    fun checkVideoFormat(url: String?): Boolean {
        if (sourceBean!!.type == 3) {
            val sp = ApiConfig.get().getCSP(sourceBean)
            if (sp != null && sp.manualVideoCheck()) return sp.isVideoFormat(url)
        }
        return DefaultConfig.isVideoFormat(url)
    }

    internal inner class MyWebView(context: Context) : WebView(context) {
        override fun setOverScrollMode(mode: Int) {
            super.setOverScrollMode(mode)
            if (mContext is Activity) AutoSize.autoConvertDensityOfCustomAdapt(mContext as Activity, this@PlayFragment)
        }

        override fun dispatchKeyEvent(event: KeyEvent): Boolean {
            return false
        }
    }

    internal inner class MyXWalkView(context: Context?) : XWalkView(context) {
        override fun setOverScrollMode(mode: Int) {
            super.setOverScrollMode(mode)
            if (mContext is Activity) AutoSize.autoConvertDensityOfCustomAdapt(mContext as Activity, this@PlayFragment)
        }

        override fun dispatchKeyEvent(event: KeyEvent): Boolean {
            return false
        }
    }

    private fun configWebViewSys(webView: WebView?) {
        if (webView == null) {
            return
        }
        val layoutParams = if (Hawk.get(HawkConfig.DEBUG_OPEN, false)) ViewGroup.LayoutParams(800, 400) else ViewGroup.LayoutParams(1, 1)
        webView.isFocusable = false
        webView.isFocusableInTouchMode = false
        webView.clearFocus()
        webView.overScrollMode = View.OVER_SCROLL_ALWAYS
        mDetailActivity?.addContentView(webView, layoutParams)
        /* 添加webView配置 */
        val settings = webView.settings
        settings.setNeedInitialFocus(false)
        settings.allowContentAccess = true
        settings.allowFileAccess = true
        settings.allowUniversalAccessFromFileURLs = true
        settings.allowFileAccessFromFileURLs = true
        settings.databaseEnabled = true
        settings.domStorageEnabled = true
        settings.javaScriptEnabled = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            settings.mediaPlaybackRequiresUserGesture = false
        }
        if (Hawk.get(HawkConfig.DEBUG_OPEN, false)) {
            settings.blockNetworkImage = false
        } else {
            settings.blockNetworkImage = true
        }
        settings.useWideViewPort = true
        settings.domStorageEnabled = true
        settings.javaScriptCanOpenWindowsAutomatically = true
        settings.setSupportMultipleWindows(false)
        settings.loadWithOverviewMode = true
        settings.builtInZoomControls = true
        settings.setSupportZoom(false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        settings.cacheMode = WebSettings.LOAD_NO_CACHE
        /* 添加webView配置 */
        //设置编码
        settings.defaultTextEncodingName = "utf-8"
        settings.userAgentString = webView.settings.userAgentString
        // settings.setUserAgentString(ANDROID_UA);
        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                return false
            }

            override fun onJsAlert(view: WebView, url: String, message: String, result: JsResult): Boolean {
                return true
            }

            override fun onJsConfirm(view: WebView, url: String, message: String, result: JsResult): Boolean {
                return true
            }

            override fun onJsPrompt(view: WebView, url: String, message: String, defaultValue: String, result: JsPromptResult): Boolean {
                return true
            }
        }
        mSysWebClient = SysWebClient()
        webView.webViewClient = mSysWebClient
        webView.setBackgroundColor(Color.BLACK)
    }

    private inner class SysWebClient : WebViewClient() {
        override fun onReceivedSslError(webView: WebView, sslErrorHandler: SslErrorHandler, sslError: SslError) {
            sslErrorHandler.proceed()
        }

        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            return false
        }

        fun checkIsVideo(url: String, headers: HashMap<String?, String?>?): WebResourceResponse? {
            if (url.endsWith("/favicon.ico")) {
                return WebResourceResponse("image/png", null, null)
            }
            LOG.i("shouldInterceptRequest url:$url")
            val ad: Boolean
            if (!loadedUrls.containsKey(url)) {
                ad = AdBlocker.isAd(url)
                loadedUrls[url] = ad
            } else {
                ad = loadedUrls[url]!!
            }
            if (!ad && !loadFound) {
                if (checkVideoFormat(url)) {
                    mHandler.removeMessages(100)
                    loadFound = true
                    if (headers != null && !headers.isEmpty()) {
                        playUrl(url, headers)
                    } else {
                        playUrl(url, null)
                    }
                    stopLoadWebView(false)
                }
            }
            return if (ad || loadFound) AdBlocker.createEmptyResource() else null
        }

        override fun shouldInterceptRequest(view: WebView, url: String): WebResourceResponse? {
            val response = checkIsVideo(url, null)
            return response ?: super.shouldInterceptRequest(view, url)
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
            var url = ""
            try {
                url = request.url.toString()
            } catch (th: Throwable) {
            }
            val webHeaders = HashMap<String?, String?>()
            try {
                val hds = request.requestHeaders
                for (k in hds.keys) {
                    if (k.equals("user-agent", ignoreCase = true)
                            || k.equals("referer", ignoreCase = true)
                            || k.equals("origin", ignoreCase = true)) {
                        webHeaders[k] = " " + hds[k]
                    }
                }
            } catch (th: Throwable) {
            }
            val response = checkIsVideo(url, webHeaders)
            return response ?: super.shouldInterceptRequest(view, request)
        }

        override fun onLoadResource(webView: WebView, url: String) {
            super.onLoadResource(webView, url)
        }
    }

    private fun configWebViewX5(webView: XWalkView?) {
        if (webView == null) {
            return
        }
        val layoutParams = if (Hawk.get(HawkConfig.DEBUG_OPEN, false)) ViewGroup.LayoutParams(800, 400) else ViewGroup.LayoutParams(1, 1)
        webView.isFocusable = false
        webView.isFocusableInTouchMode = false
        webView.clearFocus()
        webView.overScrollMode = View.OVER_SCROLL_ALWAYS
        mDetailActivity?.addContentView(webView, layoutParams)
        /* 添加webView配置 */
        val settings = webView.settings
        settings.allowContentAccess = true
        settings.allowFileAccess = true
        settings.allowUniversalAccessFromFileURLs = true
        settings.allowFileAccessFromFileURLs = true
        settings.databaseEnabled = true
        settings.domStorageEnabled = true
        settings.javaScriptEnabled = true
        if (Hawk.get(HawkConfig.DEBUG_OPEN, false)) {
            settings.blockNetworkImage = false
        } else {
            settings.blockNetworkImage = true
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            settings.mediaPlaybackRequiresUserGesture = false
        }
        settings.useWideViewPort = true
        settings.domStorageEnabled = true
        settings.javaScriptCanOpenWindowsAutomatically = true
        settings.setSupportMultipleWindows(false)
        settings.loadWithOverviewMode = true
        settings.builtInZoomControls = true
        settings.setSupportZoom(false)
        settings.cacheMode = WebSettings.LOAD_NO_CACHE
        // settings.setUserAgentString(ANDROID_UA);
        webView.setBackgroundColor(Color.BLACK)
        webView.setUIClient(object : XWalkUIClient(webView) {
            override fun onConsoleMessage(view: XWalkView, message: String, lineNumber: Int, sourceId: String, messageType: ConsoleMessageType): Boolean {
                return false
            }

            override fun onJsAlert(view: XWalkView, url: String, message: String, result: XWalkJavascriptResult): Boolean {
                return true
            }

            override fun onJsConfirm(view: XWalkView, url: String, message: String, result: XWalkJavascriptResult): Boolean {
                return true
            }

            override fun onJsPrompt(view: XWalkView, url: String, message: String, defaultValue: String, result: XWalkJavascriptResult): Boolean {
                return true
            }
        })
        mX5WebClient = XWalkWebClient(webView)
        webView.setResourceClient(mX5WebClient)
    }

    private inner class XWalkWebClient(view: XWalkView?) : XWalkResourceClient(view) {
        override fun onDocumentLoadedInFrame(view: XWalkView, frameId: Long) {
            super.onDocumentLoadedInFrame(view, frameId)
        }

        override fun onLoadStarted(view: XWalkView, url: String) {
            super.onLoadStarted(view, url)
        }

        override fun onLoadFinished(view: XWalkView, url: String) {
            super.onLoadFinished(view, url)
        }

        override fun onProgressChanged(view: XWalkView, progressInPercent: Int) {
            super.onProgressChanged(view, progressInPercent)
        }

        override fun shouldInterceptLoadRequest(view: XWalkView, request: XWalkWebResourceRequest): XWalkWebResourceResponse {
            val url = request.url.toString()
            // suppress favicon requests as we don't display them anywhere
            if (url.endsWith("/favicon.ico")) {
                return createXWalkWebResourceResponse("image/png", null, null)
            }
            LOG.i("shouldInterceptLoadRequest url:$url")
            val ad: Boolean
            if (!loadedUrls.containsKey(url)) {
                ad = AdBlocker.isAd(url)
                loadedUrls[url] = ad
            } else {
                ad = loadedUrls[url]!!
            }
            if (!ad && !loadFound) {
                if (checkVideoFormat(url)) {
                    mHandler.removeMessages(100)
                    loadFound = true
                    val webHeaders = HashMap<String?, String?>()
                    try {
                        val hds = request.requestHeaders
                        for (k in hds.keys) {
                            if (k.equals("user-agent", ignoreCase = true)
                                    || k.equals("referer", ignoreCase = true)
                                    || k.equals("origin", ignoreCase = true)) {
                                webHeaders[k] = " " + hds[k]
                            }
                        }
                    } catch (th: Throwable) {
                    }
                    if (webHeaders != null && !webHeaders.isEmpty()) {
                        playUrl(url, webHeaders)
                    } else {
                        playUrl(url, null)
                    }
                    stopLoadWebView(false)
                }
            }
            return if (ad || loadFound) createXWalkWebResourceResponse("text/plain", "utf-8", ByteArrayInputStream("".toByteArray())) else super.shouldInterceptLoadRequest(view, request)
        }

        override fun shouldOverrideUrlLoading(view: XWalkView, s: String): Boolean {
            return false
        }

        override fun onReceivedSslError(view: XWalkView, callback: ValueCallback<Boolean>, error: SslError) {
            callback.onReceiveValue(true)
        }
    }
}