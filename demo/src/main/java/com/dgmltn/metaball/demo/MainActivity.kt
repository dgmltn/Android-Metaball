package com.dgmltn.metaball.demo

import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import com.dgmltn.metaball.ViewPagerMetaballView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        val viewPager = findViewById(R.id.viewpager) as ViewPager
        viewPager.adapter = SampleFragmentPagerAdapter(supportFragmentManager, this)

        // Give the TabLayout the ViewPager
        val dots = findViewById(R.id.viewpager_dots) as ViewPagerMetaballView
        dots.setupWithViewPager(viewPager)
    }
}
