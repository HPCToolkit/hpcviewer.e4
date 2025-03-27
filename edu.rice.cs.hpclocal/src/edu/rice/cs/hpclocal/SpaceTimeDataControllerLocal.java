// SPDX-FileCopyrightText: Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: Apache-2.0

package edu.rice.cs.hpclocal;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import edu.rice.cs.hpcbase.IProcessTimeline;
import edu.rice.cs.hpcbase.ITraceDataCollector;
import edu.rice.cs.hpcbase.ITraceDataCollector.TraceOption;
import org.hpctoolkit.db.local.db.IFileDB;
import org.hpctoolkit.db.local.db.IdTuple;
import org.hpctoolkit.db.local.experiment.IExperiment;
import edu.rice.cs.hpctraceviewer.config.TracePreferenceManager;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;
import edu.rice.cs.hpctraceviewer.data.timeline.ProcessTimeline;


/**
 * The local disk version of the Data controller
 * 
 * @author Philip Taffet
 * 
 * @author Refined, updated, refactored by other developers
 * 
 */
public class SpaceTimeDataControllerLocal extends SpaceTimeDataController 
{	
	private AtomicInteger currentLine;
	private boolean changedBounds;
	
	/***
	 * Constructor to setup local database
	 * 
	 * @param experiment 
	 * 			IExperiment
	 * @param fileDB 
	 * 			IFileDB
	 * 
	 * @throws IOException
	 */
	public SpaceTimeDataControllerLocal(
			IExperiment experiment, 
			IFileDB fileDB)
					throws IOException {
		super(experiment);

		final var exp = getExperiment();
		var location = Path.of(exp.getDirectory()).toFile();
		String traceFilePath = location.getAbsolutePath();
		
		fileDB.open(traceFilePath);
		
		var dataTrace  = new FilteredBaseData(fileDB);
		super.setBaseData(dataTrace);
	}


	@Override
	public void startTrace(int numTraces, boolean changedBounds) {
		this.changedBounds = changedBounds;				
		currentLine = new AtomicInteger(numTraces);
	}


	@Override
	public IProcessTimeline getNextTrace() throws Exception {
		var line = currentLine.decrementAndGet();
		if (line < 0)
			return null;

		if (changedBounds) {			
			var profile = super.getProfileFromPixel(line);
			return new ProcessTimeline(line, this, profile);
		}
		return getProcessTimelineService().getProcessTimeline(line);
	}

	
	@Override
	public void closeDB() {
		// nothing
	}


	@Override
	public String getName() {
		return getExperiment().getDirectory();
	}
	

	@Override
	public ITraceDataCollector getTraceDataCollector(int lineNum, IdTuple idtuple) {
		var idtupleType = getExperiment().getIdTupleType();
		boolean isGpuTrace = idtuple.isGPU(idtupleType);

		TraceOption traceOption = TraceOption.ORIGINAL_TRACE;

		if (TracePreferenceManager.getGPUTraceExposure() && isGpuTrace)
			traceOption = TraceOption.REVEAL_GPU_TRACE;
		
		return new LocalTraceDataCollector(getBaseData(), idtuple, getPixelHorizontal(), traceOption);
	}
}
