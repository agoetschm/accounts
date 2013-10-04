package ch.goetschy.android.accounts.objects;

import android.util.Log;

public class Filter {
	private int upperBound;
	private int lowerBound;
	private long typeId;
	
	public int getUpperBound() {
		return upperBound;
	}
	
	public void setUpperBound(int upperBound) {
		this.upperBound = upperBound;
	}
	
	public int getLowerBound() {
		return lowerBound;
	}
	
	public void setLowerBound(int lowerBound) {
		this.lowerBound = lowerBound;
	}

	public long getTypeId() {
		return typeId;
	}

	public void setTypeId(long typeId) {
		this.typeId = typeId;
	}

	public void previous() {
		// TODO Auto-generated method stub
		Log.w("filter", "previous");
	}

	public void next() {
		// TODO Auto-generated method stub
		Log.w("filter", "next");
	}
	
}
