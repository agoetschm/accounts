package ch.goetschy.android.accounts.activities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;
import java.util.concurrent.Callable;

import ch.goetschy.android.accounts.R;
import ch.goetschy.android.accounts.objects.Account;
import ch.goetschy.android.accounts.objects.Filter;
import ch.goetschy.android.accounts.objects.Savable;
import ch.goetschy.android.accounts.objects.Transaction;
import ch.goetschy.android.accounts.objects.Tree;
import ch.goetschy.android.accounts.objects.Type;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class SaveRestoreActivity extends Activity {

	private Spinner mSaveSpinnerAccounts;
	private Button mChooseSaveLocation;
	private Button mChooseRestoreLocation;
	private Button mRestore;
	private Button mPreview;
	private Button mSave;
	private LinearLayout mPreviewLayout;

	private File mSaveFile;
	private File mRestoreFile;

	private boolean previewLoaded;

	private ArrayList<Account> accountsList;
	private ArrayList<Tree> previewContent;
	private ArrayList<CheckBox> previewCheckboxes;

	static final private int SAVE_FILE_ACTIVITY = 10;
	static final private int RESTORE_FILE_ACTIVITY = 20;

	static final private String FILE_ENDING = ".accnt";

	// account id representing all accounts
	static final private int ALL_ACCOUNTS = -1;

	// tags
	static final private String TAG_DATA = "data";
	static final private String TAG_TYPES = "types";
	static final private String TAG_TYPE = "type";
	static final private String TAG_ACCOUNTS = "accounts";
	static final private String TAG_ACCOUNT = "account";
	static final private String TAG_TRANSACTIONS = "transactions";
	static final private String TAG_TRANSACTION = "transaction";
	static final private String TAG_NAME = "name";

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
		mPreview = (Button) findViewById(R.id.activity_save_button_preview);
		mPreviewLayout = (LinearLayout) findViewById(R.id.activity_save_preview);

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
				restoreFile(0);
			}
		});

		previewLoaded = false;
		// preview button
		mPreview.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				loadFilePreview();
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

		Environment.getExternalStorageDirectory();

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

					previewLoaded = false;
				} else
					makeToast("The selection is not a file.");
			}
		}

	}

	// preview before restoring
	private void loadFilePreview() {
		mPreviewLayout.removeAllViews(); // clear preview
		previewLoaded = false;
		mRestore.setEnabled(false);

		// controls
		if (controlFileBeforePreview() == false)
			return;

		// load file
		Tree root = restoreTreeFromFile(mRestoreFile);

		// if loading failed
		if (root == null) {
			makeToast("Loading failed ! Invalid file content.");
			Log.w("saveRestore", "root node == null");
			return;
		}

		// filter to display only types and accounts
		ArrayList<String> filter = new ArrayList<String>();
		filter.add(TAG_ACCOUNT);
		filter.add(TAG_TYPE);

		// select nodes
		Tree actNode = root.getChild(0);
		previewContent = new ArrayList<Tree>();
		while (actNode != root) {
			// next node
			Log.w("saveRestore", "actNode : " + actNode.getType());
			actNode = actNode.getNextNode(root, filter);
			// add to list
			if (actNode != root)
				previewContent.add(actNode);
		}

		// checkboxes list and check if types
		boolean isAnyType = false;
		previewCheckboxes = new ArrayList<CheckBox>();
		for (int i = 0; i < previewContent.size(); i++) {
			previewCheckboxes.add(new CheckBox(this));
			// check for type
			if (previewContent.get(i).getType().equals(TAG_TYPE))
				isAnyType = true;
		}

		// display loaded nodes
		int size = previewContent.size();

		// TYPES
		if (isAnyType) {
			// title
			mPreviewLayout.addView(constructPreviewTitleElement("TYPES",
					new OnCheckedChangeListener() {
						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							checkPreviewCheckboxes(TAG_TYPE, isChecked);
						}
					}));

			// elements
			for (int i = 0; i < size; i++) {
				Tree node = previewContent.get(i);
				if (node.getType().equals(TAG_TYPE)) { // only for types
					Log.w("saveRestore", "preview : " + node.getType());

					// construct element
					mPreviewLayout.addView(constructPreviewElement(node,
							previewCheckboxes.get(i)));
				}
			}

			// title for accounts
			mPreviewLayout.addView(constructPreviewTitleElement("ACCOUNTS",
					new OnCheckedChangeListener() {
						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							checkPreviewCheckboxes(TAG_ACCOUNT, isChecked);
						}
					}));
		}

		// ACCOUNTS
		for (int i = 0; i < size; i++) {
			Tree node = previewContent.get(i);
			if (node.getType().equals(TAG_ACCOUNT)) { // only for accounts
				Log.w("saveRestore", "preview : " + node.getType());

				// construct element
				mPreviewLayout.addView(constructPreviewElement(node,
						previewCheckboxes.get(i)));
			}
		}

		// set preview loaded
		previewLoaded = true;
		mRestore.setEnabled(true);
	}

	// construct preview element
	private LinearLayout constructPreviewElement(Tree node, CheckBox checkbox) {
		// new element layout
		LinearLayout elementLayout = new LinearLayout(this);
		elementLayout.setOrientation(LinearLayout.HORIZONTAL);

		// name
		TextView name = new TextView(this);
		name.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		String text = node.getChildData(TAG_NAME);
		if (text == null) {
			makeToast("Preview failed : no element name.");
		}
		name.setText(text);

		// add views
		elementLayout.addView(name);
		elementLayout.addView(checkbox);

		return elementLayout;
	}

	// construct preview title element
	private LinearLayout constructPreviewTitleElement(String text,
			OnCheckedChangeListener listener) {
		// new element layout
		LinearLayout elementLayout = new LinearLayout(this);
		elementLayout.setOrientation(LinearLayout.HORIZONTAL);

		// name
		TextView name = new TextView(this);
		name.setTypeface(Typeface.DEFAULT_BOLD);
		name.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		name.setText(text);

		// checkbox
		CheckBox checkbox = new CheckBox(this);
		checkbox.setOnCheckedChangeListener(listener);
		checkbox.setChecked(true); // default : checked

		// add views
		elementLayout.addView(name);
		elementLayout.addView(checkbox);

		return elementLayout;
	}

	// check or uncheck preview checkboxes
	private void checkPreviewCheckboxes(String tagType, boolean check) {
		for (int i = 0; i < previewContent.size(); i++) {
			Tree node = previewContent.get(i);
			if (node.getType().equals(tagType)) { // only for tagType
				previewCheckboxes.get(i).setChecked(check);
			}
		}
	}

	private boolean controlFileBeforePreview() {
		// verifiy file
		if (!mRestoreFile.isFile()) {
			makeToast("The selection is not a file.");
			return false;
		}

		// verify file type
		String pattern = ".+\\" + FILE_ENDING + "$";
		if (!mRestoreFile.getName().matches(pattern)) {
			makeToast("The selected file is not an account file (.accnt).");
			return false;
		}

		return true;
	}

	private Tree restoreTreeFromFile(File file) {
		makeToast("Loading from " + file.getName());

		// root node
		Tree rootNode = new Tree("root", null);
		// actual node
		Tree actNode = rootNode;

		// try/catch bloc for reading file
		try {
			// open reader
			BufferedReader reader = new BufferedReader(new FileReader(file));

			// file reading vars ---
			char charac, lastChar = ' ';
			int intChar;
			StringBuilder actWord = new StringBuilder();
			boolean openTag = false;
			boolean closingTag = false;
			// ---------------------

			while ((intChar = reader.read()) != -1) { // while there are
														// characters
				charac = (char) intChar;
				// Log.w("saveRestore", "char : " + charac);
				// handling every case
				if (charac == '<') { // begin of tag

					// see if data is void or not
					boolean vide = true;
					String dataStr = actWord.toString();
					for (int i = 0; i < dataStr.length(); i++) {
						if (!Character.isWhitespace(dataStr.charAt(i))) {
							vide = false;
							break;
						}
					}

					if (!vide) {
						actNode.setData(dataStr);
						Log.w("saveRestore", "data : " + dataStr);
					}

					openTag = true;
					actWord.setLength(0); // 'clear' the actual word
				} else if (charac == '/') { // this tag is a 'close' tag
					if (!openTag)
						actWord.append(charac);
					else if (lastChar == '<') // only if tag open
						closingTag = true;

				} else if (charac == '>') { // end of tag
					String tagStr = actWord.toString();
					if (closingTag) {
						if (tagStr.equals(actNode.getType())) {
							Log.w("saveRestore",
									"close tag : " + actNode.getType());
							actNode = actNode.getParent(); // close node
						} else {
							Log.w("saveRestore", "------> problem : " + tagStr
									+ " != " + actNode.getType());
							return null;
						}
					} else { // opening tag
						// add node
						actNode = actNode.addChild(tagStr);
						Log.w("saveRestore", "open tag : " + actNode.getType());
					}

					openTag = false;
					closingTag = false;
					actWord.setLength(0); // 'clear' the actual word
				} else {
					// content of a tag must be a letter
					if (openTag && (charac > 'z' || charac < 'A')) {
						Log.w("saveRestore", "openTag and " + charac
								+ " is not a letter !");
					}

					// only append if
					if (openTag && charac == ' ') { // not a space in tag
					} else if (!openTag // or a newline, tab, or cr out of tag
							&& (charac == '\n' || charac == '\t' || charac == '\r')) {
					} else
						actWord.append(charac);
				}

				// update lastChar
				lastChar = charac;
			}

			if (actNode != rootNode)
				makeToast("problem in reading the data...");

			// close reader
			reader.close();
		} catch (FileNotFoundException e) {
			makeToast(file.getName() + " was not found.");
		} catch (IOException e) {
			Log.w("saveRestore", "restore error : IOException");
		}

		return rootNode;
	}

	// restore file
	private void restoreFile(int from) {
		// file loaded
		if (!previewLoaded) {
			makeToast("There is no file loaded (preview).");
			return;
		}
		Log.w("saveRestore", "restore from " + from);

		// retore each element checked
		int size = previewContent.size();
		boolean onlyOne = false;
		int i;
		for (i = from; i < size; i++) {
			if (previewCheckboxes.get(i).isChecked()) { // if checked
				Tree node = previewContent.get(i);
				if (node.getType().equals(TAG_ACCOUNT))
					onlyOne = !restoreAccount(node, i + 1);
				else if (node.getType().equals(TAG_TYPE))
					restoreType(node);
				else
					Log.w("saveRestore",
							"error : restore node " + node.getType());
			}
			if (onlyOne) // interrupt id dialog is called
				break;
		}

		// restoring finished
		if (i >= size) {
			makeToast("File restored with succes.");

			mPreviewLayout.removeAllViews(); // clear preview
			previewLoaded = false;
			mRestore.setEnabled(false);
		}
	}

	// returns whether the next accounts should be restored
	private boolean restoreAccount(Tree node, final int next) {
		// final node for inner class
		final Tree fNode = node;

		// set name
		final String name = node.getChildData(TAG_NAME);
		if (name == null) {
			makeToast("Restoring failed : no account name.");
			return true;
		} else if (Account.accountNameExists(getContentResolver(), name) > 0) { // account
																				// name
																				// already
																				// exits
			Log.w("saveRestore", "restore account : name exists");
			// edit text
			final EditText input = new EditText(this);
			input.setHint(name); // with hint

			MyDialog.nameAlreadyExists(this,
					R.string.activity_save_change_name, input,
					new Callable<Object>() { // confirm
						@Override
						public Object call() throws Exception {
							String newName = input.getText().toString();
							if (newName.length() > 0
									&& !newName.equals(name)
									&& Account.accountNameExists(
											getContentResolver(), newName) == 0) { // valid
																					// new
																					// name
								Log.w("saveRestore",
										"restore account : new name valid");
								restoreAccountWithValidName(fNode, newName);
							} else {
								Log.w("saveRestore",
										"restore account : new name invalid");
								makeToast("Invalid new name : account ignored.");
							}

							restoreFile(next); // continue restoring

							return null;
						}
					}, new Callable<Object>() { // cancel
						@Override
						public Object call() throws Exception {
							restoreFile(next); // continue restoring
							return null;
						}
					});

			return false; // only one
		} else { // all ok
			restoreAccountWithValidName(node, name);
			return true;
		}
	}

	private void restoreAccountWithValidName(Tree node, String name) {
		Log.w("saveRestore", "restore with name " + name);

		Account newAccount = new Account();
		newAccount.setName(name);
		// save account
		newAccount.saveInDB(getContentResolver());

		// transactions
		Tree transactionsNode = node.getChild(TAG_TRANSACTIONS);
		if (transactionsNode == null) {
			makeToast("Restoring info : no transactions in " + name);
			return;
		}
		List<Tree> transactionsNodeList = transactionsNode.getChildren();
		if (transactionsNodeList.isEmpty()) {
			// makeToast("Restoring info : no transactions in " + name);
			return;
		}

		// account amount
		double amount = 0;

		for (Tree transNode : transactionsNodeList) {
			Log.w("saveRestore", "transNode");

			if (!transNode.getType().equals(TAG_TRANSACTION)) {
				Log.w("saveRestore", "transNode type " + transNode.getType());
				continue; // jump
			}

			// map of fields
			HashMap<String, String> fields = new HashMap<String, String>();
			for (Tree field : transNode.getChildren()) {
				fields.put(field.getType(), field.getData());
				Log.w("saveRestore", "put field " + field.getType());
			}

			// construct transaction
			Transaction trans = new Transaction();
			Log.w("saveRestore", "setFields");
			if (!trans.setFields(fields)) {
				Log.w("saveRestore", "restore transaction fields failed");
				continue; // jump this trans
			}
			Log.w("saveRestore", "fields set");
			// set parent
			trans.setParent(newAccount);
			// set type (special case bacause it needs the content resolver)
			trans.setType(Type.getOrCreateType(getContentResolver(),
					fields.get("typeName")));

			// cumulate amount
			amount += trans.getAmount();

			Log.w("saveRestore", "save trans");
			// save trans
			trans.saveInDB(getContentResolver());
		}

		// save amount
		Log.w("saveRestore", "set account amount");
		newAccount.setAmount(amount);
		Log.w("saveRestore", "save account amount");
		newAccount.saveAmountInDB(getContentResolver());
		Log.w("saveRestore", "save account amount");
	}

	private void restoreType(Tree node) {
		// set name
		String name = node.getChildData(TAG_NAME);
		if (name == null) {
			makeToast("Restoring failed : no type name.");
			return;
		}
		if (Type.typeNameExists(getContentResolver(), name) > 0) {
			makeToast("Restoring failed : type " + name + " already exists.");
			return;
		}

		// fields
		HashMap<String, String> fields = new HashMap<String, String>();
		for (Tree field : node.getChildren()) {
			fields.put(field.getType(), field.getData());
			Log.w("saveRestore", "put field " + field.getType());
		}

		// new Type
		Type newType = new Type();
		newType.setFields(fields);

		if (!newType.setFields(fields)) {
			Log.w("saveRestore", "restore type fields failed");
			return; // jump this type
		}

		Log.w("saveRestore", "save type");
		newType.saveInDB(getContentResolver());
		Log.w("saveRestore", "type saved");
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
		String filename = selectedAccount.getName().replace(' ', '_')
				+ FILE_ENDING;
		if (allAccounts)
			filename = "allAccounts" + FILE_ENDING;
		File path = new File(mSaveFile, filename);

		// delete, in case it already exists
		path.delete();

		Log.w("saveRestore", "filename : " + filename);

		makeToast("Saving " + filename + "...");

		// CONSTRUCT TREE -----------------

		// root node of the data tree
		Tree rootNode = new Tree(TAG_DATA, null);

		if (allAccounts) { // save types
			// add the 'types' node
			Tree typesNode = rootNode.addChild(TAG_TYPES);

			ArrayList<Type> typesToSave = Type.getTypes(getContentResolver());

			// each type
			for (Type type : typesToSave)
				typesNode.addChild(TAG_TYPE, type);
		}

		// list of accounts to save (one or all)
		ArrayList<Account> accountsToSave;
		if (allAccounts)
			accountsToSave = accountsList;
		else {
			accountsToSave = new ArrayList<Account>();
			accountsToSave.add(selectedAccount);
		}

		// add the 'accounts' node
		Tree accountsNode = rootNode.addChild(TAG_ACCOUNTS);

		// for all accounts to save
		for (Account acc : accountsToSave) {
			if (acc.getId() != ALL_ACCOUNTS) { // ignore "all accounts"
												// pseudo account
				// node for this account
				Tree thisAccount = accountsNode.addChild(TAG_ACCOUNT);
				thisAccount.addChild(TAG_NAME, acc.getName());

				// get transactions
				ArrayList<Transaction> transactions = acc
						.getListTransactions(getContentResolver());

				Log.w("saveRestore", acc.getName() + " -> id = " + acc.getId()
						+ ", uri -> " + acc.getUri());

				// if there is any transaction
				if (!transactions.isEmpty()) {
					Tree transNode = thisAccount.addChild(TAG_TRANSACTIONS);
					// for each transaction
					for (Transaction trans : transactions) {
						// type if doesn't exist
						if (trans.getType().getName() == null)
							trans.getType()
									.setName( // set the id as name
											String.valueOf(trans
													.getTypeIdFromDB(getContentResolver())));

						transNode.addChild(TAG_TRANSACTION, trans);
						Log.w("saveRestore", "transaction " + trans.getName());
					}
				}
			}
		}

		Log.w("saveRestore", "tree constructed, writing to file");

		// END OF TREE -------------------

		// write the tree to the file
		try {
			// open writer
			BufferedWriter outWriter = new BufferedWriter(new FileWriter(path,
					true)); // writer in append mode

			// save the pseudo xml tree
			saveNode(outWriter, rootNode, 0);

			// close writer
			outWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		makeToast(filename + " has been saved");
	}

	private void saveNode(BufferedWriter writer, Tree node, int indentLevel) {
		if (node.hasChildren()) {
			// open node
			writeToFile(writer, node.getType(), null, false, indentLevel);

			Log.w("saveRestore", "saveElement : " + node.getType());
			Log.w("saveRestore", "fields : " + node.getChildren());

			// each child node
			for (Tree child : node.getChildren()) {
				saveNode(writer, child, indentLevel + 1);
			}

			// close node
			writeToFile(writer, node.getType(), null, true, indentLevel);

		} else { // if no children
			String data = node.getData();
			// no null data (else there is no closing tag)
			if (data == null)
				data = "";
			writeToFile(writer, node.getType(), data, false, indentLevel);
		}
	}

	// write a single element in the file
	private void writeToFile(BufferedWriter writer, String key, String value,
			boolean close, int indentLevel) {

		// indent level
		String buffer = new String(new char[indentLevel]).replace("\0", "\t");

		// no < or >
		if (value != null)
			value = value.replace('<', '?').replace('>', '?');

		// format
		if (close) // closing tag
			buffer += "</" + key + ">\n";
		else { // open + close tag
			buffer += "<" + key + ">";
			if (value != null) {
				buffer += value + "</" + key + ">\n";
			} else
				buffer += "\n";
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
