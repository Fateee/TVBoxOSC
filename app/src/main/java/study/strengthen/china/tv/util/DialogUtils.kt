package study.strengthen.china.tv.util

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import study.strengthen.china.tv.R

object DialogUtils {

    fun showTextDialog(mContext: Context?, title: String?, msg: String?, leftBtnListener: View.OnClickListener?, rightBtnListener: View.OnClickListener?, vararg msgGravity: Int): Dialog? {
        var gra = Gravity.START
        if (msgGravity.isNotEmpty()) {
            gra = msgGravity[0]
        }
        return showTextDialog(mContext, title, msg, "", "", leftBtnListener, rightBtnListener,"","", gra)
    }
    fun showTextDialog(mContext: Context?, title: String?, msg: String?, leftBtnStr: String?, rightBtnStr: String?, leftBtnListener: View.OnClickListener?, rightBtnListener: View.OnClickListener?, vararg msgGravity: Int): Dialog? {
        var gra = Gravity.START
        if (msgGravity.isNotEmpty()) {
            gra = msgGravity[0]
        }
        return showTextDialog(mContext, title, msg, leftBtnStr, rightBtnStr, leftBtnListener, rightBtnListener,"","", gra)
    }

    fun showTextDialog(mContext: Context?, title: String?, msg: String?, leftBtnStr: String?, rightBtnStr: String?, leftBtnListener: View.OnClickListener?, rightBtnListener: View.OnClickListener?,rightColor: String?, vararg msgGravity: Int): Dialog? {
        var gra = Gravity.START
        if (msgGravity.isNotEmpty()) {
            gra = msgGravity[0]
        }
        return showTextDialog(mContext, title, msg, leftBtnStr, rightBtnStr, leftBtnListener, rightBtnListener,"",rightColor, gra)
    }

    fun showTextDialog(mContext: Context?, title: String?, msg: String?, leftBtnStr: String?, rightBtnStr: String?, leftBtnListener: View.OnClickListener?, rightBtnListener: View.OnClickListener?,leftColor: String?,rightColor: String?, vararg msgGravity: Int): Dialog? {
        if (mContext == null) return null
        val root = LayoutInflater.from(mContext).inflate(R.layout.dialog_title_msg_two_bts_v, null)
        val dialogTitle = root.findViewById<View>(R.id.dialog_title) as TextView
        val msgView = root.findViewById<View>(R.id.message) as TextView
        val cancelView = root.findViewById<View>(R.id.cancel) as TextView
        val okView = root.findViewById<View>(R.id.ok) as TextView
        if (title != null) {
            dialogTitle.text = if (TextUtils.isEmpty(title)) "" else title
        }
        msgView.text = if (TextUtils.isEmpty(msg)) "" else msg
        if (!leftColor.isNullOrEmpty()) {
            cancelView.setTextColor(Color.parseColor(leftColor))
        }
        if (!rightColor.isNullOrEmpty()) {
            okView.setTextColor(Color.parseColor(rightColor))
        }
        if (!leftBtnStr.isNullOrEmpty()) {
            cancelView.text = leftBtnStr
        }
        if (!rightBtnStr.isNullOrEmpty()) {
            okView.text = rightBtnStr
        }
        if (msgGravity.isNotEmpty()) {
            msgView.gravity = msgGravity[0]
        }
        val dialog: Dialog = AlertDialog.Builder(mContext, R.style.progress_dialog).create()
        cancelView.setOnClickListener { v: View? ->
            dialog.dismiss()
            leftBtnListener?.onClick(v)
        }
        okView.setOnClickListener { v: View? ->
            dialog.dismiss()
            rightBtnListener?.onClick(v)
        }
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
        dialog.window?.setContentView(root)
        val width = DensityUtil.getWidth()-DensityUtil.dip2px(120f)
        val params = dialog.window?.attributes
        params?.width = width
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog.window?.attributes = params
        return dialog
    }


//    fun showOneBtDialog(mContext: Context?, title: String?="提示", msg: String?, msgColor: String?, btnStr: String?, btColor: String?, btnListener: View.OnClickListener?, vararg msgGravity: Int) : Dialog? {
//        if (mContext == null) return null
//        val root = LayoutInflater.from(mContext).inflate(R.layout.dialog_title_msg_one_bts, null)
//        root.message.text = msg
//        root.message.setCompoundDrawables(null,null,null,null)
//        msgColor?.let {
//            root.message.setTextColor(Color.parseColor(it))
//        }
//        btnStr?.let {
//            root.match_ok.text = it
//        }
//        btColor?.let {
//            root.match_ok.setTextColor(Color.parseColor(it))
//        }
//        title?.let {
//            root.dialog_title.text = it
//        }
//        val dialog = AlertDialog.Builder(mContext, R.style.progress_dialog).create()
//        root.match_ok.setOnClickListener { v: View? ->
//            btnListener?.onClick(v)
//            dialog.dismiss()
//        }
//        dialog.show()
//        dialog.window?.setContentView(root)
//        val width = DensityUtil.getWidth()-DensityUtil.dip2px(159f)
//        val params = dialog.window?.attributes
//        params?.width = width
//        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
//        dialog.window?.attributes = params
//        return dialog
//    }
}