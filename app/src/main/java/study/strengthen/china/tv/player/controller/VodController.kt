package study.strengthen.china.tv.player.controller

import android.content.Context
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_UP
import android.view.View
import android.view.View.OnClickListener
import android.widget.*
import com.orhanobut.hawk.Hawk
import com.owen.tvrecyclerview.widget.TvRecyclerView
import com.owen.tvrecyclerview.widget.V7LinearLayoutManager
import kotlinx.android.synthetic.main.player_dialog_speed.view.*
import kotlinx.android.synthetic.main.player_vod_control_view_new.view.*
import org.json.JSONException
import org.json.JSONObject
import study.strengthen.china.tv.R
import study.strengthen.china.tv.api.ApiConfig
import study.strengthen.china.tv.bean.ParseBean
import study.strengthen.china.tv.player.controller.BaseController.HandlerCallback
import study.strengthen.china.tv.player.thirdparty.MXPlayer
import study.strengthen.china.tv.player.thirdparty.ReexPlayer
import study.strengthen.china.tv.ui.activity.DetailActivity
import study.strengthen.china.tv.ui.adapter.ParseAdapter
import study.strengthen.china.tv.util.DensityUtil
import study.strengthen.china.tv.util.HawkConfig
import study.strengthen.china.tv.util.PlayerHelper
import xyz.doikki.videoplayer.player.VideoView
import xyz.doikki.videoplayer.player.VideoView.STATE_START_ABORT
import xyz.doikki.videoplayer.util.PlayerUtils
import java.text.SimpleDateFormat
import java.util.*

