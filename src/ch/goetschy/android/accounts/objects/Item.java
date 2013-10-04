package ch.goetschy.android.accounts.objects;

import android.content.ContentResolver;
import android.net.Uri;

public abstract class Item {
	protected long id;
	protected double amount;
	protected String name;
	protected Uri uri;
	protected Item parent;
	
	public static final long DEFAULT_ID = -1;
	public static final int DEFAULT_AMOUNT = 0;
	public static final String DEFAULT_NAME = "";
	public static final Uri DEFAULT_URI = null;
	public static final Item DEFAULT_PARENT = null;
	
	public Item(){
		setId(DEFAULT_ID);
		setAmount(DEFAULT_AMOUNT);
		setName(DEFAULT_NAME);
		setParent(DEFAULT_PARENT);
		setUri(DEFAULT_URI);
	}
	
	public Item(long p_id, double p_amount, String p_name, Item p_parent){
		setId(p_id);
		setAmount(p_amount);
		setName(p_name);
		setParent(p_parent);
		setUri(null);
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	public void setId(long id) {
		this.id = id;
	}
	
	public long getId() {
		return id;
	}

	public Item getParent() {
		return parent;
	}

	public void setParent(Item parent) {
		this.parent = parent;
	}

	public Uri getUri() {
		return uri;
	}

	public void setUri(Uri uri) {
		this.uri = uri;
	}
	
	public abstract void delete(ContentResolver contentResolver);
	
	public abstract void saveInDB(ContentResolver contentResolver);
	
	public abstract boolean loadFromDB(ContentResolver contentResolver);
}
