package ch.goetschy.android.accounts.activities;

import java.util.ArrayList;

import ch.goetschy.android.accounts.R;
import ch.goetschy.android.accounts.objects.Transaction;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TransactionsAdapter extends ArrayAdapter<Transaction> {
	private final Context context;
	private ArrayList<Transaction> list;

	public TransactionsAdapter(Context context, ArrayList<Transaction> list) {
		super(context, R.layout.activity_detail_item, list);
		this.context = context;
		this.list = list;
	}
	
	@Override
	  public View getView(int position, View convertView, ViewGroup parent) {
	    LayoutInflater inflater = (LayoutInflater) context
	        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View rowView = inflater.inflate(R.layout.activity_detail_item, parent, false);
	    TextView textView = (TextView) rowView.findViewById(R.id.label);
	    ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
	    textView.setText(values[position]);
	    // Change the icon for Windows and iPhone
	    String s = values[position];
	    if (s.startsWith("iPhone")) {
	      imageView.setImageResource(R.drawable.no);
	    } else {
	      imageView.setImageResource(R.drawable.ok);
	    }

	    return rowView;
	  }
}
