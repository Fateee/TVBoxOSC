package study.strengthen.china.tv.ui.activity

import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import com.orhanobut.hawk.Hawk
import kotlinx.android.synthetic.main.activity_setting_new.*
import okhttp3.HttpUrl
import study.strengthen.china.tv.R
import study.strengthen.china.tv.api.ApiConfig
import study.strengthen.china.tv.base.BaseActivity
import study.strengthen.china.tv.bean.IJKCode
import study.strengthen.china.tv.bean.SourceBean
import study.strengthen.china.tv.ui.adapter.ApiHistoryDialogAdapter
import study.strengthen.china.tv.ui.adapter.SelectDialogAdapter
import study.strengthen.china.tv.ui.dialog.ApiHistoryDialog
import study.strengthen.china.tv.ui.dialog.SelectDialog
import study.strengthen.china.tv.util.FastClickCheckUtil
import study.strengthen.china.tv.util.HawkConfig
import study.strengthen.china.tv.util.OkGoHelper
import study.strengthen.china.tv.util.PlayerHelper
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import java.util.*


class SettingActivity : BaseActivity() {
    override fun getLayoutResID(): Int {
        return R.layout.activity_setting_new
    }

    override fun init() {
        initView()
        initData()
    }

    private fun initView() {
        tvApi?.setSubTitle(Hawk.get(HawkConfig.API_URL, ""));
        tvApi?.setOnClickListener { v ->
            showApiDialog()
        }

        tvHomeApi?.setSubTitle(ApiConfig.get().homeSourceBean.name)
        tvHomeApi?.setOnClickListener { v ->
            val sites = ApiConfig.get().sourceBeanList
            if (sites.size > 0) {
                val dialog = SelectDialog<SourceBean>(this)
                dialog.setTip("请选择首页数据源")
                dialog.setAdapter(object : SelectDialogAdapter.SelectDialogInterface<SourceBean> {
                    override fun click(value: SourceBean, pos: Int) {
                        ApiConfig.get().setSourceBean(value)
                        tvHomeApi?.setSubTitle(ApiConfig.get().homeSourceBean.name)
                        dialog.dismiss()
                        rebootApp()
                    }

                    override fun getDisplay(`val`: SourceBean): String {
                        return `val`.name
                    }
                }, object : DiffUtil.ItemCallback<SourceBean>() {
                    override fun areItemsTheSame(oldItem: SourceBean, newItem: SourceBean): Boolean {
                        return oldItem === newItem
                    }

                    override fun areContentsTheSame(oldItem: SourceBean, newItem: SourceBean): Boolean {
                        return oldItem.key == newItem.key
                    }
                }, sites, sites.indexOf(ApiConfig.get().homeSourceBean))
                dialog.show()
            }
        }

        tvHomeRec?.setSubTitle(getHomeRecName(Hawk.get(HawkConfig.HOME_REC, 0)))
        tvHomeRec?.setOnClickListener { v ->
            FastClickCheckUtil.check(v)
            val defaultPos = Hawk.get(HawkConfig.HOME_REC, 0)
            val types = ArrayList<Int>()
            types.add(0)
            types.add(1)
            types.add(2)
            val dialog = SelectDialog<Int>(this)
            dialog.setTip("请选择主页列表数据")
            dialog.setAdapter(object : SelectDialogAdapter.SelectDialogInterface<Int> {
                override fun click(value: Int, pos: Int) {
                    Hawk.put(HawkConfig.HOME_REC, value)
                    tvHomeRec?.setSubTitle(getHomeRecName(value))
                    dialog.dismiss()
                    rebootApp()
                }

                override fun getDisplay(`val`: Int): String {
                    return getHomeRecName(`val`)!!
                }
            }, object : DiffUtil.ItemCallback<Int>() {
                override fun areItemsTheSame(oldItem: Int, newItem: Int): Boolean {
                    return oldItem == newItem
                }

                override fun areContentsTheSame(oldItem: Int, newItem: Int): Boolean {
                    return oldItem == newItem
                }
            }, types, defaultPos)
            dialog.show()
        }

        tvPlay?.setSubTitle(PlayerHelper.getPlayerName(Hawk.get(HawkConfig.PLAY_TYPE, 0)))
        tvPlay?.setOnClickListener {
            val defaultPos = Hawk.get(HawkConfig.PLAY_TYPE, 0)
            val players = ArrayList<Int>()
            players.add(0)
            players.add(1)
            players.add(2)
            val dialog = SelectDialog<Int>(this)
            dialog.setTip("请选择默认播放器")
            dialog.setAdapter(object : SelectDialogAdapter.SelectDialogInterface<Int> {
                override fun click(value: Int, pos: Int) {
                    Hawk.put(HawkConfig.PLAY_TYPE, value)
                    tvPlay.setSubTitle(PlayerHelper.getPlayerName(value))
                    PlayerHelper.init()
                    dialog.dismiss()
                }

                override fun getDisplay(`val`: Int): String {
                    return PlayerHelper.getPlayerName(`val`)
                }
            }, object : DiffUtil.ItemCallback<Int>() {
                override fun areItemsTheSame(oldItem: Int, newItem: Int): Boolean {
                    return oldItem == newItem
                }

                override fun areContentsTheSame(oldItem: Int, newItem: Int): Boolean {
                    return oldItem == newItem
                }
            }, players, defaultPos)
            dialog.show()
        }

        tvMediaCodec?.setSubTitle(Hawk.get(HawkConfig.IJK_CODEC, ""))
        tvMediaCodec?.setOnClickListener(View.OnClickListener { v ->
            val ijkCodes = ApiConfig.get().ijkCodes
            if (ijkCodes == null || ijkCodes.size == 0) return@OnClickListener
            var defaultPos = 0
            val ijkSel = Hawk.get(HawkConfig.IJK_CODEC, "")
            for (j in ijkCodes.indices) {
                if (ijkSel == ijkCodes[j].name) {
                    defaultPos = j
                    break
                }
            }
            val dialog = SelectDialog<IJKCode>(this)
            dialog.setTip("请选择IJK解码方式")
            dialog.setAdapter(object : SelectDialogAdapter.SelectDialogInterface<IJKCode> {
                override fun click(value: IJKCode, pos: Int) {
                    value.selected(true)
                    tvMediaCodec?.setSubTitle(value.name)
                    dialog.dismiss()
                }

                override fun getDisplay(`val`: IJKCode): String {
                    return `val`.name
                }
            }, object : DiffUtil.ItemCallback<IJKCode>() {
                override fun areItemsTheSame(oldItem: IJKCode, newItem: IJKCode): Boolean {
                    return oldItem === newItem
                }

                override fun areContentsTheSame(oldItem: IJKCode, newItem: IJKCode): Boolean {
                    return oldItem.name == newItem.name
                }
            }, ijkCodes, defaultPos)
            dialog.show()
        })
        val speedValue = Hawk.get("long_speed", 3.0f)
        playerLongSpeed?.setSubTitle(speedValue.toString())
        playerLongSpeed?.setOnClickListener {
            val oldValue = Hawk.get("long_speed", 3.0f)
            val speeds = ArrayList<Float>()
            speeds.add(2.0f)
            speeds.add(3.0f)
            var defaultPos = 0
            for (j in speeds.indices) {
                if (oldValue == speeds[j]) {
                    defaultPos = j
                    break
                }
            }
            val dialog = SelectDialog<Float>(this)
            dialog.setTip("请选择全屏长按倍速")
            dialog.setAdapter(object : SelectDialogAdapter.SelectDialogInterface<Float> {
                override fun click(value: Float, pos: Int) {
                    Hawk.put("long_speed", value)
                    playerLongSpeed?.setSubTitle(value.toString()+"倍速")
                    dialog.dismiss()
                }

                override fun getDisplay(`val`: Float): String {
                    return `val`.toString()+"倍速"
                }
            }, object : DiffUtil.ItemCallback<Float>() {
                override fun areItemsTheSame(oldItem: Float, newItem: Float): Boolean {
                    return oldItem == newItem
                }

                override fun areContentsTheSame(oldItem: Float, newItem: Float): Boolean {
                    return oldItem == newItem
                }
            }, speeds, defaultPos)
            dialog.show()
        }

        tvRenderType?.setSubTitle(PlayerHelper.getRenderName(Hawk.get(HawkConfig.PLAY_RENDER, 0)))
        tvRenderType?.setOnClickListener { v ->
            val defaultPos = Hawk.get(HawkConfig.PLAY_RENDER, 0)
            val renders = ArrayList<Int>()
            renders.add(0)
            renders.add(1)
            val dialog = SelectDialog<Int>(this)
            dialog.setTip("请选择默认渲染方式")
            dialog.setAdapter(object : SelectDialogAdapter.SelectDialogInterface<Int> {
                override fun click(value: Int, pos: Int) {
                    Hawk.put(HawkConfig.PLAY_RENDER, value)
                    tvRenderType?.setSubTitle(PlayerHelper.getRenderName(value))
                    PlayerHelper.init()
                    dialog.dismiss()
                }

                override fun getDisplay(`val`: Int): String {
                    return PlayerHelper.getRenderName(`val`)
                }
            }, object : DiffUtil.ItemCallback<Int>() {
                override fun areItemsTheSame(oldItem: Int, newItem: Int): Boolean {
                    return oldItem == newItem
                }

                override fun areContentsTheSame(oldItem: Int, newItem: Int): Boolean {
                    return oldItem == newItem
                }
            }, renders, defaultPos)
            dialog.show()
        }

        tvDns?.setSubTitle(OkGoHelper.dnsHttpsList.get(Hawk.get(HawkConfig.DOH_URL, 0)))
        tvDns?.setOnClickListener { v ->
            FastClickCheckUtil.check(v)
            val dohUrl = Hawk.get(HawkConfig.DOH_URL, 0)
            val dialog = SelectDialog<String>(this)
            dialog.setTip("请选择安全DNS")
            dialog.setAdapter(object : SelectDialogAdapter.SelectDialogInterface<String> {
                override fun click(value: String, pos: Int) {
                    tvDns?.setSubTitle(OkGoHelper.dnsHttpsList[pos])
                    Hawk.put(HawkConfig.DOH_URL, pos)
                    val url = OkGoHelper.getDohUrl(pos)
                    OkGoHelper.dnsOverHttps.setUrl(if (url.isEmpty()) null else HttpUrl.get(url))
                    IjkMediaPlayer.toggleDotPort(pos > 0)
                    dialog.dismiss()
                }

                override fun getDisplay(`val`: String): String {
                    return `val`
                }
            }, object : DiffUtil.ItemCallback<String>() {
                override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
                    return oldItem == newItem
                }

                override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
                    return oldItem == newItem
                }
            }, OkGoHelper.dnsHttpsList, dohUrl)
            dialog.show()
        }

        tvScaleType?.setSubTitle(PlayerHelper.getScaleName(Hawk.get(HawkConfig.PLAY_SCALE, 0)))
        tvScaleType?.setOnClickListener { v ->
            FastClickCheckUtil.check(v)
            val defaultPos = Hawk.get(HawkConfig.PLAY_SCALE, 0)
            val players = ArrayList<Int>()
            players.add(0)
            players.add(1)
            players.add(2)
            players.add(3)
            players.add(4)
            players.add(5)
            val dialog = SelectDialog<Int>(this)
            dialog.setTip("请选择默认画面缩放")
            dialog.setAdapter(object : SelectDialogAdapter.SelectDialogInterface<Int> {
                override fun click(value: Int, pos: Int) {
                    Hawk.put(HawkConfig.PLAY_SCALE, value)
                    tvScaleType?.setSubTitle(PlayerHelper.getScaleName(value))
                    dialog.dismiss()
                }

                override fun getDisplay(`val`: Int): String {
                    return PlayerHelper.getScaleName(`val`)
                }
            }, object : DiffUtil.ItemCallback<Int>() {
                override fun areItemsTheSame(oldItem: Int, newItem: Int): Boolean {
                    return oldItem == newItem
                }

                override fun areContentsTheSame(oldItem: Int, newItem: Int): Boolean {
                    return oldItem == newItem
                }
            }, players, defaultPos)
            dialog.show()
        }
    }

    private fun showApiDialog() {
        val dialog = ApiHistoryDialog(this)
        dialog.setTip("历史配置列表")
        val history = Hawk.get(HawkConfig.API_HISTORY, ArrayList<String>())
        if (history.isNotEmpty()) {
            val current = Hawk.get(HawkConfig.API_URL, "")
            var idx = 0
            if (history.contains(current)) idx = history.indexOf(current)
            dialog.setAdapter(object : ApiHistoryDialogAdapter.SelectDialogInterface {
                override fun click(value: String) {
                    tvApi?.setSubTitle(value)
                    Hawk.put(HawkConfig.API_URL, value)
                    dialog.dismiss()
                    rebootApp()
                }

                override fun del(value: String, data: ArrayList<String>) {
                    Hawk.put(HawkConfig.API_HISTORY, data)
                }
            }, history, idx)
        }
        dialog.setOnListener(object :ApiHistoryDialog.OnListener{
            override fun onchange(api: String?) {
                tvApi?.setSubTitle(api)
                Hawk.put(HawkConfig.API_URL, api)
                rebootApp()
            }
        })
        dialog.show()
    }

    private fun initData() {
    }

    private fun getHomeRecName(type: Int): String? {
        return if (type == 1) {
            "站点推荐"
        } else if (type == 2) {
            "观看历史"
        } else {
            "豆瓣热播"
        }
    }

    fun rebootApp() {
        Toast.makeText(this, "配置生效，即将重启!", Toast.LENGTH_SHORT).show()
        tvApi?.postDelayed({
            val intent = baseContext.packageManager.getLaunchIntentForPackage(baseContext.packageName)
            intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finishAffinity()
        },1000)
    }
}