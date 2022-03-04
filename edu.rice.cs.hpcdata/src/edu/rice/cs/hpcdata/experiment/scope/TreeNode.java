package edu.rice.cs.hpcdata.experiment.scope;

import java.util.ArrayList;
import java.util.List;

/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/


/**
 * This class is a modified version of org.eclipse.jface.viewers.TreeNode
 * {@link org.eclipse.jface.viewers.TreeNode} without using Eclipse classes
 * and with additional features, plus a flexible children update 
 * 
 * A simple data structure that is useful for implemented tree models. This can
 * be returned by
 * {@link org.eclipse.jface.viewers.IStructuredContentProvider#getElements(Object)}.
 * It allows simple delegation of methods from
 * {@link org.eclipse.jface.viewers.ITreeContentProvider} such as
 * {@link org.eclipse.jface.viewers.ITreeContentProvider#getChildren(Object)},
 * {@link org.eclipse.jface.viewers.ITreeContentProvider#getParent(Object)} and
 * {@link org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(Object)}
 * 
 * @since 3.2
 */
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
}

