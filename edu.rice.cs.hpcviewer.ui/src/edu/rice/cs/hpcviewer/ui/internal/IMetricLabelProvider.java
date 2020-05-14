package edu.rice.cs.hpcviewer.ui.internal;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

public interface IMetricLabelProvider {
	public Color getBackground(Object element);
	public Color getForeground(Object element);
	public Font getFont(Object element);
	public String getText(Object element);
	public boolean isEnabled();
	
	public void setScope(Object scope);
	public void setMetric(Object metric);
}
