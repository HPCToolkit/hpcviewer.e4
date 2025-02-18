// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpcbase.ui;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;

public abstract class AbstractUpperPart extends CTabItem implements IUpperPart {

	protected AbstractUpperPart(CTabFolder parent, int style) {
		super(parent, style);
	}


	/***
	 * Sometimes the content of the upper part depends on the activated lower part.
	 * For instance, the content of metric view depends on the activated view like
	 * top-down or bottom-up or even thread view.
	 * <br/>
	 * If we change the active view, then we need to refresh the content of this
	 * view as well.
	 * 
	 * @apiNote Some views do not depend on the lower part. In this case no op is fine.
	 * 
	 * @param lowerPart
	 * 			The activated view
	 */
	public abstract void refresh(ILowerPart lowerPart);
}
