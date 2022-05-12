package edu.rice.cs.hpcdata.experiment.scope;

import edu.rice.cs.hpcdata.experiment.source.SourceFile;
import edu.rice.cs.hpcdata.util.IUserData;

public class ProcedureCallScope extends ProcedureScope {

	public ProcedureCallScope(RootScope root, LoadModuleScope loadModule, SourceFile file, int lineNumber,
			String proc, boolean isalien, int cctId, int flatId, IUserData<String, String> userData,
			int procedureFeature) {
		super(root, loadModule, file, lineNumber, lineNumber, proc, isalien, cctId, flatId, userData, procedureFeature);
	}

	
	@Override
	public Scope duplicate() {
		return new ProcedureCallScope(
				root, 
				objLoadModule, 
				sourceFile, 
				firstLineNumber, 
				procedureName, 
				isalien, 
				getCCTIndex(), 
				getFlatIndex(), 
				null, 
				FeatureProcedure);
	}
}
