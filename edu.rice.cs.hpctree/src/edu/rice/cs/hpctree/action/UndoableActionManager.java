package edu.rice.cs.hpctree.action;

import java.util.Stack;

public class UndoableActionManager implements IUndoableActionManager 
{
	private Stack<String> actions;

	
	public UndoableActionManager() {
		actions = new Stack<>();
	}
	
	@Override
	public void push(String context) {
		actions.push(context);
	}

	@Override
	public String undo() {
		if (actions.isEmpty())
			return null;
		
		return actions.pop();
	}

	@Override
	public boolean canUndo(String context) {
		if (actions.isEmpty())
			return false;
		
		return context.equals(actions.peek());
	}

	@Override
	public void clear() {
		actions.clear();
	}
}
