package edu.rice.cs.hpcviewer.ui.parts.bottomup;

import org.eclipse.jface.viewers.Viewer;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.IMergedScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.data.experiment.scope.filters.ExclusiveOnlyMetricPropagationFilter;
import edu.rice.cs.hpc.data.experiment.scope.filters.InclusiveOnlyMetricPropagationFilter;
import edu.rice.cs.hpcviewer.ui.internal.AbstractContentProvider;
import edu.rice.cs.hpcviewer.ui.internal.ScopeTreeViewer;

/************************************************************************
 * 
 * Content provider class specifically for caller view
 * This class will update the children of a scope dynamically, unlike
 * other views
 *
 ************************************************************************/
public class BottomUpContentProvider extends AbstractContentProvider 
{
	private ExclusiveOnlyMetricPropagationFilter exclusiveOnly;
	private InclusiveOnlyMetricPropagationFilter inclusiveOnly;
	
	public BottomUpContentProvider(ScopeTreeViewer viewer) {
		super(viewer);
	}
	

    /**
     * find the list of children
     */
	@Override
    public Object[] getChildren(Object parentElement) {
    	Object []results = null;
    	
    	if(parentElement instanceof IMergedScope) {
    		// normal mode
    		IMergedScope parent = ((IMergedScope) parentElement);
    		results = parent.getAllChildren(inclusiveOnly, exclusiveOnly);
        	
    	} else if (parentElement instanceof Scope) {
    		Scope scope = (Scope) parentElement;
    		results = scope.getChildren();
    	}
    	return results;
    }
    
    /***
     * Update the database
     * @param experiment
     */
    public void setDatabase(Experiment experiment) {
    	exclusiveOnly = new ExclusiveOnlyMetricPropagationFilter(experiment);
    	inclusiveOnly = new InclusiveOnlyMetricPropagationFilter(experiment);
    }


	@Override
	public void dispose() {
		inclusiveOnly = null;
		exclusiveOnly = null;
	}


	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}    
}
