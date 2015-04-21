package com.yongchun.sildingupdwon;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends ActionBarActivity {
	private SildingView dragView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		dragView = (SildingView) findViewById(R.id.dragView);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.pull_enable) {
			dragView.setPullEnable(!dragView.isPullEnable());
		}else if(id == R.id.push_enable){
			dragView.setPushEnable(!dragView.isPushEnable());
		}else if(id == R.id.cover_enable){
			dragView.setCover(!dragView.isCover());
		}else if(id == R.id.top_position){
			dragView.setTopPosition(dragView.getTopPosition()==0?200:0);
		}
		return super.onOptionsItemSelected(item);
	}
}
