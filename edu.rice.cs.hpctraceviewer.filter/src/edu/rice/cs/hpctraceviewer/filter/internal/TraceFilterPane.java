// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.filter.internal;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import edu.rice.cs.hpcfilter.AbstractFilterPane;
import edu.rice.cs.hpcfilter.FilterDataItem;
import edu.rice.cs.hpcfilter.FilterDataItemSortModel;
import edu.rice.cs.hpcfilter.FilterDataProvider;
import edu.rice.cs.hpcfilter.FilterInputData;
import edu.rice.cs.hpcfilter.IFilterChangeListener;
import edu.rice.cs.hpcsetting.fonts.FontManager;

public class TraceFilterPane extends AbstractFilterPane<IExecutionContext> implements IFilterChangeListener
{
	private final IEnableButtonOk buttonEnabler;
	
	private FilterDataProvider<IExecutionContext> dataProvider;
	
	private Label message;
	
	private ExecutionContextMatcherEditor matcherEditor;

	/****
	 * Create a filter pane just for trace lines.
	 * 
	 * @param parent
	 * 			The parent widget (or shell)
	 * @param style
	 * 			Either {@code STYLE_COMPOSITE} or {@code STYLE_INDEPENDENT}
	 * @param inputData
	 * 			The data to be filtered, has to be a type of {@code IExecutionContext}
	 * @param buttonEnabler
	 * 			A call back when to turn on or off the close (or ok) button.
	 */
	public TraceFilterPane(Composite parent, int style, TraceFilterInputData inputData, final IEnableButtonOk buttonEnabler) {
		super(parent, style, inputData);
		this.buttonEnabler = buttonEnabler;
		
		var filterList = getFilterList();
		filterList.setMatcherEditor(matcherEditor);
	}
	
	
	@Override
	protected TextMatcherEditor<FilterDataItem<IExecutionContext>> getTextMatcher() {
		if (matcherEditor == null) {
			TraceFilterInputData inputData = (TraceFilterInputData) getInputData();
			matcherEditor = new ExecutionContextMatcherEditor(inputData.getIdTupleType());
		}
		return matcherEditor.getTextMatcherEditor();
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
	protected FilterDataItemSortModel<IExecutionContext> createSortModel(EventList<FilterDataItem<IExecutionContext>> eventList) {
		return new FilterDataItemSortModel<>(eventList) {
			@Override
			public int compare(FilterDataItem<IExecutionContext> o1, FilterDataItem<IExecutionContext> o2) {
				if (currentSortColumn == 2) {
					int factor = 1;
					if (currentSortDirect == SortDirectionEnum.DESC) {
						factor = -1;
					}
					return factor * Integer.compare(o1.data.getNumSamples(), o2.data.getNumSamples());
				}
				return super.compare(o1, o2);				
			}
		};
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
		message = new Label(parent, SWT.RIGHT);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(message);
		
		return 1;
	}

	@Override
	protected void selectionEvent(FilterDataItem<IExecutionContext> item, int click) {
		// no action is needed
	}

	@Override
	protected void addConfiguration(NatTable table) {
		var config = new AbstractRegistryConfiguration() {
			
			@Override
			public void configureRegistry(IConfigRegistry configRegistry) {
				Font font = FontManager.getMetricFont();

				final Style style = new Style();
				style.setAttributeValue(CellStyleAttributes.FONT, font);
				style.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.RIGHT);
				style.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, VerticalAlignmentEnum.MIDDLE);

				configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, 
													   style, 
													   DisplayMode.NORMAL, 
													   ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 2);
				configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, 
						   							   style, 
						   							   DisplayMode.SELECT, 
						   							   ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 2);
			}
		};
		table.addConfiguration(config);
	}

	@Override
	protected int createAdditionalFilter(Composite parent, FilterInputData<IExecutionContext> inputData) {
		Label lblFilter = new Label(parent, SWT.NONE);
		lblFilter.setText("Minimum samples:");

		var comboSampleFilter = new Text(parent, SWT.DROP_DOWN);
		comboSampleFilter.setSize(50, comboSampleFilter.getSize().y);
		comboSampleFilter.addModifyListener(event -> {
			var strSamples = comboSampleFilter.getText();
			int numSamples = 0;
			message.setText("");
			
			// if the text is empty, we assume including all profiles
			// if the text is not a number, display an error message and show all profiles
			// otherwise, filter based on the minimum number of samples
			if (!strSamples.isEmpty() && !strSamples.isBlank()) {
				try {
					numSamples = Integer.parseInt(strSamples);
				} catch (NumberFormatException e) {
					message.setText("Incorrect number");
					numSamples = 0;
				}
			}
			filterSamples(numSamples);
		});

		// give enough room for the samples 
		GridDataFactory.fillDefaults().hint(100, 20).applyTo(comboSampleFilter);

		return 2;
	}
	
	
	@Override
	protected void eventFilterText(String text) {
		if (matcherEditor == null)
			// matcher is not set yet
			return;

		// Fix issue #347: use the parent filter text
		// to handle regular expression filtering
		super.eventFilterText(text);
		
		matcherEditor.fireChanged();
	}
	
	private void filterSamples(final int minSamples) {
		if (matcherEditor == null)
			// matcher is not set yet
			return;
		
		matcherEditor.filterMinSamples(minSamples);
		getNatTable().refresh();
	}
}