class VodController(context: Context) : BaseController(context) {
    private val mHideBottomRunable: Runnable = Runnable {
        hideBottom()
    }
    private val showSpeedTimeRunnable = object : Runnable {
        override fun run() {
            val date = Date()
            val simpleDateFormat = SimpleDateFormat("HH:mm")
            tv_sys_time?.text = simpleDateFormat.format(date)
            val netSpeedStr = PlayerHelper.formatNetSpeed(mControlWrapper?.tcpSpeed?:0)

            tv_play_load_net_speed_right_top?.text = netSpeedStr
            tv_play_load_net_speed?.text = netSpeedStr
            //视频分辨率
            tv_videosize?.text = "[ ${mControlWrapper?.videoSize?.get(0)} X ${mControlWrapper?.videoSize?.get(1)} ]"
            val currentPosition = (mControlWrapper?.currentPosition?:0)/1000
            val duration = (mControlWrapper?.duration?:0)/1000
            val stringBuilder = StringBuilder()
//                Log.e("huyi","netSpeedStr $netSpeedStr currentPosition $currentPosition duration $duration")
            stringBuilder.append(String.format("%02d", currentPosition / 60))
            stringBuilder.append(":")
            stringBuilder.append(String.format("%02d", currentPosition % 60))
            stringBuilder.append(" | ")
            stringBuilder.append(String.format("%02d", duration / 60))
            stringBuilder.append(":")
            stringBuilder.append(String.format("%02d", duration % 60))
            tv_seek_time?.text = stringBuilder.toString()
            if (isBottomVisible || mLoading?.visibility == View.VISIBLE) {
                mHandler?.postDelayed(this,900L)
            }
        }
    }
    private var isFullScreenPortrait = false
    private var mLastSpeed: Float = 1.0f
    private var mFastForwardPopShown: Boolean = false
    private var mCurrentPlayState: Int = 0
    var mSeekBar: SeekBar? = null
    var mCurrentTime: TextView? = null
    var mTotalTime: TextView? = null
    var mIsDragging = false
    var mProgressRoot: LinearLayout? = null
    var mProgressText: TextView? = null
    var mProgressIcon: ImageView? = null
    var mBottomRoot: LinearLayout? = null
    var mParseRoot: LinearLayout? = null
    var mGridView: TvRecyclerView? = null
    var mPlayTitle: TextView? = null
    var mNextBtn: TextView? = null
    var mPreBtn: TextView? = null
    var mPlayerScaleBtn: TextView? = null
    var mPlayerSpeedBtn: TextView? = null
    var mPlayerBtn: TextView? = null
    var mPlayerIJKBtn: TextView? = null
    var mPlayerRetry: TextView? = null
    var mPlayerTimeStartBtn: TextView? = null
    var mPlayerTimeSkipBtn: TextView? = null
//    var mFastForwardPop : PopupWindow? = null;
    //    TextView mPlayerTimeStepBtn;
    override fun initView() {
        super.initView()
        mCurrentTime = findViewById(R.id.curr_time)
        mTotalTime = findViewById(R.id.total_time)
        mPlayTitle = findViewById(R.id.tv_info_name)
        mSeekBar = findViewById(R.id.seekBar)
        mProgressRoot = findViewById(R.id.tv_progress_container)
        mProgressIcon = findViewById(R.id.tv_progress_icon)
        mProgressText = findViewById(R.id.tv_progress_text)
        mBottomRoot = findViewById(R.id.bottom_container)
        mParseRoot = findViewById(R.id.parse_root)
        mGridView = findViewById(R.id.mGridView)
        mPlayerRetry = findViewById(R.id.play_retry)
        mNextBtn = findViewById(R.id.play_next)
        mPreBtn = findViewById(R.id.play_pre)
        mPlayerScaleBtn = findViewById(R.id.play_scale)
        mPlayerSpeedBtn = findViewById(R.id.play_speed)
        mPlayerBtn = findViewById(R.id.play_player)
        mPlayerIJKBtn = findViewById(R.id.play_ijk)
        mPlayerTimeStartBtn = findViewById(R.id.play_time_start)
        mPlayerTimeSkipBtn = findViewById(R.id.play_time_end)
        //        mPlayerTimeStepBtn = findViewById(R.id.play_time_step);
        mGridView?.setLayoutManager(V7LinearLayoutManager(context, 0, false))
        val parseAdapter = ParseAdapter()
        parseAdapter.setOnItemClickListener { adapter, view, position ->
            val parseBean = parseAdapter.getItem(position)
            // 当前默认解析需要刷新
            val currentDefault = parseAdapter.data.indexOf(ApiConfig.get().defaultParse)
            parseAdapter.notifyItemChanged(currentDefault)
            ApiConfig.get().defaultParse = parseBean
            parseAdapter.notifyItemChanged(position)
            listener!!.changeParse(parseBean)
            hideBottom()
        }
        mGridView?.setAdapter(parseAdapter)
        parseAdapter.setNewData(ApiConfig.get().parseBeanList)
        mParseRoot?.setVisibility(View.VISIBLE)
        mSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (!fromUser) {
                    return
                }
                val duration = mControlWrapper.duration
                val newPosition = duration * progress / seekBar.max
                if (mCurrentTime != null) mCurrentTime!!.text = PlayerUtils.stringForTime(newPosition.toInt())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                mIsDragging = true
                mControlWrapper.stopProgress()
                mControlWrapper.stopFadeOut()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val duration = mControlWrapper.duration
                val newPosition = duration * seekBar.progress / seekBar.max
                mControlWrapper.seekTo(newPosition)
                mIsDragging = false
                mControlWrapper.startProgress()
                mControlWrapper.startFadeOut()
            }
        })
        mPlayerRetry?.setOnClickListener(OnClickListener {
            listener!!.replay()
            hideBottom()
        })
        mNextBtn?.setOnClickListener(OnClickListener {
            listener!!.playNext(false)
            hideBottom()
        })
        iv_play_next?.setOnClickListener(OnClickListener {
            listener?.playNext(false)
            hideBottom()
        })
        mPreBtn?.setOnClickListener(OnClickListener {
            listener!!.playPre()
            hideBottom()
        })
        iv_play_back?.setOnClickListener(OnClickListener {
            listener?.playPre()
            hideBottom()
        })
        iv_play?.setOnClickListener {
            onDoubleTap(null)
        }
        fullscreen?.setOnClickListener {
            toggleFullScreen()
            isFullScreenPortrait = false
        }
        t_back?.setOnClickListener {
            mActivity?.finish()
        }
        back?.setOnClickListener {
            toggleFullScreen()
            isFullScreenPortrait = false
        }
        pip?.setOnClickListener {
            if (mActivity is DetailActivity) {
                (mActivity as DetailActivity).enterPipModel()
            }
        }
        lock?.setOnClickListener {
            mControlWrapper?.toggleLockState()
        }
        iv_landscape_portrait?.setOnClickListener {
            isFullScreenPortrait = if (isFullScreenPortrait) {
                startFullScreen()
                false
            } else {
                toggleFullScreen()
                mControlWrapper?.startFullScreen()
                true
            }

        }
        setting?.setOnClickListener {

        }
        mPlayerScaleBtn?.setOnClickListener(OnClickListener {
            try {
                var scaleType = mPlayerConfig!!.getInt("sc")
                scaleType++
                if (scaleType > 5) scaleType = 0
                mPlayerConfig!!.put("sc", scaleType)
                updatePlayerCfgView()
                listener!!.updatePlayerCfg()
                mControlWrapper.setScreenScaleType(scaleType)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        })
        mPlayerSpeedBtn?.setOnClickListener(OnClickListener {
            try {
                var speed = mPlayerConfig!!.getDouble("sp").toFloat()
                speed += 0.5f
                if (speed > 3) speed = 0.5f
                mPlayerConfig!!.put("sp", speed.toDouble())
                updatePlayerCfgView()
                listener!!.updatePlayerCfg()
                mControlWrapper.speed = speed
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        })
        mPlayerBtn?.setOnClickListener(OnClickListener {
            try {
                var playerType = mPlayerConfig!!.getInt("pl")
                var playerVail = false
                do {
                    playerType++
                    if (playerType <= 2) {
                        playerVail = true
                    } else if (playerType == 10) {
                        playerVail = mxPlayerExist
                    } else if (playerType == 11) {
                        playerVail = reexPlayerExist
                    } else if (playerType > 11) {
                        playerType = 0
                        playerVail = true
                    }
                } while (!playerVail)
                mPlayerConfig!!.put("pl", playerType)
                updatePlayerCfgView()
                listener!!.updatePlayerCfg()
                listener!!.replay()
                // hideBottom();
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        })
        mPlayerIJKBtn?.setOnClickListener(OnClickListener {
            try {
                var ijk = mPlayerConfig!!.getString("ijk")
                val codecs = ApiConfig.get().ijkCodes
                for (i in codecs.indices) {
                    if (ijk == codecs[i].name) {
                        ijk = if (i >= codecs.size - 1) codecs[0].name else {
                            codecs[i + 1].name
                        }
                        break
                    }
                }
                mPlayerConfig!!.put("ijk", ijk)
                updatePlayerCfgView()
                listener!!.updatePlayerCfg()
                listener!!.replay()
                hideBottom()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        })
        mPlayerTimeStartBtn?.setOnClickListener(OnClickListener {
            try {
                val step = Hawk.get(HawkConfig.PLAY_TIME_STEP, 5)
                var st = mPlayerConfig!!.getInt("st")
                st += step
                if (st > 60 * 10) st = 0
                mPlayerConfig!!.put("st", st)
                updatePlayerCfgView()
                listener!!.updatePlayerCfg()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        })
        mPlayerTimeSkipBtn?.setOnClickListener(OnClickListener {
            try {
                val step = Hawk.get(HawkConfig.PLAY_TIME_STEP, 5)
                var et = mPlayerConfig!!.getInt("et")
                et += step
                if (et > 60 * 10) et = 0
                mPlayerConfig!!.put("et", et)
                updatePlayerCfgView()
                listener!!.updatePlayerCfg()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        })
        //        mPlayerTimeStepBtn.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                int step = Hawk.get(HawkConfig.PLAY_TIME_STEP, 5);
//                step += 5;
//                if (step > 30) {
//                    step = 5;
//                }
//                Hawk.put(HawkConfig.PLAY_TIME_STEP, step);
//                updatePlayerCfgView();
//            }
//        });
    }

    override fun getLayoutId(): Int {
        return R.layout.player_vod_control_view_new
    }

    fun showParse(userJxList: Boolean) {
        mParseRoot!!.visibility = if (userJxList) View.VISIBLE else View.GONE
    }

    private var mPlayerConfig: JSONObject? = null
    private var mxPlayerExist = false
    private var reexPlayerExist = false
    fun setPlayerConfig(playerCfg: JSONObject?) {
        mPlayerConfig = playerCfg
        updatePlayerCfgView()
        mxPlayerExist = MXPlayer.getPackageInfo() != null
        reexPlayerExist = ReexPlayer.getPackageInfo() != null
    }

    fun updatePlayerCfgView() {
        try {
            val playerType = mPlayerConfig!!.getInt("pl")
            mPlayerBtn!!.text = PlayerHelper.getPlayerName(playerType)
            mPlayerScaleBtn!!.text = PlayerHelper.getScaleName(mPlayerConfig!!.getInt("sc"))
            mPlayerIJKBtn!!.text = mPlayerConfig!!.getString("ijk")
            mPlayerIJKBtn!!.visibility = if (playerType == 1) View.VISIBLE else View.GONE
            mPlayerScaleBtn!!.text = PlayerHelper.getScaleName(mPlayerConfig!!.getInt("sc"))
            mPlayerSpeedBtn!!.text = "x" + mPlayerConfig!!.getDouble("sp")
            mPlayerTimeStartBtn!!.text = PlayerUtils.stringForTime(mPlayerConfig!!.getInt("st") * 1000)
            mPlayerTimeSkipBtn!!.text = PlayerUtils.stringForTime(mPlayerConfig!!.getInt("et") * 1000)
            //            mPlayerTimeStepBtn.setText(Hawk.get(HawkConfig.PLAY_TIME_STEP, 5) + "s");
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun setTitle(playTitleInfo: String?) {
        mPlayTitle!!.text = playTitleInfo
        tv_info_name1?.text = playTitleInfo
    }

    fun resetSpeed() {
        skipEnd = true
        mHandler.removeMessages(1004)
        mHandler.sendEmptyMessageDelayed(1004, 100)
    }

    interface VodControlListener {
        fun playNext(rmProgress: Boolean)
        fun playPre()
        fun changeParse(pb: ParseBean?)
        fun updatePlayerCfg()
        fun replay()
        fun errReplay()
    }

    fun setListener(listener: VodControlListener?) {
        this.listener = listener
    }

    private var listener: VodControlListener? = null
    private var skipEnd = true
    override fun setProgress(duration: Int, position: Int) {
        if (mIsDragging) {
            return
        }
        super.setProgress(duration, position)
        if (skipEnd && position != 0 && duration != 0) {
            var et = 0
            try {
                et = mPlayerConfig!!.getInt("et")
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            if (et > 0 && position + et * 1000 >= duration) {
                skipEnd = false
                listener!!.playNext(true)
            }
        }
        mCurrentTime!!.text = PlayerUtils.stringForTime(position)
        mTotalTime!!.text = PlayerUtils.stringForTime(duration)
        if (duration > 0) {
            mSeekBar!!.isEnabled = true
            val pos = (position * 1.0 / duration * mSeekBar!!.max).toInt()
            mSeekBar!!.progress = pos
        } else {
            mSeekBar!!.isEnabled = false
        }
        val percent = mControlWrapper.bufferedPercentage
        if (percent >= 95) {
            mSeekBar!!.secondaryProgress = mSeekBar!!.max
        } else {
            mSeekBar!!.secondaryProgress = percent * 10
        }
    }

    private var simSlideStart = false
    private var simSeekPosition = 0
    private var simSlideOffset: Long = 0
    fun tvSlideStop() {
        if (!simSlideStart) return
        mControlWrapper.seekTo(simSeekPosition.toLong())
        if (!mControlWrapper.isPlaying) mControlWrapper.start()
        simSlideStart = false
        simSeekPosition = 0
        simSlideOffset = 0
    }

    fun tvSlideStart(dir: Int) {
        val duration = mControlWrapper.duration.toInt()
        if (duration <= 0) return
        if (!simSlideStart) {
            simSlideStart = true
        }
        // 每次10秒
        simSlideOffset += (10000.0f * dir).toLong()
        val currentPosition = mControlWrapper.currentPosition.toInt()
        var position = (simSlideOffset + currentPosition).toInt()
        if (position > duration) position = duration
        if (position < 0) position = 0
        updateSeekUI(currentPosition, position, duration)
        simSeekPosition = position
    }

    override fun updateSeekUI(curr: Int, seekTo: Int, duration: Int) {
        super.updateSeekUI(curr, seekTo, duration)
        if (seekTo > curr) {
            mProgressIcon!!.setImageResource(R.drawable.icon_pre)
        } else {
            mProgressIcon!!.setImageResource(R.drawable.ic_back)
        }
        mProgressText!!.text = PlayerUtils.stringForTime(seekTo) + " / " + PlayerUtils.stringForTime(duration)
        mHandler.sendEmptyMessage(1000)
        mHandler.removeMessages(1001)
        mHandler.sendEmptyMessageDelayed(1001, 1000)
    }

    override fun onPlayStateChanged(playState: Int) {
        //todo
        super.onPlayStateChanged(playState)
        mCurrentPlayState = playState
        when (playState) {
            VideoView.STATE_ERROR -> listener!!.errReplay()
            VideoView.STATE_IDLE,STATE_START_ABORT -> {
                ll_back?.visibility = if (mControlWrapper?.isFullScreen == true) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            }
            VideoView.STATE_PREPARED -> {
                tv_play_load_net_speed?.visibility = View.GONE

                if (mControlWrapper?.isFullScreen == true) {
                    ll_back?.visibility = View.GONE
                }
                title_container?.visibility = View.GONE
            }
            VideoView.STATE_PLAYING -> {
                //要加显示竖屏code？
                iv_play?.isSelected = true
                startProgress()
                ll_back?.visibility = if (mControlWrapper?.isFullScreen == true) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            }
            VideoView.STATE_PAUSED -> {
                iv_play?.isSelected = false
            }
            VideoView.STATE_PLAYBACK_COMPLETED -> {
                listener?.playNext(true)
                title_container?.visibility = View.GONE
            }
            VideoView.STATE_PREPARING, VideoView.STATE_BUFFERING -> {
                mHandler?.post(showSpeedTimeRunnable)
                iv_play?.isSelected = mControlWrapper?.isPlaying?:false
                if (tv_progress_container?.visibility == View.GONE) {
                    tv_play_load_net_speed?.visibility = View.VISIBLE
                }
            }
            VideoView.STATE_BUFFERED -> {
                tv_play_load_net_speed?.visibility = View.GONE
                iv_play?.isSelected = mControlWrapper?.isPlaying?:false
            }

        }
    }

    override fun onPlayerStateChanged(playerState: Int) {
        when(playerState) {
            VideoView.PLAYER_NORMAL -> {
                showUI10or12()
                showUI10()
            }
            VideoView.PLAYER_FULL_SCREEN -> {
                if (mControlWrapper?.isLocked == false) {
                    title_container?.visibility = View.VISIBLE
                    lock?.visibility = View.VISIBLE
                    iv_landscape_portrait?.visibility = View.VISIBLE
                }
                back?.visibility = View.VISIBLE
                ll_back?.visibility = View.GONE


                fullscreen?.setSelected(true)
                iv_play_next?.visibility = View.VISIBLE
                iv_play_back?.visibility = View.VISIBLE
                tv_select_number?.visibility = View.VISIBLE
                pip?.visibility = View.GONE
                play_speed?.visibility = View.VISIBLE

//                seekBarBox?.visibility = View.INVISIBLE
            }
            VideoView.PLAYER_TINY_SCREEN -> {
                showUI10or12()
            }
        }
        if (mActivity != null && mControlWrapper?.hasCutout() == true) {
            val i = mActivity?.requestedOrientation
            if (i == SCREEN_ORIENTATION_PORTRAIT) {
                play_load_error?.setPadding(0, 0, 0, 0);
                title_container?.setPadding(0, 0, 0, 0);
                ll_back?.setPadding(0, 0, 0, 0);
                mBottomRoot?.setPadding(0, 0, 0, 0);
                bottom_progress?.setPadding(0, 0, 0, 0);
                lockBox?.setPadding(0, 0, 0, 0);
                menuBox?.setPadding(0, 0, 0, 0);
            } else if (i == 0 || i == 8 || i == 6) {
                val j = mControlWrapper?.cutoutHeight?:0
                play_load_error?.setPadding(j, 0, j, 0);
                title_container?.setPadding(j, 0, j, 0);
                ll_back?.setPadding(j, 0, j, 0);
                mBottomRoot?.setPadding(j, 0, j, 0);
                bottom_progress?.setPadding(j, 0, j, 0);
                lockBox?.setPadding(j, 0, 0, 0);
                menuBox?.setPadding(0, 0, j, 0);
            }
        }
        super.onPlayerStateChanged(playerState)
    }

    fun showUI10or12() {
        back?.visibility = View.INVISIBLE
        ll_back?.visibility = View.VISIBLE
        lock?.visibility = View.GONE
        iv_landscape_portrait?.visibility = View.GONE
    }
    fun showUI10() {
        if (play_load_error?.visibility == View.VISIBLE) {
            play_load_error?.visibility = View.GONE
        }
        fullscreen?.setSelected(false)
        iv_play_next?.visibility = View.GONE
        iv_play_back?.visibility = View.GONE
        tv_select_number?.visibility = View.GONE
        if (Build.VERSION.SDK_INT >= 26) {
            pip?.visibility = View.VISIBLE
        }
        play_speed?.visibility = View.GONE


//        seekBarBox?.visibility = View.VISIBLE
//        fullSeekBarBox?.visibility = View.GONE
    }

    private val isBottomVisible: Boolean
        get() = mBottomRoot?.visibility == View.VISIBLE || lock?.visibility == View.VISIBLE

    fun showBottom() {
        mHandler.removeMessages(1003)
        mHandler.sendEmptyMessage(1002)
    }

    fun hideBottom() {
        if (mFastForwardPop?.visibility == View.VISIBLE) {
            tv_bottom_center_container?.visibility = View.VISIBLE
        } else {
            tv_bottom_center_container?.visibility = View.GONE
        }
        mHandler.removeMessages(1002)
        mHandler.sendEmptyMessage(1003)
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (super.onKeyEvent(event)) {
            return true
        }
        if (isBottomVisible) {
            return super.dispatchKeyEvent(event)
        }
        val isInPlayback = isInPlaybackState
        val keyCode = event.keyCode
        val action = event.action
        if (action == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                if (isInPlayback) {
                    tvSlideStart(if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) 1 else -1)
                    return true
                }
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                if (isInPlayback) {
                    togglePlay()
                    return true
                }
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                if (!isBottomVisible) {
                    showBottom()
                }
            }
        } else if (action == KeyEvent.ACTION_UP) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                if (isInPlayback) {
                    tvSlideStop()
                    return true
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
        mHandler?.removeCallbacks(mHideBottomRunable)
        if (!isBottomVisible) {
            showBottom()
            mHandler?.post(showSpeedTimeRunnable)
            mHandler?.postDelayed(mHideBottomRunable,5000)
        } else {
            hideBottom()
        }
        return true
    }

    override fun onBackPressed(): Boolean {
        if (super.onBackPressed()) {
            return true
        }
        if (isBottomVisible) {
            hideBottom()
            return true
        }
        return false
    }

    fun clearHandlerMsgCallback() {
        mHandler?.removeCallbacksAndMessages(null)
    }

    override fun onLongPress(e: MotionEvent?) {
        super.onLongPress(e)
        if (mCurrentPlayState != VideoView.STATE_PAUSED && !mIsLocked) {
            this.mFastForwardPopShown = true
            try {
                mLastSpeed = mPlayerConfig!!.getDouble("sp").toFloat()
                val speed = Hawk.get("long_speed", 3.0f)
                mPlayerConfig?.put("sp", speed.toDouble())
                updatePlayerCfgView()
                listener?.updatePlayerCfg()
                mControlWrapper?.speed = speed
                showFastForwardUI(speed)
            } catch (e : Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun showFastForwardUI(speed: Float) {
        if (!mIsLocked) {
            if (mFastForwardPop?.visibility == View.GONE)
                mFastForwardPop?.visibility = View.VISIBLE
            mFastForwardPop?.tv_speed?.text = "${speed}X";
            if (isBottomVisible) {
                tv_bottom_center_container?.visibility = View.GONE
            } else {
                tv_bottom_center_container?.visibility = View.VISIBLE
            }
        }
    }

//    fun isBottomOrLockShown() : Boolean {
//        return (mBottomRoot?.visibility == View.VISIBLE || lock?.visibility == View.VISIBLE)
//    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == ACTION_UP && this.mFastForwardPopShown) {
            this.mFastForwardPopShown = false
            try {
                tv_bottom_center_container?.visibility = View.GONE
                mFastForwardPop?.visibility = View.GONE
                mPlayerConfig?.put("sp", mLastSpeed)
                updatePlayerCfgView()
                listener?.updatePlayerCfg()
                mControlWrapper?.speed = mLastSpeed
            } catch (e : Exception) {
                e.printStackTrace()
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onLockStateChanged(isLocked: Boolean) {
        super.onLockStateChanged(isLocked)
        lock?.isSelected = isLocked
        if(isLocked) {
            hideBottom()
        } else {
            showBottom()
        }
    }

    init {
        mHandlerCallback = HandlerCallback { msg ->
            when (msg.what) {
                1000 -> {
                    // seek 刷新
                    mProgressRoot?.visibility = View.VISIBLE
                }
                1001 -> {
                    // seek 关闭
                    mProgressRoot?.visibility = View.GONE
                }
                1002 -> {
                    // 显示底部菜单
                    if (mControlWrapper?.isLocked == true && mControlWrapper?.isFullScreen == true) {
                        lock?.setVisibility(View.VISIBLE)
                    } else {
                        title_container?.visibility = View.VISIBLE
                        mBottomRoot?.visibility = View.VISIBLE
                        tv_top_l_container?.visibility = View.VISIBLE
                        tv_top_r_container?.visibility = View.VISIBLE
                        tv_info_name?.visibility = View.VISIBLE
                        setting?.visibility = View.VISIBLE
                        pushTv?.visibility = View.VISIBLE
                        if (mControlWrapper?.isFullScreen == true) {
                            lock?.visibility = View.VISIBLE
                            iv_landscape_portrait?.visibility = View.VISIBLE
                        }
                    }
//                    a.C0 = false;
                }
                1003 -> {
                    // 隐藏底部菜单
                    mBottomRoot?.visibility = View.GONE
                    title_container?.visibility = View.GONE
                    pushTv?.visibility = View.GONE
                    setting?.visibility = View.GONE
                    iv_landscape_portrait?.visibility = View.GONE
                    lock?.visibility = View.GONE
                }
                1004 -> {
                    // 设置速度
                    if (isInPlaybackState) {
                        try {
                            val speed = mPlayerConfig?.getDouble("sp")?.toFloat() ?:0f
                            mControlWrapper?.speed = speed
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    } else mHandler?.sendEmptyMessageDelayed(1004, 100)
                }
            }
        }
    }
}