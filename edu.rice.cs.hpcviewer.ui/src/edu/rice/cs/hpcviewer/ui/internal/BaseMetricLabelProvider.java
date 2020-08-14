package edu.rice.cs.hpcviewer.ui.internal;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpcsetting.fonts.FontManager;
import edu.rice.cs.hpcviewer.ui.base.IMetricLabelProvider;


/****
 * 
 * Class to provide basic label provider for metric columns
 *
 */
public class BaseMetricLabelProvider extends ColumnLabelProvider implements IMetricLabelProvider {
	protected Scope scope = null;
	protected BaseMetric metric = null;


	public BaseMetricLabelProvider(BaseMetric metricNew) {
		this.metric = metricNew;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.viewer.metric.IMetricLabelProvider#isEnabled()
	 */
	public boolean isEnabled() {
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.viewer.metric.IMetricLabelProvider#setScope(java.lang.Object)
	 */
	public void setScope(Object scope) {
		if (scope instanceof Scope) {
			this.scope = (Scope)scope;
		}
		return;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.viewer.metric.IMetricLabelProvider#setMetric(java.lang.Object)
	 */
	public void setMetric(Object metric) {
		if (metric instanceof BaseMetric) {
			this.metric = (BaseMetric)metric;
		}
		return;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ColumnLabelProvider#getFont(java.lang.Object)
	 */
	public Font getFont(Object element) {
		return FontManager.getMetricFont();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ColumnLabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		String text = null; 

		if ((metric != null) && (element instanceof Scope)) {
			Scope node = (Scope) element;
			text = metric.getMetricTextValue(node);
		}
		return text;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ColumnLabelProvider#getBackground(java.lang.Object)
	 */
	public Color getBackground(final Object element) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ColumnLabelProvider#getForeground(java.lang.Object)
	 */
	public Color getForeground(final Object element) {
		return null;
	}
}
