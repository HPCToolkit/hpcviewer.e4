package edu.rice.cs.hpcdata.experiment;

import java.util.List;

public interface ITreeNode<T> 
{
	public void setParent(T parent);
	
	public T getParent();

	public List<T> getChildren();

	public T getChildAt(int index);
	
	public int getChildCount(); 
	
	public boolean hasChildren();
	
	public void add(T child);
	
	public void remove(T child);

	public void remove(int index);

	public Object getValue();

	public void setValue(int index);
}
