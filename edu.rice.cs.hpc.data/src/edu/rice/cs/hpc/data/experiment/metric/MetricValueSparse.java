package edu.rice.cs.hpc.data.experiment.metric;

public class MetricValueSparse 
{
	private int   index;
	private float value;
	
	public MetricValueSparse() {
		index = 0;
		value = 0.0f;
	}
	
	public MetricValueSparse(int index, float value) {
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
	public void setIndex(int index) {
		this.index = index;
	}
	
	/**
	 * @return the value
	 */
	public float getValue() {
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(float value) {
		this.value = value;
	}

	
}
