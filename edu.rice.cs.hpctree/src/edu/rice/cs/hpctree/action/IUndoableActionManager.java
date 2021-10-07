package edu.rice.cs.hpctree.action;

public interface IUndoableActionManager 
{
	public void push(String context);
	public String undo();
	public boolean canUndo(String context);
}
