package edu.rice.cs.hpcremote.filter;

public class TraceName {
	int process;
	int thread;

	public TraceName(int process, int thread) {
		this.process = process;
		this.thread  = thread;
	}

	@Override
	public String toString() {
		if (thread == -1) {
			return Integer.toString(process);
		} else {
			return process + "." + thread;
		}
	}
}
