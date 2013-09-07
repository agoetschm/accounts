package ch.goetschy.android.accounts.activities;

import java.util.ArrayList;

import ch.goetschy.android.accounts.R;
import ch.goetschy.android.accounts.R.layout;
import ch.goetschy.android.accounts.R.menu;
import android.os.Bundle;
import android.app.Activity;
import android.app.ListActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

public class AccountsOverviewActivity extends ListActivity {
	private ArrayAdapter<String> adapter;
	// test
	private ArrayList<String> list;
	// end test
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_overview);
		fillData();
		this.registerForContextMenu(getListView());
	}

	private void fillData() {
		// test adapter
		String[] values = new String[] { "one", "two", "three" };
		list = new ArrayList<String>();
		for(int i = 0; i < values.length; i++){
			list.add(values[i]);
		}
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
		// end test
		this.getListView().setAdapter(adapter);
	}


	private void createAccount() {
		// test
		adapter.add("new one");
		// end test
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_overview, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_add:
			createAccount();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
