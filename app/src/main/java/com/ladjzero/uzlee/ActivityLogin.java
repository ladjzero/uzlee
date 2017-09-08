package com.ladjzero.uzlee;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ladjzero.hipda.api.OnRespondCallback;
import com.ladjzero.hipda.api.Response;
import com.ladjzero.hipda.entities.User;
import com.ladjzero.uzlee.stores.MetaStore;
import com.ladjzero.uzlee.utils.Utils;
import com.rey.material.app.Dialog;
import com.rey.material.widget.Spinner;
import com.tencent.stat.StatConfig;
import com.tencent.stat.StatService;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by chenzhuo on 15-11-8.
 */
public class ActivityLogin extends ActionBarActivity {
	@Bind(R.id.user_name)
	TextView name;
	@Bind(R.id.user_password)
	TextView passwd;
	@Bind(R.id.answer)
	TextView answer;
	@Bind(R.id.question)
	Spinner spn;
	@Bind(R.id.logo)
	View logo;
	private User mUser;

	@OnClick(R.id.login)
	void onLogin() {
		App.getInstance().getApi().login(
				name.getText().toString(),
				passwd.getText().toString(),
				spn.getSelectedItemPosition(),
				answer.getText().toString(),
				new OnRespondCallback() {
					@Override
					public void onRespond(Response res) {
						if (res.isSuccess()) {
							Utils.showToast(ActivityLogin.this, "登录成功");
							Utils.replaceActivity(ActivityLogin.this, ActivityMain.class);
						} else {
							Utils.showToast(ActivityLogin.this, res.getData().toString());
						}
					}
				}
		);
	}

	@OnClick(R.id.register)
	void onRegister() {
		final Dialog dialog = new Dialog(this);
		dialog.title("注册")
				.contentView(R.layout.register_info)
				.positiveAction("前往HiPDA注册")
				.negativeAction("取消")
				.positiveActionClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						String url = "http://www.hi-pda.com/forum/tobenew.php";

						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setData(Uri.parse(url));
						startActivity(intent);
					}
				})
				.negativeActionClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						dialog.dismiss();
					}
				})
				.cancelable(true)
				.show();
	}

	@OnClick(R.id.view_as_visitor)
	void viewAsVisitor() {
		Utils.replaceActivity(this, ActivityMain.class);
	}

	@Override
	protected void onCreate(Bundle bundle) {
		SharedPreferences setting = PreferenceManager.getDefaultSharedPreferences(this);
		String themeColor = setting.getString("theme", ActivityBase.DefaultTheme);
		setTheme(Utils.getTheme(themeColor));

		super.onCreate(bundle);
		setContentView(R.layout.activity_login);
		ButterKnife.bind(this);

		Animation animation = AnimationUtils.loadAnimation(this, R.anim.rotate);
		logo.setAnimation(animation);

		Integer userId = MetaStore.getMeta().getUid();

		if (userId != null) {
			Utils.replaceActivity(this, ActivityMain.class);
		} else {
			String[] questions = getResources().getStringArray(R.array.questions);
			spn.setAdapter(new ArrayAdapter<>(this, R.layout.item_spinner, questions));
			spn.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
				@Override
				public void onItemSelected(Spinner spinner, View view, int i, long l) {
					answer.setVisibility(i == 0 ? View.GONE : View.VISIBLE);
				}
			});
		}


		StatConfig.setDebugEnable(true);
		StatService.trackCustomEvent(this, "onCreate", "");
	}
}
