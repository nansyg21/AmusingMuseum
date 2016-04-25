package com.Anaptixis.AmusingMuseum;

/**
 * Created by Nasia on 30/8/2015.
 */

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

public class HelpscreenSliderActivity extends FragmentActivity {

    private static final int NUM_PAGES = 12; // number of help pages -> static because of implementation

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;

    String[] advices; // Array with help statements for each game

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;
    private int currentApiVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helpscreen_slider);

        advices=getResources().getStringArray(R.array.advices);

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        // Set animation from android standar library
        mPager.setPageTransformer(true, new ZoomOutPageTransformer());

        // ------------------ Code in order to hide the navigation bar -------------------- //
        menu.hideNavBar(this.getWindow());
    }

    // Redefine the flags for hidden navigation bar every time the window has focus, otherwise with the first touch on the screen
    // the navigation bar appears and won't hide again
    @SuppressLint("NewApi")
    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        menu.hideNavBar(this.getWindow());
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // Create new instance of HelpScreenFragment according to the current shown advice
            // newInstance is overrided in the HelpScreenFragment class
            return HelpScreenFragment.newInstance(advices[position]);
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }


}


