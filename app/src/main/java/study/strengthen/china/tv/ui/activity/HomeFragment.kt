package study.strengthen.china.tv.ui.activity

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Handler
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import study.strengthen.china.tv.api.ApiConfig
import study.strengthen.china.tv.api.ApiConfig.LoadConfigCallback
import study.strengthen.china.tv.base.BaseLazyFragment
import study.strengthen.china.tv.bean.AbsSortXml
import study.strengthen.china.tv.event.RefreshEvent
import study.strengthen.china.tv.server.ControlManager
import study.strengthen.china.tv.ui.adapter.HomePageAdapter
import study.strengthen.china.tv.ui.dialog.TipDialog
import study.strengthen.china.tv.ui.fragment.GridFragment
import study.strengthen.china.tv.ui.tv.widget.DefaultTransformer
import study.strengthen.china.tv.ui.tv.widget.FixedSpeedScroller
import study.strengthen.china.tv.viewmodel.SourceViewModel
import com.orhanobut.hawk.Hawk
import kotlinx.android.synthetic.main.fragment_home.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import study.strengthen.china.tv.R
import study.strengthen.china.tv.bean.MovieSort
import study.strengthen.china.tv.ui.fragment.HotFragment
import study.strengthen.china.tv.ui.fragment.KindFragment
import study.strengthen.china.tv.util.*
import java.util.*

class HomeFragment : BaseLazyFragment() {
//    private var topLayout: LinearLayout? = null
//    private var contentLayout: LinearLayout? = null
//    private var tvDate: TextView? = null
//    private var mGridView: TvRecyclerView? = null
//    private var mViewPager: NoScrollViewPager? = null
    private var sourceViewModel: SourceViewModel? = null
//    private var sortAdapter: SortAdapter? = null
    private var pageAdapter: HomePageAdapter? = null
    private val fragments: MutableList<BaseLazyFragment> = ArrayList()
    private var isDownOrUp = false
    private var sortChange = false
    private var currentSelected = 0
    private var sortFocused = 0
    var sortFocusView: View? = null
    private val mHandler = Handler()
    private var mExitTime: Long = 0
//    private val mRunnable: Runnable = object : Runnable {
//        @SuppressLint("DefaultLocale", "SetTextI18n")
//        override fun run() {
//            val date = Date()
//            @SuppressLint("SimpleDateFormat") val timeFormat = SimpleDateFormat("yyyy/MM/dd HH:mm")
//            tvDate?.text = timeFormat.format(date)
//            mHandler.postDelayed(this, 1000)
//        }
//    }

    override fun getLayoutResID(): Int {
        return R.layout.fragment_home
    }

    var useCacheConfig = false
    override fun init() {
        EventBus.getDefault().register(this)
        ControlManager.get().startServer()
//        setLoadSir(contentLayout)
//        initView()
        initViewModel()
        useCacheConfig = false
        val intent = activity?.intent
        if (intent != null && intent.extras != null) {
            val bundle = intent.extras
            useCacheConfig = bundle?.getBoolean("useCache", false) ?: false
        }
        initData()
    }

