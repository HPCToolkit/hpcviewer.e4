package edu.rice.cs.hpcdata.db.version4;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.collections.api.map.primitive.LongObjectMap;
import org.eclipse.collections.impl.map.mutable.primitive.LongObjectHashMap;

import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.ExperimentConfiguration;
import edu.rice.cs.hpcdata.experiment.IExperiment;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric.AnnotationType;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric.VisibilityType;
import edu.rice.cs.hpcdata.experiment.metric.DerivedMetric;
import edu.rice.cs.hpcdata.experiment.metric.HierarchicalMetric;
import edu.rice.cs.hpcdata.experiment.metric.MetricType;
import edu.rice.cs.hpcdata.experiment.scope.CallSiteScope;
import edu.rice.cs.hpcdata.experiment.scope.CallSiteScopeType;
import edu.rice.cs.hpcdata.experiment.scope.InstructionScope;
import edu.rice.cs.hpcdata.experiment.scope.LineScope;
import edu.rice.cs.hpcdata.experiment.scope.LoadModuleScope;
import edu.rice.cs.hpcdata.experiment.scope.LoopScope;
import edu.rice.cs.hpcdata.experiment.scope.ProcedureScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.experiment.scope.visitors.CallingContextReassignment;
import edu.rice.cs.hpcdata.experiment.scope.visitors.TraceScopeVisitor;
import edu.rice.cs.hpcdata.experiment.source.SimpleSourceFile;
import edu.rice.cs.hpcdata.experiment.source.SourceFile;


/*********************************************
 * 
 * Class to manage the meta.db file.
 * Once instantiated, it requires to call consecutively:
 * <ul>
 *   <li>open the file
 * </ul>
 * Example:
 * <pre>
 *   DataMeta dm = new DataMeta()
 *   dm.open(filename);
 * </pre>
 *
 *********************************************/
public class DataMeta extends DataCommon 
{
	// --------------------------------------------------------------------
	// constants
	// --------------------------------------------------------------------
	public static final String  DB_META_FILE       = "meta.db";
	
	private final static String HEADER_MAGIC_STR   = "HPCTOOLKITmeta";
	
	private final static String METRIC_SCOPE_POINT     = "point";
	private static final String METRIC_SCOPE_EXECUTION = "execution";
		
	private static final int INDEX_GENERAL = 0;
	private static final int INDEX_NAMES   = 1;
	private static final int INDEX_METRICS = 2;
	private static final int INDEX_CONTEXT = 3;
	private static final int INDEX_STRINGS = 4;
	private static final int INDEX_MODULES = 5;
	private static final int INDEX_FILES   = 6;
	private static final int INDEX_FUNCTIONS = 7;
	
	// future usage: 
	// private static final int FMT_METADB_RELATION_LEXICAL_NEST = 0;
	// private static final int FMT_METADB_RELATION_CALL = 1;
	private static final int FMT_METADB_RELATION_CALL_INLINED = 2;

	private static final int FMT_METADB_LEXTYPE_FUNCTION = 0;
	private static final int FMT_METADB_LEXTYPE_LOOP = 1;
	private static final int FMT_METADB_LEXTYPE_LINE = 2;
	private static final int FMT_METADB_LEXTYPE_INSTRUCTION = 3;

	// --------------------------------------------------------------------
	// variables
	// --------------------------------------------------------------------
			  	
	private LongObjectMap<LoadModuleScope>    mapLoadModules;
	private LongObjectHashMap<SourceFile>     mapFileSources;
	private LongObjectHashMap<ProcedureScope> mapProcedures;
	
	private RootScope rootCCT;
	
	private StringArea stringArea;
	private List<BaseMetric> metrics;

	private DataSummary dataSummary;
	private IExperiment experiment;

	
	public DataMeta() {
		super();
	}
	
	// --------------------------------------------------------------------
	// methods
	// --------------------------------------------------------------------
	
