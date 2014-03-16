package ch.goetschy.android.accounts.objects;

import java.util.HashMap;

public interface Savable {
	public HashMap<String, String> getFields();
	
	public boolean setFields(HashMap<String, String> fields);
}
