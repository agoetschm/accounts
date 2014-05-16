package ch.goetschy.android.accounts.activities;

import java.util.ArrayList;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import ch.goetschy.android.accounts.BuildConfig;
import ch.goetschy.android.accounts.R;
import ch.goetschy.android.accounts.contentprovider.MyAccountsContentProvider;
import ch.goetschy.android.accounts.objects.Type;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

public class ManageTypesActivity extends SherlockListActivity {

	private TypesAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// listview
		this.setContentView(R.layout.activity_types);
		this.getListView().setDividerHeight(2);
		fillData();

		// ACTION BAR ------------------
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	private void fillData() {
		ArrayList<Type> listTypes = Type.getTypes(getContentResolver());
		if (listTypes != null) {
			if (BuildConfig.DEBUG)
				Log.w("typesDetail", "break1");
			adapter = new TypesAdapter(this, listTypes);
			if (BuildConfig.DEBUG)
				Log.w("typesDetail", "list size : " + listTypes.size());
			if (BuildConfig.DEBUG) {
				for (Type i : listTypes) {
					Log.w("typesDetail", i.getName());
				}
			}
			this.setListAdapter(adapter);
		} else if (adapter != null)
			adapter.clear();

	}

	private void createType() {
		Intent intent = new Intent(this, EditTypeActivity.class);
		startActivity(intent);
	}

	// ADD BUTTON ------------------------

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_types, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_types_add:
			createType();
			return true;
		case android.R.id.home:
			finish();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	// EDIT TYPE -------------------

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		Intent intent = new Intent(this, EditTypeActivity.class);
		Uri typeUri = Uri.parse(MyAccountsContentProvider.CONTENT_URI_TYPES
				+ "/" + id);
		intent.putExtra(MyAccountsContentProvider.CONTENT_ITEM_TYPE, typeUri);

		startActivity(intent);

	}

	// ---------------------------------------------

	@Override
	protected void onResume() {
		super.onResume();
		fillData();
	}
}
