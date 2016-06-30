package com.dgmltn.metaball.demo

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

class SampleFragmentPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    private val tabTitles = arrayOf("Tab1", "Tab2", "Tab3", "Tab4")
    internal val PAGE_COUNT = tabTitles.size

    override fun getCount(): Int {
        return PAGE_COUNT
    }

    override fun getItem(position: Int): Fragment {
        return PageFragment.newInstance(position + 1)
    }

    override fun getPageTitle(position: Int): CharSequence {
        // Generate title based on item position
        return tabTitles[position]
    }
}