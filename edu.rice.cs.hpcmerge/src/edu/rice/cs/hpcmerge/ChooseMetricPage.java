package edu.rice.cs.hpcmerge;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;

public class ChooseMetricPage extends WizardPage 
{
	private static final String TITLE = "Choose a metric to compare";
	private static final String DEFAULT_LABEL = "Please select a metric from the list below";
	
	private Label labelDatabase[];
	private List  listMetrics[];
	private Group groups[];
	private DatabasesToMerge database;
	
	public ChooseMetricPage(DatabasesToMerge database) {
		super(TITLE);
		setTitle(TITLE);
		setDescription("Select a metric from each database to be compared. " +
						"Metric values are one of the criteria if two nodes correspond.");
		this.database = database;
	}
	
	
	/***
	 * Reset the content to include the new databases to be merged. 
	 * @param db
	 */
	public void setDatabasesToMerge(DatabasesToMerge db) {
		this.database = db;
		fillContent(0);
		fillContent(1);
	}
	
	
	private void fillContent(int index) {
		Experiment exp = database.experiment[index];
		Group group = groups[index];
		List list   = listMetrics[index];
		Label label = labelDatabase[index];
		label.setText(DEFAULT_LABEL);
		
		String text = exp.getXMLExperimentFile().getAbsolutePath();
		group.setText(text);
		group.setToolTipText(text);

		// clear and fill the list of metrics
		list.removeAll();
		
		exp.getVisibleMetrics().stream().forEach(metric -> {
			list.add(metric.getDisplayName());
		});
		
		setPageComplete(isDone());

		list.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String s = list.getSelection()[0];
				if (s != null) {
					BaseMetric metric = exp.getVisibleMetrics().get(index);
					database.metric[index] = metric;
					
					label.setText("Metric: " + s);
					setPageComplete(isDone());
				}
			}
		});
	}
	
	private boolean isDone() {
		return  (listMetrics[0].getSelectionCount() == 1) &&
				(listMetrics[1].getSelectionCount() == 1);
	}
	
	
	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.BORDER_SOLID);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(container);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(container);

		labelDatabase = new Label[2];
		listMetrics   = new List[2];
		groups        = new Group[2];
		
		groups[0] = new Group(container, SWT.BORDER | SWT.WRAP);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(groups[0]);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(groups[0]);
		groups[0].setText("Database 1");
		
		labelDatabase[0] = new Label(groups[0], SWT.WRAP | SWT.BORDER);
		labelDatabase[0].setText(DEFAULT_LABEL);
		GridDataFactory.swtDefaults().grab(true, false).applyTo(labelDatabase[0]);
		
		listMetrics[0] = new List(groups[0], SWT.V_SCROLL | SWT.SINGLE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(listMetrics[0]);

		groups[1] = new Group(container, SWT.BORDER | SWT.WRAP);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(groups[1]);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(groups[1]);
		groups[1].setText("Database 1");
		
		labelDatabase[1] = new Label(groups[1], SWT.WRAP | SWT.BORDER);
		labelDatabase[1].setText(DEFAULT_LABEL);
		GridDataFactory.swtDefaults().grab(true, false).applyTo(labelDatabase[1]);
		
		listMetrics[1] = new List(groups[1], SWT.V_SCROLL | SWT.SINGLE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(listMetrics[1]);
		
		setControl(container);
		setPageComplete(false);
		
		// if the databases have been selected, we can go to fill the content
		if (database.experiment[0] != null && database.experiment[1] != null) {
			fillContent(0);
			fillContent(1);
		}
	}

	@Override
	public boolean canFlipToNextPage() {
		return false;
	}
	
	@Override
	public IWizardPage getNextPage() {
		return null;
	}
}
