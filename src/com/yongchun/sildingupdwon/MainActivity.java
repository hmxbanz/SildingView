package com.yongchun.sildingupdwon;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

@SuppressLint("NewApi")
public class MainActivity extends ActionBarActivity {
	public SildingView dragView;
	private ListView base_list;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		dragView = (SildingView) findViewById(R.id.dragView);
		base_list = (ListView)findViewById(R.id.base_list);
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1);
		adapter.addAll(getResources().getStringArray(R.array.countries));
		base_list.setAdapter(adapter);
		base_list.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				
			}
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (base_list != null && base_list.getChildCount() > 0) {
		            if (base_list.getChildAt(0).getTop() < 0) {
		            	dragView.setDragStart(false);
		                return;
		            }
		        }
				dragView.setDragStart(true);
			}
		});
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
			dragView.setTopOffset(dragView.getTopOffset()==0?200:0);
		}
		return super.onOptionsItemSelected(item);
	}
}
