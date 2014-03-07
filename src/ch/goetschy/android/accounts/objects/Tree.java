package ch.goetschy.android.accounts.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import android.util.Log;

/**
 * This class is needed to convert the data from the database to a file and back
 * 
 * @author goetschy
 * 
 */

public class Tree {
	private List<Tree> children = new LinkedList<Tree>();
	private Tree parent = null;
	private String type;
	private String data;

	public Tree(String type, Tree parent) {
		this.type = type;
		this.parent = parent;
		this.data = null;
	}

	public Tree(String type, Tree parent, String data) {
		this.type = type;
		this.parent = parent;
		this.data = data;
	}

	public Tree addChild(String type) {
		Tree newTree = new Tree(type, this);
		children.add(newTree);
		return newTree;
	}

	public Tree addChild(String type, String data) {
		Tree newTree = new Tree(type, this, data);
		children.add(newTree);
		return newTree;
	}

	// convert savable obj to tree
	public Tree addChild(String type, Savable obj) {
		Tree newTree = new Tree(type, this);

		// get fields
		HashMap<String, String> fields = obj.getFields();

		// each field
		Set<String> keys = fields.keySet();
		for (String key : keys)
			newTree.addChild(key, fields.get(key));

		children.add(newTree);
		return newTree;
	}

	public Tree getChild(int at) {
		return children.get(at);
	}

	public List<Tree> getChildren() {
		return children;
	}

	public Tree getParent() {
		return this.parent;
	}

	public String getData() {
		return this.data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getType() {
		return this.type;
	}

	public boolean hasChildren() {
		return !this.children.isEmpty();
	}

	public String getChildData(String childName) {
		int size = children.size();
		for (int i = 0; i < size; i++) {
			if (getChild(i).getType().equals(childName)) {
				String data = getChild(i).getData();
				if (data != null)
					return data;
			}
		}
		// if not found
		return null;
	}

	// next node of the tree
	public Tree getNextNode(Tree root, ArrayList<String> filter) {
		Tree actNode = this, tmp = null;

		do {
			// Log.w("tree", "nextNode");

			if (actNode.hasChildren()) // go to first child
				actNode = actNode.getChild(0);
			else { // if no children
				// Log.w("tree", "no children");
				tmp = actNode.getNextNode0(); // next child of parent
				while (tmp == null) { // means no next node
					// Log.w("tree", "go to parent ");
					tmp = actNode.getParent();
					if (tmp != root) {// next node of parent
						actNode = tmp;
						tmp = tmp.getNextNode0();
					} else
						// finished
						return tmp;
				}
				actNode = tmp; // actualize
			}
		} while (!filter.contains(actNode.getType())); // only return selected
														// nodes

		return actNode;
	}

	// next node, ignoring the children, MUST HAVE PARENT
	public Tree getNextNode0() {
		List<Tree> actNodeList = parent.getChildren();
		int position = actNodeList.indexOf(this);
		// Log.w("tree", "nextNode0 : position = " + position + " / size = " +
		// actNodeList.size());

		if (position >= actNodeList.size() - 1) // if last child
			return null;
		else
			return actNodeList.get(position + 1);
	}
}
