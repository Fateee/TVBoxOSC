package study.strengthen.china.tv.ui.activity

import android.graphics.Color
import android.graphics.Typeface
import android.view.Menu
import android.view.View
import androidx.viewpager.widget.ViewPager
import study.strengthen.china.tv.base.BaseActivity
import study.strengthen.china.tv.base.BaseLazyFragment
import study.strengthen.china.tv.ui.adapter.HomePageAdapter
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.orhanobut.hawk.Hawk
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.tab_item.view.*
import study.strengthen.china.tv.R
import study.strengthen.china.tv.ui.fragment.HomeFragment
import study.strengthen.china.tv.ui.fragment.MineFragment
import study.strengthen.china.tv.ui.fragment.RankFragment
import study.strengthen.china.tv.util.HawkConfig

class MainActivity : BaseActivity() {

    private val listFragment : ArrayList<BaseLazyFragment> = arrayListOf()
    private var focusTab: View? = null

    override fun getLayoutResID() = R.layout.activity_main

    override fun init() {
        //todo hy
        Hawk.put(HawkConfig.HOME_REC, 1)
        val menu: Menu = nav_view.menu
        for (i in 0 until menu.size()) {
            val view = (nav_view.getChildAt(0) as BottomNavigationMenuView).getChildAt(i) as BottomNavigationItemView
            val itemBottomNavigation = layoutInflater.inflate(R.layout.tab_item, null, false)
            itemBottomNavigation?.tab_iv?.setImageDrawable(menu.getItem(i).icon)
            itemBottomNavigation?.tab_tv?.text = menu.getItem(i).title
            view.removeAllViews()
            view.addView(itemBottomNavigation)
        }
        initPagesWithPager()
    }
    private fun initPagesWithPager() {
        listFragment.apply {
            add(HomeFragment())
            add(RankFragment())
            add(MineFragment.newInstance())
//            add(staticHorFragment)
//            add(hotRankFragment)
//            add(albumFragment)
        }
        val pagerAdapter = HomePageAdapter(supportFragmentManager, listFragment)
        mainPager?.adapter = pagerAdapter
        mainPager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                showCurrentTab(position)
            }
        })
        nav_view?.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> {
//                    SAStatistics.track("main_tab","dynamic")
                    setStatusBarColor(Color.WHITE)
                    mainPager.setCurrentItem(0,false)
                }
                R.id.navigation_live -> {
//                    SAStatistics.track("main_tab","quiet")
                    setStatusBarColor(Color.WHITE)
                    mainPager.setCurrentItem(1,false)
                }
                R.id.navigation_mine -> {
//                    SAStatistics.track("main_tab","quiet")
                    setStatusBarColor(Color.parseColor("#1d1d1d"))
                    mainPager.setCurrentItem(2,false)
                }
//                R.id.navigation_diy -> {
////                    SAStatistics.track("home","diy_tab_click")
//                    mainPager.setCurrentItem(2,false)
//                }
//                R.id.navigation_rank -> {
////                    SAStatistics.track("main_tab","me")
//                    if (TokenManger.instance.isLogined()) {
//                        viewModel.model.getCollectionIds(Consts.VIDEO)
//                        viewModel.model.getCollectionIds(Consts.IMAGE)
//                        viewModel.model.getCollectionIds(Consts.HOR_IMAGE)
//                    }
//                    mainPager.setCurrentItem(3,false)
//                }
//                R.id.navigation_album -> {
//                    mainPager.setCurrentItem(4,false)
//                }
            }
            true
        }
        showCurrentTab(0)
    }
    private fun showCurrentTab(selectIndex: Int) {
        focusTab?.tabItem?.isSelected = false
        focusTab?.tab_tv?.setTextColor(resources.getColor(R.color.c161616))
        focusTab?.tab_tv?.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
        focusTab = (nav_view.getChildAt(0) as BottomNavigationMenuView).getChildAt(selectIndex)
        focusTab?.tabItem?.isSelected = true
        focusTab?.tab_tv?.setTextColor(resources.getColor(R.color.main_color))
        focusTab?.tab_tv?.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
    }
}