    private fun initView(data: MutableList<MovieSort.SortData>) {
        data.forEachIndexed { index, sortData->
            tabLayout?.newTab()?.apply {
                customView = TextView(context).apply {
                    text = sortData.name
                    gravity = Gravity.CENTER
                }
                tabLayout?.addTab(this)
                if (index == 0) { //默认选中0
                    onTabChecked(this)
                } else {
                    onTabUnchecked(this)
                }
            }
        }
        tabLayout?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                onTabChecked(tab)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                onTabUnchecked(tab)
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
//        topLayout = findViewById(R.id.topLayout)
//        tvDate = findViewById(R.id.tvDate)
//        contentLayout = findViewById(R.id.contentLayout)
//        mGridView = findViewById<TvRecyclerView>(R.id.mGridView)
//        mViewPager = findViewById(R.id.mViewPager)
//        sortAdapter = SortAdapter()
//        mGridView?.setLayoutManager(V7LinearLayoutManager(mContext, 0, false))
//        mGridView?.setSpacingWithMargins(0, AutoSizeUtils.dp2px(mContext, 10.0f))
//        mGridView?.setAdapter(sortAdapter)
//        mGridView?.setOnItemListener(object : OnItemListener {
//            override fun onItemPreSelected(tvRecyclerView: TvRecyclerView, view: View, position: Int) {
//                if (view != null && !isDownOrUp) {
//                    view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(300).start()
//                    val textView = view.findViewById<TextView>(R.id.tvTitle)
//                    textView.paint.isFakeBoldText = false
//                    textView.setTextColor(this@HomeFragment.resources.getColor(R.color.color_BBFFFFFF))
//                    textView.invalidate()
//                    view.findViewById<View>(R.id.tvFilter).visibility = View.GONE
//                }
//            }
//
//            override fun onItemSelected(tvRecyclerView: TvRecyclerView, view: View, position: Int) {
//                if (view != null) {
//                    isDownOrUp = false
//                    sortChange = true
//                    view.animate().scaleX(1.1f).scaleY(1.1f).setInterpolator(BounceInterpolator()).setDuration(300).start()
//                    val textView = view.findViewById<TextView>(R.id.tvTitle)
//                    textView.paint.isFakeBoldText = true
//                    textView.setTextColor(this@HomeFragment.resources.getColor(R.color.color_FFFFFF))
//                    textView.invalidate()
//                    if (sortAdapter?.getItem(position)?.filters?.isEmpty() == false) view.findViewById<View>(R.id.tvFilter).visibility = View.VISIBLE
//                    sortFocusView = view
//                    sortFocused = position
////                    mHandler.removeCallbacks(mDataRunnable)
////                    mHandler.postDelayed(mDataRunnable, 200)
//                }
//            }
//
//            override fun onItemClick(parent: TvRecyclerView, itemView: View, position: Int) {
//                if (itemView != null && currentSelected == position && sortAdapter?.getItem(position)?.filters?.isEmpty() == false) { // 弹出筛选
//                    val baseLazyFragment = fragments[currentSelected]
//                    if (baseLazyFragment is GridFragment) {
//                        baseLazyFragment.showFilter()
//                    }
//                }
//            }
//        })
//        mGridView?.setOnInBorderKeyEventListener(OnInBorderKeyEventListener { direction, view ->
//            if (direction != View.FOCUS_DOWN) {
//                return@OnInBorderKeyEventListener false
//            }
//            isDownOrUp = true
//            val baseLazyFragment = fragments[sortFocused] as? GridFragment
//                    ?: return@OnInBorderKeyEventListener false
//            !baseLazyFragment.isLoad
//        })
        //mHandler.postDelayed(mFindFocus, 500);
    }

    private fun initViewModel() {
        sourceViewModel = ViewModelProvider(this).get(SourceViewModel::class.java)
        sourceViewModel?.sortResult?.observe(this, Observer { absXml ->
            showSuccess()
//            var data = ArrayList<MovieSort.SortData>()
            val data = if (absXml?.classes != null && absXml.classes.sortList != null) {
                DefaultConfig.adjustSort(ApiConfig.get().homeSourceBean.key, absXml.classes.sortList, true)
//                sortAdapter?.setNewData(DefaultConfig.adjustSort(ApiConfig.get().homeSourceBean.key, absXml.classes.sortList, true))
            } else {
                DefaultConfig.adjustSort(ApiConfig.get().homeSourceBean.key, ArrayList(), true)
//                sortAdapter?.setNewData(DefaultConfig.adjustSort(ApiConfig.get().homeSourceBean.key, ArrayList(), true))
            }
            initView(data)
            initViewPager(absXml,data)
        })
    }

    private var dataInitOk = false
    private var jarInitOk = false
    private fun initData() {
        if (dataInitOk && jarInitOk) {
            showLoading()
            sourceViewModel?.getSort(ApiConfig.get().homeSourceBean.key)
            return
        }
        showLoading()
        if (dataInitOk && !jarInitOk) {
            if (!ApiConfig.get().spider.isEmpty()) {
                ApiConfig.get().loadJar(useCacheConfig, ApiConfig.get().spider, object : LoadConfigCallback {
                    override fun success() {
                        jarInitOk = true
                        mHandler.postDelayed({
                            if (!useCacheConfig) Toast.makeText(activity, "自定义jar加载成功", Toast.LENGTH_SHORT).show()
                            initData()
                        }, 50)
                    }

                    override fun retry() {}
                    override fun error(msg: String) {
                        jarInitOk = true
                        mHandler.post {
                            Toast.makeText(activity, "jar加载失败", Toast.LENGTH_SHORT).show()
                            initData()
                        }
                    }
                })
            }
            return
        }
        ApiConfig.get().loadConfig(useCacheConfig, object : LoadConfigCallback {
            var dialog: TipDialog? = null
            override fun retry() {
                mHandler.post { initData() }
            }

            override fun success() {
                dataInitOk = true
                if (ApiConfig.get().spider.isEmpty()) {
                    jarInitOk = true
                }
                mHandler.postDelayed({ initData() }, 50)
            }

            override fun error(msg: String) {
                if (msg.equals("-1", ignoreCase = true)) {
                    mHandler.post {
                        dataInitOk = true
                        jarInitOk = true
                        initData()
                    }
                    return
                }
                mHandler.post {
                    if (dialog == null) dialog = TipDialog(context!!, msg, "重试", "取消", object : TipDialog.OnListener {
                        override fun left() {
                            mHandler.post {
                                initData()
                                dialog?.hide()
                            }
                        }

                        override fun right() {
                            dataInitOk = true
                            jarInitOk = true
                            mHandler.post {
                                initData()
                                dialog?.hide()
                            }
                        }

                        override fun cancel() {
                            dataInitOk = true
                            jarInitOk = true
                            mHandler.post {
                                initData()
                                dialog?.hide()
                            }
                        }
                    })
                    if (dialog?.isShowing == false) dialog?.show()
                }
            }
        }, activity)
    }

