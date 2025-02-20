// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.ui.base;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;


public class TableStatComparator extends ViewerComparator 
{
	private int propertyIndex;
    private static final int DESCENDING = 1;
    private int direction = DESCENDING;
    
    public TableStatComparator() {
    	propertyIndex = 1;
    	direction = DESCENDING;
    }
    
    
    public int getDirection() {
    	return direction == DESCENDING ? SWT.DOWN : SWT.UP;
    }
    
    public void setColumn(int column) {
    	if (column == propertyIndex) {
    		direction = 1 - direction;
    	} else  {
    		propertyIndex = column;
    		direction = DESCENDING; 
    	}
    }
    
    
    public int compare(Viewer viewer, Object e1, Object e2) {
    	StatisticItem item1 = (StatisticItem) e1;
    	StatisticItem item2 = (StatisticItem) e2;
    	int rc = 0;
    	
    	switch (propertyIndex) {
    	case 0:
    		String proc1 = item1.procedure.getProcedure();
    		String proc2 = item2.procedure.getProcedure();
    		rc = proc1.compareTo(proc2);
    		break;
    		
    	case 1:
    		rc = (Float.compare(item1.percent, item2.percent));
    		break;
    		
   		default:
   			rc = 0;
    	}
    	if (direction == DESCENDING) {
    		rc = -rc;
    	}
    	return rc;
    }
}
