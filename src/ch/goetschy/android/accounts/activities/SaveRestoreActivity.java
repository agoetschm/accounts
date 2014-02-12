package ch.goetschy.android.accounts.activities;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import ch.goetschy.android.accounts.R;
import ch.goetschy.android.accounts.objects.Account;
import ch.goetschy.android.accounts.objects.Filter;
import ch.goetschy.android.accounts.objects.Transaction;
import ch.goetschy.android.accounts.objects.Type;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

public class SaveRestoreActivity extends Activity {

	private Spinner mSaveSpinnerAccounts;
	private Button mChooseSaveLocation;
	private Button mSave;

	private File mSaveFile;

	static final private int SAVE_FILE_ACTIVITY = 10;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_save_restore);

		mSaveSpinnerAccounts = (Spinner) findViewById(R.id.activity_save_spinner_save);
		mChooseSaveLocation = (Button) findViewById(R.id.activity_save_button_choose_location);
		mSave = (Button) findViewById(R.id.activity_save_button_save);

		mSaveFile = new File(""); // void path

		// account spinner
		ArrayList<Account> accountsList = Account
				.getListAccounts(getContentResolver());
		if (accountsList != null) {
			ArrayAdapter<Account> accountsAdapter = new AccountsAdapter(this,
					accountsList);

			accountsAdapter
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			mSaveSpinnerAccounts.setAdapter(accountsAdapter);
		}

		// button choose save location
		mChooseSaveLocation.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setSaveFile();
			}
		});

		// save button
		mSave.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				saveFile();
			}
		});
	}

	// open the file browser activity
	private void setSaveFile() {

		// verify external storage for write
		if (!isExternalStorageWritable()) {
			makeToast("There is no available external storage.");
			return;
		}

		Intent intent = new Intent(this, FileExplore.class);
		intent.putExtra(File.class.toString(), mSaveFile);
		startActivityForResult(intent, SAVE_FILE_ACTIVITY);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case SAVE_FILE_ACTIVITY: // selection for the location of the saved file
			if (resultCode == Activity.RESULT_OK) {
				File tmp = (File) data.getSerializableExtra(File.class
						.toString());
				if (tmp.isDirectory()) {
					mSaveFile = tmp;
					mChooseSaveLocation.setText(mSaveFile.getAbsolutePath());
				} else
					makeToast("The selection is not a directory.");
			}
		}
	}

	// save file
	private void saveFile() {
		Account selectedAccount = (Account) mSaveSpinnerAccounts
				.getSelectedItem();
		// verifiy account
		if (selectedAccount == null) {
			makeToast("There is no valid account selected.");
			return;
		}

		// verify external storage for write
		if (!isExternalStorageWritable()) {
			makeToast("There is no available external storage.");
			return;
		}

		// verifiy directory
		if (!mSaveFile.isDirectory()) {
			makeToast("There is no valid directory selected.");
			return;
		}

		// create path
		String filename = selectedAccount.getName() + ".accnt";
		File path = new File(mSaveFile, filename);

		Log.w("saveRestore", "filename : " + filename);

		try {
			// open stream
			FileOutputStream outputStream = new FileOutputStream(path);

			ArrayList<Transaction> transactions = selectedAccount
					.getListTransactions(getContentResolver());

			Log.w("saveRestore",
					selectedAccount.getName() + " -> id = "
							+ selectedAccount.getId() + ", uri -> "
							+ selectedAccount.getUri());
			// for each transaction
			for (Transaction trans : transactions) {
				// open trans
				writeToStream(outputStream, "transaction", null, false);

				Log.w("saveRestore", "transaction : " + trans.getName());

				// id
				writeToStream(outputStream, "id",
						String.valueOf(trans.getId()), false);
				// name
				writeToStream(outputStream, "name", trans.getName(), false);
				// amount
				writeToStream(outputStream, "amount",
						String.valueOf(trans.getAmount()), false);
				// description
				writeToStream(outputStream, "description",
						trans.getDescription(), false);
				// date
				writeToStream(outputStream, "date",
						String.valueOf(trans.getDate()), false);
				// type

				writeToStream(outputStream, "type", trans.getType().getName(),
						false);

				// close trans
				writeToStream(outputStream, "transaction", null, true);
			}

			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		makeToast(filename + " has been saved");
	}

	// make toast
	private void makeToast(String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}

	/* Checks if external storage is available for read and write */
	private boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	private void writeToStream(FileOutputStream stream, String key,
			String value, boolean close) {
		String buffer;
		if (close)
			buffer = "</" + key + ">\n";
		else {
			buffer = "<" + key + ">";
			if (value != null) {
				buffer += value + "</" + key + ">";
			}
		}
		try {
			Log.w("saveRestore", "write : " + buffer);
			byte[] bytesBuffer = buffer.getBytes();
			stream.write(bytesBuffer, 0, bytesBuffer.length);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
