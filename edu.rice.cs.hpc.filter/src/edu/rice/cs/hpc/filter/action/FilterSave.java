package edu.rice.cs.hpc.filter.action;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import edu.rice.cs.hpc.filter.service.FilterMap;

public class FilterSave extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		FilterMap map = FilterMap.getInstance();
		map.save();
		return null;
	}

}
