package ch.goetschy.android.accounts.activities;

import java.util.concurrent.Callable;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import ch.goetschy.android.accounts.R;
import ch.goetschy.android.accounts.objects.Account;

public abstract class MyDialog {
	static void confirm(Context context, int messageId,
			final Callable<?> funcTrue, final Callable<?> funcFalse) {
		new AlertDialog.Builder(context).setMessage(messageId)
				.setCancelable(false)
				.setPositiveButton(R.string.confirm_dialog_yes,// if yes
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// do smth
								try {
									funcTrue.call();
								} catch (Exception e) {
									Log.w("mydialog", "error in true function");
								}
							}
						}).setNegativeButton(R.string.edit_account_no, // if no
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// do smth
								try {
									funcFalse.call();
								} catch (Exception e) {
									Log.w("mydialog", "error in false function");
								}
							}
						}).show();
	}

	static void nameAlreadyExists(Context context, int messageId,
			EditText input, final Callable<?> funcConfirm,
			final Callable<?> funcCancel) {
		new AlertDialog.Builder(context).setMessage(messageId).setView(input)
				.setCancelable(false)
				.setPositiveButton(R.string.confirm_dialog_confirm,// if confirm
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// do smth
								try {
									funcConfirm.call();
								} catch (Exception e) {
									Log.w("mydialog", "error in true function");
								}
							}
						}).setNegativeButton(R.string.confirm_dialog_cancel, // if
																				// cancel
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// do smth
								try {
									funcCancel.call();
								} catch (Exception e) {
									Log.w("mydialog", "error in false function");
								}
							}
						}).show();
	}

	static void chooseAccount(Context context, int messageId,
			ArrayAdapter<Account> accountsAdapter, OnClickListener onClickListener) {
		new AlertDialog.Builder(context).setMessage(messageId)
				.setCancelable(true)
				.setAdapter(accountsAdapter, onClickListener).show();
	}
}
