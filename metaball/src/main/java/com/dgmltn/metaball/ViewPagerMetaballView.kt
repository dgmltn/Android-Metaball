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

    private var mViewPager: ViewPager? = null
    private var mPagerAdapter: PagerAdapter? = null
    private var mPagerAdapterObserver: DataSetObserver? = null
    private var mPageChangeListener: MetaballViewOnPageChangeListener? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (parent is ViewPager && mViewPager == null) {
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

     * @param viewPager The ViewPager to link, or `null` to clear any previous link.
     */
    fun setupWithViewPager(viewPager: ViewPager?) {
        if (mPageChangeListener != null) {
            // If we've already been setup with a ViewPager, remove us from it
            mViewPager?.removeOnPageChangeListener(mPageChangeListener)
        }

        if (viewPager != null) {
            val adapter = viewPager.adapter ?: throw IllegalArgumentException("ViewPager does not have a PagerAdapter set")
            mViewPager = viewPager
            // Add our custom OnPageChangeListener to the ViewPager
            if (mPageChangeListener == null) {
                mPageChangeListener = MetaballViewOnPageChangeListener(this)
            }
            viewPager.addOnPageChangeListener(mPageChangeListener)
            // Now we'll populate ourselves from the pager adapter
            setPagerAdapter(adapter, true)
        }
        else {
            // We've been given a null ViewPager so we need to clear out the internal state,
            // listeners and observers
            mViewPager = null
            setPagerAdapter(null, true)
        }
    }

    private fun setPagerAdapter(adapter: PagerAdapter?, addObserver: Boolean) {
        if (mPagerAdapterObserver != null) {
            // If we already have a PagerAdapter, unregister our observer
            mPagerAdapter?.unregisterDataSetObserver(mPagerAdapterObserver)
        }

        mPagerAdapter = adapter
        if (addObserver && adapter != null) {
            // Register our observer on the new adapter
            if (mPagerAdapterObserver == null) {
                mPagerAdapterObserver = PagerAdapterObserver()
            }
            adapter.registerDataSetObserver(mPagerAdapterObserver)
        }

        // Finally make sure we reflect the new adapter
        populateFromPagerAdapter()
    }

    /**
     * A [ViewPager.OnPageChangeListener] class which contains the
     * necessary calls back to the provided [ViewPagerMetaballView] so that the tab position is
     * kept in sync.

     *
     * This class stores the provided TabLayout weakly, meaning that you can use
     * [ addOnPageChangeListener(OnPageChangeListener)][ViewPager.addOnPageChangeListener] without removing the listener and
     * not cause a leak.
     */
    private class MetaballViewOnPageChangeListener(view: ViewPagerMetaballView) : ViewPager.OnPageChangeListener {

        private val mMetaballView = WeakReference(view)

        override fun onPageScrollStateChanged(state: Int) {
        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            val view = mMetaballView.get()
            view?.cursorPosition = position + positionOffset
        }

        override fun onPageSelected(position: Int) {
            val view = mMetaballView.get()
            view?.connectedIndex = position
        }
    }

    private inner class PagerAdapterObserver : DataSetObserver() {
        override fun onChanged() {
            populateFromPagerAdapter()
        }

        override fun onInvalidated() {
            populateFromPagerAdapter()
        }
    }

    private fun populateFromPagerAdapter() {
        dotCount = mPagerAdapter?.count ?: 0
        val curItem = mViewPager?.currentItem ?: 0
        connectedIndex = curItem
        cursorPosition = curItem.toFloat()
    }
}
