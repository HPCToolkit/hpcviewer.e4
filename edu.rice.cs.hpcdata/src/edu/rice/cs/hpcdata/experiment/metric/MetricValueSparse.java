package edu.rice.cs.hpcdata.experiment.metric;

public class MetricValueSparse implements Comparable<MetricValueSparse> 
{
	private short   index;
	private double value;
	
	public MetricValueSparse() {
		index = 0;
		value = 0.0f;
	}
	
	public MetricValueSparse(short index, double value) {
		this.index = index;
		this.value = value;
	}
	
	/**
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}
	
	/**
	 * @param index the index to set
	 */
	public void setIndex(short index) {
		this.index = index;
	}
	
	/**
	 * @return the value
	 */
	public double getValue() {
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(double value) {
		this.value = value;
	}

	public String toString() {
		return index + ": " + value;
	}

	@Override
	public int compareTo(MetricValueSparse o) {
		return (int) (index-o.index);
	}
}
