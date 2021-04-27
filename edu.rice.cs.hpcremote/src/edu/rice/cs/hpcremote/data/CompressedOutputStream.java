package edu.rice.cs.hpcremote.data;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

/**
 * A wrapper around DataOutputStream and GZIPOutputStream. It's main purpose is
 * to ensure that the GZIPOutputStream's finish is called during the flushing of
 * data so that data is actually sent.
 * 
 * @author Philip Taffet
 * 
 */
public class CompressedOutputStream extends DataOutputStream {

	GZIPOutputStream outStr;

	public CompressedOutputStream(GZIPOutputStream _outStr) {
		super(_outStr);
		outStr = _outStr;
	}

	@Override
	public void flush() throws IOException {
		
		outStr.finish();
		super.flush();
		System.out.println("Comp OS Flushed");
	}

}
