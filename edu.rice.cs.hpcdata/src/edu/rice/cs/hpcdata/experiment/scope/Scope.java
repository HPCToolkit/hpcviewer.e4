//////////////////////////////////////////////////////////////////
//																//
//	Scope.java													//
//																//
//																//
//	(c) Copyright 2002-2022 Rice University. All rights reserved.	//
//																//
//	$LastChangedDate$		
//  $LastChangedBy$ 					//
//////////////////////////////////////////////////////////////////




package edu.rice.cs.hpcdata.experiment.scope;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import edu.rice.cs.hpcdata.experiment.IExperiment;
import edu.rice.cs.hpcdata.experiment.metric.AggregateMetric;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.DerivedMetric;
import edu.rice.cs.hpcdata.experiment.metric.HierarchicalMetric;
import edu.rice.cs.hpcdata.experiment.metric.IMetricValueCollection;
import edu.rice.cs.hpcdata.experiment.metric.MetricRaw;
import edu.rice.cs.hpcdata.experiment.metric.MetricType;
import edu.rice.cs.hpcdata.experiment.metric.MetricValue;
import edu.rice.cs.hpcdata.experiment.scope.filters.MetricValuePropagationFilter;
import edu.rice.cs.hpcdata.experiment.scope.visitors.FilterScopeVisitor;
import edu.rice.cs.hpcdata.experiment.scope.visitors.IScopeVisitor;
import edu.rice.cs.hpcdata.experiment.source.SourceFile;



//////////////////////////////////////////////////////////////////////////
//	CLASS SCOPE							//
//////////////////////////////////////////////////////////////////////////

/**
 *
 * A scope in an HPCView experiment.
 *
 * FIXME: do we want to merge the functionality of Scope and Scope.Node?
 * it's kind of irritating to have the two things be distinct and having
 * objects which point at each other makes me a little uneasy.
 */


