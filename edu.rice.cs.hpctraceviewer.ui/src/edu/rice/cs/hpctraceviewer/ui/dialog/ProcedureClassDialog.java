// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.ui.dialog;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import edu.rice.cs.hpcbase.map.ProcedureClassData;
import edu.rice.cs.hpctraceviewer.data.util.ProcedureClassMap;

/*********************************
 * 
 * Dialog window to show the class and the procedure associated
 *
 */
public class ProcedureClassDialog extends Dialog 
{
	/***
	 * enumeration type to determine the sorting: ascending or descending 
	 *
	 */
	private enum Direction {ASC, DESC}
	
	private enum COLUMN_ID {CLASS, PROCEDURE}

	private static final String EMPTY = "\u2588";
	private static final String UnknownData = "unknown";

	private final ProcedureClassMap data;
	
	// a map from rgb to a color
	// we need to cache the color for the same rgb to avoid creating 
	// too many colors since Windows has limited number of handles
	private final Map<Integer, Color> mapColor;
	
	private TableViewer tableViewer ;
	
	private Button btnRemove;
	private Button btnEdit;
	
	private boolean isModified;
	
	/***
	 * constructor 
	 * @param parentShell
	 */
	public ProcedureClassDialog(Shell parentShell, ProcedureClassMap data ) {
		super(parentShell);
		this.data = data;
		
		mapColor = new HashMap<>();
	}

	/***
	 * return true if the data has been modified
	 * 
	 * @return
	 */
	public boolean isModified() {
		return isModified;
	}
	
