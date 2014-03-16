package ch.goetschy.android.accounts.activities;

import java.util.concurrent.Callable;

import ch.goetschy.android.accounts.R;
import ch.goetschy.android.accounts.activities.ColorPickerDialog.OnColorChangedListener;
import ch.goetschy.android.accounts.contentprovider.MyAccountsContentProvider;
import ch.goetschy.android.accounts.database.AccountsTable;
import ch.goetschy.android.accounts.database.TypeTable;
import ch.goetschy.android.accounts.objects.Account;
import ch.goetschy.android.accounts.objects.Type;
import android.app.Activity;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class EditTypeActivity extends Activity implements
		OnColorChangedListener {

	private EditText mName;
	private Button mColor;
	private Type type;
	private Paint mPaint;

	private boolean mDelete = false;
	private boolean editing;

	private String baseName = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_edit_type);

		mName = (EditText) findViewById(R.id.edit_type_name);
		mColor = (Button) findViewById(R.id.edit_type_color);
		Button confirmButton = (Button) findViewById(R.id.edit_type_confirm);
		Button deleteButton = (Button) findViewById(R.id.edit_type_delete);

		// data from parent activity
		Bundle extras = getIntent().getExtras();

		// type object
		type = new Type();
		// paint object
		mPaint = new Paint();
		mPaint.setColor(Type.DEFAULT_COLOR);

		// if saved instance
		type.setUri((savedInstanceState == null) ? null
				: (Uri) savedInstanceState
						.getParcelable(MyAccountsContentProvider.CONTENT_ITEM_TYPE));
		// edit or add
		if (extras != null) {
			type.setUri((Uri) extras
					.getParcelable(MyAccountsContentProvider.CONTENT_ITEM_TYPE));

			fillData();
			editing = true;
		} else
			editing = false;

		// color
		mColor.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new ColorPickerDialog(EditTypeActivity.this,
						EditTypeActivity.this, mPaint.getColor(),
						EditTypeActivity.this.getWindow().getDecorView()
								.getWidth()).show();
			}
		});

		// confirm
		confirmButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (TextUtils.isEmpty(mName.getText().toString())) {
					makeToast("Please enter a name");
				} else {
					setResult(RESULT_OK);
					finish();
				}
			}
		});

		// delete
		deleteButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				if (type.getUri() == null) {
					finish();
				} else {
					MyDialog.confirm(EditTypeActivity.this,
							R.string.edit_type_delete_question, new Callable() {
								@Override
								public Object call() throws Exception {
									// if yes, delete
									mDelete = true; // do not save
									type.delete(getContentResolver());
									finish();
									return null;
								}
							}, null);
				}
			}
		});
	}

	private void fillData() {
		if (type.loadFromDB(getContentResolver())) {
			mName.setText(type.getName());
			mPaint.setColor(type.getColor());
			mColor.setBackgroundColor(type.getColor());
			

			// detecting name changing
			baseName = type.getName();
		}
	}

	private void makeToast(String message) {
		Toast.makeText(EditTypeActivity.this, message, Toast.LENGTH_LONG)
				.show();
	}

	@Override
	protected void onPause() {
		save();
		super.onPause();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		save();
		outState.putParcelable(MyAccountsContentProvider.CONTENT_ITEM_TYPE,
				type.getUri());
	}

	private void save() {
		if (!mDelete) {
			String name = mName.getText().toString();
			int color = mPaint.getColor();

			if (name.length() == 0) // if no name
				return;
			
			// complexe condition
			boolean conditionAlreadyExits = false;
			int occurences = Type.typeNameExists(getContentResolver(), name);
			if (occurences > 0) {
				if (editing && name.equals(baseName)) { // editing + no change
														// -> already exists
														// once in db
					if (occurences > 1) // more than once
						conditionAlreadyExits = true;
				} else
					conditionAlreadyExits = true;
			}
			if (conditionAlreadyExits) { // if name exists in DB
				makeToast("This type name already exists");
				return; // do not save
			}

			type.setName(name);
			type.setColor(color);
			type.saveInDB(getContentResolver());
		}
	}

	@Override
	public void colorChanged(int color) {
		mPaint.setColor(color);
		mColor.setBackgroundColor(color);
	}
}
