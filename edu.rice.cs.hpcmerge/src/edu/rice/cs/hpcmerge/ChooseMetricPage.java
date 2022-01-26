package edu.rice.cs.hpcmerge;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.merge.DatabasesToMerge;

public class ChooseMetricPage extends WizardPage 
{
	private static final String TITLE = "Choose a metric to compare";
	private static final String DEFAULT_LABEL = "Please select a metric from the list above";
	
	private Label labelDatabase[], labelMetric[];
	private List  listMetrics[];
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
		if (database.experiment[0] != null) fillContent(0);
		if (database.experiment[1] != null) fillContent(1);
		
		if (db.metric[0] == null || db.metric[1] == null) {
			db.metric = DatabasesToMerge.getMetricsToCompare(db.experiment);
		}
		selectMetric(listMetrics[0], db.metric[0], labelMetric[0]);
		selectMetric(listMetrics[1], db.metric[1], labelMetric[1]);
		setPageComplete(isDone());
	}
	
	
	private void selectMetric(List list, BaseMetric metric, Label label) {
		for(int i=0; i<list.getItemCount(); i++) {
			if (list.getItem(i).equals(metric.getDisplayName())) {
				list.select(i);
				setMetricLabel(label, metric.getDisplayName());
				return;
			}
		}
	}
	
	private void setMetricLabel(Label label, String metricName) {
		label.setText("Metric: " + metricName);
	}
	
	private void fillContent(int index) {
		Experiment exp = database.experiment[index];
		
		String text = exp.getExperimentFile().getAbsolutePath();
		labelDatabase[index].setText(text);

		List list   = listMetrics[index];
		Label label = labelMetric[index];

		// clear and fill the list of metrics
		list.removeAll();
		
		final java.util.List<BaseMetric> metrics = exp.getVisibleMetrics();
		
		metrics.stream().forEach(metric -> {
			list.add(metric.getDisplayName());
		});
		
		setPageComplete(isDone());

		list.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (list.getSelectionCount() > 0) {
					int select = list.getSelectionIndex();
					database.metric[index] = metrics.get(select);
					
					setMetricLabel(label, database.metric[index].getDisplayName());
					setPageComplete(isDone());
				}
			}
		});
	}
	
	private boolean isDone() {
		return  (listMetrics[0].getSelectionCount() == 1) &&
				(listMetrics[1].getSelectionCount() == 1);
	}
	
	
	private void createPanel(SashForm form, int index) {

		Composite areaPanel = new Composite(form, SWT.BORDER);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(areaPanel);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(areaPanel);
		
		labelDatabase[index] = new Label(areaPanel, SWT.WRAP);
		labelDatabase[index].setText(DEFAULT_LABEL);
		GridDataFactory.swtDefaults().grab(true, false).applyTo(labelDatabase[index]);
		
		listMetrics[index] = new List(areaPanel, SWT.V_SCROLL | SWT.SINGLE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(listMetrics[index]);

		labelMetric[index] = new Label(areaPanel, SWT.WRAP);
		labelMetric[index].setText(DEFAULT_LABEL);
		GridDataFactory.swtDefaults().grab(true, false).applyTo(labelMetric[index]);
	}
	
	@Override
	public void createControl(Composite parent) {
		labelDatabase = new Label[2];
		labelMetric   = new Label[2];
		listMetrics   = new List[2];
		
		SashForm form = new SashForm(parent, SWT.HORIZONTAL);
		form.setLayout(new FillLayout());

		createPanel(form, 0);
		createPanel(form, 1);
		
		setControl(form);
		setPageComplete(false);
		
		// if the databases have been selected, we can go to fill the content
		if (database.experiment[0] != null && database.experiment[1] != null)
			setDatabasesToMerge(database);
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
