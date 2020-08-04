 
package edu.rice.cs.hpctraceviewer.ui.handlers;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Evaluate;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;

import edu.rice.cs.hpctraceviewer.ui.base.ITracePart;

public class TraceDataExist 
{
	@Evaluate
	public boolean evaluate(@Optional @Named(IServiceConstants.ACTIVE_PART)MPart part) {
		if (part == null)
			return false;
		
		Object obj = part.getObject();

		if (!(obj instanceof ITracePart))
			return false;
		
		return ((ITracePart)obj).getInput() != null;
	}
}
