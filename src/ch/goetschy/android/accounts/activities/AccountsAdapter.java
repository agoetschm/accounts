package ch.goetschy.android.accounts.activities;

import java.text.DecimalFormat;
import java.util.ArrayList;

import ch.goetschy.android.accounts.BuildConfig;
import ch.goetschy.android.accounts.R;
import ch.goetschy.android.accounts.objects.Account;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class AccountsAdapter extends ArrayAdapter<Account> {
	private final Context context;
	private ArrayList<Account> list;
	private DecimalFormat amountFormat = new DecimalFormat("#0.00");
	
	public AccountsAdapter(Context context, ArrayList<Account> list) {
		super(context, R.layout.activity_overview_item, list);
		this.context = context;
		this.list = list;
	}

	@Override
	public long getItemId(int position) {
		return list.get(position).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if(BuildConfig.DEBUG)
		Log.w("accountsAdapter", "begin");
		View rowView = inflater.inflate(R.layout.activity_overview_item, parent,
				false);

		TextView name = (TextView) rowView
				.findViewById(R.id.activity_overview_name);
		TextView amount = (TextView) rowView
				.findViewById(R.id.activity_overview_amount);

		if(BuildConfig.DEBUG)
		Log.w("accountsAdapter", list.get(position).getName());

		// set name & amount
		name.setText(list.get(position).getName());
		amount.setText(amountFormat.format(list.get(position).getAmount()));

		if(BuildConfig.DEBUG)
		Log.w("accountsAdapter", "end");
		return rowView;
	}
	
	
}
