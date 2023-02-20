package study.strengthen.china.tv.ui.fragment

import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_home.*
import study.strengthen.china.tv.R
import study.strengthen.china.tv.base.BaseLazyFragment
import study.strengthen.china.tv.ui.adapter.HomePageAdapter
import java.util.ArrayList

class RankFragment : BaseLazyFragment() {

    private val fragments: MutableList<BaseLazyFragment> = ArrayList()

    override fun getLayoutResID() = R.layout.fragment_home

    override fun init() {
        tabLayout?.newTab()?.setText("豆瓣热门")?.let { tabLayout?.addTab(it) }
        tabLayout?.newTab()?.setText("360热门")?.let { tabLayout?.addTab(it) }
        tabLayout?.newTab()?.setText("IQY热门")?.let { tabLayout?.addTab(it) }
        tabLayout?.tabMode = TabLayout.MODE_FIXED
        fragments.add(HotFragment.newInstance(null))
        fragments.add(HotFragment.newInstance(null,1))
        fragments.add(HotFragment.newInstance(null,2))
        val pageAdapter = HomePageAdapter(childFragmentManager, fragments)
        mViewPager?.adapter = pageAdapter
        mViewPager?.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
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