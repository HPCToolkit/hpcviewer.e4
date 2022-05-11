package edu.rice.cs.hpcdata.experiment.scope;

import edu.rice.cs.hpcdata.experiment.source.SourceFile;
import edu.rice.cs.hpcdata.util.IUserData;

public class ProcedureCallScope extends ProcedureScope {

	public ProcedureCallScope(RootScope root, LoadModuleScope loadModule, SourceFile file, int lineNumber,
			String proc, boolean _isalien, int cct_id, int flat_id, IUserData<String, String> userData,
			int procedureFeature) {
		super(root, loadModule, file, lineNumber, lineNumber, proc, _isalien, cct_id, flat_id, userData, procedureFeature);
	}

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
