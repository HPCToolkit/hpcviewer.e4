package edu.rice.cs.hpcdata.experiment.metric;

import java.io.IOException;
import java.util.List;

import edu.rice.cs.hpcdata.db.IdTuple;
import edu.rice.cs.hpcdata.experiment.extdata.IThreadDataCollection;
import edu.rice.cs.hpcdata.experiment.scope.IMetricScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;

/****************************************
 * Raw metric class\n
 * a.k.a thread-level metric
 ****************************************/
public class MetricRaw  extends BaseMetric 
{
	private int ID;			 // the index of this metric as specified in XML
	private String db_glob;  // old format: the glob pattern of the metric-db file
	private int db_id;		 // sequential index of the metric in the XML. Is has to be between 0 to the number of metrics
	private int num_metrics; // number of metrics
	
	
	/*** list of threads that its metric values have to be computed.<br/> 
	 *   each MetricRaw may have different threads. **/
	private List<IdTuple> threads = null;
	
	/*** similar to partner index, but this partner refers directly to the metric partner.**/
	private MetricRaw partner;
	
	
	/******
	 * creation of a new raw metric
	 * @param id
	 * @param title
	 * @param db_pattern
	 * @param db_num
	 * @param partner_index
	 * @param type
	 * @param metrics
	 */
	public MetricRaw(int id, String title, String description, String db_pattern, int db_num, int partner_index, 
			MetricType type, int metrics) {
		// raw metric has no partner
		// default annotation: percentage, although some metrics may have no percent due to missing value
		// on its root (such as exclusive metric)
		super( String.valueOf(id), title, description, VisibilityType.SHOW, null, AnnotationType.PERCENT, db_num, partner_index, type);
		this.ID 	 = id;
		this.db_glob = db_pattern;
		this.db_id 	 = db_num;
		this.num_metrics = metrics;
	}
	
	
	/**** 
	 * Create a metric raw based on from another metric
	 * @param metric
	 * @return
	 */
	public static MetricRaw create(BaseMetric metric) {
		int numMetrics = 1;
		if (metric instanceof MetricRaw) {
			numMetrics = ((MetricRaw)metric).num_metrics;
		}
		return new MetricRaw(metric.index, 
									 metric.getDisplayName(), 
									 metric.getDescription(), 
									 null, 
									 metric.index, 
									 metric.getPartner(), 
									 metric.getMetricType(), 
									 numMetrics);
	}
	
	
	public void setThread(List<IdTuple> threads)
	{
		this.threads = threads;
	}
	
	public List<IdTuple> getThread()
	{
		return threads;
	}
	
	/***
	 * return the glob pattern of files of this raw metric
	 * @return
	 */
	public String getGlob() {
		return this.db_glob;
	}
	
	
	/***
	 * retrieve the "local" ID of the raw metric
	 * This ID is unique among raw metrics in the same experiment 
	 * @return
	 */
	public int getRawID() {
		return this.db_id;
	}
	
	
	/***
	 * retrieve the number of raw metrics in this experiment
	 * @return
	 */
	public int getSize() {
		return this.num_metrics;
	}
	
	
	/***
	 * return the ID of the raw metric
	 * The ID is unique for all raw metric across experiments 
	 * @return
	 */
	public int getID() {
		return this.ID;
	}

	/**
	 * set the metric partner of this metric. If this metric is exclusive,
	 * the metric partner should be inclusive.
	 * 
	 * @param partner : the metric partner
	 */
	public void setMetricPartner(MetricRaw partner) {
		this.partner = partner;
	}

	/**
	 * get the metric partner
	 * @return metric partner
	 */
	public MetricRaw getMetricPartner() {
		return partner;
	}
	
	@Override
	public String getMetricTextValue(Scope scope)  {
		MetricValue mv = getValue(scope);
		boolean showPercent = getAnnotationType() == AnnotationType.PERCENT;
		MetricValue rootValue = showPercent ? getValue(scope.getRootScope()) : MetricValue.NONE;
		
		return super.getMetricTextValue(mv, rootValue);

	}
	
	@Override
	public MetricValue getValue(IMetricScope s) {
		if (s == null) return null;
		
		try {
			
			if (threads != null)
			{
				return getValue(s, threads);					
			}
		} catch (IOException e) {
			// something wrong
		}
		return MetricValue.NONE;
	}


	@Override
	public BaseMetric duplicate() {
		MetricRaw dup = new MetricRaw(ID, displayName, description, db_glob, db_id, 
				partnerIndex, metricType, num_metrics);
		dup.setOrder(order);
		
		return dup;
	}
	
	
	/****
	 * Basic method to retrieve the value of a scope for a given set of threads
	 * @param s : scope 
	 * @param threads : a list of threads
	 * @return a metric value
	 * @throws IOException
	 */
	private MetricValue getValue(IMetricScope s, List<IdTuple> threads) throws IOException  {
		MetricValue value = MetricValue.NONE;
		if (threads != null)
		{
			if (threads.size()>1)
			{
				// by default we return the sum of the the value of the threads
				// we should make this customizable in the future
				// users may like to see the average or the min or the max, ...
				
				value = getSumValue(s, threads);
			} else if (threads.size()==1)
			{
				value = getSpecificValue(s, threads.get(0));
			}
			if (value == MetricValue.NONE          && 
				s instanceof RootScope             && 
				metricType == MetricType.EXCLUSIVE &&
				partner != null) {
				
				value = partner.getValue(s, threads);
			}
		}
		return value;
	}
	
	public MetricValue getRawValue(IMetricScope s)
	{
		return getValue(s);
	}
	
	/***
	 * compute the sum of the value across the threads
	 * @param s the current scope
	 * @param threads list of threads
	 * @return
	 * @throws IOException
	 */
	private MetricValue getSumValue(IMetricScope s, List<IdTuple> threads) throws IOException
	{
		double valSum = 0.0;

		Scope scope    = (Scope) s;
		RootScope root = scope.getRootScope();
		
		IThreadDataCollection threadData = root.getExperiment().getThreadData();
		
		long nodeIndex = scope.getCCTIndex();
		
		for(var thread : threads)
		{
			valSum += threadData.getMetric(nodeIndex, ID, thread, num_metrics);
		}
		return createMetricValue(valSum);
	}
	

	/****
	 * get the metric value of a give scope on a given thread
	 * 
	 * @param s : the scope
	 * @param thread_id : the thread ID
	 * @return a metric value
	 * @throws IOException
	 */
	private MetricValue getSpecificValue(IMetricScope s, IdTuple idtuple) throws IOException
	{
		Scope scope = (Scope)s;

		// there is no API implementation for reading the whole CCT metrics
		RootScope root = scope.getRootScope();
		IThreadDataCollection threadData = root.getExperiment().getThreadData();

		if (threadData == null)
			// this shouldn't happen, unless hpcprof doesn't generate data properly
			return MetricValue.NONE;
		
		int cctIndex = scope.getCCTIndex();
		
		double value = threadData.getMetric(cctIndex, ID, idtuple, num_metrics);
		return createMetricValue(value);
	}
	
	
	/****
	 * Create a metric value object based on the real value.
	 * If the real value is around zero, it returns {@code MetricValue.NONE}.
	 * 
	 * @param value
	 * @return {@code MetricValue}
	 */
	private MetricValue createMetricValue(double value)
	{
		MetricValue mv = MetricValue.NONE;
		if (Double.compare(value, 0) != 0)
			mv = new MetricValue(value);
		return mv;
	}
}
