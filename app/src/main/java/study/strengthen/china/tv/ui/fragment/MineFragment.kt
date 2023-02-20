package study.strengthen.china.tv.ui.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.dialog_title_msg_two_bts.view.*
import kotlinx.android.synthetic.main.fragment_my.*
import study.strengthen.china.tv.R
import study.strengthen.china.tv.base.BaseLazyFragment
import study.strengthen.china.tv.ui.activity.CollectActivity
import study.strengthen.china.tv.ui.activity.FastSearchActivity
import study.strengthen.china.tv.ui.activity.HistoryActivity
import study.strengthen.china.tv.ui.activity.SettingActivity
import study.strengthen.china.tv.ui.dialog.AboutDialog
import study.strengthen.china.tv.util.DensityUtil

class MineFragment : BaseLazyFragment() {
    companion object {
        fun newInstance(): MineFragment {
            return MineFragment()
        }
    }
    override fun init() {
        ll_collect?.setOnClickListener {
            val newIntent = Intent(mContext, CollectActivity::class.java)
            mActivity.startActivity(newIntent)
        }
        ll_record?.setOnClickListener {
            val newIntent = Intent(mContext, HistoryActivity::class.java)
            mActivity.startActivity(newIntent)
        }
        stv_setting_new?.setOnClickListener {
            val newIntent = Intent(mContext, SettingActivity::class.java)
            mActivity.startActivity(newIntent)
        }
        stv_declare?.setOnClickListener {
            showDialog("本软件只提供聚合展示功能，所有资源来自网上, 软件不参与任何制作, 上传, 储存, 下载等内容. 软件仅供学习参考, 请于安装后24小时内删除。")
        }
        tvCleanCache?.setOnClickListener {
            showClearDialog()
        }
    }

    private fun showClearDialog(): Dialog {
        val root = LayoutInflater.from(mContext).inflate(R.layout.dialog_title_msg_two_bts, null)
        val cancelView = root.findViewById<View>(R.id.cancel) as TextView
        val okView = root.findViewById<View>(R.id.ok) as TextView
        root.message?.text = "确认清除缓存吗？"
        val dialog: Dialog = AlertDialog.Builder(mContext, R.style.progress_dialog).create()
        cancelView.setOnClickListener { v: View? ->
            dialog.dismiss()
        }
        okView.setOnClickListener { v: View? ->
            Toast.makeText(mContext,"清除成功",Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
        dialog.window?.setContentView(root)
        val width = DensityUtil.getWidth()- DensityUtil.dip2px(120f)
        val params = dialog.window?.attributes
        params?.width = width
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog.window?.attributes = params
        return dialog
    }

    private fun showDialog(txt : String): Dialog {
        val root = LayoutInflater.from(mContext).inflate(R.layout.dialog_title_msg_two_bts, null)
        val cancelView = root.findViewById<View>(R.id.cancel) as TextView
        cancelView?.visibility = View.GONE
        val okView = root.findViewById<View>(R.id.ok) as TextView
        root.message?.text = txt
        val dialog: Dialog = AlertDialog.Builder(mContext, R.style.progress_dialog).create()
        cancelView.setOnClickListener { v: View? ->
            dialog.dismiss()
        }
        okView.setOnClickListener { v: View? ->
            dialog.dismiss()
        }
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
        dialog.window?.setContentView(root)
        val width = DensityUtil.getWidth()- DensityUtil.dip2px(120f)
        val params = dialog.window?.attributes
        params?.width = width
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog.window?.attributes = params
        return dialog
    }

    override fun getLayoutResID() = R.layout.fragment_my
}