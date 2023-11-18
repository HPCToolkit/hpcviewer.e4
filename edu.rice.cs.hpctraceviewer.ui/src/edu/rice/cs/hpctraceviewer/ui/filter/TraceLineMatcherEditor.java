package edu.rice.cs.hpctraceviewer.ui.filter;

import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcfilter.FilterDataItem;

public class TraceLineMatcherEditor extends AbstractMatcherEditor<FilterDataItem<IExecutionContext>> 
{
	ExecutionContextMatcher matcher;
	
	public TraceLineMatcherEditor(IdTupleType idTupleType) {
		matcher = new ExecutionContextMatcher(idTupleType);
	}
	
	
	public void filterText(String text) {
		matcher.setTextToFilter(text);
		fireChanged(matcher);
	}
	
	
	public void filterMinSamples(int minSamples) {
		matcher.setMinSamplesToFilter(minSamples);
		fireChanged(matcher);
	}
}