public abstract class Scope 
implements IMetricScope
{

	//////////////////////////////////////////////////////////////////////////
	//PROTECTED CONSTANTS						//
	//////////////////////////////////////////////////////////////////////////

	protected static int id = Integer.MAX_VALUE;

	//////////////////////////////////////////////////////////////////////////
	//PUBLIC CONSTANTS						//
	//////////////////////////////////////////////////////////////////////////


	/** The value used to indicate "no line number". */
	public static final int NO_LINE_NUMBER = -169; // any negative number other than -1

	static public final int SOURCE_CODE_UNKNOWN = 0;
	static public final int SOURCE_CODE_AVAILABLE = 1;
	static public final int SOURCE_CODE_NOT_AVAILABLE= 2;

	//////////////////////////////////////////////////////////////////////////
	// ATTRIBUTES
	//////////////////////////////////////////////////////////////////////////


	protected RootScope root;

	/** The source file containing this scope. */
	protected SourceFile sourceFile;

	/** the scope identifier */
	protected int flat_node_index;

	/** The first line number of this scope. */
	protected int firstLineNumber;

	/** The last line number of this scope. */
	protected int lastLineNumber;

	/** The metric values associated with this scope. */
	private IMetricValueCollection metrics;

	/**
	 * FIXME: this variable is only used for the creation of callers view to count
	 * 			the number of instances. To be removed in the future
	 */
	private int iCounter;

	//the cpid is removed in hpcviewer, but hpctraceview still requires it in order to dfs
	private int cpid;

	public int iSourceCodeAvailability = Scope.SOURCE_CODE_UNKNOWN;

	protected ITreeNode<Scope> node;


	//////////////////////////////////////////////////////////////////////////
	//	INITIALIZATION														//
	//////////////////////////////////////////////////////////////////////////




	/*************************************************************************
	 *	Creates a Scope object with associated source line range.
	 ************************************************************************/

	public Scope(RootScope root, SourceFile file, int first, int last, int cct_id, int flat_id)
	{
		// creation arguments
		this.root       = root;
		this.sourceFile = file;
		this.firstLineNumber = first;
		this.lastLineNumber  = last;
		this.flat_node_index = flat_id;
		this.cpid      = cct_id;
		this.iCounter  = 0;

		node = new TreeNode<>(cct_id);
	}


	public Scope(RootScope root, SourceFile file, int first, int last, int cct_id, int flat_id, int cpid)
	{
		this(root, file, first, last, cct_id, flat_id);
		this.cpid = cpid;
	}



	/*************************************************************************
	 *	Creates a Scope object with associated source file.
	 ************************************************************************/

	public Scope(RootScope root, SourceFile file, int scopeID)
	{
		this(root, file, Scope.NO_LINE_NUMBER, Scope.NO_LINE_NUMBER, scopeID, scopeID);
	}

	/***
	 * get the flat (static) index of the scope. Some scopes may have the same
	 * flat index if they are from the same address range. 
	 * @return
	 */
	public int getFlatIndex() {
		return this.flat_node_index;
	}

	/***
	 * retrieve the CCT index of this scope.<br/>
	 * The index is theoretically unique, so it can be used as an ID.
	 * 
	 * @return
	 */
	public int getCCTIndex() {
		return (int) node.getValue();
	}

	/***
	 * Set new index for this scope. cct index is usually constant,
	 * but in case needed, it can be modified.
	 * <br>
	 * Use it on your own risk.
	 * 
	 * @param index
	 * 			The new index
	 */
	public void setCCTIndex(int index) {
		node.setValue(index);
	}

	/***
	 * Make this scope as a virtual root to be displayed on the table.
	 * 
	 * @return new root which is the duplicate of this scope.
	 */
	public Scope createRoot() {
		Scope root = duplicate();
		root.setParentScope(getParentScope());
		root.addSubscope(this);
		if (root instanceof CallSiteScopeCallerView) {
			((CallSiteScopeCallerView)root).markScopeHasChildren();
		}
		copyMetrics(root, 0);

		return root;
	}


	//////////////////////////////////////////////////////////////////////////
	// DUPLICATION														//
	//////////////////////////////////////////////////////////////////////////



	/*************************************************************************
	 *	Creates a Scope object with no associated source file.
	 ************************************************************************/

	public abstract Scope duplicate();



	//////////////////////////////////////////////////////////////////////////
	//	SCOPE DISPLAY														//
	//////////////////////////////////////////////////////////////////////////




	/*************************************************************************
	 *	Returns the user visible name for this scope.
	 *
	 *	Subclasses should override this to implement useful names.
	 *
	 ************************************************************************/

	public abstract String getName();


	//////////////////////////////////////////////////////////////////////////
	// counter														//
	//////////////////////////////////////////////////////////////////////////


	public void incrementCounter() {
		this.iCounter++;
	}

	public void decrementCounter() {
		if (this.isCounterPositif())
			this.iCounter--;
		else {
			System.err.println("Scope [" + this.getCCTIndex() + "/" + this.flat_node_index + "] "  + this.getName() + " has non-positive counter");
		}
	}

	public void setCounter(int counter) {
		this.iCounter = counter;
	}

	public int getCounter() {
		return this.iCounter;
	}

	public boolean isCounterPositif() {
		return this.iCounter>0;
	}

	public boolean isCounterZero() {
		return (this.iCounter == 0);
	}

	/*************************************************************************
	 * Returns which processor was active
	 ************************************************************************/

	public int getCpid()
	{
		return cpid;
	}


	/*************************************************************************
	 *	Sets the value of the cpid
	 ************************************************************************/

	public void setCpid(int _cpid)
	{
		this.cpid = _cpid;
	}

	/*************************************************************************
	 *	Returns the tool tip for this scope.
	 ************************************************************************/

	public String getToolTip()
	{
		return this.getSourceCitation();
	}




	/*************************************************************************
	 *	Converts the scope to a <code>String</code>.
	 *
	 *	<p>
	 *	This method is for the convenience of <code>ScopeTreeModel</code>,
	 *	which passes <code>Scope</code> objects to the default tree cell
	 *	renderer.
	 *
	 ************************************************************************/

	public String toString()
	{
		return this.getName();
	}

	
	public static int getLexicalType(Scope scope) {
		String type = scope.getClass().getSimpleName().substring(0, 2);
		return type.hashCode();
	}

	public static int generateFlatID(int lexicalType, int lmId, int fileId, int procId, int line) {
		// linearize the flat id. This is not sufficient and causes collisions for large and complex source code
		// This needs to be computed more reliably.
		int flatId = lexicalType << 28 |
					 lmId        << 24 |
					 fileId      << 16 | 
					 procId      << 8  | 
					 line;
		return flatId;
	}


	/*************************************************************************
	 *	Returns a display string describing the scope's source code location.
	 ************************************************************************/

	protected String getSourceCitation()
	{
		return getSourceCitation(sourceFile, firstLineNumber, lastLineNumber);
	}


	private String getSourceCitation(SourceFile sourceFile, int line1, int line2)
	{

		// some scopes such as load module, doesn't have a source code file (they are binaries !!)
		// this hack will return the name of the scope instead of the citation file
		if (sourceFile == null) {
			return this.getName();
		}
		return sourceFile.getName() + ": " + this.getLineOnlyCitation(line1, line2);

	}




	/*************************************************************************
	 *	Returns a display string describing the scope's line number range.
	 ************************************************************************/

	protected String getLineNumberCitation()
	{
		return this.getLineNumberCitation(firstLineNumber, lastLineNumber);
	}


	private String getLineNumberCitation(int line1, int line2)
	{
		String cite;

		// we must display one-based line numbers
		int first1 = 1 + line1;
		int last1  = 1 + line2;

		if(line1 == Scope.NO_LINE_NUMBER) {
			cite = "";	// TEMPORARY: is this the right thing to do?
		} else if(line1 == line2)
			cite = "line" + " " + first1;
		else
			cite = "lines" + " " + first1 + "-" + last1;

		return cite;
	}


	private String getLineOnlyCitation(int line1, int line2) {
		String cite;

		// we must display one-based line numbers
		int first1 = 1 + line1;
		int last1  = 1 + line2;

		if(line1 == Scope.NO_LINE_NUMBER) {
			cite = "";	// TEMPORARY: is this the right thing to do?
		} else if(line1 == line2)
			cite = String.valueOf(first1);
		else
			cite = first1 + "-" + last1;

		return cite;
	}

	//////////////////////////////////////////////////////////////////////////
	//	ACCESS TO SCOPE														//
	//////////////////////////////////////////////////////////////////////////




	/*************************************************************************
	 *	Returns the source file of this scope.
	 *
	 *	<p>
	 *	<em>TEMPORARY: This assumes that each scope "has" (i.e. intersects)
	 *	at most one source file -- not true for discontiguous scopes.</em>
	 *
	 ************************************************************************/

	public SourceFile getSourceFile()
	{
		return this.sourceFile;
	}

	
	/*************************************************************************
	 * Set the scope to a new source file
	 * 
	 * @param sourceFile
	 * 			A source file to be assigned. It can be null.
	 ************************************************************************/
	public void setSourceFile(SourceFile sourceFile) 
	{
		this.sourceFile = sourceFile;
	}

	/*************************************************************************
	 *	Returns the first line number of this scope in its source file.
	 *
	 *	<p>
	 *	<em>TEMPORARY: This assumes that each scope "has" (i.e. intersects)
	 *	at most one source file -- not true for discontiguous scopes.</em>
	 *
	 ************************************************************************/

	public int getFirstLineNumber()
	{
		return this.firstLineNumber;
	}




	/*************************************************************************
	 *	Returns the last line number of this scope in its source file.
	 *
	 *	<p>
	 *	<em>TEMPORARY: This assumes that each scope "has" (i.e. intersects)
	 *	at most one source file -- not true for discontiguous scopes.</em>
	 *
	 ************************************************************************/

	public int getLastLineNumber()
	{
		return this.lastLineNumber;
	}




	//////////////////////////////////////////////////////////////////////////
	//	SCOPE HIERARCHY														//
	//////////////////////////////////////////////////////////////////////////




	/*************************************************************************
	 *	Returns the parent scope of this scope.
	 ************************************************************************/

	public Scope getParentScope()
	{
		return node.getParent();
	}


	/*************************************************************************
	 *	Sets the parent scope of this scope.
	 ************************************************************************/

	public void setParentScope(Scope parentScope)
	{
		node.setParent(parentScope);
	}




	/*************************************************************************
	 *	Returns the number of subscopes within this scope.
	 ************************************************************************/

	public int getSubscopeCount()
	{
		return node.getChildCount();
	}




	/*************************************************************************
	 *	Returns the subscope at a given index.
	 ************************************************************************/

	public Scope getSubscope(int index)
	{
		return node.getChildAt(index);
	}


	/*************************************************************************
	 *	Adds a subscope to the scope.
	 ************************************************************************/

	public void addSubscope(Scope subscope)
	{
		node.add(subscope);
	}



	//////////////////////////////////////////////////////////////////////////
	// EXPERIMENT DATABASE 													//
	//////////////////////////////////////////////////////////////////////////
	public IExperiment getExperiment() {
		return root.getExperiment();
	}


	//////////////////////////////////////////////////////////////////////////
	// Access to RootScope 													//
	//////////////////////////////////////////////////////////////////////////

	/*******
	 * set the root scope
	 * @param root
	 */
	public void setRootScope(RootScope root)
	{
		this.root = root;
	}

	/****
	 * get the root scope of this scope
	 * @return
	 */
	public RootScope getRootScope()
	{
		return root;
	}




	//////////////////////////////////////////////////////////////////////////
	//ACCESS TO METRICS													//
	//////////////////////////////////////////////////////////////////////////


	/***************************************************************************
	 * check whether the scope has at least a non-zero metric value
	 * @return true if the scope has at least a non-zero metric value
	 ***************************************************************************/
	public boolean hasNonzeroMetrics() {
		ensureMetricStorage();
		return metrics.hasMetrics(this);
	}



	/*************************************************************************
	 * Returns the value of a given metric at this scope.
	 * This method pays attention of the type of metric. If the metric is a 
	 * derived incremental metric (class {@code AggregateMetric}) it will ask
	 * the metric class to finalize the value.
  	 * 
  	 * @param metric
  	 * 			The metric 
  	 * @return {@code MetricValue}
  	 * 			The metric value. If the index has no value, it returns {@code MetricValue.NONE}
	 ************************************************************************/

	public MetricValue getMetricValue(BaseMetric metric)
	{
		ensureMetricStorage();
		
		// special case for raw metric: we need to grab the value
		// from the metric directly. No caching here.
		
		if (metric instanceof MetricRaw || metric instanceof AggregateMetric)
			return metric.getValue(this);
		
		return metrics.getValue(this, metric);
	}


	/***************************************************************************
  	 * <p>
  	 * This method returns the cached raw metric value.
  	 * Unlike {@link getMetricValue(BaseMetric)}, it doesn't trigger calculation
  	 * of final metric value.
  	 * </p>
  	 * overload the method to take-in the index ---FMZ
  	 * 
  	 * @param index
  	 * 			The metric index
  	 * @return {@code MetricValue}
  	 * 			The metric value. If the index has no value, it returns {@code MetricValue.NONE}
	 ***************************************************************************/

	public MetricValue getMetricValue(int index)
	{
		ensureMetricStorage();
		return metrics.getValue(this, index);
	}


	/*************************************************************************
	 *	Sets the value of a given metric at this scope.
	 ************************************************************************/
	public void setMetricValue(int index, MetricValue value)
	{
		ensureMetricStorage();
		metrics.setValue(index, value);
	}

	/*************************************************************************
	 *	Add the metric cost from a source with a certain filter for all metrics
	 ************************************************************************/
	public void accumulateMetrics(Scope source, MetricValuePropagationFilter filter, int nMetrics) {
		var experiment = root.getExperiment();
		var metrics    = experiment.getMetrics();
		List<DerivedMetric> listDerivedMetrics = new ArrayList<>();

		// compute metrics with predefined values first.
		// we'll compute the derived metrics once all other metrics are initialized
		for (BaseMetric m: metrics) {
			if (m instanceof DerivedMetric) {
				listDerivedMetrics.add((DerivedMetric) m);
			} else {
				accumulateMetric(source, m, filter);
			}
		}
		// compute the derived metrics
		for(DerivedMetric m: listDerivedMetrics) {
			accumulateMetric(source, m, filter);
		}
	}

	/*************************************************************************
	 *	Add the metric cost from a source with a certain filter for a certain metric
	 ************************************************************************/
	private void accumulateMetric(Scope source, 
								  BaseMetric metric, 
								  MetricValuePropagationFilter filter) {
		final int mIndex = metric.getIndex();
		if (filter.doPropagation(source, this, mIndex, mIndex)) {
			MetricValue m = source.getMetricValue(metric);
			if (m != MetricValue.NONE && Double.compare(MetricValue.getValue(m), 0.0) != 0) {
				this.accumulateMetricValue(metric, m);
			}
		}
	}

	/*************************************************************************
	 * Laks: accumulate a metric value (used to compute aggregate value)
	 * @param index
	 * @param value
	 ************************************************************************/
	private void accumulateMetricValue(BaseMetric metric, MetricValue value)
	{
		ensureMetricStorage();
		
		MetricValue m = metrics.getValue(this, metric);
		if (m == MetricValue.NONE) {
			metrics.setValue(metric.getIndex(), value);
		} else {
			// TODO Could do non-additive accumulations here?
			double newValue = m.getValue() + value.getValue();
			m.setValue(newValue);
		}
	}


	/***************************************************************************
	 * Reduce the value of this scope from another scope with the same metric.
	 * <pre>
	 * If scope A has metric values {a1, a2, ... an} and 
	 * scope B has metric values {b1, b2, ... bn} 
	 * then A.reduce(B, filter) equals to:
	 *  for all a of filter <- a reduce b
	 * </pre>
	 * where reduce is usually subtraction for most cases. 
	 * 
	 * @param scope the source of the reduction 
	 * @param filter the metric filter 
	 ***************************************************************************/
	public void reduce(Scope scope, MetricType type) {
		var experiment = root.getExperiment();
		var metrics    = experiment.getMetrics();
		
		for (var m: metrics) {
			if (!(m instanceof HierarchicalMetric))
				continue;
			
			if (m.getMetricType() != type)
				continue;
			
			HierarchicalMetric hm = (HierarchicalMetric) m;
			MetricValue mv = hm.reduce(getMetricValue(m), scope.getMetricValue(m));
			setMetricValue(m.getIndex(), mv);
		}
	}

	/***************************************************************************
	 * retrieve the default metrics
	 * @return
	 ***************************************************************************/
	public IMetricValueCollection getMetricValues() {
		// bug fix: we need to ensure that the metrics exist before giving
		// to the outside world
		ensureMetricStorage();
		return this.metrics;
	}

	/***************************************************************************
	 * set the default metrics
	 * @param values
	 ***************************************************************************/
	public void setMetricValues(IMetricValueCollection values) {	
		this.metrics = values;
	}



	/**************************************************************************
	 * combining metric from source. use this function to combine metric between
	 * 	different views
	 * @param source
	 * @param filter
	 **************************************************************************/
	public void combine(Scope source, MetricValuePropagationFilter filter) {

		var exp  = getExperiment();
		var list = exp.getMetrics();

		for (var metric: list) {

			if (metric instanceof AggregateMetric) {
				//--------------------------------------------------------------------
				// aggregate metric need special treatment when combining two metrics
				//--------------------------------------------------------------------
				AggregateMetric aggMetric = (AggregateMetric) metric;
				if (filter.doPropagation(source, this, metric.getIndex(), metric.getIndex())) {
					aggMetric.combine(source, this);
				}
			} else {
				this.accumulateMetric(source, metric, filter);
			}
		}
	}


	/**********************************************************************************
	 * Safely combining metrics from another scope. 
	 * This method checks if the number of metrics is the same as the number of metrics
	 * 	in the experiment. If not, it generates additional metrics
	 * this method is used for dynamic metrics creation such as when computing metrics
	 * 	in caller view (if a new metric is added)
	 * @param source
	 * @param filter
	 **********************************************************************************/
	public void safeCombine(Scope source, MetricValuePropagationFilter filter) {
		ensureMetricStorage();
		this.combine(source, filter);
	}

	/*************************************************************************
	 *	Makes sure that the scope object has storage for its metric values.
	 ************************************************************************/

	protected void ensureMetricStorage()
	{	
		if (metrics == null)
		{
			try {
				metrics = root.getMetricValueCollection();
			} catch (IOException e) {
				RuntimeException e2 = new RuntimeException(e.getMessage());
				e2.setStackTrace(e.getStackTrace());
				throw e2;
			}
		}
	}


	/*************************************************************************
	 * Copies defensively the metric array into a target scope.
	 * Used to implement duplicate() in subclasses of Scope  
	 ************************************************************************/

	public void copyMetrics(Scope targetScope, int offset) {
		targetScope.ensureMetricStorage();

		IMetricValueCollection targetMetrics = targetScope.getMetricValues();
		targetMetrics.appendMetrics(getMetricValues(), offset);
	}



	//////////////////////////////////////////////////////////////////////////
	//support for visitors													//
	//////////////////////////////////////////////////////////////////////////

	public void dfsVisitScopeTree(IScopeVisitor sv) {
		accept(sv, ScopeVisitType.PreVisit);
		int nKids = getSubscopeCount();
		for (int i=0; i< nKids; i++) {
			Scope childScope = getSubscope(i);
			if (childScope != null)
				childScope.dfsVisitScopeTree(sv);
		}
		accept(sv, ScopeVisitType.PostVisit);
	}

	public void accept(IScopeVisitor visitor, ScopeVisitType vt) {
		visitor.visit(this, vt);
	}

	/*******
	 * depth first search scope with checking whether we should go deeper or not
	 * 
	 * @param sv
	 */
	public void dfsVisitFilterScopeTree(FilterScopeVisitor sv) {
		accept(sv, ScopeVisitType.PreVisit);
		if (sv.needToContinue())
		{
			// during the process of filtering, it is possible the tree has been changed
			// and the some children may be removed. 
			// we will first retrieve the original list of children, and then investigate
			// one-by-one, even though the list of children has changed.

			var children = node.getChildren(); 
			if (children != null)
			{
				// copy the original children
				var copyChildren = new ArrayList<Scope>(children);			
				for(var child: copyChildren)
				{
					Scope scope = (Scope) child;
					scope.dfsVisitFilterScopeTree(sv);
				}
			}
		}
		accept(sv, ScopeVisitType.PostVisit);
	}

	/*
	 * (non-Javadoc)
	 * @see edu.rice.cs.hpc.data.experiment.scope.TreeNode#dispose()
	 */
	public void dispose()
	{
		root 		= null;
		metrics 	= null;
		sourceFile  = null;
	}


	/****
	 * Retrieve the list of all children.
	 * 
	 * 
	 * @return {@code List}
	 */
	public List<Scope> getChildren() {
		return node.getChildren();
	}

	public void remove(int index) {
		node.remove(index);
	}

	public void remove(Scope child) {
		node.remove(child);
	}

	public boolean hasChildren() {
		return node.hasChildren();
	}
}