    private fun initViewPager(absXml: AbsSortXml?, rootData: MutableList<MovieSort.SortData>) {
        if (rootData.size > 0) {
            for (data in rootData) {
                if (data.id == "my0") {
                    if (Hawk.get(HawkConfig.HOME_REC, 0) == 1 && absXml != null && absXml.videoList != null && absXml.videoList.size > 0) {
                        fragments.add(HotFragment.newInstance(absXml.videoList))
                    } else {
                        fragments.add(HotFragment.newInstance(null))
                    }
                } else {
                    fragments.add(KindFragment.newInstance(data))
                }
            }
            pageAdapter = HomePageAdapter(childFragmentManager, fragments)
//            try {
//                val field = ViewPager::class.java.getDeclaredField("mScroller")
//                field.isAccessible = true
//                val scroller = FixedSpeedScroller(mContext, AccelerateInterpolator())
//                field[mViewPager] = scroller
//                scroller.setmDuration(300)
//            } catch (e: Exception) {
//            }
//            mViewPager?.setPageTransformer(true, DefaultTransformer())
            mViewPager?.adapter = pageAdapter
            mViewPager?.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
            mViewPager?.setCurrentItem(currentSelected, false)
            tabLayout?.setOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    tab?.position?.let {
                        mViewPager?.currentItem = it
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                }
            })
        }
    }

//    override fun onBackPressed() {
//        var i: Int
//        if (fragments.size <= 0 || sortFocused >= fragments.size || sortFocused.also { i = it } < 0) {
//            exit()
//            return
//        }
//        val baseLazyFragment = fragments[i]
//        if (baseLazyFragment is GridFragment) {
//            val view = sortFocusView
//            if (view != null && !view.isFocused) {
//                sortFocusView?.requestFocus()
//            } else if (sortFocused != 0) {
//                mGridView?.setSelection(0)
//            } else {
//                exit()
//            }
//        } else {
//            exit()
//        }
//    }

