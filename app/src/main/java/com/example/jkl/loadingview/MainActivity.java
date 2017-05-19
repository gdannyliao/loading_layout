package com.example.jkl.loadingview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import com.ggdsn.loadinglayout.LoadingLayout;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

	private LoadingLayout loadingLayout;

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		loadingLayout = (LoadingLayout) findViewById(R.id.activity_main);
	}

	@Override public void onClick(View v) {
		switch (v.getId()) {
			case R.id.buttonShow:
				loadingLayout.showLoading("hehe");
				break;
			case R.id.buttonHide:
				loadingLayout.hideLoading();
				break;
			case R.id.buttonError:
				loadingLayout.showError("qunide");
		}
	}
}
