package edu.rice.cs.hpc.data.db.version3;

/***
 * struct trace record
 * 
 *
 */
public class DataRecord {
	public long timestamp;
	public int cpId;
	// Intentionally remove Nathan's metric ID. 
	// Nathan: could you please derive this data record if you want to add additional field ?
	//public int metricId;

	public DataRecord(long _timestamp, int _cpId, int _metricId) {
		this.timestamp = _timestamp;
		this.cpId = _cpId;
		//this.metricId = _metricId;
	}
	@Override
	public String toString() {
		return String.format("Time: %d, Call Path: %d", timestamp, cpId);
	}

}
