package edu.rice.cs.hpcdata.experiment.scope;

import edu.rice.cs.hpcdata.experiment.source.SourceFile;

public class UnknownScope extends Scope 
{	
	private static final String PREFIX = "<unknown context>";

	public UnknownScope(RootScope root, SourceFile file, int scopeID) {
		super(root, file, scopeID);
	}

	@Override
	public Scope duplicate() {
		return new UnknownScope(root, sourceFile, id);
	}

	@Override
	public String getName() {
		StringBuilder sb = new StringBuilder(PREFIX);
		sb.append(" ");
		if (sourceFile != null && sourceFile != SourceFile.NONE) {
			sb.append(sourceFile.getName());
			if (firstLineNumber > 0) {
				sb.append(":");
				sb.append(firstLineNumber);
			}
		}		
		return sb.toString();
	}
}
