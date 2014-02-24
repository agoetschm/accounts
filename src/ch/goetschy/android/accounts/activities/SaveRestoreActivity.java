package ch.goetschy.android.accounts.activities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import ch.goetschy.android.accounts.R;
import ch.goetschy.android.accounts.objects.Account;
import ch.goetschy.android.accounts.objects.Filter;
import ch.goetschy.android.accounts.objects.Savable;
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
	private Button mChooseRestoreLocation;
	private Button mRestore;
	private Button mSave;

	private File mSaveFile;
	private File mRestoreFile;

	private ArrayList<Account> accountsList;

	static final private int SAVE_FILE_ACTIVITY = 10;
	static final private int RESTORE_FILE_ACTIVITY = 20;

	static final private String FILE_ENDING = ".accnt";

	// account id representing all accounts
	static final private int ALL_ACCOUNTS = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_save_restore);

		// get views
		mSaveSpinnerAccounts = (Spinner) findViewById(R.id.activity_save_spinner_save);
		mChooseSaveLocation = (Button) findViewById(R.id.activity_save_button_choose_location);
		mChooseRestoreLocation = (Button) findViewById(R.id.activity_save_button_restore_location);
		mSave = (Button) findViewById(R.id.activity_save_button_save);
		mRestore = (Button) findViewById(R.id.activity_save_button_restore);

		// init file vars
		mSaveFile = new File(""); // void path
		mRestoreFile = new File("");

		// SAVE --------

		// account spinner
		accountsList = Account.getListAccounts(getContentResolver());
		if (accountsList != null) {
			ArrayAdapter<Account> accountsAdapter = new AccountsAdapter(this,
					accountsList);
			// "save all" item
			// test
			accountsAdapter.add(new Account(ALL_ACCOUNTS,
					"All accounts and types", 0));
			// end test

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

		// RESTORE --------

		// button choose restore file location
		mChooseRestoreLocation.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setRestoreFile();
			}

		});

		// restore button
		mRestore.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				restoreFile();
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

	private void setRestoreFile() {
		// verify external storage for write
		if (!isExternalStorageWritable()) {
			makeToast("There is no available external storage.");
			return;
		}

		Intent intent = new Intent(this, FileExplore.class);
		intent.putExtra(File.class.toString(), mRestoreFile);
		startActivityForResult(intent, RESTORE_FILE_ACTIVITY);
	}

	// when file chosen
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
			break;
		case RESTORE_FILE_ACTIVITY: // selection for the location of the saved
									// file
			if (resultCode == Activity.RESULT_OK) {
				File tmp = (File) data.getSerializableExtra(File.class
						.toString());
				if (tmp.isFile()) {
					mRestoreFile = tmp;
					mChooseRestoreLocation.setText(mRestoreFile
							.getAbsolutePath());
				} else
					makeToast("The selection is not a file.");
			}
		}

	}

	// restore file
	private void restoreFile() {
		// verifiy file
		if (!mRestoreFile.isFile()) {
			makeToast("The selection is not a file.");
			return;
		}

	}

	// save file
	private void saveFile() {

		// get account
		Account selectedAccount = (Account) mSaveSpinnerAccounts
				.getSelectedItem();

		// verifiy account
		if (selectedAccount == null) {
			makeToast("There is no valid account selected.");
			return;
		}

		// if all accounts
		boolean allAccounts = false;
		if (selectedAccount.getId() == ALL_ACCOUNTS)
			allAccounts = true;

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
		String filename = selectedAccount.getName() + FILE_ENDING;
		if (allAccounts)
			filename = "allAccounts" + FILE_ENDING;
		File path = new File(mSaveFile, filename);

		// delete, in case it already exists
		path.delete();

		Log.w("saveRestore", "filename : " + filename);

		makeToast("Saving " + filename + "...");

		try {
			// open stream
			BufferedWriter outWriter = new BufferedWriter(new FileWriter(path,
					true)); // writer in append mode

			// list of accounts to save (one or all)
			ArrayList<Account> accountsToSave;
			if (allAccounts) {
				accountsToSave = accountsList;
			} else {
				accountsToSave = new ArrayList<Account>();
				accountsToSave.add(selectedAccount);
			}

			// for all accounts to save
			for (Account acc : accountsToSave) {
				if (acc.getId() != ALL_ACCOUNTS) { // ignore "all accounts" pseudo account

					// get transactions
					ArrayList<Transaction> transactions = acc
							.getListTransactions(getContentResolver());

					// open account
					writeToFile(outWriter, "account name=\"" + acc.getName()
							+ "\"", null, false);

					Log.w("saveRestore",
							acc.getName() + " -> id = " + acc.getId()
									+ ", uri -> " + acc.getUri());

					// for each transaction
					for (Transaction trans : transactions)
						saveElement(outWriter, "transaction", trans);

					// close account
					writeToFile(outWriter, "account", null, true);
				}
			}

			outWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		makeToast(filename + " has been saved");
	}

	private void saveElement(BufferedWriter writer, String type, Savable obj) {
		// get fields
		HashMap<String, String> fields = obj.getFields();

		// open element
		writeToFile(writer, type, null, false);

		Log.w("saveRestore", "saveElement : " + type);
		Log.w("saveRestore", "fields : " + fields.keySet().toString());

		// each field
		Set<String> keys = fields.keySet();
		for (String key : keys) {
			Log.w("saveRestore", "\t field : " + key);

			writeToFile(writer, key, fields.get(key), false);
		}

		// close element
		writeToFile(writer, type, null, true);
	}

	// write a single element in the file
	private void writeToFile(BufferedWriter writer, String key, String value,
			boolean close) {
		String buffer;
		// format
		if (close) // closing tag
			buffer = "</" + key + ">\n";
		else { // open + close tag
			buffer = "<" + key + ">";
			if (value != null) {
				buffer += value + "</" + key + ">";
			}
		}
		// write
		try {
			Log.w("saveRestore", "write : " + buffer);
			writer.write(buffer);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
}
