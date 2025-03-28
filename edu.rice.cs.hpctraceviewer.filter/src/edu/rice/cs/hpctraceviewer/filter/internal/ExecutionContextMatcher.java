// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpctraceviewer.filter.internal;

import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import org.hpctoolkit.db.local.db.IdTupleType;
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

	
	/****
	 * Return the matcher editor for the text part.
	 * 
	 * @return {@code TextMatcherEditor}
	 */
	public TextMatcherEditor<FilterDataItem<IExecutionContext>> getTextMatcherEditor() {
		return textMatcherEditor;
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
