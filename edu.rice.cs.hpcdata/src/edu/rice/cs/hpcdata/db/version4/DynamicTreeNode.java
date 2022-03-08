package edu.rice.cs.hpcdata.db.version4;

import java.util.ArrayList;
import java.util.List;

import edu.rice.cs.hpcdata.experiment.scope.ITreeNode;

public class DynamicTreeNode<T> implements ITreeNode<T> 
{
	private final DataMeta database;
	private final int ctxId;
	
	private T parent;
	private List<T> children;
	
	public DynamicTreeNode(DataMeta database, int ctxId) {
		this.database = database;
		this.ctxId    = ctxId;
		children = new ArrayList<>();
	}
	
	@Override
	public void setParent(T parent) {
		this.parent = parent;
	}

	@Override
	public T getParent() {
		return parent;
	}

	@Override
	public List<T> getChildren() {
		if (children == null) {
			
		}
		return null;
	}

	@Override
	public T getChildAt(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getChildCount() {
		return children.size();
	}

	@Override
	public boolean hasChildren() {
		return children.size()>0;
	}

	@Override
	public void add(T child) {
		children.add(child);
	}

	@Override
	public void remove(T child) {
		children.remove(child);
	}

	@Override
	public void remove(int index) {
		children.remove(index);
	}
}
