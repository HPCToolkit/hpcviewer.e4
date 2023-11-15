package edu.rice.cs.hpctraceviewer.ui.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import edu.rice.cs.hpcbase.IFilteredData;
import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.db.IFileDB.IdTupleOption;
import edu.rice.cs.hpcdata.util.OSValidator;
import edu.rice.cs.hpcfilter.AbstractFilterPane;
import edu.rice.cs.hpcfilter.FilterDataItem;
import edu.rice.cs.hpcfilter.FilterInputData;

/**************************************************
 * 
 * Special window to filter trace lines.
 * The filter is based on the execution contexts or id-tuples or profiles
 * (whatever the name, they are interchanged).
 * In addition, it is possible to filter based on the number of samples (to do).
 *
 **************************************************/
public class TraceFilterDialog extends Dialog implements IEnableButtonOk
{
	private final IFilteredData traceData;
	private AbstractFilterPane<IExecutionContext> filterPane;

	
	public TraceFilterDialog(Shell parentShell, final IFilteredData traceData) {
		super(parentShell);

		this.traceData = traceData;
	}


	private List<FilterDataItem<IExecutionContext>> getList() {
		if (filterPane == null)
			return Collections.emptyList();
		
		return filterPane.getEventList();
	}
	
	
	public List<Integer> getCheckedIndexes() {
		if (getReturnCode() != Window.OK)
			return Collections.emptyList();
		
		var allList = getList();
		return allList.stream()
				      .filter(item -> item.checked)
				      .map(elem -> ((TraceFilterDataItem)elem).getIndex())
				      .collect(Collectors.toUnmodifiableList());
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
		Map<IdTuple, Integer> mapToSamples;
		try {
			mapToSamples = traceData.getMapFromExecutionContextToNumberOfTraces();
		} catch (IllegalAccessError e) {
			mapToSamples = null;
		}
		
        
        List<IdTuple> listDenseIds = traceData.getDenseListIdTuple(IdTupleOption.BRIEF);
        List<IdTuple> listIds = traceData.getListOfIdTuples(IdTupleOption.BRIEF);
        
        List<FilterDataItem<IExecutionContext>> items = new ArrayList<>(listDenseIds.size());
        
        // initialize the ranks to be displayed in the dialog box
        // to know which ranks are already shown, we need to compare with the filtered ranks.
        // if the id tuple in the original list is the same as the one in filtered list, 
        // them the rank is already displayed.
        // This list needs to be optimized.
        int j = 0;
        
        for(var idt: listDenseIds) {

        	int numSamples = 0;
        	var samples = mapToSamples != null ? mapToSamples.get(idt) : null;
        	if (samples != null)
        		numSamples = samples.intValue();

        	var executionContext = new ExecutionContext(idt, numSamples);
        	
        	var checked = (j<listIds.size() && idt == listIds.get(j));
        	if (checked)
        		j++;

        	var item = new TraceFilterDataItem(items.size(), executionContext, checked, true) {
        		@Override
        		public String getLabel() {
        			// special label for id-tuple: we need to get the label by using a id tuple type.
        			// Unfortunately, this type only available from IFilteredBaseData
        			//
        			return executionContext.getIdTuple().toString(traceData.getIdTupleTypes());
        		}
        	};
        	
        	items.add(item);
        }
		
		var inputData = new FilterInputData<IExecutionContext>(items);
		
		return new TraceFilterPane(composite, AbstractFilterPane.STYLE_INDEPENDENT, inputData, this);
	}
}
