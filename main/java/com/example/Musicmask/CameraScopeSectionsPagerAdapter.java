package com.example.Musicmask;



/**
 *    Copyright 2021 The MathWorks, Inc.
 */

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the scopes or camera view.
 */
class CameraScopeSectionsPagerAdapter extends FragmentStateAdapter {

    public CameraScopeSectionsPagerAdapter(FragmentActivity fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a CameraScopeFragment (defined as a static inner class below).
        return CameraScopeFragment.newInstance(position + 1);
    }

    @Override
    public int getItemCount() {
        // Show 3 total pages.
        return 2;
    }

}
