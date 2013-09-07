package ch.goetschy.android.accounts.objects;

import java.util.ArrayList;

public class Account extends Item {
	private ArrayList<Transaction> list_transactions;
	private int order;
	
	public Account(int p_id, String p_name, Item p_parent){
		super(p_id, 0, p_name, p_parent);
		list_transactions = new ArrayList<Transaction>();
	}

	public void addTransaction(Transaction transaction){
		getList_transactions().add(transaction);
	}

	public ArrayList<Transaction> getList_transactions() {
		return list_transactions;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}
}
