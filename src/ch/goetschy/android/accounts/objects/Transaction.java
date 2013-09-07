package ch.goetschy.android.accounts.objects;

public class Transaction extends Item {
	private Type type;
	private String description;
	private long date;

	public Transaction(int p_id, int p_amount, String p_name,
			String p_description, long p_date, Type p_type, Account p_parent) {
		super(p_id, p_amount, p_name, p_parent);
		setType(p_type);
		setDescription(p_description);
		setDate(p_date);
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

}
