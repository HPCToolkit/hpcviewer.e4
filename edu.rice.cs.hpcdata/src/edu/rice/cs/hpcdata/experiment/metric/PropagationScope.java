package edu.rice.cs.hpcdata.experiment.metric;


/*********
 * The fields of the {PS} structure
describe the function mapping from each context to the subset of its
decendants included in the sum. While presentable metric values can be
produced based only on the *pScopeName and METRICS.yaml, more complex
analysis can be performed if the propagation scope is defined in the meta.db.
 * <p>
 * The propagation scope's name *pScopeName may be any string, however to aid
writing and maintaining metric taxonomies (see METRICS.yaml) the propagation
scope for a name rarely changes meanings. Regardless, readers are still
strongly encouraged to use the definition provided by other fields described
below to perform analysis requiring any non-trivial understanding of the
propagation scope.
</p>
The propagation scope type is an enumeration with the following values:
<ul>
 <li>0: Custom propagation scope, not defined in the meta.db.
 <li>1: Standard "point" propagation scope. No propagation occurs, all metric
values are recorded as measured.
The canonical *pScopeName for this case is "point".

 <li>2: Standard "execution" propagation scope. Propagation always occurs, the
propagated value sums values measured from all descendants without exception.

The canonical *pScopeName for this case is "execution". This case is
often used for inclusive metric costs.

<li> 3: Transitive propagation scope. Propagation occurs from every context to
its parent when the propagationIndexth bit is set in the
context's propagation bitmask, and to further ancestors transitively
under the same condition.
</ul>
<p>
An example transitive propagation scope is named "function," its propagated
values sum measurements from all descendants not separated by a call, in
other words its the metric cost exclusive to the function. The appropriate
bit in the propagation bitmask is set only if the context's relation
is a caller-callee relationship (of some kind).
</p>
 *
 */
public class PropagationScope 
{	
	public static final String SUFFIX_EXCLUSIVE = "(E)";
	public static final String SUFFIX_INCLUSIVE = "(I)";
	public static final String SUFFIX_POINT_EXC = "(X)";

	public static final byte TYPE_CUSTOM     = 0;
	public static final byte TYPE_POINT      = 1;
	public static final byte TYPE_EXECUTION  = 2;
	public static final byte TYPE_TRANSITIVE = 3;
	
	/** Name of the propagation scope 
	 **/
	private final String scopeName;

	/** Type of propagation scope described 
	 **/
	private final byte   type;
	
	/** Index of this propagation's propagation bit
	 **/
	private final byte   propagationIndex;
	
	public PropagationScope(String newScopeName, byte newType, byte newIndex) {
		this.scopeName = newScopeName;
		this.type = newType;
		this.propagationIndex = newIndex;
	}

	/**
	 * @return the scopeName
	 */
	public String getScopeName() {
		return scopeName;
	}

	/**
	 * Retrieve the propagation type.
	 * 
	 * @see TYPE_CUSTOM
	 * @see TYPE_POINT
	 * @see TYPE_EXECUTION
	 * @see TYPE_TRANSITIVE
	 * 
	 * @return {@code byte}
	 * 			the type
	 */
	public byte getType() {
		return type;
	}

	/****
	 * Return the propagation index
	 * 
	 * @return
	 */
	public byte getPropagationIndex() {
		return propagationIndex;
	}

	/****
	 * Return the metric type of this propagation index
	 * 
	 * @see MetricType
	 * 
	 * @return {@code MetricType}
	 */
	public MetricType getMetricType() {
		var metricType = MetricType.convertFromPropagationScope(type);
		
		// check if we can resolve the metric type from the type byte.
		// If not, we try to resolve based on the name
		if (metricType == MetricType.UNKNOWN)
			metricType = MetricType.convertFromPropagationScope(scopeName);
		
		return metricType;
	}
	
	public String getMetricTypeSuffix() {
		if (getMetricType() == MetricType.EXCLUSIVE || 
			getMetricType() == MetricType.LEXICAL_AWARE)
			return(PropagationScope.SUFFIX_EXCLUSIVE);
		
		if (getMetricType() == MetricType.INCLUSIVE)
			return(PropagationScope.SUFFIX_INCLUSIVE);
		
		if (getMetricType() == MetricType.POINT_EXCL)
			return(PropagationScope.SUFFIX_POINT_EXC);

		return "";
	}
	
	
	@Override
	public String toString() {
		return propagationIndex + ". " + scopeName ;
	}
}
