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
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
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
import edu.rice.cs.hpcdata.experiment.scope.ProcedureCallScope;
import edu.rice.cs.hpcdata.experiment.scope.ProcedureScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.experiment.scope.UnknownScope;
import edu.rice.cs.hpcdata.experiment.scope.visitors.CallingContextReassignment;
import edu.rice.cs.hpcdata.experiment.scope.visitors.TraceScopeVisitor;
import edu.rice.cs.hpcdata.experiment.source.SimpleSourceFile;
import edu.rice.cs.hpcdata.experiment.source.SourceFile;
import edu.rice.cs.hpcdata.util.Constants;


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
	private static final String METRIC_SCOPE_FUNCTION  = "function";
	private static final String METRIC_SCOPE_EXECUTION = "execution";
		
	private static final int INDEX_GENERAL = 0;
	private static final int INDEX_NAMES   = 1;
	private static final int INDEX_METRICS = 2;
	private static final int INDEX_CONTEXT = 3;
	private static final int INDEX_STRINGS = 4;
	private static final int INDEX_MODULES = 5;
	private static final int INDEX_FILES   = 6;
	private static final int INDEX_FUNCTIONS = 7;
	
	// Parent-child relation: 
	private static final int FMT_METADB_RELATION_LEXICAL_NEST = 0;
	private static final int FMT_METADB_RELATION_CALL 		  = 1;
	private static final int FMT_METADB_RELATION_CALL_INLINED = 2;

	// lexical type:
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
	 * Parser for the metric description section with
	 * the following diagram:
	 * <pre>
                 ,-------------------.             
                 |Performance_Metrics|             
                 |-------------------|             
                 |pMetric : long     |             
                 |nMetrics : u32     |             
                 |szMetric : u8      |             
                 |szScopeInst : u8   |             
                 |szSummary : u8     |             
                 |                   |             
                 |pScopes : long     |             
                 |nScopes : u16      |             
                 |szScope : u8       |             
                 `-------------------'             
                            |                      
                            |                      
                  ,------------------.             
                  |Metric_Descriptors|             
                  |------------------|             
                  |pName : char*     |             
                  |pScopeInsts : long|             
                  |pSummaries : long |             
                  |nScopeInsts : u16 |             
                  |nSummaries : u16  |             
                  `------------------'             
                      |            |               
                      |        ,------------------.
