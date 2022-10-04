package edu.rice.cs.hpcdata.experiment.scope;

import edu.rice.cs.hpcdata.experiment.source.SourceFile;


public class EntryScope extends ProcedureScope 
{
	private final short entryPoint;
	
	public EntryScope(RootScope root, String proc, int cctId, short entryPoint) {
		super(root, LoadModuleScope.NONE, SourceFile.NONE, 0, 0, proc, false, cctId, 0, null, ProcedureScope.FEATURE_TOPDOWN);
		this.entryPoint = entryPoint;
	}

	public short getEntryPoint() {
		return entryPoint;
	}
}
