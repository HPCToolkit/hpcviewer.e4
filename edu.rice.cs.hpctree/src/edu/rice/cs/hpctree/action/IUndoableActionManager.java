package edu.rice.cs.hpctree.action;

public interface IUndoableActionManager 
{
	public void push(String context);
	public String undo();
	public boolean canUndo(String context);
	public void clear();
	
	public void addActionListener(String context, IUndoableActionListener listener);
	public void removeActionListener(IUndoableActionListener listener);
	
	public static interface IUndoableActionListener {
		public void actionPush(String context);
		public void actionUndo(String context);
	};
}
