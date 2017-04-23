package com.ladjzero.uzlee;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ladjzero.uzlee.utils.Utils;
import com.rey.material.widget.Button;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by chenzhuo on 2017/4/13.
 */
public class FragmentToPickForums extends Fragment {
	@Bind(R.id.toPickForumns)
	Button mButton;

	public static FragmentToPickForums newInstance(Bundle bundle) {
		FragmentToPickForums fragment = new FragmentToPickForums();
		return fragment;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_to_pick_forums, container, false);
		ButterKnife.bind(this, rootView);

		mButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Utils.replaceActivity(getActivity(), ActivityForumPicker.class);
			}
		});

		return rootView;
	}
}
