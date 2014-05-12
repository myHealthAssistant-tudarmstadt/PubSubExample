/* 
 * Copyright (C) 2014 TU Darmstadt, Hessen, Germany.
 * Department of Computer Science Databases and Distributed Systems
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */ 
 
 package de.tudarmstadt.dvs.myhealthassistant.pubsubexample.withfragment;

import de.tudarmstadt.dvs.myhealthassistant.pubsubexample.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;

/**
 * @author HieuHa
 */
public class MFragmentActivity extends FragmentActivity {
	private static final String TAG = MFragmentActivity.class.getSimpleName();
	private ViewPager mViewPager;
	private Menu mOptionsMenu;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home_fragment);
		
		mViewPager = (ViewPager) findViewById(R.id.pager);
		FragmentManager fm = getSupportFragmentManager();
		if (mViewPager != null) {
            mViewPager.setAdapter(new HomePagerAdapter(fm));
		}
	}
	
	private class HomePagerAdapter extends FragmentPagerAdapter {
        public HomePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
            case 0:
            	return new MFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 1;
        }
    }
}