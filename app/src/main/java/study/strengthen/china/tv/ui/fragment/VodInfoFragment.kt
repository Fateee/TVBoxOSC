//package study.strengthen.china.tv.ui.fragment
//
//import android.text.Html
//import android.text.TextUtils
//import android.view.View
//import android.widget.TextView
//import com.squareup.picasso.Picasso
//import kotlinx.android.synthetic.main.fragment_play_pop.*
//import study.strengthen.china.tv.R
//import study.strengthen.china.tv.base.BaseLazyFragment
//import study.strengthen.china.tv.bean.Movie
//import study.strengthen.china.tv.bean.VodInfo
//import study.strengthen.china.tv.util.DefaultConfig
//
//class VodInfoFragment : BaseLazyFragment() {
//
//    private var mVideo: Movie.Video? = null
//    private var vodInfo: VodInfo? = null
//
//    fun setArguments(video: Movie.Video?, vodInfo: VodInfo?): VodInfoFragment {
//        this.mVideo = video
//        this.vodInfo = vodInfo
//        return this
//    }
//
//    fun refreshUi(video: Movie.Video?, vodInfo: VodInfo?) {
//        this.mVideo = video
//        this.vodInfo = vodInfo
//        init()
//    }
//
//    override fun init() {
//        tv_name?.text = mVideo?.name
////        setTextShow(tvSite, "来源：", ApiConfig.get().getSource(mVideo.sourceKey).name)
//        setTextShow(tv_vod_year, "", if (mVideo?.year == 0) "" else mVideo?.year.toString())
//        setTextShow(tv_vod_area, "", mVideo?.area)
//        setTextShow(tvLang, "语言：", mVideo?.lang)
//        setTextShow(tvType, "类型：", mVideo?.type)
//        setTextShow(tv_actor, "", mVideo?.actor)
//        setTextShow(tv_director, "", mVideo?.director)
//        setTextShow(tv_abstract, "", removeHtmlTag(mVideo?.des))
//        if (!TextUtils.isEmpty(mVideo?.pic)) {
//            Picasso.get()
//                    .load(DefaultConfig.checkReplaceProxy(mVideo?.pic))
////                    .transform(RoundTransformation(MD5.string2MD5(mVideo?.pic + mVideo?.name))
////                            .centerCorp(true)
////                            .override(AutoSizeUtils.mm2px(mContext, 300f), AutoSizeUtils.mm2px(mContext, 400f))
////                            .roundRadius(AutoSizeUtils.mm2px(mContext, 10f), RoundTransformation.RoundType.ALL))
//                    .placeholder(R.drawable.img_loading_placeholder)
//                    .error(R.drawable.img_loading_placeholder)
//                    .into(ivThumb)
//        } else {
//            ivThumb?.setImageResource(R.drawable.img_loading_placeholder)
//        }
//    }
//
//    override fun getLayoutResID() = R.layout.fragment_play_pop
//
//    private fun setTextShow(view: TextView?, tag: String, info: String?) {
//        if (info == null || info.trim { it <= ' ' }.isEmpty()) {
//            view?.visibility = View.GONE
//            return
//        }
//        view?.visibility = View.VISIBLE
//        view?.text = Html.fromHtml(getHtml(tag, info))
//    }
//
//    private fun removeHtmlTag(info: String?): String {
//        return info?.replace("\\<.*?\\>".toRegex(), "")?.replace("\\s".toRegex(), "") ?: ""
//    }
//
//    private fun getHtml(label: String, content: String): String {
//        var content: String? = content
//        if (content == null) {
//            content = ""
//        }
//        return "$label<font color=\"#FFFFFF\">$content</font>"
//    }
//}