,---------------------------.  |Summary_Statistics|
|Propagation_Scopes_Instance|  |------------------|
|---------------------------|  |pScope : long     |
|pScope : long              |  |pFormula : char*  |
|propMetricId : u16         |  |combine : u8      |
`---------------------------'  |statMetricId : u16|
                   |           `------------------'
                   |               |               
                   |               |               
               ,-----------------------.           
               |Propagation_Scope      |           
               |-----------------------|           
               |pScopeName : char*     |           
               |type : byte            |           
               |propagationIndex : byte|           
               `-----------------------'           
	 * </pre>
	 * @param channel
	 * 			The file IO channel
	 * @param section
	 * 			The data for metric section
	 * @throws IOException
	 */
	private List<BaseMetric> parseMetricDescription(FileChannel channel, DataSection section) 
			throws IOException {
		var buffer = channel.map(MapMode.READ_ONLY, section.offset, section.size);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		var pMetrics = buffer.getLong();
		var nMetrics = buffer.getInt();
		var szMetric = buffer.get(0x0c);
		var szScopeInst  = buffer.get(0x0d);
		var szSummary    = buffer.get(0x0e);
		
		long pScopes = buffer.getLong(0x10);
		var nScopes  = buffer.getShort(0x18);
		var szScope  = buffer.get(0x1a);
		
		var metricDesc = new ArrayList<BaseMetric>(nMetrics);
		int position = (int) (pMetrics - section.offset);
		
		LongObjectHashMap<PropagationIndex> mapPropagationIndex = new LongObjectHashMap<>(nScopes);

		// --------------------------------------
		// Propagation Scope (PS)
		// --------------------------------------
		/*
		  00:	char* pScopeName	    4.0	Name of the propagation scope
		  08:	u8	  type	            4.0	Type of propagation scope described
		  09:	u8	  propagationIndex	4.0	Index of this propagation's propagation bit
		 */
		for(int i=0; i<nScopes; i++) {

			int basePosition  = (int) ((pScopes - section.offset) + (i * szScope));			
			long pScopeName   = buffer.getLong(basePosition);
			int scopePosition = (int) (pScopeName - section.offset);

			PropagationIndex pi = new PropagationIndex();

			pi.scopeType = buffer.get(basePosition + 0x08);
			pi.propIndex = buffer.get(basePosition + 0x09);			
			pi.scopeName = getNullTerminatedString(buffer, scopePosition);
			
			long pScope  = pScopes + (i * szScope);
			mapPropagationIndex.put(pScope, pi);
		}
		
		// --------------------------------------
		// Gathering the descriptions of performance metrics
		// --------------------------------------
		// Hex	Type	Name	Ver.	Description (see the Formats legend)
		// A 8		ALIGNMENT		See Alignment properties
		// 00:	char*	pName	4.0	Canonical name for the metric
		// 08:	{PSI}[nScopeInsts]*	pScopeInsts	4.0	Instantiated propagated sub-metrics
		// 10:	{SS}[nSummaries]*	pSummaries	4.0	Descriptions of generated summary statistics
		// 18:	u16	nScopeInsts	4.0	Number of instantiated sub-metrics for this metric
		// 1a:	u16	nSummaries	4.0	Number of summary statistics for this metric
		for(int i=0; i<nMetrics; i++) {
			int metricLocation = position + (i * szMetric);

			var pName   = buffer.getLong (metricLocation);
			var pScopeInsts = buffer.getLong(metricLocation  + 0x08);
			var pSummaries  = buffer.getLong(metricLocation  + 0x10);
			var nScopeInsts = buffer.getShort(metricLocation + 0x18);
			var nSummaries  = buffer.getShort(metricLocation + 0x1a);
			
			int scopesPosition = (int) (pScopeInsts - section.offset);
			
			// --------------------------------------
			// Instantiated propagated sub-metrics (PSI)
			// --------------------------------------
			/*
				00:	{PS}*  pScope	    4.0	Propagation scope instantiated
				08:	u16	   propMetricId	4.0	Unique identifier for propagated metric values
			 */
			short []propMetricId  = new short[nScopeInsts];
			
			for (int j=0; j<nScopeInsts; j++) {
				int basePosition = (int) (scopesPosition + (j * szScope));
				propMetricId[j]  = buffer.getShort(basePosition + 0x08);
			}
			int strPosition = (int) (pName - section.offset);
			String metricName = getNullTerminatedString(buffer, strPosition);
			int []metricIndexesPerScope = new int[nScopes];
			
			int baseSummariesLocation = (int) (pSummaries - section.offset);
					
			// --------------------------------------
			// Summary Statistics (SS)
			// --------------------------------------
			/*
			 00:	{PS}*  pScope	    4.0	Propagation scope summarized
			 08:	char*  pFormula	    4.0	Canonical unary function used for summary values
			 10:	u8	   combine	    4.0	Combination n-ary function used for summary values
			 12:	u16	   statMetricId	4.0	Unique identifier for summary statistic values
			 */
			for(short k=0; k<nSummaries; k++) {
				int summaryLoc = baseSummariesLocation + k * szSummary;
				
				long pScope    = buffer.getLong(summaryLoc);
				long pFormula  = buffer.getLong(summaryLoc + 0x08);
				byte combine   = buffer.get(summaryLoc + 0x10);
				short statMetric = buffer.getShort(summaryLoc + 0x12);
										
				var strFormula = getNullTerminatedString(buffer, (int) (pFormula-section.offset));
				
				var m = new HierarchicalMetric(dataSummary, statMetric, metricName);
				m.setFormula(strFormula);
				m.setCombineType(combine);
				m.setDescription(metricName);
				m.setOrder(statMetric);
				m.setIndex(propMetricId[k]);
				
				// TODO: default metric annotation for prof2 database
				// temporary quick fix: every metric is percent annotated
				// this should be fixed when we parse metrics.yaml
				m.setAnnotationType(AnnotationType.PERCENT);
				
				MetricType type;
				var pi = mapPropagationIndex.get(pScope);
				switch(pi.scopeType) {
				case 2:
					type = MetricType.INCLUSIVE;
					break;
				case 1:
					type = MetricType.POINT_EXCL;
					break;
				case 3:
					type = MetricType.EXCLUSIVE;
					break;
				default:
					type = MetricType.UNKNOWN;
				}
				m.setMetricType(type);
									
				VisibilityType vt = type == MetricType.POINT_EXCL ? 
									VisibilityType.HIDE : VisibilityType.SHOW; 
				m.setDisplayed(vt);

				// store the index of this scope.
				// we need this to propagate the partner index
				metricIndexesPerScope[k] = metricDesc.size();
				
				metricDesc.add(m);
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
		int numModules = buffer.getInt(0x08);
		short szModule = buffer.getShort(0x0c);
		
		int baseOffset = (int) (pModules - section.offset);
		LongObjectHashMap<LoadModuleScope> mapModules = new LongObjectHashMap<>(numModules);
		int baseId = Constants.FLAT_ID_BEGIN + 1;
		
		for(int i=0; i<numModules; i++) {
			int delta        = i * szModule;
			int position     = baseOffset + delta + 0x08;
			long pModuleName = buffer.getLong(position);
			String path      = stringArea.toString(pModuleName);
			
			LoadModuleScope lms = new LoadModuleScope(rootCCT, path, SourceFile.NONE, i+baseId);
			long key = pModules + delta;
			mapModules.put(key, lms);
		}
		
		return mapModules;
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
		
		long pFiles   = buffer.getLong();
		int numFiles  = buffer.getInt(0x08);
		short szFiles = buffer.getShort(0x0c);
		
		int basePosition = (int) (pFiles - section.offset);
		LongObjectHashMap<SourceFile> mapSourceFile = new LongObjectHashMap<>(numFiles);
		int baseId = Constants.FLAT_ID_BEGIN + mapLoadModules.size() + 1;
		
		for(int i=0; i<numFiles; i++) {
			int delta    = i * szFiles;
			int position = basePosition + delta;
			int flags  = buffer.getInt(position);
			long pPath = buffer.getLong(position + 0x08);
			
			boolean available = (flags & 0x1) == 0x1;
			String  name = stringArea.toString(pPath);
			
			SourceFile sf = new SimpleSourceFile(baseId+i, new File(name), available);
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

		int baseId = Constants.FLAT_ID_BEGIN + mapLoadModules.size() + mapFileSources.size() + 1;

		for(int i=0; i<nFunctions; i++) {
			int position = (int) (basePosition + (i * szFunctions));
			long pName   = buffer.getLong(position);
			long pModule = buffer.getLong(position + 0x08);
			buffer.getLong(position + 0x10);
			long pFile   = buffer.getLong(position + 0x18);
			int  line    = buffer.getInt( position + 0x20) - 1;
			
			var name = stringArea.toString(pName);
			var lms  = mapLoadModules.get(pModule);
			var file = mapFileSources.get(pFile);
			
			if (file == null)
				file = SourceFile.NONE;
			if (lms == null)
				lms = LoadModuleScope.NONE;
			
			// this line has to be remove once prof2 is fixed
			int feature = getProcedureFeature(name);
			
			long key = pFunctions + (i * szFunctions);
			ProcedureScope ps = new ProcedureScope(rootCCT, lms, file, line, line, name, false, position, i+baseId, null, feature);			
			mapProcedures.put(key, ps);
		}
		return mapProcedures;
	}

	
	/*****
	 * Temporary hack: return the procedure feature by scanning the name
	 * This is a temporary code. It has to be removed once prof2 implements
	 * properly the procedure feature. 
	 * 
	 * @param name
	 * @return
	 */
	private int getProcedureFeature(String name) {
		boolean isPartial = name.equals("<partial call paths>");

		// check if the name is in the form of <.* root>
		boolean isRoot = name.charAt(0) == '<' && name.endsWith(" root>");
		
		if (isPartial || isRoot)
			return ProcedureScope.FeatureTopDown;
		
		return ProcedureScope.FeatureProcedure;
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
	
	private final IntObjectHashMap<String> mapString = new IntObjectHashMap<>();

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
		if (mapString.containsKey(startPosition))
			return mapString.get(startPosition);

		StringBuilder sb = new StringBuilder();
		
		for(int i=0; i<buffer.capacity(); i++) {
			byte b = buffer.get(startPosition + i);
			if (b == 0)
				break;
			sb.append((char)b);
		}
		final var label = sb.toString();
		mapString.put(startPosition, label);
		
		return label;
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
			long offset  = 0;
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
				line  = buffer.getInt( loc + 0x18 + (nwords+1) * 8) - 1;
				nwords++;
			}
			if ((flags & 0x4) != 0) {
				if (nFlexWords < nwords + 2) 
					return;
				pModule = buffer.getLong(loc + 0x18 + nwords * 8);
				offset  = buffer.getLong(loc + 0x18 + (nwords+1) * 8);
				nwords++;
			}
			var ps = mapProcedures.getIfAbsent (pFunction, ()->ProcedureScope.NONE);
			var fs = mapFileSources.getIfAbsent(pFile,     ()->SourceFile.NONE);
			var lm = mapLoadModules.getIfAbsent(pModule,   ()->LoadModuleScope.NONE);
			
			// linearize the flat id. This is not sufficient and causes collisions for large and complex source code
			// This needs to be computed more reliably.
			int flatId = getKey(lm, fs, ps, line, lexicalType, relation);
			
			newScope = null; 

			switch(lexicalType) {
			case FMT_METADB_LEXTYPE_FUNCTION:
				newScope = createLexicalFunction(parent, ps, ctxId, line, relation);
				if (parent instanceof LineScope) {
					var p = parent.getParentScope();
					linkParentChild(p, newScope);
					parent.addScopeReduce(newScope);
					return;
				}
				break;
			case FMT_METADB_LEXTYPE_LOOP:
				newScope = new LoopScope(rootCCT, fs, line, line, ctxId, flatId);
				break;
			case FMT_METADB_LEXTYPE_LINE:
				newScope = new LineScope(rootCCT, fs, line, ctxId, flatId);
				break;
			case FMT_METADB_LEXTYPE_INSTRUCTION:
				newScope = new InstructionScope(rootCCT, lm, offset, ctxId, flatId);
				newScope.setSourceFile(fs);
				break;
			default:
				newScope = new UnknownScope(rootCCT, fs, flatId);
			}
			if (parent != null)
				linkParentChild(parent, newScope);					
		}
		
		private int getKey(LoadModuleScope lms, SourceFile sf, ProcedureScope ps, int line, int lexicalType, int relation) {
			final String SEPARATOR = ":";
			
			StringBuilder sb = new StringBuilder();
			sb.append('l' + lexicalType);
			sb.append(SEPARATOR);
			
			sb.append('r' + relation);
			sb.append(SEPARATOR);
			
			sb.append(lms.getFlatIndex());
			sb.append(SEPARATOR);
						
			sb.append(sf.getFileID());
			sb.append(SEPARATOR);
			
			if (ps != ProcedureScope.NONE) {
				sb.append(ps.getFlatIndex());
				sb.append(SEPARATOR);
			}
			
			sb.append(line);
			
			int hash = sb.toString().hashCode();
			int baseId = Constants.FLAT_ID_BEGIN + mapLoadModules.size() + mapFileSources.size() + mapProcedures.size() + 1;
			
			return baseId + hash;
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
											ProcedureScope ps, 
											int ctxId,
											int line, 
											int relation) {
			
			boolean alien  = relation == FMT_METADB_RELATION_CALL_INLINED;
			
			if (!(parent instanceof LineScope)) {
				if (parent instanceof RootScope && ps == ProcedureScope.NONE) {
					// partial call path if the parent is a root scope, and
					// the procedure is unknown
					return new ProcedureScope(rootCCT, 
							LoadModuleScope.NONE, SourceFile.NONE, 
							0, 0, 
							"<partial call paths>", false, 
							ctxId, Constants.FLAT_ID_PROC_PARTIAL, 
							null, ProcedureScope.FeatureRoot);

				}
				// no call site in the stack: it must be a procedure scope
				int procFeature = ps.isTopDownProcedure() ? ProcedureScope.FeatureTopDown : ProcedureScope.FeatureProcedure; 
				return new ProcedureCallScope(rootCCT, ps.getLoadModule(), ps.getSourceFile(), line, ps.getName(), alien, ctxId, ps.getFlatIndex(), null, procFeature);				
			}
			if (relation == FMT_METADB_RELATION_LEXICAL_NEST)
				return new ProcedureScope(rootCCT, ps.getLoadModule(), ps.getSourceFile(), line, line, ps.getName(), alien, ctxId, ps.getFlatIndex(), null, ProcedureScope.FeatureProcedure);				

			ps.setAlien(alien);
			LineScope ls = (LineScope) parent;
			int fid = String.valueOf(ls.getFlatIndex() + ":" + ls.getFirstLineNumber()).hashCode();
			var cs = new CallSiteScope(ls, ps, CallSiteScopeType.CALL_TO_PROCEDURE, ctxId, fid);
			
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
	
	private static class PropagationIndex 
	{
		String scopeName;
		byte scopeType;
		byte propIndex;
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
