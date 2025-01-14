// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpcremote.test;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.hpctoolkit.client_server_common.calling_context.CallingContextId;
import org.hpctoolkit.client_server_common.time.Timestamp;
import org.hpctoolkit.client_server_common.trace.TraceId;
import org.hpctoolkit.hpcclient.v1_0.BrokerClient;
import org.hpctoolkit.hpcclient.v1_0.CallingContextAtTimes;
import org.hpctoolkit.hpcclient.v1_0.FutureTraceSamplingSet;
import org.hpctoolkit.hpcclient.v1_0.TraceSampling;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import edu.rice.cs.hpcdata.db.IFileDB.IdTupleOption;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcremote.trace.RemoteProcessTimeline;
import edu.rice.cs.hpcremote.trace.RemoteSpaceTimeDataController;
import edu.rice.cs.hpctest.util.TestDatabase;
import edu.rice.cs.hpctraceviewer.data.Frame;
import io.vavr.collection.HashSet;
import io.vavr.collection.Set;


public class RemoteSpaceTimeDataControllerTest 
{
	private static List<Experiment> experiments;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		experiments = TestDatabase.getExperiments();		
	}


	@Test
	public void testGetTraceDataCollector() throws Exception {
		for(var exp: experiments) {
			var client = Mockito.mock(BrokerClient.class);
			var timeMin = exp.getTraceAttribute().dbTimeMin;			
			
			var timestampMin = createTimeStampOptionalFrom(timeMin);			
			when(client.getMinimumTraceSampleTimestamp()).thenReturn(timestampMin);
			
			var timestampMax = createTimeStampOptionalFrom(exp.getTraceAttribute().dbTimeMax);
			when(client.getMaximumTraceSampleTimestamp()).thenReturn(timestampMax);

			ExecutorService executor = Executors.newSingleThreadExecutor();
			
			var maxNodeId = exp.getMaxCCTID();
			assertTrue(maxNodeId >= 0);

			Set<Future<TraceSampling>> setOfTraces = HashSet.of(executor.submit(() -> {
				List<CallingContextAtTimes> list = new ArrayList<>(20);
				
				for(int i=0; i<20; i++) {
					Timestamp time = Timestamp.ofEpochNano(getTime(timeMin, i));
					int nodeId = Math.min(i, maxNodeId);
					
					CallingContextAtTimes cctAtTimes = CallingContextAtTimes.make(CallingContextId.make(nodeId), time, time);
					list.add(cctAtTimes);
				}
				io.vavr.collection.List<CallingContextAtTimes> setCCT = io.vavr.collection.List.ofAll(list);
				return TraceSampling.make(TraceId.make(1), setCCT, false);
			}));
			FutureTraceSamplingSet set = FutureTraceSamplingSet.make(setOfTraces);
			when(client.sampleTracesAsync(any(Set.class), any(Timestamp.class), any(Timestamp.class), anyInt())).thenReturn(set);

			RemoteSpaceTimeDataController rstdc = new RemoteSpaceTimeDataController(client, exp);
			
			var baseData = rstdc.getBaseData();
			assertNotNull(baseData);
			
			var colorTable = rstdc.getColorTable();
			assertNotNull(colorTable);
			
			var map = rstdc.getScopeMap();
			assertNotNull(map);
			
			var name = rstdc.getName();
			assertTrue(name.length() > 7);
			
			var attributes = rstdc.getTraceDisplayAttribute();
			assertNotNull(attributes);

			var listIdt = baseData.getDenseListIdTuple(IdTupleOption.BRIEF);

			var frame = new Frame();
			frame.begProcess = 0;
			frame.endProcess = listIdt.size()-1;
			frame.begTime = rstdc.getMinBegTime();
			frame.endTime = rstdc.getMaxEndTime();
			attributes.setFrame(frame);
			
			attributes.setPixelHorizontal(400);
			attributes.setPixelVertical(100);
			
			int numTraces = Math.min(listIdt.size(), attributes.getPixelVertical());

			rstdc.startTrace(numTraces, true);
			
			var idtType = exp.getIdTupleType();
			assertNotNull(idtType);
			
			RemoteProcessTimeline ptl = (RemoteProcessTimeline) rstdc.getNextTrace();
			
			while(ptl != null) {
									
				ptl.readInData();

				var profileIdt =  ptl.getProfileIdTuple();
				assertNotNull(profileIdt);
				
				var size = ptl.size();
				for(int i=0; i<size; i++) {
					var nodeId = ptl.getContextId(i);
					assertTrue(nodeId >= 0 && nodeId <= maxNodeId);
					
					var time = ptl.getTime(i);
					assertEquals(getTime(timeMin, i), time);
				}
				
				ptl = (RemoteProcessTimeline) rstdc.getNextTrace();
				
			}
			
			rstdc.closeDB();
		}
	}


	private Optional<Timestamp> createTimeStampOptionalFrom(long time) {
		Timestamp timestamp = () -> time;
		return Optional.of(timestamp);
	}

	private long getTime(long minTime, int step) {
		final long TIMESTEP = 10000;
		
		return minTime + step * TIMESTEP;
	}
}
