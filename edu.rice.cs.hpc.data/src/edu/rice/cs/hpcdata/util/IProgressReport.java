package edu.rice.cs.hpcdata.util;

public interface IProgressReport {
	public void begin(String title, int num_tasks);
	public void advance();
	public void end();
}
