package edu.rice.cs.hpctree.internal;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.ImagePainter;
import org.eclipse.swt.graphics.Image;

import edu.rice.cs.hpctree.resources.IconManager;

public class CallSiteImagePainter extends ImagePainter 
{
	private final Image callsite;
	private final Image disableCallsite;
	private final Image caller;
	private final Image disableCaller;
	
	public CallSiteImagePainter() {
		final IconManager iconManager = IconManager.getInstance();
		callsite = iconManager.getImage(IconManager.Image_CallTo);
		disableCallsite = iconManager.getImage(IconManager.Image_CallToDisabled);
		caller = iconManager.getImage(IconManager.Image_CallFrom);
		disableCaller = iconManager.getImage(IconManager.Image_CallFromDisabled);
	}
	
		
	@Override
    protected Image getImage(ILayerCell cell, IConfigRegistry configRegistry) {
		final LabelStack labels = cell.getConfigLabels();
		
		if (labels.hasLabel(ScopeTreeLabelAccumulator.LABEL_CALLSITE)) {
			return callsite;
		} else if (labels.hasLabel(ScopeTreeLabelAccumulator.LABEL_CALLSITE_DISABLED)) {
			return disableCallsite;
		} else if (labels.hasLabel(ScopeTreeLabelAccumulator.LABEL_CALLER)) {
			return caller;
		}else if (labels.hasLabel(ScopeTreeLabelAccumulator.LABEL_CALLER_DISABLED)) {
			return disableCaller;
		}
		return null;
	}
}
