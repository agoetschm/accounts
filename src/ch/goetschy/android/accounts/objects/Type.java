package ch.goetschy.android.accounts.objects;

public class Type {
	private final long id;
	private String name;
	
	public Type(long p_id, String p_name) {
		id = p_id;
		setName(p_name);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public long getId(){
		return id;
	}
}
