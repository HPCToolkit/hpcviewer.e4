package edu.rice.cs.hpctree.internal;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

import edu.rice.cs.hpcdata.experiment.scope.CallSiteScope;
import edu.rice.cs.hpcdata.experiment.scope.LineScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcsetting.color.ColorManager;
import edu.rice.cs.hpctree.resources.ViewerColorManager;

public class CallSiteTextPainter extends TextPainter 
{
	private static final String SUFFIX_LINE  = ": ";

	private final ScopeTreeDataProvider dataProvider;
	private boolean active;
	
	public CallSiteTextPainter(ScopeTreeDataProvider dataProvider) {
		this.dataProvider = dataProvider;
		this.active = false;
	}
	
	@Override
	protected String convertDataType(ILayerCell cell, IConfigRegistry configRegistry) {
		int rowIndex = cell.getRowIndex();
		Scope scope = dataProvider.getRowObject(rowIndex);
		if (scope instanceof CallSiteScope) {
			LineScope ls = ((CallSiteScope)scope).getLineScope();
			int ln = ls.getFirstLineNumber();
			if (ln < 1)
				return EMPTY;
			int line = 1 + ln;
			return line + SUFFIX_LINE;
		}
		return EMPTY;
	}
	
	
    @Override
    public void paintCell(ILayerCell cell, GC gc, Rectangle rectangle, IConfigRegistry configRegistry) {
    	LabelStack labels = cell.getConfigLabels();
    	active = labels.hasLabel(ScopeTreeLabelAccumulator.LABEL_CALLSITE) ||
    			 labels.hasLabel(ScopeTreeLabelAccumulator.LABEL_CALLER);
    	
    	super.paintCell(cell, gc, rectangle, configRegistry);
    }
    
    
	@Override
    public void setupGCFromConfig(GC gc, IStyle cellStyle) {
    	super.setupGCFromConfig(gc, cellStyle);    	
    	
		Color oldBackgrColor = gc.getBackground();
		Color color = ColorManager.getTextFg(oldBackgrColor);
    	if (active) {
			color = ViewerColorManager.getActiveColor();		    		
    	}
    	gc.setForeground(color);
    }
}
