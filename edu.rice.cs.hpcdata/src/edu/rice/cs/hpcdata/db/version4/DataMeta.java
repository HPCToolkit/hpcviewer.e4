package edu.rice.cs.hpcdata.db.version4;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.List;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.HierarchicalMetric;
import edu.rice.cs.hpcdata.experiment.metric.MetricType;


/*********************************************
 * 
 * Class to manage the meta.db file.
 * Once instantiated, it requires to call consecutively:
 * <ul>
 *   <li>open the file
 *   <li>call the finalization
 * </ul>
 * Without finalization, the metric descriptors are not set correctly.
 * Example:
 * <pre>
 *   DataMeta dm = new DataMeta()
 *   dm.open(filename);
 *   dm.finalize(profileDB); // have to do this!
 * </pre>
 *
 *********************************************/
public class DataMeta extends DataCommon 
{
	// --------------------------------------------------------------------
	// constants
	// --------------------------------------------------------------------
	private final static String HEADER_MAGIC_STR   = "HPCTOOLKITmeta";
	private final static String METRIC_SCOPE_POINT = "point";
	private static final String METRIC_SCOPE_EXECUTION    = "execution";
		
	private static final int INDEX_GENERAL = 0;
	private static final int INDEX_NAMES   = 1;
	private static final int INDEX_METRICS = 2;
	
	public String title;
	public String description;

	public String []kindNames;

	private List<BaseMetric> metrics;

	public DataMeta() {
		super();
	}

	@Override
	protected boolean isFileHeaderCorrect(String header) {
		return HEADER_MAGIC_STR.equals(header);
	}

	@Override
	protected boolean readNextHeader(FileChannel input, DataSection[] sections) throws IOException {
		parseHeaderMetaData(input, sections);
		return true;
	}

	@Override
	protected int getNumSections() {
		return 8;
	}

	
	/****
	 * Retrieve the main title of the database.
	 * Usually it's the name of the executable.
	 * 
	 * @return String
	 */
	public String getTitle() {
		return title;
	}
	
	
	/***
	 * Retrieve the database description
	 * @return String
	 */
	public String getDescription() {
		return description;
	}
	
	
	/****
	 * Get the list of the name of metric kinds
	 * @return 
	 * 		array of name of metrics
	 */
	public String [] getKindNames() {
		return kindNames;
	}
	
	
	/****
	 * Get the list of metrics
	 * @return a list of metrics
	 */
	public List<BaseMetric> getMetrics() {
		return metrics;
	}
	
	
	/******
	 * Mandatory call once the opening is successful.
	 * @param profileDB
	 * 			The summary profile database
	 */
	public void finalize(DataSummary profileDB) {
		// set the profile db to each metric
		metrics.forEach((m)-> 
			{if (m instanceof HierarchicalMetric) 
				((HierarchicalMetric)m).setProfileDatabase(profileDB);
			});
	}
	
	
	
	private void parseHeaderMetaData(FileChannel channel, DataSection []sections) throws IOException {
		// --------------------------------------
		// grab general description of the database
		// --------------------------------------
		parseGeneralDescription(channel, sections[INDEX_GENERAL]);
		
		// --------------------------------------
		// grab the id-tuple type names
		// --------------------------------------
		parseHierarchicalIdTuple(channel, sections[INDEX_NAMES]);
		
		// --------------------------------------
		// grab the description of the metrics
		// --------------------------------------
		parseMetricDescription(channel, sections[INDEX_METRICS]);
	}
	
	private void parseGeneralDescription(FileChannel channel, DataSection section) throws IOException {
		var buffer = channel.map(MapMode.READ_ONLY, section.offset, section.size);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		var pTitle = buffer.getLong();
		var pDescription = buffer.getLong();
		
		int position = (int) (pTitle-section.offset);
		title = getNullTerminatedString(buffer, position);
		
		position = (int) (pDescription - section.offset);
		description = getNullTerminatedString(buffer, position);

	}
	
	
	private void parseHierarchicalIdTuple(FileChannel channel, DataSection section) throws IOException {
		var buffer = channel.map(MapMode.READ_ONLY, section.offset, section.size);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		var pNames = buffer.getLong();
		var kinds  = buffer.get();
		
		assert(pNames > section.offset && 
			   pNames < section.offset + section.size);
		
		kindNames = new String[kinds];
		int basePosition = (int) (pNames - section.offset);
		for(int i=0; i<kinds; i++) {
			buffer.position(basePosition + i*8);
			long pKind = buffer.getLong();
			
			int position   = (int) (pKind - section.offset);
			kindNames[i] = getNullTerminatedString(buffer, position);
		}
	}
	
	
	private void parseMetricDescription(FileChannel channel, DataSection section) throws IOException {
		var buffer = channel.map(MapMode.READ_ONLY, section.offset, section.size);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		var pMetrics = buffer.getLong();
		var nMetrics = buffer.getInt();
		var szMetric = buffer.get(0x0c);
		var szScope  = buffer.get(0x0d);
		var szSummary = buffer.get(0x0e);
		
		metrics = new ArrayList<>(nMetrics);
		int position = (int) (pMetrics - section.offset);
		
		for(int i=0; i<nMetrics; i++) {
			int metricLocation = position + (i * szMetric);

			var pName   = buffer.getLong (metricLocation);
			var nScopes = buffer.getShort(metricLocation + 0x08);
			var pScopes = buffer.getLong (metricLocation + 0x10);	
			
			position = (int) (pName - section.offset);
			String metricName = getNullTerminatedString(buffer, position);
			
			position = (int) (pScopes - section.offset);				
			for(int j=0; j<nScopes; j++) {
				int basePosition   = position + (j*szScope);
				long  pScope       = buffer.getLong (basePosition);
				short nSummaries   = buffer.getShort(basePosition + 0x08);
				short propMetricId = buffer.getShort(basePosition + 0x0a);
				long  pSummaries   = buffer.getLong (basePosition + 0x10);
				
				int scopePosition  = (int) (pScope - section.offset);
				String scopeName = getNullTerminatedString(buffer, scopePosition);
				
				int baseSummariesLocation = (int) (pSummaries - section.offset);
						
				for(short k=0; k<nSummaries; k++) {
					int summaryLoc = baseSummariesLocation + i * szSummary;
					long pFormula = buffer.getLong(summaryLoc);
					byte combine  = buffer.get(summaryLoc + 0x08);
					short statMetric = buffer.getShort(summaryLoc + 0x0a);
											
					var strFormula = getNullTerminatedString(buffer, (int) (pFormula-section.offset));
					
					if (!scopeName.equals(METRIC_SCOPE_POINT)) {
						var m = new HierarchicalMetric(statMetric, metricName);
						m.setFormula(strFormula);
						MetricType type = scopeName.equals(METRIC_SCOPE_EXECUTION) ? 
															MetricType.INCLUSIVE : 
															MetricType.EXCLUSIVE;
						m.setMetricType(type);
						m.setCombineType(combine);
						metrics.add(m);
					}
				}
			}
		}
	}
	
	
	private void parseLoadModules(FileChannel channel, DataSection section) {
		
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
