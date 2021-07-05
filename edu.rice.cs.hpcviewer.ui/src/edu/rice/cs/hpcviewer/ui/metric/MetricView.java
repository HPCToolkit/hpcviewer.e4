package edu.rice.cs.hpcviewer.ui.metric;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.osgi.service.prefs.Preferences;

import edu.rice.cs.hpcbase.map.UserInputHistory;
import edu.rice.cs.hpcmetric.AbstractFilterComposite;
import edu.rice.cs.hpcmetric.IMetricFilterEvent;
import edu.rice.cs.hpcmetric.MetricFilterInput;
import edu.rice.cs.hpcviewer.ui.base.IUpperPart;

public class MetricView extends CTabItem implements IUpperPart 
{
	private static final String HISTORY_COLUMN_PROPERTY = "column_property";
	private static final String HISTORY_APPLY_ALL = "apply-all";
	public static final  String INPUT_DEFAULT = "edu.rice.cs.hpcviewer.ui.metric.MetricView";

	private final Composite container ;
	private Button btnApplyToAllViews;
	private boolean applyToAllViewOption = true;
	private AbstractFilterComposite filterPane;

	public MetricView(CTabFolder parent, int style) {
		super(parent, style);
		setShowClose(true);

		container = new Composite(parent, style);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(container);
		
	}

	
	
	public void enableAllViewOption(boolean applyToAllViewOption)
	{
		this.applyToAllViewOption = applyToAllViewOption;
	}

	
	@Override
	public String getTitle() {
		return "Metric properties";
	}

	@Override
	public void setInput(Object input) {
		if (input == null || !(input instanceof MetricFilterInput))
			return;
		MetricFilterInput inputFilter = (MetricFilterInput) input;
		
		AbstractFilterComposite filterPane = new AbstractFilterComposite(container, SWT.NONE, inputFilter) {
			
			@Override
			protected void createAdditionalButton(Composite parent) {

				btnApplyToAllViews = new Button(parent, SWT.CHECK);
				btnApplyToAllViews.setText("Apply to all views");
				btnApplyToAllViews.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
				btnApplyToAllViews.setEnabled(applyToAllViewOption);
				btnApplyToAllViews.setSelection( applyToAllViewOption && getHistory() );
				
				btnApplyToAllViews.addSelectionListener(new SelectionAdapter() {
					
					@Override
					public void widgetSelected(SelectionEvent e) {
						Preferences pref = UserInputHistory.getPreference(HISTORY_COLUMN_PROPERTY);
						pref.putBoolean(HISTORY_APPLY_ALL, btnApplyToAllViews.getSelection());
						UserInputHistory.setPreference(pref);
					}
				});
			}
		};
		GridDataFactory.fillDefaults().grab(true, true).applyTo(filterPane);
		GridLayoutFactory.fillDefaults().applyTo(container);
		setControl(container);
	}

	@Override
	public boolean hasEqualInput(Object input) {
		if (input instanceof String) {
			return INPUT_DEFAULT.compareTo((String) input) == 0;
		}
		return false;
	}

	@Override
	public void setMarker(int lineNumber) {}


	/***
	 * get the user preference of "apply-all"
	 * @return
	 */
	private boolean getHistory() {
		return UserInputHistory.getPreference(HISTORY_COLUMN_PROPERTY).getBoolean(HISTORY_APPLY_ALL, true);
	}

}
