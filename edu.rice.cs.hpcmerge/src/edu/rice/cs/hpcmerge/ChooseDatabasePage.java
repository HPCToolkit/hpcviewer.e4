package edu.rice.cs.hpcmerge;

import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.merge.DatabasesToMerge;

public class ChooseDatabasePage extends WizardPage 
{
	private static final String TITLE = "Databases to merge";

	private CheckboxTableViewer tableViewer ;
	private List<Experiment> listDb;
	private DatabasesToMerge database;
	
	protected ChooseDatabasePage( List<Experiment> listExperiments, DatabasesToMerge database) {
		super(TITLE);
		setTitle(TITLE);
		setDescription("Choose two databases to be merged");
		
		this.listDb = listExperiments;
		this.database = database;
	}

	
	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(container);
		
		Table table = new Table(container, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL);
		table.setLinesVisible(true);
		
		tableViewer = new CheckboxTableViewer(table);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(tableViewer.getTable());
		
		tableViewer.setContentProvider(new ArrayContentProvider());
		
		TableViewerColumn colViewer = new TableViewerColumn(tableViewer, SWT.V_SCROLL | SWT.H_SCROLL);
		colViewer.getColumn().setWidth(500);
		
		colViewer.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Experiment exp = (Experiment) element;
				return exp.getDirectory(); 				
			}
		});
		tableViewer.addCheckStateListener(event -> {
				setPageComplete(isDone());
				getWizard().getContainer().updateButtons();
			}
		);
		tableViewer.setInput(listDb);
		
		setControl(container);
		setPageComplete(false);
	}
	
	private boolean isDone() {
		Object []elems  = tableViewer.getCheckedElements();
		return elems != null && elems.length == 2;
	}
	
	@Override
	public boolean canFlipToNextPage() { 
		boolean canFlip = isDone();
		if (canFlip) {
			Object []elems  = tableViewer.getCheckedElements();
			database.experiment[0] = (Experiment) elems[0];
			database.experiment[1] = (Experiment) elems[1];
		}
		return canFlip;
	}

	/*****
	 * Retrieve the current selected databases
	 * @return
	 */
	public Object[] getElements() {
		return tableViewer.getCheckedElements();
	}
}
