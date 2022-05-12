package edu.rice.cs.hpcdata.experiment.metric;

/* Java 1.5 public enum MetricType { EXCLUSIVE, INCLUSIVE, EXCLUSIVE_ONLY, DERIVED }*/

// Java 1.4 Compatible enumeration type
public class MetricType {
	public static final MetricType UNKNOWN        = new MetricType("UNKNOWN");
	public static final MetricType EXCLUSIVE      = new MetricType("EXCLUSIVE");
	public static final MetricType INCLUSIVE      = new MetricType("INCLUSIVE");
	public static final MetricType POINT_EXCL     = new MetricType("XCLUSIVE");
	//public final static MetricType PREAGGREGATE   = new MetricType("PREAGGREGATE");
	//public final static MetricType DERIVED_INCR   = new MetricType("DERIVED_INCR");
	
	public String toString() { return value; }
	
	private String value;
	private MetricType(String value) { this.value = value; };
}