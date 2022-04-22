package edu.rice.cs.hpcdata.experiment.scope;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple data structure that is useful for implemented tree models. 
 * <p>
 * This class is a inspired from 
 * {@link org.eclipse.jface.viewers.TreeNode} without using Eclipse classes
 * and with additional features, plus a flexible children update. 
 * </p>
 **/
public class TreeNode<T> implements ITreeNode<T> {

	/**
	 * The array of child tree nodes for this tree node. If there are no
	 * children, then this value may either by an empty array or
	 * <code>null</code>. There should be no <code>null</code> children in
	 * the array.
	 */
	private List<T> children;

	/**
	 * The parent tree node for this tree node. This value may be
	 * <code>null</code> if there is no parent.
	 */
	private T parent;

	/**
	 * The value contained in this node. This value may be anything.
	 */
	protected Object value;

	/**
	 * Constructs a new instance of <code>TreeNode</code>.
	 * 
	 * @param value
	 *            The value held by this node; may be anything.
	 */
	public TreeNode(final Object value) {
		this.value = value;
	}
	

	/**
	 * Returns the child nodes. Empty arrays are converted to <code>null</code>
	 * before being returned.
	 * 
	 * @return The child nodes; may be <code>null</code>, but never empty.
	 *         There should be no <code>null</code> children in the array.
	 */
	@Override
	public List<T> getChildren() {
		return children;
	}
	
	/****
	 * Replace the current children with the new ones
	 * @param children
	 */
	public void setChildren(List<T> children) {
		this.children = children;
	}

	
	/****
	 * Replace the children with the new list
	 * @param children
	 */
	public void setListChildren(List<T> children) {
		this.children = children;
	}
	
	/**
	 * Returns the parent node.
	 * 
	 * @return The parent node; may be <code>null</code> if there are no
	 *         parent nodes.
	 */
	public T getParent() {
		return parent;
	}

	/**
	 * Returns the value held by this node.
	 * 
	 * @return The value; may be anything.
	 */
	@Override
	public Object getValue() {
		return value;
	}

	/**
	 * Returns whether the tree has any children.
	 * 
	 * @return <code>true</code> if its array of children is not
	 *         <code>null</code> and is non-empty; <code>false</code>
	 *         otherwise.
	 */
	public boolean hasChildren() {
		return children != null && children.size() > 0;
	}
	

	/***
	 * Add a new child
	 * 
	 * @param child : child to be added
	 */
	public void add(T child)
	{
		if (children == null) {
			children = new ArrayList<>(3);
		}
		children.add(child);
	}
	
	/**
	 * Remove a child. The child hash code (which is the cct) has to be unique.
	 * <br/>Otherwise, it will remove incorrect child.
	 * 
	 * @param child : the child to be removed
	 */
	public void remove(T child)
	{
		children.remove(child);
	}
	
	
	/**
	 * Remove a child for a given index
	 * @param index : index of the scope to be removed
	 */
	public void remove(int index)
	{
		children.remove(index);
	}
	
	/**
	 * Sets the parent for this node.
	 * 
	 * @param parent
	 *            The parent node; may be <code>null</code>.
	 */
	public void setParent(T parent) {
		this.parent = parent;
	}
	
	/***
	 * Retrieve a child node of a specific index
	 * 
	 * @param index
	 * 
	 * @return TreeNode: a node
	 */
	public T getChildAt(int index) 
	{
		if (children != null) {
			if (index < children.size())
				return children.get(index);
			else
				throw new RuntimeException("Index is not correct: " + index + " bigger than " + children.size());
		}
		return null;
	}
	
	/*****
	 * Return the number of children
	 * 
	 * @return
	 */
	public int getChildCount() 
	{
		if (children == null)
			return 0;
		return children.size();
	}

	/*****
	 * free resources
	 */
	public void dispose()
	{
		if (children != null)
		{
			children.clear();
			children = null;
		}
		parent   = null;
		value  	 = null;
	}
	
	@Override
	public String toString() {
		return value + ", p: " + parent + ", c: " + 
			  children.parallelStream().collect(StringBuilder::new, 
					  							StringBuilder::append, 
					  							StringBuilder::append);
	}
}

