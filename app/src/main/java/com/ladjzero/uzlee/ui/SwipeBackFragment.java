package com.ladjzero.uzlee.ui;

import com.ladjzero.uzlee.R;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;

public class SwipeBackFragment extends Fragment implements SwipeBackFragmentI{

	int layout = 0;
	
	public SwipeBackFragment(int layout) {
		super();
		this.layout = layout;
	}

	@Override
	final public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(layout, container, false);
//		v.setOnTouchListener(new OnTouchListener() {
//
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				// TODO Auto-generated method stub
//				return false;
//			}
//			
//		});

		return v;
	}

	@Override
	public void setOnSwipeBackListener(OnSwipeBackListener listener) {
		// TODO Auto-generated method stub
		
	}
	
	

}
