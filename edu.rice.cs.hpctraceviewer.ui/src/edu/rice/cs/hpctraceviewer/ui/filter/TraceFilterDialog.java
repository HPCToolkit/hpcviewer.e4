package edu.rice.cs.hpctraceviewer.ui.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ca.odell.glazedlists.FilterList;
import edu.rice.cs.hpcbase.IFilteredData;
import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.db.IFileDB.IdTupleOption;
import edu.rice.cs.hpcdata.util.OSValidator;
import edu.rice.cs.hpcfilter.AbstractFilterPane;
import edu.rice.cs.hpcfilter.FilterDataItem;
import edu.rice.cs.hpcfilter.FilterDataProvider;
import edu.rice.cs.hpcfilter.FilterInputData;
import edu.rice.cs.hpcfilter.IFilterChangeListener;

public class TraceFilterDialog extends Dialog implements IEnableButtonOk
{
	private final IFilteredData traceData;
	private AbstractFilterPane<IExecutionContext> filterPane;

	
	public TraceFilterDialog(Shell parentShell, final IFilteredData traceData) {
		super(parentShell);

		this.traceData = traceData;
	}


	public List<FilterDataItem<IExecutionContext>> getList() {
		if (filterPane == null)
			return Collections.emptyList();
		
		return filterPane.getEventList();
	}

	@Override
	protected boolean isResizable() {
		return true;
	}
	
	@Override
	protected Point getInitialSize() {
		return new Point(600, 600);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText("Filter execution context");
		
		Composite composite = new Composite(parent, SWT.BORDER);

		GridLayout grid = new GridLayout();
		grid.numColumns=1;
		// bad hack: Have to add a "pad" margin on the top
		// This may be a SWT bug that the position of the composite is negative on Mac
		int padding = 0;
		if (OSValidator.isMac()) 
			padding = 30;
		
		grid.marginTop=padding;

		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(grid);

		filterPane = getFilterPane(composite, traceData);
		
		return composite;
	}


	@Override
	public void enableOkButton(boolean enabled) {
		getButton(IDialogConstants.OK_ID).setEnabled(enabled);
	}

	
	/****
	 * Method to get the main filter pane.
	 * It can be override by a subclass
	 *  
	 * @param composite
	 * 			The container of the filter
	 * @return
	 */
	protected AbstractFilterPane<IExecutionContext>  getFilterPane(Composite composite, IFilteredData traceData) {
				
		/*
		 * This isn't the prettiest, but when we are local, we don't want to set
		 * it to filtered unless we have to (i.e. unless the user actually
		 * applies a filter). If the data is already filtered, we don't care and
		 * we just return the filtered data we have been using (which makes the
		 * call to set it redundant). If it's not, we wait to replace the
		 * current filter with the new filter until we know we have to.
		 */
        var mapToSamples = traceData.getMapFromExecutionContextToNumberOfTraces();
        
        List<IdTuple> listDenseIds = traceData.getDenseListIdTuple(IdTupleOption.BRIEF);
        List<IdTuple> listIds = traceData.getListOfIdTuples(IdTupleOption.BRIEF);
        
        List<FilterDataItem<IExecutionContext>> items = new ArrayList<>(listDenseIds.size());
        
        // initialize the ranks to be displayed in the dialog box
        // to know which ranks are already shown, we need to compare with the filtered ranks.
        // if the id tuple in the original list is the same as the one in filtered list, 
        // them the rank is already displayed.
        // This list needs to be optimized.
        
        for(int i=0, j=0; i<listDenseIds.size(); i++) {
        	var idt = listDenseIds.get(i);

        	int numSamples = 0;
        	var samples = mapToSamples.get(idt);
        	if (samples != null)
        		numSamples = samples.intValue();

        	var executionContext = new ExecutionContext(idt, numSamples);
        	
        	var checked = (j<listIds.size() && listDenseIds.get(i) == listIds.get(j));
        	if (checked)
        		j++;

        	var item = new TraceFilterDataItem(executionContext, checked, true) {
        		@Override
        		public String getLabel() {
        			return executionContext.getIdTuple().toString(traceData.getIdTupleTypes());
        		}
        	};
        	
        	items.add(item);
        }
		
		var inputData = new FilterInputData<IExecutionContext>(items);
		
		return new TraceFilterPane(composite, AbstractFilterPane.STYLE_INDEPENDENT, inputData, this);
	}
	
	
	
	static class TraceFilterPane extends AbstractFilterPane<IExecutionContext> implements IFilterChangeListener
	{
		private final IEnableButtonOk buttonEnabler;
		private FilterDataProvider<IExecutionContext> dataProvider;

		protected TraceFilterPane(Composite parent, int style, FilterInputData<IExecutionContext> inputData, final IEnableButtonOk buttonEnabler) {
			super(parent, style, inputData);
			this.buttonEnabler = buttonEnabler;
		}

		@Override
		protected void setLayerConfiguration(DataLayer datalayer) {
			datalayer.setColumnPercentageSizing(true);
			datalayer.setColumnWidthPercentageByPosition(0, 10);
			datalayer.setColumnWidthPercentageByPosition(1, 70);
			datalayer.setColumnWidthPercentageByPosition(2, 20);
		}

		@Override
		protected String[] getColumnHeaderLabels() {
			return new String[] {"Visible", "Execution context", "Samples"};
		}

		@Override
		protected FilterDataProvider<IExecutionContext> getDataProvider(
				FilterList<FilterDataItem<IExecutionContext>> filterList) {
			if (dataProvider == null) {
				dataProvider = new FilterDataProvider<>(filterList, this) {
					@Override
					public int getColumnCount() {
						return 3;
					}
					
					@Override
					public Object getDataValue(int columnIndex, int rowIndex) {
						var item = filterList.get(rowIndex);
						IExecutionContext context = (IExecutionContext) item.getData();

						if (columnIndex == 2) {
							return context.getNumSamples();
						}
						
						return super.getDataValue(columnIndex, rowIndex);
					}
				};
			}
			return dataProvider;
		}


		@Override
		public void changeEvent(Object data) {
			var list = getEventList();
			if (list == null || list.isEmpty())
				return;
			var enabled = (list.stream().anyMatch(item -> item.checked));
			buttonEnabler.enableOkButton(enabled);
		}

		
		@Override
		protected int createAdditionalButton(Composite parent, FilterInputData<IExecutionContext> inputData) {
			return 0;
		}

		@Override
		protected void selectionEvent(FilterDataItem<IExecutionContext> item, int click) {
			
		}

		@Override
		protected void addConfiguration(NatTable table) {
			
		}		
	}
}
