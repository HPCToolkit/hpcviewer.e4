package edu.rice.cs.hpcviewer.ui.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.scope.Scope;


/**
 * This class adds support for the MetricLabelProvider extension point in the viewer.
 * @author mohrg
 *
 */
public class MetricLabelProvider extends BaseMetricLabelProvider {
	static private enum MethodFlag { TEXT, FONT, FOREGROUND, BACKGROUND };

	// This is the ID of our extension point
	private static final String METRIC_LABEL_PROVIDER_ID = "edu.rice.cs.hpc.viewer.metric.metricLabelProvider";
	
	private IMetricLabelProvider extLabelProvider[] = null;
	private ExtensionSafeRunnable runnable = null;

	
	/***
	 * 
	 * @param metricNew
	 */
	public MetricLabelProvider(BaseMetric metricNew) {
		super(metricNew);
		IConfigurationElement[] configs = Platform.getExtensionRegistry().getConfigurationElementsFor(METRIC_LABEL_PROVIDER_ID);
		
		if (configs != null && configs.length>0) {
			
			extLabelProvider = new IMetricLabelProvider[configs.length];
			int i = 0;
			
			for (IConfigurationElement e: configs)
			{
				try {
					final Object o = e.createExecutableExtension("class");
					if (o instanceof IMetricLabelProvider) {
						((IMetricLabelProvider)o).setMetric(metricNew);
						extLabelProvider[i] = ((IMetricLabelProvider)o);
						i++;
						
						if (runnable == null)
							runnable = new ExtensionSafeRunnable();
					}
				} catch (CoreException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}		
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.viewer.metric.BaseMetricLabelProvider#getFont(java.lang.Object)
	 */
	public Font getFont(Object element) {

        if (!(element instanceof Scope)) {
                return null;
        }

        if (runnable != null) {
                if ( this.runExtension(runnable, element, MethodFlag.FONT) )
                        return (Font) runnable.getResult();
        }

        return super.getFont(element);
	}

	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.viewer.metric.BaseMetricLabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {

		if (!(element instanceof Scope)) {
			return null;
		}
		
		if (runnable != null) {
			if ( this.runExtension(runnable, element, MethodFlag.TEXT) )
				return (String) runnable.getResult();
		}
 		
		return super.getText(element);
	}

	/**
	 * This method will check to see if anyone has extended this label provider.  If it finds an extension it will create an instance of the 
	 * extending class and call its 0 argument constructor.  Then it calls setters in that class to give it the scope, metric, and metric value for 
	 * the tree viewer cell for which we are providing a label.  Next it will call the getBackground method in the extending class to allow it to 
	 * provide a background color that will be used in this cell.  If an extension is not found this method just returns null to prevent the use 
	 * of color in this tree viewer cell.
	 * @param element (actually the program scope)
	 * @return
	 */
	public Color getBackground(final Object element) {

		if (!(element instanceof Scope)) {
			return null;
		}
		
		if (runnable != null) {
			if ( this.runExtension(runnable, element, MethodFlag.BACKGROUND) )
				return (Color) runnable.getResult();
		}

		return super.getBackground(element);
	}

	/**
	 * This method will check to see if anyone has extended this label provider.  If it finds an extension it will create an instance of the 
	 * extending class and call its 0 argument constructor.  Then it calls setters in that class to give it the scope, metric, and metric value for 
	 * the tree viewer cell for which we are providing a label.  Next it will call the getForeground method in the extending class to allow it to 
	 * provide a foreground color that will be used in this cell.  If an extension is not found this method just returns null to prevent the use 
	 * of color in this tree viewer cell.
	 * @param element (actually the program scope)
	 * @return
	 */
	public Color getForeground(final Object element) {

		if (!(element instanceof Scope)) {
			return null;
		}

		if (runnable != null) {
			if ( this.runExtension(runnable, element, MethodFlag.FOREGROUND) )
				return (Color) runnable.getResult();
		}
		
		return super.getForeground(element);
	}

	/***
	 * run all register extensions of metric label provider
	 * 
	 * @param run
	 * @param element
	 * @param mf
	 */
	private boolean runExtension( ExtensionSafeRunnable run, Object element, MethodFlag mf ) {
		
		boolean isCalled = false;
		
		for (IMetricLabelProvider ext: this.extLabelProvider) {
			
			if (ext != null && ext.isEnabled()) {
				run.setInfo(ext, element, mf);
				ext.setScope(element);
				SafeRunner.run(run);
				isCalled = true;
			}
		}
		return isCalled;
	}
	
	/**
	 * 
	 * Runnable class to make sure that the execution of an extension
	 * 	doesn't perturb the current view
	 *
	 */
	private class ExtensionSafeRunnable implements ISafeRunnable {
		private Object element;
		private MethodFlag mf;
		private IMetricLabelProvider labelProvider;

		private Object result;
		
		public void setInfo(IMetricLabelProvider _labelProvider, Object _element, MethodFlag _mf) {
			labelProvider = _labelProvider;
			mf = _mf;
			element = _element;
		}
		
		public void handleException(Throwable exception) {
			System.out.println("Exception in label provider extension.");
		}
		public void run() throws Exception {
			switch(mf) {
			case TEXT:
				result = labelProvider.getText(element);
				break;
			case FONT:
				result = labelProvider.getFont(element);
				break;
			case FOREGROUND:
				result = labelProvider.getForeground(element);
				break;
			case BACKGROUND:
				result = labelProvider.getBackground(element);
				break;
			}
		}
		
		Object getResult() {
			return result;
		}
	}
}