	/****
	 * Get the experiment object
	 * 
	 * @return
	 */
	public IExperiment getExperiment() {
		return experiment;
	}
	
	
	/****
	 * Open a database
	 * 
	 * @param experiment
	 * @param directory
	 * @throws IOException
	 */
	public void open(IExperiment experiment, String directory) throws IOException {
		this.experiment = experiment;

		var root = new RootScope(experiment, directory, RootScopeType.Invisible, -1, -1);
		rootCCT  = new RootScope(experiment, RootScope.DEFAULT_SCOPE_NAME, RootScopeType.CallingContextTree);
		root.addSubscope(rootCCT);
		rootCCT.setParentScope(root);
		
		super.open(directory + File.separator + DB_META_FILE);

		// needs to manage the profile.db here since we need it
		// to access the metric value
		dataSummary.open(directory);
		
		// manually setup the metrics for the sake of backward compatibility
		final Experiment exp = (Experiment) experiment;
		exp.setMetrics(metrics);
		exp.setMetricRaw(metrics);
		
		rootCCT.setMetricValueCollection(new MetricValueCollection4(dataSummary));

		// restructure the cct
		// if a line scope has a call site, move it to be the sibling
		CallingContextReassignment ccr = new CallingContextReassignment();
		rootCCT.dfsVisitScopeTree(ccr);
		
		// needs to gather info about cct id and its depth
		// this is needed for traces
		TraceScopeVisitor visitor = new TraceScopeVisitor();
		rootCCT.dfsVisitScopeTree(visitor);
		
		exp.setRootScope(root);
		exp.setMaxDepth(visitor.getMaxDepth());
		exp.setScopeMap(visitor.getCallPath());
		exp.setVersion(versionMajor + "." + versionMinor);
		
		stringArea.dispose();
	}
	
	@Override
	public void open(final String directory) 
			throws IOException
	{
		throw new RuntimeException("Unsupported. Use open(IExperiment experiment, String directory)");
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
		return mapFileSources.size();
	}
	
