package edu.rice.cs.hpcdata.experiment.metric;

/* Java 1.5 public enum MetricType { EXCLUSIVE, INCLUSIVE, EXCLUSIVE_ONLY, DERIVED }*/

// Java 1.4 Compatible enumeration type
public class MetricType 
{
	public static final MetricType UNKNOWN        = new MetricType("UNKNOWN");
	public static final MetricType EXCLUSIVE      = new MetricType("EXCLUSIVE");
	public static final MetricType INCLUSIVE      = new MetricType("INCLUSIVE");
	public static final MetricType POINT_EXCL     = new MetricType("XCLUSIVE");
	//public final static MetricType PREAGGREGATE   = new MetricType("PREAGGREGATE");
	//public final static MetricType DERIVED_INCR   = new MetricType("DERIVED_INCR");
	
	public String toString() { return value; }
	
	public static MetricType convertFromPropagationScope(String scopePropagationName) {
		if (scopePropagationName.equalsIgnoreCase("execution")) 
			return MetricType.INCLUSIVE;
		else if (scopePropagationName.equalsIgnoreCase("function")) 
			return MetricType.EXCLUSIVE;
		else if (scopePropagationName.equalsIgnoreCase("point")) 
			return MetricType.POINT_EXCL;
		else
			return MetricType.UNKNOWN;		
	}
	
	
	public static MetricType convertFromPropagationScope(int scopePropagationType) {
		switch(scopePropagationType) {
		case 1:
			return MetricType.POINT_EXCL;
		case 2:
			return MetricType.INCLUSIVE;
		case 3:
			return MetricType.EXCLUSIVE;
		default:
			return MetricType.UNKNOWN;
		}
	}
	
	public static MetricType convertFromName(String formulaType) {
		if (formulaType.equalsIgnoreCase("inclusive")) {
			return MetricType.INCLUSIVE;
		} else if (formulaType.equalsIgnoreCase("exclusive")) {
			return MetricType.EXCLUSIVE;
		}
		throw new IllegalArgumentException("unknown formula type: " + formulaType);
	}
	
	private String value;
	private MetricType(String value) { this.value = value; };
}