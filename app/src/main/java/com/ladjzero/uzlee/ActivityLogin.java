package com.ladjzero.uzlee;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ladjzero.hipda.Core;
import com.rey.material.widget.Spinner;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by chenzhuo on 15-11-8.
 */
public class ActivityLogin extends ActionBarActivity {
	@Bind(R.id.user_name) TextView name;
	@Bind(R.id.user_password) TextView passwd;
	@Bind(R.id.answer) TextView answer;
	@Bind(R.id.question) Spinner spn;

	@OnClick(R.id.login) void onLogin() {
		Core.login(name.getText().toString(), passwd.getText().toString(), spn.getSelectedItemPosition(), answer.getText().toString(), new Core.OnRequestListener() {
			@Override
			public void onError(String error) {

			}

			@Override
			public void onSuccess(String html) {
				Utils.showToast(ActivityLogin.this, "登录成功");
				startActivity(new Intent(ActivityLogin.this, ActivityMain.class));
			}
		});
	}

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_start);
		ButterKnife.bind(this);

		Core.setup(this, false);

		String[] questions = getResources().getStringArray(R.array.questions);
		spn.setAdapter(new ArrayAdapter<>(this, R.layout.item_spinner, questions));
		spn.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
			@Override
			public void onItemSelected(Spinner spinner, View view, int i, long l) {
				answer.setVisibility(i == 0 ? View.GONE : View.VISIBLE);
			}
		});
	}
}
