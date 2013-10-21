package ch.goetschy.android.accounts.activities;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;

import ch.goetschy.android.accounts.BuildConfig;
import ch.goetschy.android.accounts.R;
import ch.goetschy.android.accounts.objects.Filter;
import ch.goetschy.android.accounts.objects.Transaction;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TransactionsAdapter extends ArrayAdapter<Transaction> {
	private final Context context;
	private ArrayList<Transaction> list;
	private DecimalFormat amountFormat = new DecimalFormat("##.00");

	public TransactionsAdapter(Context context, ArrayList<Transaction> list) {
		super(context, R.layout.activity_detail_item, list);
		this.context = context;
		this.list = list;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		Log.w("transactionsAdapter", "begin");
		View rowView = inflater.inflate(R.layout.activity_detail_item, parent,
				false);

		TextView name = (TextView) rowView
				.findViewById(R.id.activity_detail_name);
		TextView amount = (TextView) rowView
				.findViewById(R.id.activity_detail_amount);
		TextView date = (TextView) rowView
				.findViewById(R.id.activity_detail_date);

		// set type color
		rowView.setBackgroundColor(list.get(position).getType().getColor());

		if (BuildConfig.DEBUG)
			Log.w("transactionsAdapter", list.get(position).getName());

		// set name & amount
		name.setText(list.get(position).getName());
		amount.setText(amountFormat.format(list.get(position).getAmount()));

		// date format
		Calendar c = Calendar.getInstance();
		date.setText(Filter.millisToText(list.get(position).getDate()));

		if (BuildConfig.DEBUG)
			Log.w("transactionsAdapter", "end");
		return rowView;
	}

	@Override
	public long getItemId(int position) {
		return list.get(position).getId();
	}

}
