// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpctree.action;

public interface IUndoableActionManager 
{
	void push(String context);
	String undo();
	boolean canUndo(String context);
	void clear();
	
	void addActionListener(String context, IUndoableActionListener listener);
	void removeActionListener(IUndoableActionListener listener);
	
	interface IUndoableActionListener {
		/*****
		 * Callback when an action of {@code context} will be pushed to the stack
		 * of list of actions.
		 * @param context
		 */
		void actionPush(String context);
		
		/****
		 * Callback when an action of {@code context} will undo
		 * @param context
		 */
		void actionUndo(String context);
		
		/****
		 * Listener when we have to reset the action.
		 * The listener has to clear all variables and states
		 */
		void actionClear();
	}
}
