package com.dgmltn.metaball

import android.content.Context
import android.database.DataSetObserver
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.util.AttributeSet

import java.lang.ref.WeakReference

/**
 * Created by dmelton on 6/29/16.
 */

@ViewPager.DecorView
class ViewPagerMetaballView(context: Context, attrs: AttributeSet?) : MetaballView(context, attrs) {

    private var pager: ViewPager? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (parent is ViewPager && pager == null) {
            val p = parent as ViewPager
            setupWithViewPager(p)
        }
    }

    /**
     * The one-stop shop for setting up this [ViewPagerMetaballView] with a [ViewPager].

     *
     * This method will link the given ViewPager and this ViewPagerMetaballView together so that any
     * changes in one are automatically reflected in the other. This includes adapter changes,
     * scroll state changes, and clicks. The dots displayed in this layout will be populated
     * from the ViewPager adapter's page titles.

     *
     * After this method is called, you will not need this method again unless you want
     * to change the linked ViewPager.

     *
     * If the given ViewPager is non-null, it needs to already have a
     * [PagerAdapter] set.

     * @param p The ViewPager to link, or `null` to clear any previous link.
     */
    fun setupWithViewPager(p: ViewPager?) {
        pager?.removeOnPageChangeListener(listener)

        if (p != null) {
            val adapter = p.adapter ?: throw IllegalArgumentException("ViewPager does not have a PagerAdapter set")
            pager = p
            // Add our custom OnPageChangeListener to the ViewPager
            p.addOnPageChangeListener(listener)
            // Now we'll populate ourselves from the pager adapter
            setPagerAdapter(adapter, true)
        } else {
            // We've been given a null ViewPager so we need to clear out the internal state,
            // listeners and observers
            pager = null
            setPagerAdapter(null, true)
        }
    }

    private fun setPagerAdapter(adapter: PagerAdapter?, addObserver: Boolean) {
        try {
            // If we already have a PagerAdapter, unregister our observer
            pager?.adapter?.unregisterDataSetObserver(observer)
        } catch (e: IllegalStateException) {
            // Ignore "it wasn't registered" error.
        }

        if (addObserver) {
            // Register our observer on the new adapter
            adapter?.registerDataSetObserver(observer)
        }

        // Finally make sure we reflect the new adapter
        populateFromPagerAdapter()
    }

    /**
     * A [ViewPager.OnPageChangeListener] class which contains the
     * necessary calls back to the provided [ViewPagerMetaballView] so that the tab position is
     * kept in sync.
     */
    private val listener = object : ViewPager.OnPageChangeListener {

        override fun onPageScrollStateChanged(state: Int) {
        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            cursorPosition = position + positionOffset
        }

        override fun onPageSelected(position: Int) {
            connectedIndex = position
        }
    }

    /**
     * A [DataSetObserver] that'll change the number of dots if the number of pages changes.
     */
    private val observer = object : DataSetObserver() {
        override fun onChanged() {
            populateFromPagerAdapter()
        }

        override fun onInvalidated() {
            populateFromPagerAdapter()
        }
    }

    private fun populateFromPagerAdapter() {
        dotCount = pager?.adapter?.count ?: 0
        val curItem = pager?.currentItem ?: 0
        connectedIndex = curItem
        cursorPosition = curItem.toFloat()
    }
}
