package study.strengthen.china.tv.ui.activity

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.activity_splash.*
import study.strengthen.china.tv.R
import study.strengthen.china.tv.base.BaseActivity
import study.strengthen.china.tv.config.AppConfigApi
import study.strengthen.china.tv.util.DensityUtil

class SplashActivity : BaseActivity() {

    override fun getLayoutResID() = R.layout.activity_splash

    override fun init() {
        val statusBarHeight = getStatusBarHeight(this)
        splashLogo?.apply {
            (layoutParams as ConstraintLayout.LayoutParams?)?.apply {
                bottomMargin = statusBarHeight
            }
            setImageResource(R.drawable.logo_splash)
        }
        // 创建动画对象，设置要移动的属性为 "translationY"，起始位置为 0，结束位置为 200dp
        val animator = ObjectAnimator.ofFloat(splashLogo, "translationY", 0f, -DensityUtil.dip2px(250f).toFloat())
        animator.duration = 1000 // 设置动画持续时间为 1 秒
        animator.interpolator = AccelerateDecelerateInterpolator() // 设置插值器，使动画加速再减速
        animator.start() // 启动动画

        AppConfigApi.getAppConfig { _,_,_->
            splashRoot?.postDelayed({
                startActivity(Intent(this,MainActivity::class.java))
                finish()
            },1000)
        }
    }


    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        val decorView: View = window.decorView
        decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    override fun isFullScreen(): Boolean {
        return true
    }

    override fun isHideNavigation(): Boolean {
        return true
    }

    /**
     * 获取状态栏的高度
     * @param context
     * @return
     */
    private fun getStatusBarHeight(context: Context): Int {
        return try {
            context.resources.getDimensionPixelSize(
                context.resources.getIdentifier(
                    "status_bar_height",
                    "dimen",
                    "android"
                )
            )
        } catch (e : Exception) {
            0
        }
    }
}