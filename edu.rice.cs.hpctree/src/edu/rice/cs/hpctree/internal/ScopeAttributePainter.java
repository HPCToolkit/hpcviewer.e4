package edu.rice.cs.hpctree.internal;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import edu.rice.cs.hpcdata.experiment.scope.CallSiteScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpctree.ScopeTreeDataProvider;

public class ScopeAttributePainter extends TextPainter 
{
	private static final String EMPTY_STRING = "";
	private static final String SUFFIX_LINE  = ": ";
	
	private final ScopeTreeDataProvider dataProvider;
	
	public ScopeAttributePainter(ScopeTreeDataProvider dataProvider) {
		this.dataProvider = dataProvider;
	}
	
	@Override
	protected String convertDataType(ILayerCell cell, IConfigRegistry configRegistry) {
		int rowIndex = cell.getRowIndex();
		Scope scope  = dataProvider.getRowObject(rowIndex);
		if (scope instanceof CallSiteScope) {
			CallSiteScope cs = (CallSiteScope) scope;
            int lineNumber = 1 + cs.getLineScope().getFirstLineNumber();
            if (lineNumber > 0)
            	return lineNumber + SUFFIX_LINE;
		}
		return EMPTY_STRING;
	}
}
