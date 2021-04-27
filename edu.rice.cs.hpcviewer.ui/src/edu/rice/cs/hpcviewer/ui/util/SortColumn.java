package edu.rice.cs.hpcviewer.ui.util;

import org.eclipse.swt.SWT;

import edu.rice.cs.hpcdata.util.ScopeComparator;

public class SortColumn 
{

	/****
	 * convert from hpcviewer direction to SWT direction:
	 * <ul>
	 * <li>{@code SWT.UP} ascending
	 * <li>{@code SWT.DOWN} descending
	 * </ul>
	 * @param direction
	 * @return SWT direction
	 */
	public static int getSWTSortDirection(int direction) {
		int swt_direction = SWT.NONE;
		
		if( direction == ScopeComparator.SORT_DESCENDING ) {
			swt_direction = SWT.DOWN;
		} else if( direction == ScopeComparator.SORT_ASCENDING ) {
			swt_direction = SWT.UP;
		} else {
			// incorrect value. Let's try to be permissive instead of throwing exception
			direction = 0;
		}
		return swt_direction;
	}

	
	/*****
	 * Convert from SWT direction into hpcviewer direction
	 * @param swtDirection
	 * @return
	 */
	public static int getSortDirection(int swtDirection) {
		int direction = 0;
		switch(swtDirection) {
		case SWT.DOWN: 
			direction = ScopeComparator.SORT_DESCENDING;
			break;
		case SWT.UP:
			direction = ScopeComparator.SORT_ASCENDING;
			break;
		default:
			direction = 0;
		}
		return direction;
	}
}
