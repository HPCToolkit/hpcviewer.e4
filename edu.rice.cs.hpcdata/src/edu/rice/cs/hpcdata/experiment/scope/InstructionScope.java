package edu.rice.cs.hpcdata.experiment.scope;

import java.io.File;

public class InstructionScope extends LineScope 
{
	private final LoadModuleScope loadModule;
	private final long offset;

	public InstructionScope(RootScope root, LoadModuleScope loadModule, long offset, int scopeID, int flatID) {
		super(root, loadModule.getSourceFile(), 0, scopeID, flatID);
		this.loadModule = loadModule;
		this.offset = offset;
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
		String name = loadModule.getName();
		int index = name.lastIndexOf(File.separatorChar);
		return String.format("%s@0x%x",name.substring(index+1), offset);
	}
}
