package edu.rice.cs.hpcdata.experiment.scope;

import edu.rice.cs.hpcdata.experiment.source.SourceFile;


public class EntryScope extends ProcedureScope 
{
	private final short entryPoint;
	
	public EntryScope(RootScope root, String proc, int cct_id, short entryPoint) {
		super(root, LoadModuleScope.NONE, SourceFile.NONE, 0, 0, proc, false, cct_id, 0, null, ProcedureScope.FeaturePlaceHolder);
		this.entryPoint = entryPoint;
	}

	public short getEntryPoint() {
		return entryPoint;
	}
}
