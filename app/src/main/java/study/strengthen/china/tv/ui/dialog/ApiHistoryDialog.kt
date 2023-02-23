package study.strengthen.china.tv.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.postDelayed
import com.orhanobut.hawk.Hawk
import com.owen.tvrecyclerview.widget.TvRecyclerView
import kotlinx.android.synthetic.main.dialog_subscribe.view.*
import study.strengthen.china.tv.R
import study.strengthen.china.tv.ui.adapter.ApiHistoryDialogAdapter
import study.strengthen.china.tv.util.DensityUtil
import study.strengthen.china.tv.util.HawkConfig
import java.util.*

class ApiHistoryDialog(context: Context) : BaseDialog(context, R.style.progress_dialog) {
//    override fun onCreate(savedInstanceState: Bundle) {
//        super.onCreate(savedInstanceState)
//    }

    fun setTip(tip: String?) {
        (findViewById<View>(R.id.title) as TextView).text = tip
    }

    fun setAdapter(sourceBeanSelectDialogInterface: ApiHistoryDialogAdapter.SelectDialogInterface?, data: List<String?>?, select: Int) {
        val adapter = ApiHistoryDialogAdapter(sourceBeanSelectDialogInterface)
        adapter.setData(data, select)
        val tvRecyclerView = findViewById<View>(R.id.list) as TvRecyclerView
        tvRecyclerView.adapter = adapter
        tvRecyclerView.selectedPosition = select
        tvRecyclerView.post { tvRecyclerView.scrollToPosition(select) }
    }

    private fun showDialog(): Dialog {
        dismiss()
        val dialog: Dialog = AlertDialog.Builder(context, R.style.progress_dialog).create()
        val root = LayoutInflater.from(context).inflate(R.layout.dialog_subscribe, null)
        root?.inputSubmit?.setOnClickListener {
//            val nameStr = root.name?.text?.toString()
            val newApi = root.url?.text?.toString()
            if (newApi != null) {
                if (!newApi.isEmpty() && (newApi.startsWith("http") || newApi.startsWith("clan"))) {
                    val history = Hawk.get(HawkConfig.API_HISTORY, ArrayList<String>())
                    if (!history.contains(newApi)) history.add(0, newApi)
                    if (history.size > 10) history.removeAt(10)
                    Hawk.put(HawkConfig.API_HISTORY, history)
                    listener?.onchange(newApi)
                    dismiss()
                } else {
                    Toast.makeText(context,"订阅地址内容不能为空或以http开头",Toast.LENGTH_SHORT).show()
                }
            }
            dialog.dismiss()
        }
        dialog.setOnShowListener {
            root?.url?.requestFocus()
            root?.url?.postDelayed({
                val imm: InputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(root?.url, InputMethodManager.SHOW_IMPLICIT)
            },100)
        }
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.show()
        dialog.window?.setContentView(root)
        val width = DensityUtil.getWidth()- DensityUtil.dip2px(100f)
        val params = dialog.window?.attributes
        params?.width = width
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog.window?.attributes = params
        dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        return dialog
    }

    init {
        setContentView(R.layout.dialog_api_history)
        findViewById<View>(R.id.addApiTv).setOnClickListener { v: View? ->
            showDialog()
        }
        val width = DensityUtil.getWidth()- DensityUtil.dip2px(100f)
        val params = window?.attributes
        params?.width = width
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        window?.attributes = params
    }

    fun setOnListener(listener: OnListener?) {
        this.listener = listener
    }

    var listener: OnListener? = null

    interface OnListener {
        fun onchange(api: String?)
    }
}