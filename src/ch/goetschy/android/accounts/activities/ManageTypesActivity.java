package ch.goetschy.android.accounts.activities;

import java.util.ArrayList;

import ch.goetschy.android.accounts.BuildConfig;
import ch.goetschy.android.accounts.R;
import ch.goetschy.android.accounts.contentprovider.MyAccountsContentProvider;
import ch.goetschy.android.accounts.database.TypeTable;
import ch.goetschy.android.accounts.objects.Account;
import ch.goetschy.android.accounts.objects.Transaction;
import ch.goetschy.android.accounts.objects.Type;
import android.app.Activity;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class ManageTypesActivity extends ListActivity {

	private TypesAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// listview
		this.setContentView(R.layout.activity_types);
		this.getListView().setDividerHeight(2);
		fillData();
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
		} else if(adapter != null)
			adapter.clear();

	}

	private void createType() {
		Intent intent = new Intent(this, EditTypeActivity.class);
		startActivity(intent);
	}

	// ADD BUTTON ------------------------

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_types, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_types_add:
			createType();
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
