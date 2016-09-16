package com.dgmltn.metaball;

/**
 * Created by dmelton on 9/15/16.
 */

import android.database.DataSetObserver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

/**
 * All the listeners having to do with hooking the MetaballView up to a ViewPager. Initializing
 * an instance of this class attaches all the appropriate listeners.
 */
class ViewPagerHelper {

    private final MetaballView metaball;
    private final ViewPager pager;

    public ViewPagerHelper(@NonNull MetaballView metaball, @NonNull ViewPager pager) {
        this.metaball = metaball;
        this.pager = pager;
        pager.removeOnAdapterChangeListener(adapterChangeListener);
        pager.addOnAdapterChangeListener(adapterChangeListener);
        pager.removeOnPageChangeListener(pageChangeListener);
        pager.addOnPageChangeListener(pageChangeListener);
        populateFromPagerAdapter();
    }

    /**
     * A [ViewPager.OnAdapterChangeListener] object that updates the dots if the ViewPager's adapter changes.
     */
    private ViewPager.OnAdapterChangeListener adapterChangeListener = new ViewPager.OnAdapterChangeListener() {
        @Override
        public void onAdapterChanged(@NonNull ViewPager viewPager, @Nullable PagerAdapter oldAdapter, @Nullable PagerAdapter newAdapter) {
            if (oldAdapter != null) {
                oldAdapter.unregisterDataSetObserver(observer);
            }
            if (newAdapter != null) {
                newAdapter.registerDataSetObserver(observer);
            }
            populateFromPagerAdapter();
        }
    };

    /**
     * A [ViewPager.OnPageChangeListener] object to keep the dot position in sync.
     */
    private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            metaball.setCursorPosition(position + positionOffset);
        }

        @Override
        public void onPageSelected(int position) {
            metaball.setConnectedIndex(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            // Intentionally left blank
        }
    };

    /**
     * A [DataSetObserver] that'll change the number of dots if the number of pages changes.
     */
    private DataSetObserver observer = new DataSetObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            populateFromPagerAdapter();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            populateFromPagerAdapter();
        }
    };

    private void populateFromPagerAdapter() {
        metaball.setDotCount(pager.getAdapter() != null ? pager.getAdapter().getCount() : 0);
        int curItem = pager.getCurrentItem();
        metaball.setConnectedIndex(curItem);
        metaball.setCursorPosition(curItem);
        metaball.requestLayout();
    }

    void detach() {
        pager.removeOnAdapterChangeListener(adapterChangeListener);
        pager.removeOnPageChangeListener(pageChangeListener);
    }
}

