package ch.goetschy.android.accounts.objects;

public abstract class Item {
	private final long id;
	private int amount;
	private String name;
	private Item parent;
	
	public Item(long p_id, int p_amount, String p_name, Item p_parent){
		id = p_id;
		setAmount(p_amount);
		setName(p_name);
		setParent(p_parent);
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
}
