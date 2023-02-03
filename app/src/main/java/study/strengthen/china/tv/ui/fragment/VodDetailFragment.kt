package study.strengthen.china.tv.ui.fragment

import android.text.Html
import android.view.View
import android.widget.TextView
import study.strengthen.china.tv.R
import study.strengthen.china.tv.base.BaseLazyFragment
import study.strengthen.china.tv.bean.Movie
import study.strengthen.china.tv.bean.VodInfo

class VodDetailFragment : BaseLazyFragment() {

    private var mVideo: Movie.Video? = null
    private var vodInfo: VodInfo? = null

    fun setArguments(video: Movie.Video?, vodInfo: VodInfo?): VodDetailFragment {
        this.mVideo = video
        this.vodInfo = vodInfo
        return this
    }

    fun refreshUi(video: Movie.Video?, vodInfo: VodInfo?) {
        this.mVideo = video
        this.vodInfo = vodInfo
    }

    override fun init() {
    }

    override fun getLayoutResID() = R.layout.fragment_play_info

    private fun setTextShow(view: TextView?, tag: String, info: String?) {
        if (info == null || info.trim { it <= ' ' }.isEmpty()) {
            view?.visibility = View.GONE
            return
        }
        view?.visibility = View.VISIBLE
        view?.text = Html.fromHtml(getHtml(tag, info))
    }

    private fun removeHtmlTag(info: String?): String {
        return info?.replace("\\<.*?\\>".toRegex(), "")?.replace("\\s".toRegex(), "") ?: ""
    }

    private fun getHtml(label: String, content: String): String {
        var content: String? = content
        if (content == null) {
            content = ""
        }
        return "$label<font color=\"#FFFFFF\">$content</font>"
    }
}