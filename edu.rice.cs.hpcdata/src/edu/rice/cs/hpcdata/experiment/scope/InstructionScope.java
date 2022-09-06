package edu.rice.cs.hpcdata.experiment.scope;

import java.io.File;

public class InstructionScope extends Scope 
{
	private final LoadModuleScope loadModule;
	private final long offset;
	private final ProcedureScope procScope;

	public InstructionScope(RootScope root, LoadModuleScope loadModule, long offset, int scopeID, int flatID) {
		super(root, loadModule.getSourceFile(), 0, 0, scopeID, flatID);
		this.loadModule = loadModule;
		this.offset = offset;
		procScope   = new ProcedureScope(root, 
										 loadModule, 
										 loadModule.getSourceFile(), 
										 0, 
										 0, 
										 InstructionScope.getCanonicalName(loadModule, offset), 
										 false, 
										 scopeID, 
										 flatID, 
										 null, 
										 ProcedureScope.FEATURE_PROCEDURE);
	}

	
	/***
	 * Retrieve the procedure object of this instruction.
	 * 
	 * @return {@code ProcedureScope}
	 */
	public ProcedureScope getProcedure() {
		return procScope;
	}

	/****
	 * Get the load module of this instruction.
	 * 
	 * @return {@code LoadModuleScope}
	 */
	public LoadModuleScope getLoadModule() {
		return loadModule;
	}

	@Override
	public Scope duplicate() {
		return new InstructionScope(root, loadModule, offset, id, getFlatIndex());
	}

	@Override
	public String getName() {
		return getCanonicalName(loadModule, offset);
	}
	
	private static String getCanonicalName(LoadModuleScope lms, long offset ) {
		String name = lms.getName();
		int index = name.lastIndexOf(File.separatorChar);
		return String.format("%s@0x%x",name.substring(index+1), offset);
	}
}
