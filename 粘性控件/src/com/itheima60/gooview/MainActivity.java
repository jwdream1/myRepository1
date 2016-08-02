package com.itheima60.gooview;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

import com.itheima60.gooview.GooView.OnUpdateListener;
import com.itheima60.gooview.util.Utils;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		GooView view = new GooView(this);
		setContentView(view);

		view.setOnUpdateListener(new OnUpdateListener() {
			
			@Override
			public void onReset(boolean isOutOfRange) {
				Utils.showToast(getApplicationContext(), "恢复: " + isOutOfRange);
			}
			
			@Override
			public void onDisappear() {
				Utils.showToast(getApplicationContext(), "消失");
			}
		});
	}


}
