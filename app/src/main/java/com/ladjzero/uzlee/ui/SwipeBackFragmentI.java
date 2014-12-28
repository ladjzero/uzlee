package com.ladjzero.uzlee.ui;

public interface SwipeBackFragmentI {
	public void setOnSwipeBackListener(OnSwipeBackListener listener);
}

interface OnSwipeBackListener {
	void onSwipeBack();
}