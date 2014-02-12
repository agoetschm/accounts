package ch.goetschy.android.accounts.activities;

import ch.goetschy.android.accounts.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

public class FilterDialog extends AlertDialog {

	protected FilterDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.filter_dialog);
	}



}
