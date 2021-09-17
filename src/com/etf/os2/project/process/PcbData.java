package com.etf.os2.project.process;

public class PcbData {
	
	//za SJF
	public double tau;
	public boolean isPreempted;
	//za SJF i MFQS
	public long putTime;
	//za MFQS
	public int previousPriority;
	public long executionStartTime;
	//za CFS
	public long totalExecutionTime;
	public long totalWaitingTime;
}
