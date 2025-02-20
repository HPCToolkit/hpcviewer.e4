// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctree.action;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class UndoableActionManager implements IUndoableActionManager 
{
	private Deque<String> actions;
	private final Map<String, List<IUndoableActionListener>> listeners;

	
	public UndoableActionManager() {
		actions = new ArrayDeque<>();
		listeners = new HashMap<>(1);
	}
	
	@Override
	public void push(String context) {
		actions.push(context);
		
		// notify the listener of this action context
		// this is usually the case for zoom-in and zoom-out action
		// where we change the root completely.
		// In this case, we should notify other actions (like flat action)
		// that a zoom in/out occurs and they should change the level of flattened tree
		var list = listeners.get(context);
		if (list != null)
			list.forEach(f -> f.actionPush(context));
	}

	@Override
	public String undo() {
		if (actions.isEmpty())
			return null;
		
		// first, we need to notify the listener that we will undo the action
		// just in case the listener needs to do something before the action.
		// For instance, the flat action needs to update the flat level first
		String context = actions.peek();
		
		var list = listeners.get(context);
		if (list != null) {
			// notify the listeners of this action context
			list.forEach(f -> f.actionUndo(context));
		}

		// do the actual undo
		actions.pop();
		
		return context;
	}

	@Override
	public boolean canUndo(String context) {
		if (actions.isEmpty())
			return false;
		
		return context.equals(actions.peek());
	}

	@Override
	public void clear() {
		listeners.forEach((context, list) -> {
			list.forEach(f->f.actionClear());
		});
		actions.clear();
	}

	@Override
	public void addActionListener(String context, IUndoableActionListener listener) {
		var list = listeners.get(context);
		if (list == null) {
			list = new ArrayList<>(1);
		}
		list.add(listener);
		listeners.put(context, list);
	}

	@Override
	public void removeActionListener(IUndoableActionListener listener) {
		listeners.forEach((context, list) -> {
			list.remove(listener);
		});
	}
}