//    private fun exit() {
//        if (System.currentTimeMillis() - mExitTime < 2000) {
//            super.onBackPressed()
//        } else {
//            mExitTime = System.currentTimeMillis()
//            Toast.makeText(mContext, "再按一次返回键退出应用", Toast.LENGTH_SHORT).show()
//        }
//    }


    override fun onResume() {
        super.onResume()
//        mHandler.post(mRunnable)
    }

    override fun onPause() {
        super.onPause()
        mHandler.removeCallbacksAndMessages(null)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun refresh(event: RefreshEvent) {
        if (event.type == RefreshEvent.TYPE_PUSH_URL) {
            if (ApiConfig.get().getSource("push_agent") != null) {
                val newIntent = Intent(mContext, DetailActivity::class.java)
                newIntent.putExtra("id", event.obj as String)
                newIntent.putExtra("sourceKey", "push_agent")
                newIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                this@HomeFragment.startActivity(newIntent)
            }
        }
    }

//    private val mDataRunnable = Runnable {
//        if (sortChange) {
//            sortChange = false
//            if (sortFocused != currentSelected) {
//                currentSelected = sortFocused
//                mViewPager?.setCurrentItem(sortFocused, false)
//                if (sortFocused == 0) {
//                    changeTop(false)
//                } else {
//                    changeTop(true)
//                }
//            }
//        }
//    }

//    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
//        if (topHide < 0) return false
//        if (event.action == KeyEvent.ACTION_DOWN) {
//        } else if (event.action == KeyEvent.ACTION_UP) {
//        }
//        return super.dispatchKeyEvent(event)
//    }

//    var topHide: Byte = 0
//    private fun changeTop(hide: Boolean) {
//        val viewObj = ViewObj(topLayout, topLayout?.layoutParams as ViewGroup.MarginLayoutParams)
//        val animatorSet = AnimatorSet()
//        animatorSet.addListener(object : Animator.AnimatorListener {
//            override fun onAnimationStart(animation: Animator) {}
//            override fun onAnimationEnd(animation: Animator) {
//                topHide = (if (hide) 1 else 0).toByte()
//            }
//
//            override fun onAnimationCancel(animation: Animator) {}
//            override fun onAnimationRepeat(animation: Animator) {}
//        })
//        if (hide && topHide.toInt() == 0) {
//            animatorSet.playTogether(*arrayOf<Animator>(
//                    ObjectAnimator.ofObject(viewObj, "marginTop", IntEvaluator(),
//                            *arrayOf<Any>(
//                                    Integer.valueOf(AutoSizeUtils.mm2px(mContext, 10.0f)),
//                                    Integer.valueOf(AutoSizeUtils.mm2px(mContext, 0.0f))
//                            )),
//                    ObjectAnimator.ofObject(viewObj, "height", IntEvaluator(),
//                            *arrayOf<Any>(
//                                    Integer.valueOf(AutoSizeUtils.mm2px(mContext, 50.0f)),
//                                    Integer.valueOf(AutoSizeUtils.mm2px(mContext, 1.0f))
//                            )),
//                    ObjectAnimator.ofFloat(topLayout, "alpha", *floatArrayOf(1.0f, 0.0f))))
//            animatorSet.duration = 200
//            animatorSet.start()
//            return
//        }
//        if (!hide && topHide.toInt() == 1) {
//            animatorSet.playTogether(*arrayOf<Animator>(
//                    ObjectAnimator.ofObject(viewObj, "marginTop", IntEvaluator(),
//                            *arrayOf<Any>(
//                                    Integer.valueOf(AutoSizeUtils.mm2px(mContext, 0.0f)),
//                                    Integer.valueOf(AutoSizeUtils.mm2px(mContext, 10.0f))
//                            )),
//                    ObjectAnimator.ofObject(viewObj, "height", IntEvaluator(),
//                            *arrayOf<Any>(
//                                    Integer.valueOf(AutoSizeUtils.mm2px(mContext, 1.0f)),
//                                    Integer.valueOf(AutoSizeUtils.mm2px(mContext, 50.0f))
//                            )),
//                    ObjectAnimator.ofFloat(topLayout, "alpha", *floatArrayOf(0.0f, 1.0f))))
//            animatorSet.duration = 200
//            animatorSet.start()
//            return
//        }
//    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        AppManager.getInstance().appExit(0)
        ControlManager.get().stopServer()
    }

    private fun onTabChecked(tab: TabLayout.Tab?) {
        val textView = tab?.customView as TextView?
        textView?.setTextSize(TypedValue.COMPLEX_UNIT_PX, DensityUtil.sp2px(24f).toFloat())
        textView?.setTextColor(resources.getColor(R.color.main_color))
        textView?.isSingleLine = true
        textView?.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
        textView?.includeFontPadding = false
        val params = tab?.view?.layoutParams
        params?.width = ViewGroup.LayoutParams.WRAP_CONTENT
        params?.height = ViewGroup.LayoutParams.MATCH_PARENT
        tab?.view?.layoutParams = params
        textView?.layoutParams = params
        tabLayout?.isTabIndicatorFullWidth = false
    }

    private fun onTabUnchecked(tab: TabLayout.Tab?) {
        val textView = tab?.customView as TextView?
        textView?.setTextSize(TypedValue.COMPLEX_UNIT_PX,DensityUtil.sp2px(20f).toFloat())
        textView?.setTextColor(Color.BLACK)
        textView?.isSingleLine = true
        textView?.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
        textView?.includeFontPadding = false
        val params = tab?.view?.layoutParams
        params?.width = ViewGroup.LayoutParams.WRAP_CONTENT
        params?.height = ViewGroup.LayoutParams.MATCH_PARENT
        tab?.view?.layoutParams = params
        textView?.layoutParams = params
        tabLayout?.isTabIndicatorFullWidth = false
    }
}