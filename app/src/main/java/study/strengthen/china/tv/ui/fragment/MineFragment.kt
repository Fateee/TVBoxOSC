package study.strengthen.china.tv.ui.fragment

import study.strengthen.china.tv.R
import study.strengthen.china.tv.base.BaseLazyFragment

class MineFragment : BaseLazyFragment() {
    companion object {
        fun newInstance(): MineFragment {
            return MineFragment()
        }
    }
    override fun init() {
    }

    override fun getLayoutResID() = R.layout.fragment_my
}