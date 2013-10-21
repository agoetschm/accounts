package ch.goetschy.android.accounts.activities;

import java.util.ArrayList;
import ch.goetschy.android.accounts.R;
import ch.goetschy.android.accounts.objects.Type;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class TypesAdapter extends ArrayAdapter<Type> {

	private final Context context;
	private ArrayList<Type> listTypes;

	public TypesAdapter(Context context, ArrayList<Type> listTypes) {
		super(context, R.layout.activity_types_item, listTypes);
		this.context = context;
		this.listTypes = listTypes;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View rowView = inflater.inflate(R.layout.activity_types_item, parent,
				false);

		TextView name = (TextView) rowView
				.findViewById(R.id.activity_types_name);

		name.setText(listTypes.get(position).getName());
		name.setBackgroundColor(listTypes.get(position).getColor());

		return rowView;
	}

	@Override
	public long getItemId(int position) {
		return listTypes.get(position).getId();
	}

}
