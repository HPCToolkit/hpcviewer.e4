package edu.rice.cs.hpcdata.db.version4;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import edu.rice.cs.hpcdata.experiment.metric.AggregateMetric;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;

public class DataMeta extends DataCommon 
{
	// --------------------------------------------------------------------
	// constants
	// --------------------------------------------------------------------
	private final static String HEADER_MAGIC_STR  = "HPCTOOLKITmeta";

	private HeaderMetaData header;

	@Override
	protected boolean isFileHeaderCorrect(String header) {
		return HEADER_MAGIC_STR.equals(header);
	}

	@Override
	protected boolean readNextHeader(FileChannel input, DataSection[] sections) throws IOException {
		header = new HeaderMetaData(input, sections);
		return true;
	}

	@Override
	protected int getNumSections() {
		return 8;
	}

	
	public String getTitle() {
		return header.title;
	}
	
	public String getDescription() {
		return header.description;
	}
	
	
	public String [] getKindNames() {
		return header.kindNames;
	}
	
	
	public List<BaseMetric> getMetrics() {
		return header.metrics;
	}
	
	private static class HeaderMetaData
	{
		private static final int INDEX_GENERAL = 0;
		private static final int INDEX_NAMES   = 1;
		private static final int INDEX_METRICS = 2;
		
		public final String title;
		public final String description;

		public final String []kindNames;

		List<BaseMetric> metrics;
		
		public HeaderMetaData(FileChannel channel, DataSection []sections) throws IOException {
			//
			// grab general description of the database
			//
			var buffer = channel.map(MapMode.READ_ONLY, sections[INDEX_GENERAL].offset, sections[0].size);
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			var pTitle = buffer.getLong();
			var pDescription = buffer.getLong();
			
			int position = (int) (pTitle-sections[INDEX_GENERAL].offset);
			title = getNullTerminatedString(buffer, position);
			
			position = (int) (pDescription - sections[INDEX_GENERAL].offset);
			description = getNullTerminatedString(buffer, position);
			
			// --------------------------------------
			// grab the id-tuple type names
			// --------------------------------------
			buffer = channel.map(MapMode.READ_ONLY, sections[INDEX_NAMES].offset, sections[INDEX_NAMES].size);
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			var pNames = buffer.getLong();
			var kinds  = buffer.get();
			
			assert(pNames > sections[INDEX_NAMES].offset && 
				   pNames < sections[INDEX_NAMES].offset + sections[INDEX_NAMES].size);
			
			kindNames = new String[kinds];
			int basePosition = (int) (pNames - sections[INDEX_NAMES].offset);
			for(int i=0; i<kinds; i++) {
				buffer.position(basePosition + i*8);
				long pKind = buffer.getLong();
				
				position   = (int) (pKind - sections[INDEX_NAMES].offset);
				kindNames[i] = getNullTerminatedString(buffer, position);
			}
			
			// --------------------------------------
			// grab the description of the metrics
			// --------------------------------------
			buffer = channel.map(MapMode.READ_ONLY, sections[INDEX_METRICS].offset, sections[INDEX_METRICS].size);
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			var pMetrics = buffer.getLong();
			var nMetrics = buffer.getInt();
			var szMetric = buffer.get(0x0c);
			var szScope  = buffer.get(0x0d);
			var szSummary = buffer.get(0x0e);
			
			metrics = new ArrayList<>(nMetrics);
			position = (int) (pMetrics - sections[INDEX_METRICS].offset);
			
			for(int i=0; i<nMetrics; i++) {
				int metricLocation = position + (i * szMetric);

				var pName   = buffer.getLong (metricLocation);
				var nScopes = buffer.getShort(metricLocation + 0x08);
				var pScopes = buffer.getLong (metricLocation + 0x10);	
				
				position = (int) (pName - sections[INDEX_METRICS].offset);
				String metricName = getNullTerminatedString(buffer, position);
				//AggregateMetric m = new AggregateMetric(sID, metricName, sdescription, null, format, null, i, 0, null);
				
				position = (int) (pScopes - sections[INDEX_METRICS].offset);				
				for(int j=0; j<nScopes; j++) {
					int basePostition  = position + (j*szScope);
					long  pScope       = buffer.getLong (basePostition);
					short nSummaries   = buffer.getShort(basePostition + 0x08);
					short propMetricId = buffer.getShort(basePostition + 0x0a);
					long  pSummaries   = buffer.getLong (basePostition + 0x10);
					
					int scopePosition  = (int) (pScope - sections[INDEX_METRICS].offset);
					String scopeName = getNullTerminatedString(buffer, scopePosition);
					
					int baseSummariesLocation = (int) (pSummaries - sections[INDEX_METRICS].offset);
							
					for(short k=0; k<nSummaries; k++) {
						int summaryLoc = baseSummariesLocation + i * szSummary;
						long pFormula = buffer.getLong(summaryLoc);
						byte combine  = buffer.get(summaryLoc + 0x08);
						short statMetric = buffer.getShort(summaryLoc + 0x0a);
												
						var strFormula = getNullTerminatedString(buffer, (int) (pFormula-sections[INDEX_METRICS].offset));
					}
				}
			}
		}
		
		
		private String getNullTerminatedString(ByteBuffer buffer, int startPosition) throws IOException {
			StringBuilder sb = new StringBuilder();

			for(int i=0; i<buffer.capacity(); i++) {
				byte b = buffer.get(startPosition + i);
				if (b == 0)
					break;
				sb.append((char)b);
			}
			return sb.toString();
		}
	}
}
