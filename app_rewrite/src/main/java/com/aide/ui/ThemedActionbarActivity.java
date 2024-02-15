package com.aide.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import com.aide.ui.rewrite.R;
import io.github.zeroaicy.aide.preference.ZeroAicySetting;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuInflater;

public class ThemedActionbarActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle bundle) {
		
		enableFollowSystem(false);
		
		super.onCreate(bundle);
		
		if (ZeroAicySetting.isLightTheme()) {
			setTheme(R.style.App_Theme_Material3_Light_ThemedActionbarActivity);
		}
		else {
			setTheme(R.style.App_Theme_Material3_Dark_ThemedActionbarActivity);
		}
    }

	@Override
	protected void onResume() {
		super.onResume();
		enableFollowSystem(true);
	}

	@Override
	public void onConfigurationChanged(Configuration configuration) {
		super.onConfigurationChanged(configuration);
		enableFollowSystem(true);
	}

	private void enableFollowSystem(boolean recreate) {
		if (ZeroAicySetting.enableFollowSystem()) {
			if (ZeroAicySetting.isNightMode(this)) {
				if (ZeroAicySetting.isLightTheme()) {
					//修改主题为暗主题
					ZeroAicySetting.setLightTheme(false);
					if( recreate ) recreate();
				}
			}
			else {
				if (!ZeroAicySetting.isLightTheme()) {
					//修改主题为亮主题
					ZeroAicySetting.setLightTheme(true);
					if( recreate ) recreate();
				}
			}
		}
	}
	public SupportActionbar actionBar;
	@Override
	public android.app.ActionBar getActionBar() {
		if (actionBar == null) {
			actionBar = SupportActionbar.getSupportActionbar(super.getSupportActionBar());
		}
		return actionBar;
	}

	//修复MenuInflater
	MenuInflater menuInflater;
	@Override
	public MenuInflater getMenuInflater(){
		if( this.menuInflater == null ){
			menuInflater = new MenuInflater(this);
		}
		return menuInflater;
	}
}

