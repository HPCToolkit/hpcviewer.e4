package edu.rice.cs.hpc.data.util;

public interface IProgressReport {
	public void begin(String title, int num_tasks);
	public void advance();
	public void end();
}