	/***
	 * Retrieve the iterator for the list of files
	 * @return
	 */
	public Iterator<SourceFile> getFileIterator() {
		return mapFileSources.iterator();
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
		

	public DataSummary getDataSummary() {
		return dataSummary;
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
		var idTupleTypes = parseHierarchicalIdTuple(channel, sections[INDEX_NAMES]);
		experiment.setIdTupleType(idTupleTypes);

		// prepare profile.db parser
		dataSummary = new DataSummary(idTupleTypes);

		// grab the description of the metrics
		metrics = parseMetricDescription(channel, sections[INDEX_METRICS]);
		
		// grab the string block to be used later
		var buffer = channel.map(MapMode.READ_ONLY, sections[INDEX_STRINGS].offset, sections[INDEX_STRINGS].size);
		stringArea = new StringArea(buffer, sections[INDEX_STRINGS].offset);
		
		// parse the load modules
		mapLoadModules = parseLoadModules(channel, sections[INDEX_MODULES]);
		
		// parse the file table
		mapFileSources = parseFiles(channel, sections[INDEX_FILES]);
		
		// parse the procedure table
		mapProcedures = parseFunctions(channel, sections[INDEX_FUNCTIONS]);
		
		// create the top-down tree
		parseRoot(channel, sections[INDEX_CONTEXT]);	
	}
	
	
	/****
	 * Parser for the general section
	 * 
	 * @param channel
	 * @param section
	 * 			The {@code DataSection} of general section
	 * @throws IOException
	 */
	private void parseGeneralDescription(FileChannel channel, DataSection section) 
			throws IOException {
		var buffer = channel.map(MapMode.READ_ONLY, section.offset, section.size);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		var pTitle = buffer.getLong();
		var pDescription = buffer.getLong();
		
		int position = (int) (pTitle-section.offset);
		var title = getNullTerminatedString(buffer, position);
		
		// at the moment we don't use the description field.
		// still it's harmless to read it here.
		position = (int) (pDescription - section.offset);
		getNullTerminatedString(buffer, position);

		ExperimentConfiguration configuration = new ExperimentConfiguration();
		configuration.setName(ExperimentConfiguration.NAME_EXPERIMENT, title);
		
		experiment.setConfiguration(configuration);
	}
	
	
	/***
	 * Parser for Id tuple section
	 * 
	 * @param channel
	 * @param section
	 * @throws IOException
	 */
	private IdTupleType parseHierarchicalIdTuple(FileChannel channel, DataSection section) 
			throws IOException {
		var buffer = channel.map(MapMode.READ_ONLY, section.offset, section.size);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		var pNames = buffer.getLong();
		var kinds  = buffer.get();
		
		assert(pNames > section.offset && 
			   pNames < section.offset + section.size);
		
		var idTupleTypes = new IdTupleType();
		
		int basePosition = (int) (pNames - section.offset);
		for(byte i=0; i<kinds; i++) {
			buffer.position(basePosition + i*8);
			long pKind = buffer.getLong();
			
			int position = (int) (pKind - section.offset);
			String kind  = getNullTerminatedString(buffer, position);
			idTupleTypes.add(i, kind);
		}
		return idTupleTypes;
	}
	
	
	/****
	 * Parser for the metric description section
	 * @param channel
	 * @param section
	 * @throws IOException
	 */
	private List<BaseMetric> parseMetricDescription(FileChannel channel, DataSection section) 
			throws IOException {
		var buffer = channel.map(MapMode.READ_ONLY, section.offset, section.size);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		var pMetrics = buffer.getLong();
		var nMetrics = buffer.getInt();
		var szMetric = buffer.get(0x0c);
		var szScope  = buffer.get(0x0d);
		var szSummary = buffer.get(0x0e);
		
		var metricDesc = new ArrayList<BaseMetric>(nMetrics);
		int position = (int) (pMetrics - section.offset);
		
		for(int i=0; i<nMetrics; i++) {
			int metricLocation = position + (i * szMetric);

			var pName   = buffer.getLong (metricLocation);
			var nScopes = buffer.getShort(metricLocation + 0x08);
			var pScopes = buffer.getLong (metricLocation + 0x10);	
			
			int strPosition = (int) (pName - section.offset);
			String metricName = getNullTerminatedString(buffer, strPosition);
			int []metricIndexesPerScope = new int[nScopes];
			
			int scopesPosition = (int) (pScopes - section.offset);				
			for(int j=0; j<nScopes; j++) {
				int basePosition   = scopesPosition + (j*szScope);
				long  pScope       = buffer.getLong (basePosition);
				short nSummaries   = buffer.getShort(basePosition + 0x08);
				short propMetricId = buffer.getShort(basePosition + 0x0a);
				long  pSummaries   = buffer.getLong (basePosition + 0x10);
				
				int scopePosition  = (int) (pScope - section.offset);
				String scopeName = getNullTerminatedString(buffer, scopePosition);
				
				int baseSummariesLocation = (int) (pSummaries - section.offset);
						
				for(short k=0; k<nSummaries; k++) {
					int summaryLoc = baseSummariesLocation + k * szSummary;
					long pFormula  = buffer.getLong(summaryLoc);
					byte combine   = buffer.get(summaryLoc + 0x08);
					short statMetric = buffer.getShort(summaryLoc + 0x0a);
											
					var strFormula = getNullTerminatedString(buffer, (int) (pFormula-section.offset));
					
					var m = new HierarchicalMetric(dataSummary, statMetric, metricName);
					m.setFormula(strFormula);
					m.setCombineType(combine);
					m.setOrder(propMetricId);
					m.setDescription(metricName);
					
					// TODO: default metric annotation for prof2 database
					// temporary quick fix: every metric is percent annotated
					// this should be fixed when we parse metrics.yaml
					m.setAnnotationType(AnnotationType.PERCENT);
					
					MetricType type = scopeName.equals(METRIC_SCOPE_EXECUTION) ? 
														MetricType.INCLUSIVE : 
														MetricType.EXCLUSIVE;
					m.setMetricType(type);
										
					VisibilityType vt = scopeName.equals(METRIC_SCOPE_POINT) ? 
										VisibilityType.HIDE : 
										VisibilityType.SHOW; 
					m.setDisplayed(vt);

					// store the index of this scope.
					// we need this to propagate the partner index
					metricIndexesPerScope[j] = metricDesc.size();
					
					metricDesc.add(m);
				}
			}
			// Re-assign the partner index:
			// here we assume MetricType is either exclusive or inclusive 
			// (in the future can be more than that)
			// If a metric is exclusive, then its partner is the inclusive one.
			// This ugly nested loop tries to find the partner of each metric in this scope.
			for (int j=0; j<nScopes; j++) {
				int idx = metricIndexesPerScope[j];
				BaseMetric m1 =  metricDesc.get(idx);
				
				for (int k=0; k<nScopes; k++) {
					if (k == j) 
						continue;
					
					int idx2 = metricIndexesPerScope[k];
					BaseMetric m2 = metricDesc.get(idx2);
					if (m2.getMetricType() != m1.getMetricType()) {
						// the type of m2 is different than m1
						// theoretically m2 is the partner of m1. and vice versa
						m2.setPartner(m1.getIndex());
						m1.setPartner(m2.getIndex());
					}
				}
			}
		}
		
		// for the derived metric, we need to initialize it manually.
		for(var metric: metricDesc) {
			if (metric instanceof DerivedMetric) {
				var dm = (DerivedMetric) metric;
				dm.resetMetric((Experiment) experiment, rootCCT);
			} else if (metric instanceof HierarchicalMetric) {
				((HierarchicalMetric)metric).setProfileDatabase(dataSummary);
			}
		}
		return metricDesc;
	}
	
	
	
	/***
	 * Parser for the load module section
	 * @param channel
	 * @param section
	 * @return {@code LongObjectMap<LoadModuleScope>}
	 * 			Map from load module pointer to the load module object
	 * @throws IOException
	 */
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
			
			LoadModuleScope lms = new LoadModuleScope(rootCCT, path, SourceFile.NONE, i+1);
			long key = pModules + delta;
			mapLoadModules.put(key, lms);
		}
		
