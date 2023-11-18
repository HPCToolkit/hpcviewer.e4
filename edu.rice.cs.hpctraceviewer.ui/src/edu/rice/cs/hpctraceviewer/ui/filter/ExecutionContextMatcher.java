package edu.rice.cs.hpctraceviewer.ui.filter;

import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcfilter.FilterDataItem;


public class ExecutionContextMatcher implements Matcher<FilterDataItem<IExecutionContext>> 
{
	private final TextMatcherEditor<FilterDataItem<IExecutionContext>> textMatcherEditor;
	private Matcher<Integer> minSamplesMatcher;

	
	public ExecutionContextMatcher(IdTupleType idTupleType) {
		textMatcherEditor = new TextMatcherEditor<>( (list, elem) -> list.add(elem.data.getIdTuple().toString(idTupleType)) );
		minSamplesMatcher = new MinSampleMatcher(0);
	}
	
	@Override
	public boolean matches(FilterDataItem<IExecutionContext> item) {
		boolean isMatched = true;
		
		isMatched = textMatcherEditor.getMatcher().matches(item);
		if (isMatched) {
			return minSamplesMatcher.matches(item.data.getNumSamples());
		}
		return isMatched;
	}


	/**
	 * Set the new text to filter in (included)
	 * 
	 * @param text
	 * 			{@code String} the text to be matched
	 */
	public void setTextToFilter(String text) {
		textMatcherEditor.setFilterText(new String[] {text});
	}
	
	
	/****
	 * Set the minimum samples to be filtered
	 * 
	 * @param minSamples
	 * 			{@code int} the minimum number of allowed samples 
	 */
	public void setMinSamplesToFilter(final int minSamples) {
		minSamplesMatcher = new MinSampleMatcher(minSamples);
	}
	
	
	/****
	 * 
	 * Simple matcher for filtering the minimum number of samples
	 *
	 */
	static class MinSampleMatcher implements Matcher<Integer>
	{
		private final int minSamples;
		
		public MinSampleMatcher(int minSamples) {
			this.minSamples = minSamples;
		}
		
		@Override
		public boolean matches(Integer item) {
			return minSamples <= item.intValue();
		}
	}
}
