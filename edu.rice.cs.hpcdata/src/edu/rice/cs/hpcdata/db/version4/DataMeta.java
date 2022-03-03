package edu.rice.cs.hpcdata.db.version4;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.collections.api.map.primitive.LongObjectMap;
import org.eclipse.collections.impl.map.mutable.primitive.LongObjectHashMap;

import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.HierarchicalMetric;
import edu.rice.cs.hpcdata.experiment.metric.MetricType;
import edu.rice.cs.hpcdata.experiment.scope.CallSiteScope;
import edu.rice.cs.hpcdata.experiment.scope.CallSiteScopeType;
import edu.rice.cs.hpcdata.experiment.scope.LineScope;
import edu.rice.cs.hpcdata.experiment.scope.LoadModuleScope;
import edu.rice.cs.hpcdata.experiment.scope.LoopScope;
import edu.rice.cs.hpcdata.experiment.scope.ProcedureScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeDynamic;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.experiment.source.SimpleSourceFile;
import edu.rice.cs.hpcdata.experiment.source.SourceFile;
import edu.rice.cs.hpcdata.util.Constants;


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
	private static final int INDEX_CONTEXT = 3;
	private static final int INDEX_STRINGS = 4;
	private static final int INDEX_MODULES = 5;
	private static final int INDEX_FILES   = 6;
	private static final int INDEX_FUNCTIONS = 7;
	
	private static final int FMT_METADB_RELATION_LexicalNest = 0;
	private static final int FMT_METADB_RELATION_Call = 1;
	private static final int FMT_METADB_RELATION_InlinedCall = 2;

	private static final int FMT_METADB_LEXTYPE_Function = 0;
	private static final int FMT_METADB_LEXTYPE_Loop = 1;
	private static final int FMT_METADB_LEXTYPE_Line = 2;
	private static final int FMT_METADB_LEXTYPE_Instruction = 3;
			  
	private String title;
	private String description;
	private String []kindNames;
	
	private LongObjectMap<LoadModuleScope>    mapLoadModules;
	private LongObjectHashMap<SourceFile>     mapFiles;
	private LongObjectHashMap<ProcedureScope> mapProcedures;
	
	private StringArea stringArea;
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
	
	
	/***
	 * Get the load module for a specified id
	 * @param id
	 * @return
	 */
	public LoadModuleScope getLoadModule(long id) {		
		return mapLoadModules.get(id);
	}
	
	/****
	 * Get the number of load modules
	 * @return
	 */
	public int getNumLoadModules() {
		return mapLoadModules.size();
	}
	
	/***
	 * Retrieve the iterator for list of load modules
	 * @return
	 */
	public Iterator<LoadModuleScope> getLoadModuleIterator() {
		return mapLoadModules.iterator();
	}
	
	/****
	 * Retrieve the number of files in this database
	 * @return
	 */
	public int getNumFiles() {
		return mapFiles.size();
	}
	
	/***
	 * Retrieve the iterator for the list of files
	 * @return
	 */
	public Iterator<SourceFile> getFileIterator() {
		return mapFiles.iterator();
	}
	
	/****
	 * Retrieve the number of procedures
	 * @return
	 */
	public int getNumProcedures() {
		return mapProcedures.size();
	}
	
	/****
	 * Retrieve the iterator of list of procedure scopes
	 * @return Iterator
	 */
	public Iterator<ProcedureScope> getProcedureIterator() {
		return mapProcedures.iterator();
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
		
		// no string buffer needed
		stringArea.dispose();
	}
	
	
	/*****
	 * Main function to parse the section header of meta.db file
	 * 
	 * @param channel
	 * 			File channel
	 * @param sections
	 * 			array of section headers
	 * 
	 * @throws IOException
	 */
	private void parseHeaderMetaData(FileChannel channel, DataSection []sections) 
			throws IOException {
		// grab general description of the database
		parseGeneralDescription(channel, sections[INDEX_GENERAL]);
		
		// grab the id-tuple type names
		parseHierarchicalIdTuple(channel, sections[INDEX_NAMES]);
		
		// grab the description of the metrics
		parseMetricDescription(channel, sections[INDEX_METRICS]);
		
		// grab the string block to be used later
		var buffer = channel.map(MapMode.READ_ONLY, sections[INDEX_STRINGS].offset, sections[INDEX_STRINGS].size);
		stringArea = new StringArea(buffer, sections[INDEX_STRINGS].offset);
		
		// parse the load modules
		mapLoadModules = parseLoadModules(channel, sections[INDEX_MODULES]);
		
		// parse the file table
		mapFiles = parseFiles(channel, sections[INDEX_FILES]);
		
		// parse the procedure table
		mapProcedures = parseFunctions(channel, sections[INDEX_FUNCTIONS]);
		
		parseRoot(channel, sections[INDEX_CONTEXT]);
	}
	
	private void parseGeneralDescription(FileChannel channel, DataSection section) 
			throws IOException {
		var buffer = channel.map(MapMode.READ_ONLY, section.offset, section.size);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		var pTitle = buffer.getLong();
		var pDescription = buffer.getLong();
		
		int position = (int) (pTitle-section.offset);
		title = getNullTerminatedString(buffer, position);
		
		position = (int) (pDescription - section.offset);
		description = getNullTerminatedString(buffer, position);

	}
	
	
	private void parseHierarchicalIdTuple(FileChannel channel, DataSection section) 
			throws IOException {
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
	
	
	private void parseMetricDescription(FileChannel channel, DataSection section) 
			throws IOException {
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
					
					if (scopeName.equals(METRIC_SCOPE_POINT)) 
						continue;
					
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
	
	
	private LongObjectMap<LoadModuleScope> parseLoadModules(FileChannel channel, DataSection section) 
			throws IOException {
		var buffer = channel.map(MapMode.READ_ONLY, section.offset, section.size);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		
		long pModules  = buffer.getLong();
		int  nModules  = buffer.getInt(0x08);
		short szModule = buffer.getShort(0x0c);
		
		int baseOffset = (int) (pModules - section.offset);
		LongObjectHashMap<LoadModuleScope> mapLoadModules = new LongObjectHashMap<>(nModules);

		for(int i=0; i<nModules; i++) {
			int delta        = i * szModule;
			int position     = baseOffset + delta + 0x08;
			long pModuleName = buffer.getLong(position);
			String path      = stringArea.toString(pModuleName);
			
			LoadModuleScope lms = new LoadModuleScope(null, path, null, i);
			mapLoadModules.put(pModules + delta, lms);
		}
		return mapLoadModules;
	}
	
	
	private LongObjectHashMap<SourceFile> parseFiles(FileChannel channel, DataSection section) 
			throws IOException {
		var buffer = channel.map(MapMode.READ_ONLY, section.offset, section.size);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		
		long pFiles = buffer.getLong();
		int  nFiles = buffer.getInt(0x08);
		short szFiles = buffer.getShort(0x0c);
		
		int basePosition = (int) (pFiles - section.offset);
		LongObjectHashMap<SourceFile> mapSourceFile = new LongObjectHashMap<>(nFiles);
		
		for(int i=0; i<nFiles; i++) {
			int delta    = i * szFiles;
			int position = basePosition + delta;
			int flags  = buffer.getInt(position);
			long pPath = buffer.getLong(position + 0x08);
			
			boolean available = (flags & 0x1) == 0x1;
			String  name = stringArea.toString(pPath);
			
			SourceFile sf = new SimpleSourceFile(i, new File(name), available);
			mapSourceFile.put(pFiles + delta, sf);
		}
		return mapSourceFile;
	}
	
	
	private LongObjectHashMap<ProcedureScope> parseFunctions(FileChannel channel, DataSection section) 
			throws IOException {
		var buffer = channel.map(MapMode.READ_ONLY, section.offset, section.size);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		
		long pFunctions = buffer.getLong();
		int  nFunctions = buffer.getInt(0x08);
		short szFunctions = buffer.getShort(0x0c);
		
		var basePosition = pFunctions - section.offset;
		LongObjectHashMap<ProcedureScope> mapProcedures = new LongObjectHashMap<>(nFunctions);
		
		for(int i=0; i<nFunctions; i++) {
			int position = (int) (basePosition + (i * szFunctions));
			long pName   = buffer.getLong(position);
			long pModule = buffer.getLong(position + 0x08);
			long offset  = buffer.getLong(position + 0x10);
			long pFile   = buffer.getLong(position + 0x18);
			int  line    = buffer.getInt( position + 0x20);
			
			var name = stringArea.toString(pName);
			var lms  = mapLoadModules.get(pModule);
			var file = mapFiles.get(pFile);
			
			ProcedureScope ps = new ProcedureScope(null, lms, file, line, line, name, false, i, i, null, 0);
			mapProcedures.put(position, ps);
		}
		return mapProcedures;
	}
	
	
	private Scope parseContext(ByteBuffer buffer, RootScope root, long startLocation, long size) 
			throws IOException {

		int loc = (int) startLocation;

		long szChildren = buffer.getLong(loc);
		long pChildren  = buffer.getLong(loc + 0x08);
		int  ctxId      = buffer.getInt (loc + 0x10);
		
		byte flags       = buffer.get(loc + 0x14);
		byte relation    = buffer.get(loc + 0x15);
		byte lexicalType = buffer.get(loc + 0x16);
		byte nFlexWords  = buffer.get(loc + 0x17);
		
		long pFunction = 0;
		long pFile   = 0;
		int  line    = 0;
		long pModule = 0;
		long offset  = 0;		
		byte nwords  = 0;
		
		/*
			{Flags} above refers to an u8 bit field with the following sub-fields (bit 0 is least significant):
			
			Bit 0: hasFunction. If 1, the following sub-fields of flex are present:
			    - flex[0]: FS* pFunction: Function associated with this context
			Bit 1: hasSrcLoc. If 1, the following sub-fields of flex are present:
			    - flex[1]: SFS* pFile: Source file associated with this context
			    - flex[2]: u32 line: Associated source line in pFile
			Bit 2: hasPoint. If 1, the following sub-fields of flex are present:
			    - flex[3]: LMS* pModule: Load module associated with this context
			    - flex[4]: u64 offset: Assocated byte offset in *pModule
			Bits 3-7: Reserved for future use.
		 */
		if ((flags & 0x1) != 0) {
			if (nFlexWords < nwords + 1) 
				return null;
			pFunction = buffer.getLong(loc + 0x18 + nwords * 8);
			nwords++;
		}
		if ((flags & 0x2) != 0) {
			if (nFlexWords < nwords + 2) 
				return null;
			pFile = buffer.getLong(loc + 0x18 + nwords * 8);
			nwords++;
		}
		if ((flags & 0x4) != 0) {
			if (nFlexWords < nwords + 2) 
				return null;
			pModule = buffer.getLong(loc + 0x18 + nwords * 8);
			offset  = buffer.getLong(loc + 0x18 + (nwords+1) * 8);
			nwords++;
		}
		ProcedureScope ps = mapProcedures.get(pFunction);
		SourceFile file = mapFiles.get(pFile);
		LoadModuleScope lm = mapLoadModules.get(pModule);
		Scope scope = null;

		boolean alien = false;
		switch(relation) {
		case FMT_METADB_RELATION_LexicalNest:
			break;
		case FMT_METADB_RELATION_InlinedCall:
			alien = true;
		case FMT_METADB_RELATION_Call:
			break;
		default:
			throw new RuntimeException("Invalid node relation field");
		}

		switch(lexicalType) {
		case FMT_METADB_LEXTYPE_Function:
			LineScope ls = new LineScope(root, file, line, ctxId, ctxId);
			if (ps == null) {
				ps = new ProcedureScope(root, lm, file, line, line, Constants.PROCEDURE_UNKNOWN, alien, ctxId, ctxId, null, 0);
			}
			scope = new CallSiteScope(ls, ps, CallSiteScopeType.CALL_TO_PROCEDURE, ctxId, ctxId);			
		case FMT_METADB_LEXTYPE_Instruction:
		case FMT_METADB_LEXTYPE_Line:
			scope = new LineScope(root, file, line, ctxId, ctxId);
			break;
		case FMT_METADB_LEXTYPE_Loop:
			scope = new LoopScope(root, file, line, line, ctxId, ctxId);
			break;
		default:
			throw new RuntimeException("Invalid node relation field");
		}
		return scope;
	}
	
	
	private RootScope parseRoot(FileChannel channel, DataSection section) 
			throws IOException {
		var buffer = channel.map(MapMode.READ_ONLY, section.offset, section.size);
		buffer.order(ByteOrder.LITTLE_ENDIAN);

		long szRoot = buffer.getLong();
		long pRoot  = buffer.getLong();
		
		RootScope root = new RootScopeDynamic(this, RootScope.DEFAULT_SCOPE_NAME, RootScopeType.CallingContextTree);
		Scope scope = parseContext(buffer, root, pRoot-section.offset, szRoot);
		root.addSubscope(scope);
		
		return root;
	}
	
	private final int FMT_METADB_MINSZ_Context = 0x18;
	
	private long FMT_METADB_SZ_Context(int nFlexWords)  {
		return (FMT_METADB_MINSZ_Context + 8 * (nFlexWords));
	}
	
	/*****
	 * Find a null-terminated string in a buffer from a specified relative position
	 *  
	 * @param buffer
	 * 			file buffer
	 * @param startPosition
	 * 			the relative offset of the string (zero-based)
	 * @return
	 * 			The string if exist, empty string otherwise
	 * @throws IOException
	 */
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
	
	
	/*******************
	 * 
	 * Class to retrieve a string from the string section in meta.db
	 * <br/>
	 * The caller has to call {@code dispose} to free the allocated resources
	 *
	 *******************/
	private static class StringArea
	{
		private final ByteBuffer stringsArea;
		private final long baseLocation;
		
		public StringArea(ByteBuffer stringsArea, long baseLocation) {
			this.stringsArea  = stringsArea;
			this.baseLocation = baseLocation;
		}
		
		public String toString(long absoluteLocation) {
			int location = (int) (absoluteLocation - baseLocation);			
			StringBuilder sb = new StringBuilder();

			for(int i=0; i<stringsArea.capacity(); i++) {
				byte b = stringsArea.get(location + i);
				if (b == 0)
					break;
				sb.append((char)b);
			}
			return sb.toString();
		}
		
		/***
		 * Free the resources
		 */
		public void dispose() {
			stringsArea.clear();
		}
	}
}
