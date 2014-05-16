package ch.goetschy.android.accounts.activities;

import java.util.concurrent.Callable;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.haarman.supertooltips.ToolTip;
import com.haarman.supertooltips.ToolTipRelativeLayout;
import com.haarman.supertooltips.ToolTipView;

import ch.goetschy.android.accounts.R;
import ch.goetschy.android.accounts.activities.ColorPickerDialog.OnColorChangedListener;
import ch.goetschy.android.accounts.contentprovider.MyAccountsContentProvider;
import ch.goetschy.android.accounts.objects.Type;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class EditTypeActivity extends SherlockActivity implements
		OnColorChangedListener, ToolTipView.OnToolTipViewClickedListener  {

	private EditText mName;
	private Button mColor;
	private Type type;
	private Paint mPaint;

	private boolean mDelete = false;
	private boolean editing;

	private String baseName = null;
	
	private ToolTipView mTooltipColor = null;
	private ToolTipRelativeLayout mTooltipLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_edit_type);
		
		mTooltipLayout = (ToolTipRelativeLayout) findViewById(R.id.activity_edit_type_tooltiplayout);

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

		// ACTION BAR ------------------
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
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

	// ADD and TYPES BUTTON IN ACTION BAR ------------------------

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_edit_type, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_edit_type_help:
			nextTooltip(null);
			return true;
		case android.R.id.home:
			finish();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	
	// TOOLTIPS -------------------

		// display the first or the next tooltip
		private void nextTooltip(ToolTipView toolTipView) {
			if (toolTipView == null) {
				addColorTooltip();
			} else if (toolTipView == mTooltipColor) {
				mTooltipColor = null;
			}
		}

		private void addColorTooltip() {
			mTooltipColor = mTooltipLayout
					.showToolTipForView(
							new ToolTip()
									.withText(
											"This color will be the background of\nthe transactions with the type\nbeing edited.")
									.withColor(
											getResources().getColor(
													R.color.holo_orange))
									.withShadow(true),
							findViewById(R.id.edit_type_color));
			mTooltipColor
					.setOnToolTipViewClickedListener(this);
		}

		@Override
		public void onToolTipViewClicked(ToolTipView toolTipView) {
			nextTooltip(toolTipView);
		}
}
