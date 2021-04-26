package edu.rice.cs.hpcdata.experiment.metric;

import edu.rice.cs.hpcdata.experiment.scope.Scope;

public class CombineAggregateMetricVarMap extends MetricVarMap {
	private Scope scopes[];
	private int iCounter;
	

	public CombineAggregateMetricVarMap() {
		this.scopes = new Scope[2];
		iCounter = -1;
	}
	
	public void setScopes(Scope s_source, Scope s_target) {
		scopes[0] = s_source;
		scopes[1] = s_target;
		iCounter = 0;
	}
	
	/**
	 * Overloaded method: a callback to retrieve the value of a variable (or a metric)
	 * If the variable is a normal variable, it will call the parent method.		
	 */
	public double getValue(String varName) {
		assert(iCounter==0 || iCounter==1);
		if (iCounter<0 || iCounter>1) {
			final String msg = "Unable to retrieve value.\n\tscopes=[" + scopes[0] + ", " + scopes[1]+"]\n\tscopes-id=[" +
					 scopes[0].getCCTIndex() + "," + scopes[1].getCCTIndex()  + "]\n\tcounter=" + iCounter  ;
			throw new RuntimeException(msg);
		}
			
		super.setScope(this.scopes[iCounter]);
		
		this.iCounter++;
		return super.getValue(varName);
	}

}