	/****
	 * update data in the map and in the table 
	 * it is unfortunate we have two storage of the same data.
	 * 
	 * @param proc
	 * @param procClass
	 * @param rgb
	 */
	private void updateData(String proc, String procClass, RGB rgb) {
		// update the map
		data.put(proc, procClass, rgb);
		
		isModified = true;
		tableViewer.setInput(data.getEntrySet());
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite composite) {
		
		GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(composite);
		
		//-----------------------------------------------------------------
		// Toolbar area
		//-----------------------------------------------------------------
		
		final Composite areaAction = new Composite( composite, SWT.NULL );

		final Button btnAdd   = new Button(areaAction, SWT.PUSH | SWT.FLAT);
		btnAdd.setText("Add");
		btnAdd.setToolTipText("Add a procedure-color pair");
		btnAdd.addSelectionListener( new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				ProcedureMapDetailDialog dlg = new ProcedureMapDetailDialog(getShell(), 
													"Add a new procedure-color map", 
													"", 
													"", 
													null);
				
				if (dlg.open() == Window.OK) {
					// update the map and the table
					updateData(dlg.getProcedure(), dlg.getDescription(), dlg.getRGB());
				}
			}
		});
		
		btnRemove = new Button(areaAction, SWT.PUSH| SWT.FLAT);
		btnRemove.setText("Delete");
		btnRemove.setToolTipText("Remove a selected procedure-color pair");
		btnRemove.setEnabled(false);
		btnRemove.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				remove();
			}
		});
		
		btnEdit = new Button(areaAction, SWT.PUSH| SWT.FLAT);
		btnEdit.setText("Edit");
		btnEdit.setToolTipText("Edit a selected procedure-color pair");
		btnEdit.setEnabled(false);
		btnEdit.addSelectionListener( new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
				Object item = selection.getFirstElement();
				
				if (item instanceof Entry<?,?>) {
					final String proc = (String)((Entry<?, ?>) item).getKey();
					final ProcedureClassData pclass = (ProcedureClassData) 
							((Entry<?, ?>)item).getValue();

					ProcedureMapDetailDialog dlg = new ProcedureMapDetailDialog(getShell(), 
							"Edit procedure-color map", proc, pclass.getProcedureClass(), pclass.getRGB());
					
					if (dlg.open() == Window.OK) {
						// update: remove the old data, and then insert a new one
						// Attention: these two actions have to be atomic !
						data.remove(proc);
						// update the map and the table
						updateData(dlg.getProcedure(), dlg.getDescription(), dlg.getRGB());
					}
				}
			}
		});
		
		final Button btnReset = new Button(areaAction, SWT.PUSH | SWT.FLAT);
		btnReset.setText("Reset");
		btnReset.setToolTipText("Reset to the default configuration");
		btnReset.addSelectionListener( new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				ProcedureClassDialog.this.data.initDefault();
				ProcedureClassDialog.this.tableViewer.setInput(data.getEntrySet());
			}
		});
		
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).grab(true, false).applyTo(areaAction);
		GridLayoutFactory.fillDefaults().margins(10, 10).numColumns(4).applyTo(areaAction);
		
		//-----------------------------------------------------------------
		// table area
		//-----------------------------------------------------------------
		
		tableViewer = new TableViewer(composite, SWT.MULTI | SWT.VIRTUAL);

		// set color column
		TableViewerColumn colColor = new TableViewerColumn(tableViewer, SWT.NONE);
		colColor.setLabelProvider(new ColumnLabelProvider() {			
			
			@Override
			public Color getForeground(Object element) {
				String proc = getProcedureName(element);
				if (proc != UnknownData) {
					ProcedureClassData procData = data.get(proc);
					if (procData != null) {
						RGB rgb = procData.getRGB();
						Color color = mapColor.get(rgb.hashCode());
						if (color == null) {
							color = new Color(getShell().getDisplay(), rgb);
							mapColor.put(rgb.hashCode(), color);
						}
						return color;
					}
				}
				return null;
			}
			
			@Override
			public String getText(Object element) {
				return EMPTY;
			}
		});
		
		TableColumn colC = colColor.getColumn();
		colC.setText(" ");
		colC.setToolTipText("Color of the procedure");
		colC.setResizable(false);
		colC.setWidth(24);
		
		// set procedure column
		final TableViewerColumn colProcedure = new TableViewerColumn(tableViewer, SWT.LEFT);
		TableColumn col = colProcedure.getColumn();
		col.setText("Procedure");
		col.setToolTipText("Procedure pattern");
		col.setResizable(true);
		col.setMoveable(true);
		col.setWidth(220);
		
		colProcedure.setLabelProvider( new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return getProcedureName(element);
			}
		});
		new ColumnViewerSorter(tableViewer, colProcedure, COLUMN_ID.PROCEDURE);
		
		// set description column
		final TableViewerColumn colClass = new TableViewerColumn(tableViewer, SWT.LEFT);
		col = colClass.getColumn();
		col.setText("Description");
		col.setResizable(true);
		col.setMoveable(true);
		col.setWidth(250);
		
		colClass.setLabelProvider( new ColumnLabelProvider(){
			@Override
			public String getText(Object element) {
				return ProcedureClassDialog.this.getClassName(element);
			}
		});
		ColumnViewerSorter sortColClass = new ColumnViewerSorter(tableViewer, colClass, COLUMN_ID.CLASS);
		
		tableViewer.setUseHashlookup(true);		
		tableViewer.setContentProvider(new ArrayContentProvider());

		final Table table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		GridDataFactory.fillDefaults().grab(true, true).applyTo(table);
		
		tableViewer.setInput(data.getEntrySet());
		tableViewer.getTable().addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				checkButton();
			}
		});
		tableViewer.addDoubleClickListener(event ->
				btnEdit.notifyListeners(SWT.Selection, new Event())
		);
		
		sortColClass.setSorter(sortColClass, Direction.ASC);

		getShell().setText("Color mapping");
		
		return composite;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#setShellStyle(int)
	 */
	@Override
	protected void setShellStyle(int newShellStyle) {

		super.setShellStyle(newShellStyle | SWT.RESIZE );
	} 
	
	@Override
	protected void okPressed() {
		dispose();
		super.okPressed();
	}
	
	
	@Override
	protected void cancelPressed() {
		dispose();
		super.cancelPressed();
	}
	
	
	/***
	 * Deallocate resources
	 */
	private void dispose() {
		for(Map.Entry<Integer, Color> entry: mapColor.entrySet()) {
			Color clr = entry.getValue();
			clr.dispose();
		}
		mapColor.clear();
	}
	
	/***
	 * check and set button status
	 */
	private void checkButton() {
		
		int numSelection = tableViewer.getTable().getSelectionCount();

		btnEdit.setEnabled(numSelection == 1);		
		btnRemove.setEnabled(numSelection>0);
	}
	
	/***
	 * refresh the data and reset the table
	 */
	private void refresh() {
		tableViewer.setInput(data.getEntrySet());
		tableViewer.refresh();
		checkButton();
	}
	
	
	/***
	 * removing selected element in the table
	 * @param event
	 */
	private void remove() {
		IStructuredSelection selection = (IStructuredSelection) this.tableViewer.getSelection();
		Object[] sels = selection.toArray();
		
		boolean cont = false;
		if (sels != null) {
			String text = "";
			for (Object o: sels) {
				if (o instanceof Entry<?,?> elem) {
					text += elem.getKey() + "\n"; 
				}
			}
			if (sels.length>1) {
				cont = MessageDialog.openQuestion(getShell(), "Removing " + sels.length+ " mappings", 
						"Are you sure to remove " + sels.length + " mapping elements ?\n" + text);
			} else {
				cont = MessageDialog.openQuestion(getShell(), "Removing an element", 
						"Are you sure to remove this mapping element ?\n" + text );
			}
		}
		if (!cont)
			return;
		
		// remove the data
		for (Object o: sels) {
			if (o instanceof Entry<?,?> elem) {
				data.remove((String) elem.getKey());

				isModified = true;
			}
		}
		// refresh the table
		refresh();
	}
	
	
	/**
	 * retrieve the class name
	 * @param element
	 * @return
	 */
	private String getClassName(Object element) {
		if (element instanceof Entry<?,?> oLine) {
			final Object o = oLine.getValue();
			if (o instanceof ProcedureClassData) {
				final ProcedureClassData objValue = (ProcedureClassData) oLine.getValue();
				return objValue.getProcedureClass();
			}
		}
		return UnknownData;	
	}
	
	/***
	 * retrieve the procedure name
	 * @param element
	 * @return
	 */
	private String getProcedureName(Object element) {
		if (element instanceof Entry<?,?> oLine) {
			return (String) oLine.getKey();
		}
		return UnknownData;
	}
	
	
	
	/******************************************************************************
	 * 
	 * Class for Sorting a column
	 *
	 *******************************************************************************/
	private class ColumnViewerSorter extends ViewerComparator
	{
		private final TableViewerColumn column;
		private final TableViewer viewer;
		private Direction direction = Direction.ASC;
		private final COLUMN_ID colid;
		
		/**
		 * Initialization to sort a column of a table viewer
		 * 
		 * @param viewer
		 * @param column : column to sort
		 */
		public ColumnViewerSorter(TableViewer viewer, TableViewerColumn column, COLUMN_ID columnID) {
			this.viewer = viewer;
			this.column = column;
			this.colid = columnID;
			
			column.getColumn().addSelectionListener( new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					ViewerComparator comparator = ColumnViewerSorter.this.viewer.getComparator();
					if ( comparator != null  &&  (comparator == ColumnViewerSorter.this)) {
							Direction dir = ColumnViewerSorter.this.direction;
							dir = (dir==Direction.ASC? Direction.DESC : Direction.ASC);
							setSorter (ColumnViewerSorter.this, dir);
							return;
						
					}
					setSorter (ColumnViewerSorter.this, Direction.ASC);
				}
			});
		}
		
		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			final String elem1 = (colid == COLUMN_ID.CLASS ? 
									getClassName(e1) : 
									getProcedureName(e1));
			final String elem2 = (colid == COLUMN_ID.CLASS ? 
									getClassName(e2) : 
									getProcedureName(e2));
			
			int k = (direction == Direction.ASC ? 1 : -1 );
			return k * super.compare(viewer, elem1, elem2);
		}

		/****
		 * 
		 * @param sorter
		 * @param dir
		 */
		public void setSorter(ColumnViewerSorter sorter, Direction dir) {
			column.getColumn().getParent().setSortColumn(column.getColumn());
			sorter.direction = dir;
			if( direction == Direction.ASC ) {
				column.getColumn().getParent().setSortDirection(SWT.DOWN);
			} else {
				column.getColumn().getParent().setSortDirection(SWT.UP);
			}
			
			if( viewer.getComparator() == sorter ) {
				viewer.refresh();
			} else {
				viewer.setComparator(sorter);
			}
			ProcedureClassDialog.this.checkButton();
		}
	}
	
    @Override
    protected Point getInitialSize() {
        return new Point(450, 300);
    }
}
