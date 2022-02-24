package edu.rice.cs.hpcdata.db.version4;

import java.io.IOException;
import java.nio.channels.FileChannel;

public class DataMeta extends DataCommon 
{
	// --------------------------------------------------------------------
	// constants
	// --------------------------------------------------------------------
	private final static String HEADER_MAGIC_STR  = "HPCTOOLKITmeta";


	@Override
	protected boolean isFileHeaderCorrect(String header) {
		return HEADER_MAGIC_STR.equals(header);
	}

	@Override
	protected boolean readNextHeader(FileChannel input, DataSection[] sections) throws IOException {
		return false;
	}

	@Override
	protected int getNumSections() {
		return 8;
	}

}