		return mapLoadModules;
	}
	
	
	/***
	 * Parser for source file section
	 * @param channel
	 * @param section
	 * @return {@code LongObjectHashMap<SourceFile>}
	 * @throws IOException
	 */
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
			
			SourceFile sf = new SimpleSourceFile(i+1, new File(name), available);
			long key = pFiles + delta;
			mapSourceFile.put(key, sf);
		}
		
		return mapSourceFile;
	}
	
	
	/***
	 * Parser for procedure or function section
	 * @param channel
	 * @param section
	 * @return {@code LongObjectHashMap<ProcedureScope>}
	 * @throws IOException
	 */
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
			buffer.getLong(position + 0x10);
			long pFile   = buffer.getLong(position + 0x18);
			int  line    = buffer.getInt( position + 0x20);
			
			var name = stringArea.toString(pName);
			var lms  = mapLoadModules.get(pModule);
			var file = mapFileSources.get(pFile);
			
			if (file == null)
				file = SourceFile.NONE;
			if (lms == null)
				lms = LoadModuleScope.NONE;
			
			long key = pFunctions + (i * szFunctions);
			ProcedureScope ps = new ProcedureScope(rootCCT, lms, file, line, line, name, false, position, i+1, null, 0);			
			mapProcedures.put(key, ps);
		}
		return mapProcedures;
	}
	
		
	/****
	 * Parse all the children of a given parent node.
	 *  
	 * @param buffer
	 * 			buffer containing the node and its children
	 * @param parent 
	 * 			(in and out) the parent node. All children (if exist) will be attached to this node.
	 * @param startLocation
	 * 			The absolute offset of the children in the {@code buffer}
	 * @param size
	 * 			The size of children in bytes
	 * @throws IOException
	 */
	private void parseChildrenContext(
										ByteBuffer buffer, 
										Scope parent, 
										long startLocation, 
										long size) 
					throws IOException {
		
		final ArrayDeque<ContextStack> stack = new ArrayDeque<>();
		int ctxLoc   = (int) (startLocation - sections[INDEX_CONTEXT].offset);
		long ctxSize = size;
		
		// look for the children as long as we still have the remainder bytes
		while(ctxSize >= FMT_METADB_MINSZ_Context) {

			ScopeContext context = new ScopeContext(buffer, ctxLoc, parent);
			
			long szAdditionalCtx = FMT_METADB_SZ_Context(context.nFlexWords);
			ctxSize -= szAdditionalCtx;
			ctxLoc += szAdditionalCtx;			

			if (context.szChildren > 0) {
				// we have children: try to traverse the children
				
				// store the current information: parent, location and size
				var ctx = new ContextStack(parent, ctxLoc, ctxSize);
				stack.push(ctx);
				
				// prepare for parsing the next children
				parent  = context.newScope;
				ctxLoc  = (int) (context.pChildren - sections[INDEX_CONTEXT].offset);
				ctxSize = context.szChildren;
			} else {
				// the current context has no children
				// check if: 
				// - we still have space for the siblings; or
				// - the parent has siblings

				// the parent has no space for sibling:
				// Try to find an ancestor that has siblings
				while (!stack.isEmpty() && (ctxSize < FMT_METADB_MINSZ_Context)) {
					var ctx = stack.pop();
					parent  = ctx.parent;
					ctxLoc  = ctx.startLocation;
					ctxSize = ctx.size;
				}
			}
		}
	}

	
	/***
	 * Create the main root and parse its direct children
	 * @param channel
	 * @param section
	 * @return
	 * @throws IOException
	 */
	private RootScope parseRoot(FileChannel channel, DataSection section) 
			throws IOException {
		var ctxBuffer = channel.map(MapMode.READ_ONLY, section.offset, section.size);
		ctxBuffer.order(ByteOrder.LITTLE_ENDIAN);

		long szRoot = ctxBuffer.getLong();
		long pRoot  = ctxBuffer.getLong();
		
		parseChildrenContext(ctxBuffer, rootCCT, pRoot, szRoot);
		
		return rootCCT;
	}
	
	private final int FMT_METADB_MINSZ_Context = 0x18;
	
	private long FMT_METADB_SZ_Context(int nFlexWords)  {
		return (FMT_METADB_MINSZ_Context + (8 * nFlexWords));
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

	
	// --------------------------------------------------------------------
	// classes
	// --------------------------------------------------------------------

	
	private static class ContextStack
	{
		Scope parent; 
		int startLocation; 
		long size;
		
		public ContextStack(Scope parent, 
				int startLocation, 
				long size) {

			this.parent = parent;
			this.startLocation = startLocation;
			this.size = size;
		}
	}
	


	/***************************************
	 * 
	 * Class to parse the context tree section of meta.db
	 *
	 ***************************************/
	private class ScopeContext
	{
		long szChildren;
		long pChildren;
		int  ctxId;
		Scope newScope;
		byte nFlexWords;
		
		public ScopeContext(ByteBuffer buffer, int loc, Scope parent) {
			szChildren = buffer.getLong(loc);
			pChildren  = buffer.getLong(loc + 0x08);
			ctxId      = buffer.getInt (loc + 0x10);
			
			readRecord(buffer, loc, parent);
		}

		
		private void readRecord(ByteBuffer buffer, int loc, Scope parent) {
			
			byte flags       = buffer.get(loc + 0x14);
			byte relation    = buffer.get(loc + 0x15);
			byte lexicalType = buffer.get(loc + 0x16);
			nFlexWords  = buffer.get(loc + 0x17);
			
			long pFunction = 0;
			long pFile   = 0;
			int  line    = 0;
			long pModule = 0;
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
					return;
				pFunction = buffer.getLong(loc + 0x18 + nwords * 8);
				nwords++;
			}
			if ((flags & 0x2) != 0) {
				if (nFlexWords < nwords + 2) 
					return;
				pFile = buffer.getLong(loc + 0x18 + nwords * 8);
				nwords++;
			}
			if ((flags & 0x4) != 0) {
				if (nFlexWords < nwords + 2) 
					return;
				pModule = buffer.getLong(loc + 0x18 + nwords * 8);
				buffer.getLong(loc + 0x18 + (nwords+1) * 8);
				nwords++;
			}
			var ps = mapProcedures.getIfAbsent (pFunction, ()->ProcedureScope.NONE);
			var fs = mapFileSources.getIfAbsent(pFile,     ()->SourceFile.NONE);
			var lm = mapLoadModules.getIfAbsent(pModule,   ()->LoadModuleScope.NONE);
			
			if (ps == ProcedureScope.NONE) {
				Scope s = getEnclosingProc(parent);
				if (s instanceof ProcedureScope)
					ps = (ProcedureScope) s;
			}
			// linearize the flat id. This is not sufficient and causes collisions for large and complex source code
			// This needs to be computed more reliably.
			int flatId = Scope.generateFlatID(lexicalType, lm.getFlatIndex(), fs.getFileID(), ps.getFlatIndex(), line); 
			
			newScope = null; 

			switch(lexicalType) {
			case FMT_METADB_LEXTYPE_FUNCTION:
				newScope = createLexicalFunction(parent, lm, fs, ps, ctxId, flatId, line, relation);
				break;
			case FMT_METADB_LEXTYPE_LOOP:
				newScope = new LoopScope(rootCCT, fs, line, line, ctxId, flatId);
				break;
			case FMT_METADB_LEXTYPE_LINE:
				newScope = new LineScope(rootCCT, fs, line, ctxId, flatId);
				break;
			case FMT_METADB_LEXTYPE_INSTRUCTION:
				newScope = new InstructionScope(rootCCT, lm, ctxId, flatId);
				newScope.setSourceFile(fs);
				break;
			default:
				throw new RuntimeException("Invalid node relation field");
			}
			linkParentChild(parent, newScope);					
		}
		
		
		private Scope getEnclosingProc(Scope scope) {
			Scope current = scope;
			while(current != null 
				  && !(current instanceof RootScope) 
				  && !(current instanceof ProcedureScope)
				  && !(current instanceof CallSiteScope)) 
				current = current.getParentScope();
			
			if (current instanceof CallSiteScope)
				current = ((CallSiteScope)current).getProcedureScope();
			
			return current;
		}
		
		/***
		 * Create a lexical function scope.
		 * 
		 * @param parent
		 * @param lm
		 * @param fs
		 * @param ps
		 * @param ctxId
		 * @param line
		 * @param relation
		 * 
		 * @return {@code Scope}
		 * 			A call site scope if the line scope exists.
		 * 			A procedure scope otherwise.
		 */
		private Scope createLexicalFunction( 
											Scope parent, 
											LoadModuleScope lm,
											SourceFile fs,
											ProcedureScope ps, 
											int ctxId,
											int flatId,
											int line, 
											int relation) {
			
			boolean alien  = relation == FMT_METADB_RELATION_CALL_INLINED;
			var fileSource = fs == null ? SourceFile.NONE : fs;
			
			if (!(parent instanceof LineScope)) {
				// no call site in the stack: it must be a procedure scope
				return new ProcedureScope(rootCCT, lm, fileSource, line, line, ps.getName(), alien, ctxId, flatId, null, ProcedureScope.FeatureProcedure);				
			}
			
			ps.setAlien(alien);
			LineScope ls = (LineScope) parent;
			var cs = new CallSiteScope(ls, ps, CallSiteScopeType.CALL_TO_PROCEDURE, ctxId, flatId);
			
			// Only the line statement knows where the source file is
			// If the line statement is unknown then the source file is unknown.
			cs.setSourceFile(ls.getSourceFile());
			
			return cs;
		}

		
		/****
		 * Begin a new scope if needed. If the child scope doesn't exist
		 * (null value), it doesn't create a child tree and just returns the parent.
		 * 
		 * @param parent
		 * @param scope
		 * 
		 * @return {@code Scope}
		 * 			a new parent if the child scope is valid. 
		 * 			Otherwise returns the parent itself.
		 */
		private Scope linkParentChild(Scope parent, Scope scope) {
			parent.addSubscope(scope);
			scope.setParentScope(parent);

			return scope;
		}
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
