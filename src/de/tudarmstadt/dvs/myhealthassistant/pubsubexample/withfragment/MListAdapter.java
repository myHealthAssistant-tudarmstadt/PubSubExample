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

import java.util.ArrayList;

import de.tudarmstadt.dvs.myhealthassistant.pubsubexample.R;
import de.tudarmstadt.dvs.myhealthassistant.pubsubexample.withfragment.MFragment.MEvent;
import android.R.color;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;

/**
 * 
 * @author HieuHa
 * 
 */
public class MListAdapter extends ArrayAdapter<MEvent> {
	private static final int POS_TAG = 1 + 2 << 24;
	private LayoutInflater mInflater;

	public MListAdapter(Context context, int resource) {
		super(context, resource);
		mInflater = (LayoutInflater) LayoutInflater.from(context);
	}

	static class ViewHolder {
		private TextView mTitle;
		private CheckBox mCheckbox;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final View view;
		if (convertView == null) {
			view = mInflater.inflate(R.layout.item_row, parent, false);
		} else {
			view = convertView;
		}

		final MEvent item = getItem(position);

		// Create the view holder
		ViewHolder viewHolder = new ViewHolder();
		viewHolder.mTitle = (TextView) view.findViewById(R.id.titleText);
		viewHolder.mCheckbox = (CheckBox) view.findViewById(R.id.genSW);

		if (item.isOnAdv()){
			viewHolder.mTitle.setTextColor(Color.GREEN);
		} else if (item.isOnSub()){
			viewHolder.mTitle.setTextColor(Color.RED);
		} else {
			viewHolder.mTitle.setTextColor(Color.BLACK);
		}
		
		
		viewHolder.mCheckbox.setEnabled(item.isOnAdv());
		viewHolder.mCheckbox
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked)
							item.onGenEvent();
						else
							item.stopGenEvent();
					}
				});

		viewHolder.mTitle.setText(item.toString());
		view.setTag(POS_TAG, position);

		MyRunnable mLongPressed = new MyRunnable(view);
		view.setOnTouchListener(new MOnTouch(mLongPressed));
		return view;
	}

	private class MOnTouch implements OnTouchListener {
		private MyRunnable mLongPressed;

		public MOnTouch(MyRunnable mLongPressed) {
			this.mLongPressed = mLongPressed;
		}

		@SuppressWarnings("deprecation")
		@Override
		public boolean onTouch(View v, MotionEvent evt) {
			// to dispatch click / long click event,
			// you must pass the event to it's default callback
			// View.onTouchEvent
			boolean defaultResult = v.onTouchEvent(evt);
			switch (evt.getAction()) {
			case MotionEvent.ACTION_DOWN: {
				// backward compatibility
				if (android.os.Build.VERSION.SDK_INT < 16)
					v.setBackgroundDrawable(getContext().getResources()
							.getDrawable(android.R.color.holo_blue_light));
				else
					v.setBackground(getContext().getResources().getDrawable(
							android.R.color.holo_blue_light));
				handler.postDelayed(mLongPressed, 50); // delay 50ms
				break;
			}
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP: {
				handler.removeCallbacks(mLongPressed);
				if (android.os.Build.VERSION.SDK_INT < 16)
					v.setBackgroundDrawable(null);
				else
					v.setBackground(null);

			}
			case MotionEvent.ACTION_OUTSIDE: {
				handler.removeCallbacks(mLongPressed);
				if (android.os.Build.VERSION.SDK_INT < 16)
					v.setBackgroundDrawable(null);
				else
					v.setBackground(null);
				break;
			}
			default:
				return defaultResult;
			}

			// if you reach here, you have consumed the event
			return true;
		}
	}

	final Handler handler = new Handler();

	private class MyRunnable implements Runnable {
		private View v;

		public MyRunnable(View v) {
			this.v = v;
		}

		@Override
		public void run() {
			showPopupMenu(v);
		}
	}

	private void showPopupMenu(View v) {
		int pos = (Integer) v.getTag(POS_TAG);
		final MEvent event = getItem(pos);
		PopupMenu popupMenu = new PopupMenu(this.getContext(), v);
		popupMenu.getMenuInflater().inflate(R.menu.popupmenugenevt,
				popupMenu.getMenu());

		popupMenu
				.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

					@Override
					public boolean onMenuItemClick(MenuItem item) {
						switch (item.getItemId()) {
						case R.id.m_adv:
							event.onAdvEvent();
							break;
						case R.id.m_unadv:
							event.onUnadvEvent();
							break;
						case R.id.m_sub:
							event.onSubEvent();
							break;
						case R.id.m_unsub:
							event.onUnsubEvent();
							break;

						}
						return true;
					}
				});
		popupMenu.show();
	}

	public void setData(ArrayList<MEvent> mList) {
		clear();
		if (mList != null) {
			for (int i = 0; i < mList.size(); i++) {
				add(mList.get(i));
			}
		}
	}

}