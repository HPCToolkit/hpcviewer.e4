// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.ui.base;

import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;

import edu.rice.cs.hpcsetting.fonts.FontManager;
import edu.rice.cs.hpcsetting.preferences.PreferenceConstants;
import edu.rice.cs.hpcsetting.preferences.ViewerPreferenceManager;

public abstract class AbstractBaseTableViewer 
	extends TableViewer 
	implements Listener, DisposeListener, IPropertyChangeListener
{

	private int tableRowHeight;

	public AbstractBaseTableViewer(Composite parent, int style) {
		super(parent, SWT.SINGLE | SWT.READ_ONLY | SWT.FULL_SELECTION | style);
		computeCellBounds();
		
		PreferenceStore pref = ViewerPreferenceManager.INSTANCE.getPreferenceStore();
		pref.addPropertyChangeListener(this);
		
		final Table table = getTable();
		table.setLinesVisible(true);
		
		table.addDisposeListener(this);
		table.addListener(SWT.MeasureItem, this);
		
		ColumnViewerToolTipSupport.enableFor(this);		
	}

	@Override
	public void widgetDisposed(DisposeEvent e) {
		getTable().removeDisposeListener(this);
		getTable().removeListener(SWT.MeasureItem, this);
		
		PreferenceStore pref = ViewerPreferenceManager.INSTANCE.getPreferenceStore();
		pref.removePropertyChangeListener(this);
	}
	
	@Override
	public void handleEvent (Event event) {
		switch(event.type) {
		case SWT.MeasureItem:
			event.height = tableRowHeight;
			break;
		}
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent event) {

		final String property = event.getProperty();
		
		boolean need_to_refresh = (property.equals(PreferenceConstants.ID_FONT_GENERIC) || 
								   property.equals(PreferenceConstants.ID_FONT_METRIC)); 

		if (need_to_refresh) {
			computeCellBounds();
			refresh();
		}
	}
	
	final String text = "|{[(/`,q";

	private void computeCellBounds() {
		
		GC gc = new GC(getControl());
		
		gc.setFont(FontManager.getFontGeneric());
		Point extent1 = gc.stringExtent(text);
		
		// check the height if we use generic font (tree column)
		// if this font is higher, we should use this height as the standard.
		
		gc.setFont(FontManager.getMetricFont());
		Point extent2 = gc.stringExtent(text);
		
		Point extent  = new Point(Math.max(extent1.x, extent2.x), 
								  Math.max(extent1.y, extent2.y));
		
		extent = computeCellBounds(gc, extent);
		
		tableRowHeight = extent.y;
		
		gc.dispose();
	}

	/****
	 * Called when it's necessary to recompute the cell bound
	 * @param gc the graphic context. Implementer shouldn't dispose the gc.
	 * @param Point the suggested bound
	 * 
	 * @return Point final bound
	 */
	abstract protected Point computeCellBounds(GC gc, Point extent);
}
