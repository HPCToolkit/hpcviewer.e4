package edu.rice.cs.hpcdata.experiment.scope;

import java.io.File;

public class InstructionScope extends LineScope 
{
	private static final String LINE_ZERO = ":0";
	
	private final LoadModuleScope loadModule;

	public InstructionScope(RootScope root, LoadModuleScope loadModule, int scopeID) {
		super(root, loadModule.getSourceFile(), 0, scopeID, scopeID);
		this.loadModule = loadModule;
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
		return new InstructionScope(root, loadModule, id);
	}

	@Override
	public String getName() {
		String name = loadModule.getName();
		int index = name.lastIndexOf(File.separatorChar);
		return name.substring(index+1) + LINE_ZERO;
	}

}
