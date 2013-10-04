package ch.goetschy.android.accounts.activities;

import ch.goetschy.android.accounts.R;
import ch.goetschy.android.accounts.activities.ColorPickerDialog.OnColorChangedListener;
import ch.goetschy.android.accounts.contentprovider.MyAccountsContentProvider;
import ch.goetschy.android.accounts.objects.Account;
import ch.goetschy.android.accounts.objects.Type;
import android.app.Activity;
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

		// if saved instance
		type.setUri((savedInstanceState == null) ? null
				: (Uri) savedInstanceState
						.getParcelable(MyAccountsContentProvider.CONTENT_ITEM_TYPE));
		// edit or add
		if (extras != null) {
			type.setUri((Uri) extras
					.getParcelable(MyAccountsContentProvider.CONTENT_ITEM_TYPE));
			fillData();
		}

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
					makeToast();
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
					type.delete(getContentResolver());
					finish();
				}
			}
		});
	}

	private void fillData() {
		if (type.loadFromDB(getContentResolver())) {
			mName.setText(type.getName());
			mPaint.setColor(type.getColor());
			mColor.setBackgroundColor(type.getColor());
		}
	}

	private void makeToast() {
		Toast.makeText(EditTypeActivity.this, "Please enter a name",
				Toast.LENGTH_LONG).show();
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
		String name = mName.getText().toString();
		int color = mPaint.getColor();

		if (name.length() == 0)
			return;

		type.setName(name);
		type.setColor(color);
		type.saveInDB(getContentResolver());
	}

	@Override
	public void colorChanged(int color) {
		mPaint.setColor(color);
		mColor.setBackgroundColor(color);
	}
}
