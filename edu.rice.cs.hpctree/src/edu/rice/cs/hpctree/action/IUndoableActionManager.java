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
		/*****
		 * Callback when an action of {@code context} will be pushed to the stack
		 * of list of actions.
		 * @param context
		 */
		public void actionPush(String context);
		
		/****
		 * Callback when an action of {@code context} will undo
		 * @param context
		 */
		public void actionUndo(String context);
		
		/****
		 * Listener when we have to reset the action.
		 * The listener has to clear all variables and states
		 */
		public void actionClear();
	};
}
