package edu.rice.cs.hpcdata.experiment.xml;


import java.io.File;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcdata.experiment.BaseExperiment;
import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.ExperimentConfiguration;
import edu.rice.cs.hpcdata.experiment.scope.AlienScope;
import edu.rice.cs.hpcdata.experiment.scope.CallSiteScope;
import edu.rice.cs.hpcdata.experiment.scope.CallSiteScopeType;
import edu.rice.cs.hpcdata.experiment.scope.FileScope;
import edu.rice.cs.hpcdata.experiment.scope.LineScope;
import edu.rice.cs.hpcdata.experiment.scope.LoadModuleScope;
import edu.rice.cs.hpcdata.experiment.scope.LoopScope;
import edu.rice.cs.hpcdata.experiment.scope.ProcedureScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScopeType;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.experiment.scope.StatementRangeScope;
import edu.rice.cs.hpcdata.experiment.scope.ProcedureScope.ProcedureType;
import edu.rice.cs.hpcdata.experiment.source.FileSystemSourceFile;
import edu.rice.cs.hpcdata.experiment.source.SourceFile;
import edu.rice.cs.hpcdata.trace.TraceAttribute;
import edu.rice.cs.hpcdata.util.CallPath;
import edu.rice.cs.hpcdata.util.Constants;
import edu.rice.cs.hpcdata.util.Dialogs;
import edu.rice.cs.hpcdata.util.IUserData;


/****
 * 
 * Base class to build experiment class without metrics associated
 * The base class is ideal for using CCT only like hpctraceviewer
 * 
 * @see ExperimentBuilder2 for building experiment with metrics
 */
public class BaseExperimentBuilder extends Builder 
{	
	protected final static String ATTRIBUTE_LINE		= "l";
	protected final static String ATTRIBUTE_NAME 		= "n";
	protected final static String ATTRIBUTE_FILENAME 	= "f";
	protected final static String ATTRIBUTE_VALUE 		= "v";
	protected final static String ATTRIBUTE_ID 			= "i";
	protected final static String ATTRIBUTE_TYPE 		= "t";
	protected final static String ATTRIBUTE_ALIEN 		= "a";
	
	private final static String PROCEDURE_UNKNOWN = "unknown procedure";

	/** The default name for the experiment, in case none is found by the parser. */
	protected String defaultName;

	/** The parsed configuration. */
	protected ExperimentConfiguration configuration;

	/** Master root scope, invisible **/
	protected Scope rootScope;
	/*** root scope of the view ***/
	protected RootScope viewRootScope;

	/** The experiment to own parsed objects. */
	protected BaseExperiment experiment;

	private boolean csviewer;
	
	final private IUserData<String, String> userData;

	//--------------------------------------------------------------------------------------
	// stacks
	//--------------------------------------------------------------------------------------

	/** A stack to keep track of scope nesting while parsing. */
	protected Stack<Scope> scopeStack;

	/** The current source file while parsing. */
	protected Stack<SourceFile> srcFileStack;
	
	private Stack<RootScope> rootStack;

	//--------------------------------------------------------------------------------------
	// hash map
	//--------------------------------------------------------------------------------------
	private final HashMap<Integer, String> 			hashProcedureTable;
	private final HashMap<Integer, LoadModuleScope> hashLoadModuleTable;
	private final HashMap<Integer, SourceFile> 		hashSourceFileTable;
	
	private final HashMap<Integer /*id*/, Integer /*status*/>	  statusProcedureMap;

	private final Map<Integer, CallPath> mapCpidToCallpath;
	
	private final IdTupleType idTupleType;
 
	//--------------------------------------------------------------------------------------
	// trace information
	//--------------------------------------------------------------------------------------
	private int max_depth = 0;
	
	private int min_cctid = Integer.MAX_VALUE;
	private int max_cctid = Integer.MIN_VALUE;

	private int current_depth = 0;
	
	private boolean removeInvisibleProcedure = false;
	
	//=============================================================
	
	/*************************************************************************
	 *	Creates a ExperimentBuilder.
	 *
	 *	Parsed objects are added to the experiment as soon as possible, but
	 *	all the parsed objects of a given kind must be added at once. The
	 *	builder keeps parsed objects on lists until a whole set can be added
	 *	to the experiment.
	 *	<p>
	 *	Because of the way <code>Metric</code>s are implemented, the metric
	 *	objects must be added to the experiment before any scope objects are
	 *	constructed. Scopes need to know how many metrics the experiment has,
	 *	and find out by asking the experiment.
	 *
	 *  @param experiment: experiment class
	 *  @param defaultName: the default name of the experiment
	 *
	 ************************************************************************/
	public BaseExperimentBuilder(BaseExperiment experiment, String defaultName, IUserData<String, String> userData) {
		super();
		this.csviewer = false;
		
		hashProcedureTable  = new HashMap<Integer, String>();
		hashLoadModuleTable = new HashMap<Integer, LoadModuleScope>();
		hashSourceFileTable = new HashMap<Integer, SourceFile>();
		statusProcedureMap  = new HashMap<Integer, Integer>();
		
		mapCpidToCallpath   = new HashMap<>();
		
		idTupleType = new IdTupleType();
		
		// parse action data structures
		this.scopeStack   = new Stack<Scope>();
		this.rootStack    = new Stack<RootScope>();
		this.srcFileStack = new Stack<SourceFile>();
		
		this.srcFileStack.push(null); // mimic old behavior

		// creation arguments
		this.experiment = experiment;
		this.defaultName = defaultName;
		
		this.userData = userData;
	}
	
	
	protected boolean isCallingContextTree() {
		return this.csviewer;
	}
	
	private SourceFile getSourceFile(String fileIdString)
	{
		SourceFile sourceFile = null;
		try {
			Integer objFileKey = Integer.parseInt(fileIdString);
			 sourceFile=hashSourceFileTable.get(objFileKey.intValue());
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return sourceFile;
	}	


	/*************************************************************************
	 *	Takes notice of the beginning of an element.
	 * @throws OldXMLFormatException 
	 ************************************************************************/

	public void beginElement(String element, String[] attributes, String[] values) 
	{
		//TokenXML current = Token2.map(element);

		switch(element)
		{
		case "HPCToolkitExperiment":
			this.do_HPCTOOLKIT(attributes, values);
			break;
		case "Header":
			do_Header(attributes,values);	
			break;
		case "Info":
			this.do_Info();
			break;

		case "SecCallPathProfile":
			// we need to retrieve the profile name and the ID
			csviewer = true;
			do_TITLE(attributes, values);
			break;

		case "SecFlatProfile":
			csviewer = false;
			do_TITLE(attributes, values);
			break;

			// PGM elements
		case "SecFlatProfileData":
		case "CallPathProfileData":	// semi old format. some data has this kind of tag
		case "SecCallPathProfileData":
			begin_SecData(attributes, values);	break;

			// load module dictionary
		case "LoadModule":
			do_LoadModule(attributes, values);
			break;
			// file dictionary
		case "File":
			do_File(attributes, values); break;
			
			// flat profiles
		case "LM":
			begin_LM (attributes, values);	break;
		case "F":
			begin_F  (attributes, values);	break;
		case "P":
		case "Pr":
		case "PF":
			begin_PF  (attributes, values);	break;
			
		case "A":
			begin_A  (attributes, values);	break;
		case "L":
			begin_L  (attributes, values);	break;
		case "S":
			begin_S  (attributes, values);	break;

			// callstack elements
		case "C":
			begin_CALLSITE(attributes,values); 
			break;
			
		case "Procedure":
			do_Procedure(attributes, values); break;


			// trace database
		case "TraceDBTable":
			this.begin_TraceDBTable(attributes, values);
			break;
			
		case "TraceDB":
			this.do_TraceDB(attributes, values); break;

		// ---------------------
		// XML v. 3.0
		// ---------------------
		case "SummaryDBFile":
			do_DBFile(BaseExperiment.Db_File_Type.DB_SUMMARY, attributes, values);
			break;
			
		case "TraceDBFile":
			do_DBFile(BaseExperiment.Db_File_Type.DB_TRACE, attributes, values);
			break;
			
		case "PlotDBFile":
			do_DBFile(BaseExperiment.Db_File_Type.DB_PLOT, attributes, values);
			break;
			
		case "ThreadIDFile":
			do_DBFile(BaseExperiment.Db_File_Type.DB_THREADS, attributes, values);
			break;

		// ---------------------
		// XML v. 4.0
		// ---------------------
		case "IdentifierNameTable":
			break;
			
			// ---------------------
		case "Identifier":
			do_identifier(attributes, values);
			break;
			
		// ---------------------
		// old token from old XML
		// ---------------------
		case "CSPROFILE":
		case "HPCVIEWER":
			throw new java.lang.RuntimeException(new OldXMLFormatException());
			// unknown elements
			
		// ---------------------
		// Tokens to be ignored 
		// ---------------------
			
		case "ProcedureTable":
		case "FileTable":
		case "LoadModuleTable":
		case "SecHeader":
			break;
		
		default:
			break;
		} 
	}


	
	/*************************************************************************
	 *	Takes notice of the ending of an element.
	 ************************************************************************/
	public void endElement(String element)
	{
		//TokenXML current = Token2.map(element);
		switch(element)
		{
		case "SecFlatProfile":
		case "SecCallPathProfile":
			break;

		// Data elements
		case "CallPathProfileData":	// @deprecated: semi old format. some data has this kind of tag
		case "SecFlatProfileData":
		case "SecCallPathProfileData":
			end_PGM();
			break;
		case "LM":
			end_LM();
			break;
			
		case "P":
		case "Pr":
		case "PF":
			end_PF();
			break;
		case "A":
			end_A();
			break;
		case "L":
			end_L();
			break;
		case "S":
			end_S();
			break;
		case "C": 		
			end_CALLSITE();
			break;
		case "F":
			end_F();
			break;
			
		case "TraceDBTable":
			end_TraceDBTable();
			break;


		// ---------------------
		// XML v. 4.0
		// ---------------------
		case "IdentifierNameTable":
			endIdentifierNameTable();
			break;

			// ignored elements
			/*
		case "ProcedureTable":
			break;
			// trace database
		case "TraceDB":
		case "MetricDB":
		case "M":
		case "HPCToolkitExperiment":
		case "NV":
		case "Header":
		case "Info":
		case "MetricFormula":
		case "SecHeader":
		case "Metric":
		case "Procedure":
		case "FileTable":
		case "File":
		case "LoadModuleTable":
		case "LoadModule":
			 */
		default:
			break;
		} 
	}


//	------------------------------- BUILDING		---------------------------//

	/*************************************************************************
	 * Process a HPCToolkitExperiment
	 *************************************************************************/
	private void do_HPCTOOLKIT(String[] attributes, String[] values) {
		String version = null;
		for (int i=0; i<attributes.length; i++) {
			if (attributes[i].charAt(0) == 'v') {
				//version of the database
				version = values[i];
			}
		}
		this.experiment.setVersion(version);
	}

	/*************************************************************************
	 * Process a Database file name
	 *************************************************************************/
	private void do_DBFile(BaseExperiment.Db_File_Type db_type, String []attributes, String []values) {
		for (int i=0; i<attributes.length; i++) {
			if (attributes[i].charAt(0) == 'n') {
				experiment.setDBFilename(db_type, values[i]);
				return;
			}
		}
	}
	
	/*************************************************************************
	 *      Processes a TARGET element as TITLE.
	 ************************************************************************/

	private void do_Header(String[] attributes, String[] values)
	{
		this.configuration.setName(ExperimentConfiguration.NAME_EXPERIMENT, values[0]);
	}

	private void do_Info() {
		//this.elemInfoState = this.previousToken;
	}

	/*************************************************************************
	 *	Processes a TITLE element.
	 ************************************************************************/

	private void do_TITLE(String[] attributes, String[] values)
	{
		// <!ATTLIST SecCallPathProfile
		//        i CDATA #REQUIRED
		//        n CDATA #REQUIRED>
		this.Assert(attributes.length == 2);
		String sTitle = "";
		if(values.length == 2) {
			sTitle = values[1];
		}
		this.configuration.setName(ExperimentConfiguration.NAME_EXPERIMENT, sTitle);
	}

	/************************************************************************
	 * <!ELEMENT LoadModule (Info?)>
    <!ATTLIST LoadModule
              i CDATA #REQUIRED
              n CDATA #REQUIRED>
	 * Example:
	 *   <LoadModule i="43497" n="/lib64/libc-2.7.so"/>
	 * @param attributes
	 * @param values
	 ************************************************************************/
	private void do_LoadModule(String[] attributes, String[] values) {
		if(values.length < 2)
			return;
		
		// We assume that the 1st attribute is always the ID and the 2nd attribute is the value
		String sValue = values[1];
		String sID = values[0];
		try {
			Integer objID = Integer.valueOf(sID);
			LoadModuleScope lmScope = new LoadModuleScope(viewRootScope, sValue, null, objID.intValue());
			hashLoadModuleTable.put(objID, lmScope);
		} catch (java.lang.NumberFormatException e) {
			System.err.println("Incorrect load module ID: "+sID);
		} catch (java.lang.NullPointerException e) {
			System.err.println("load module table is empty. Key: "+sID+" value: "+sValue);
		}
	}



	/*************************************************************************
	 *      Processes a FILE.
	 *          <!ELEMENT File (Info?)>
    <!ATTLIST File
              i CDATA #REQUIRED
              n CDATA #REQUIRED>
	 ************************************************************************/
	private void do_File(String[] attributes, String[] values) {
		if(values.length < 2)
			return;
		String sID = values[0];		
		try {
			final Integer objFileID = Integer.parseInt(sID);
			// just in case if there is a duplicate key in the dictionary, we need to make a test
			final SourceFile sourceFile = this.getOrCreateSourceFile(values[1], objFileID.intValue());
			
			this.hashSourceFileTable.put(objFileID, sourceFile);
			
		} catch (Exception e) {
			
		}
	}

	
	/*************************************************************************
	 *	Begins processing an LM (load module) element.
	 *	<!ATTLIST LM
                i CDATA #IMPLIED
                n CDATA #REQUIRED
                v CDATA #IMPLIED>
	 ************************************************************************/
	private void begin_LM(String[] attributes, String[] values)
	{
		// LM n="load module name"
		String name = getAttributeByName(ATTRIBUTE_NAME, attributes, values);
		String sIndex = getAttributeByName(ATTRIBUTE_ID, attributes, values);
		
		try {
			Integer objIndex = Integer.parseInt(sIndex);
			SourceFile sourceFile = this.getOrCreateSourceFile(name, objIndex.intValue());
			Scope lmScope = new LoadModuleScope(rootStack.peek(), name, sourceFile, objIndex.intValue());
			// make a new load module scope object
			this.beginScope(lmScope);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/*************************************************************************
	 *	Begins processing an F (file) element.
	 *      <!ATTLIST F
                i CDATA #IMPLIED
                n CDATA #REQUIRED>
	 ************************************************************************/
	private void begin_F(String[] attributes, String[] values)
	{
		// F n="filename"
		String inode = getAttributeByName(ATTRIBUTE_ID, attributes, values);
		try {
			Integer objFileKey = Integer.parseInt(inode);
			// make a new file scope object
			SourceFile sourceFile  = this.getOrCreateSourceFile(getAttributeByName(ATTRIBUTE_NAME, attributes, values), 
					objFileKey.intValue());

			this.srcFileStack.push(sourceFile);
			Scope fileScope = new FileScope(rootStack.peek(), sourceFile, objFileKey.intValue());

			this.beginScope(fileScope);

		} catch (NumberFormatException e) {
			e.printStackTrace();
		}

	}


	/*************************************************************************
	 *	Begins processing a PF (procedure frame) element.
	 *       <!ATTLIST Pr
                i  CDATA #IMPLIED
                s  CDATA #IMPLIED
                n  CDATA #REQUIRED
                lm CDATA #IMPLIED
                f  CDATA #IMPLIED
                l  CDATA #IMPLIED
                a  (1|0) "0"
                v  CDATA #IMPLIED>
	 ************************************************************************/
	private void begin_PF(String[] attributes, String[] values)
	{
			boolean istext  = true; 
			boolean isalien = false; 
			boolean new_cct_format = false;
			
			int cct_id  = 0, flat_id = 0;
			int firstLn = 0, lastLn  = 0;
			
			SourceFile srcFile 			  = null; // file location of this procedure
			LoadModuleScope objLoadModule = null;
			String procName 		  = null;

			for(int i=0; i<attributes.length; i++) {
				if (attributes[i].equals("s")) { 
					// new database format: s is the flat ID of the procedure
					procName = this.getProcedureName(values[i]);
					flat_id = Integer.parseInt(values[i]);
					if (!new_cct_format)
						// old format: cct ID = flat ID
						cct_id = flat_id;
					
				} else if (attributes[i].equals(ATTRIBUTE_ID)) {
					// id of the proc frame. needs to cross ref
					cct_id = Integer.parseInt(values[i]); 
					new_cct_format = true;
					
				} else if(attributes[i].equals(ATTRIBUTE_FILENAME)) {
					// file
					istext = true;
					try {
						Integer indexFile = Integer.parseInt(values[i]);
						srcFile = this.hashSourceFileTable.get(indexFile);
					} catch (java.lang.NumberFormatException e) {
						// in this case, either the value of "f" is invalid or it is the name of the file
						// In some old format the attribute f contains the file not in the dictionary. So 
						// 	we need to create it from here
						if (this.srcFileStack.size()==1) {
							// the first stack is null, so let start from number 1
							srcFile = this.getOrCreateSourceFile(values[i], this.srcFileStack.size()+1);
						}
					}
					
				} else if(attributes[i].equals("lm")) { 
					// load module
					try {
						// let see if the value of ln is an ID or a simple load module name
						Integer indexFile = Integer.parseInt(values[i]);
						// look at the dictionary for the name of the load module
						objLoadModule = this.hashLoadModuleTable.get(indexFile);
						if (objLoadModule == null) {
							// old database
							objLoadModule = new LoadModuleScope(rootStack.peek(), values[i], null, indexFile.intValue());
							this.hashLoadModuleTable.put(indexFile, objLoadModule);
						}
					} catch (java.lang.NumberFormatException e) {
						// old database:
						// this error means that the lm is not based on dictionary
						objLoadModule = new LoadModuleScope(rootStack.peek(), values[i], null, values[i].hashCode());
					}
				} else if (attributes[i].equals("p") ) {
					// obsolete format: p is the name of the procedure
					procName = values[i];
					
				} else if(attributes[i].equals(ATTRIBUTE_NAME)) {
					// new database format: n is the flat ID of the procedure
					procName = this.getProcedureName(values[i]);

				} else if(attributes[i].equals(ATTRIBUTE_LINE)) {
					// line number (or range)
					StatementRange objRange = new StatementRange(values[i]);
					firstLn = objRange.getFirstLine();
					lastLn = objRange.getLastLine();
				} else if(attributes[i].equals(ATTRIBUTE_TYPE)) {
					// type of the procedure frame
					// mainly used by datacentric
					
					
				} else if(attributes[i].equals(ATTRIBUTE_ALIEN)) { 
					// alien
					isalien = values[i].equals("1");
					
				} else if(attributes[i].equals(ATTRIBUTE_VALUE)) {
				}
			}
			
			if (isalien) {

				if (procName.isEmpty()) {
					// this is a line scope
					Scope scope;
					if (firstLn == lastLn)
						scope = new LineScope(rootStack.peek(), srcFile, firstLn-1, cct_id, flat_id);
					else
						scope = new StatementRangeScope(rootStack.peek(), srcFile, 
								firstLn-1, lastLn-1, cct_id, flat_id);
					scope.setCpid(0);
					scopeStack.push(scope);

					srcFile.setIsText(istext);
					this.srcFileStack.add(srcFile);
					return;
				} else {
					// this is a procedure scope uses the handling below
				}
			}

			// FLAT PROFILE: we retrieve the source file from the previous tag
			if(srcFile == null) {
					srcFile = this.srcFileStack.peek();
			} 
			 
			srcFile.setIsText(istext);
			this.srcFileStack.add(srcFile);
			
			int feature = 0;
			Integer statusProc = statusProcedureMap.get(flat_id);
			if (statusProc != null) {
				feature = statusProc.intValue();
				if (feature == ProcedureScope.FeatureRoot) {
					RootScope datacentricRoot = new RootScope(experiment, procName, RootScopeType.DatacentricTree);
					// push the new scope to the stack
					scopeStack.push(datacentricRoot);
					rootStack.push(datacentricRoot);

					experiment.setDatacentricRootScope(datacentricRoot);
					return;
				}
			}
			if (objLoadModule == null) {
				objLoadModule = LoadModuleScope.build(rootStack.peek(), "<unknown>", srcFile);
			}
			
			ProcedureScope procScope  = new ProcedureScope(rootStack.peek(), objLoadModule, srcFile, 
					firstLn-1, lastLn-1, 
					procName, isalien, cct_id, flat_id, userData, feature);

			if ( (this.scopeStack.size()>1) && ( this.scopeStack.peek() instanceof LineScope)  ) {

				LineScope ls = (LineScope)this.scopeStack.pop();
				int	callsiteID = ls.getFlatIndex();
				
				CallSiteScope csn = new CallSiteScope( ls, procScope, 
						CallSiteScopeType.CALL_TO_PROCEDURE, cct_id, callsiteID );

				// beginScope pushes csn onto the node stack and connects it with its parent
				// this is done while the ls is off the stack so the parent of csn is ls's parent. 
				// afterward, we rearrange the top of stack to tuck ls back underneath csn in case it is 
				// needed for a subsequent procedure frame that is a sibling of csn in the tree.
				this.beginScope(csn);
				ls.setParentScope(csn.getParentScope());
				CallSiteScope csn2 = (CallSiteScope) this.scopeStack.pop();
				this.scopeStack.push(ls);
				this.scopeStack.push(csn2);

				if (isalien)
					procScope.setProcedureType(ProcedureType.ProcedureInlineFunction);
				else 
					procScope.setProcedureType(ProcedureType.ProcedureNormal);
			} else {
				this.beginScope(procScope);

				// fix bug #36 (omp idle is omitted in flat view)
				// the root cause of the bug is that we do not differentiate between procedure inline macro
				//   and procedure root (such as thread root, program root and omp idle).
				// need to check if a procedure is an alien or not. Procedure root has no alien information
				
				if (isalien)
					procScope.setProcedureType(ProcedureType.ProcedureInlineMacro);
				else
					procScope.setProcedureType(ProcedureType.ProcedureRoot);
			}
	}
	

	
	/*************************************************************************
	 *	Begins processing a A (alien) element.
      <!ELEMENT A (A|L|S|C|M)*>      <!ATTLIST A
                i CDATA #IMPLIED
                f CDATA #IMPLIED
                n CDATA #IMPLIED
                l CDATA #IMPLIED
                v CDATA #IMPLIED>
	 ************************************************************************/

	private void begin_A(String[] attributes, String[] values)
	{
		String sIndex = null;
		String filenm = null;
		String procnm = null;
		String sLine = null;
		
		// make a new alien scope object
		for (int i=0; i<attributes.length; i++) {
			if (attributes[i].equals(ATTRIBUTE_ID)) {
				sIndex = values[i];
			} else if (attributes[i].equals(ATTRIBUTE_FILENAME)) {
				filenm = values[i];
			} else if (attributes[i].equals(ATTRIBUTE_NAME)) {
				procnm = values[i];
			} else if (attributes[i].equals(ATTRIBUTE_LINE)) {
				sLine = values[i];
			}
		}
		
		try {
			Integer objIndex = Integer.parseInt(sIndex);

			int firstLn, lastLn;
			StatementRange objRange = new StatementRange(sLine);
			firstLn = objRange.getFirstLine();
			lastLn = objRange.getLastLine();

			SourceFile sourceFile = this.getOrCreateSourceFile(filenm, objIndex.intValue());
			this.srcFileStack.push(sourceFile);

			Scope alienScope = new AlienScope(rootStack.peek(), sourceFile, filenm, procnm, firstLn-1, lastLn-1, objIndex.intValue());

			this.beginScope(alienScope);

		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		
	}

	
	/*************************************************************************
	 * Handling the {@code Procedure} tag in the procedure dictionary table
	 * <pre>
              i CDATA #REQUIRED
              n CDATA #REQUIRED
              f CDATA> 
       </pre>
       The value of f is determined by hpctoolkit/src/lib/prof/NameMapping.cpp :
       <pre>
       	const int  TYPE_NORMAL_PROC  = 0;  // nothing special. Default value
		const int  TYPE_PLACEHOLDER  = 1;  // the proc is just a a place holder
		const int  TYPE_ROOT         = 2;  // the proc is a root tyoe, shown in a separate view in hpcviewer
		const int  TYPE_ELIDED       = 3;  // the proc shouldn't be shown in hpcviewer at all
		const int  TYPE_TOPDOWN_PLACEHOLDER  = 4;  // special place holder for top-down tree only.
		</pre>
	 * 
	 * @param attributes
	 * @param values
	 * 
	 *************************************************************************/
	private void do_Procedure(String[] attributes, String[] values) {
		if(values.length < 2)
			return;
		
		String sID   = null;
		String sData = null;
		
		Integer statusProc = null;

		for (int i=0; i<attributes.length; i++)
		{
			if (attributes[i].equals("i")) {
				sID = values[i];
			} else if (attributes[i].equals("n")) {
				sData = values[i];
			} else if (attributes[i].equals("f")) {
				statusProc  = Integer.parseInt(values[i]);
			}			
		}
		try {
			Integer objID = Integer.parseInt(sID);
			this.hashProcedureTable.put(objID, sData);
			
			if (statusProc != null)
				this.statusProcedureMap.put(objID, statusProc);
			
		} catch (java.lang.NumberFormatException e) {
			e.printStackTrace();
		}
	}
	
	


	/*************************************************************************
	 *	Begins processing an L (loop) element.
	 *	<!ELEMENT L (A|L|S|C|M)*>
      <!ATTLIST L
                i CDATA #IMPLIED
                s CDATA #IMPLIED
                l CDATA #IMPLIED
                v CDATA #IMPLIED>
	 ************************************************************************/

	private void begin_L(String[] attributes, String[] values)
	{
		int cct_id = 0, flat_id = 0;
		int firstLn = 0;
		int lastLn = 0;
		SourceFile sourceFile = null;
		
		for(int i=0; i<attributes.length; i++) {
			if(attributes[i].equals("s")) {
				flat_id = Integer.parseInt(values[i]);
				if (cct_id == 0)
					cct_id = flat_id;
				
			} else if(attributes[i].equals(ATTRIBUTE_LINE)) {
				String sLine = values[i];
				StatementRange objRange = new StatementRange( sLine );
				firstLn = objRange.getFirstLine();
				lastLn = objRange.getLastLine();
			} else if (attributes[i].equals(ATTRIBUTE_FILENAME)) {
				String fileIdString = values[i];
				sourceFile = getSourceFile(fileIdString);
			} else if(attributes[i].equals(ATTRIBUTE_ID)) {
				cct_id = Integer.parseInt(values[i]);
			} 
		}
		
		if (sourceFile == null) {	
			sourceFile = this.srcFileStack.peek();
			if (this.csviewer) {
				// Use the source file of the Procedure Frame
				// NOTE: the current scope (i.e. the parent of this
				// nascent loop scope) should be either a procedure frame
				// or a loop that recursively obtained its file from the
				// procedure frame.
				Scope frameScope = this.getCurrentScope();
				//while ( !(frameScope instanceof ProcedureScope) ) {
				//  frameScope = frameScope.getParentScope();
				//}
				sourceFile = frameScope.getSourceFile();
			}
		}
		Scope loopScope = new LoopScope(rootStack.peek(), sourceFile, firstLn-1, lastLn-1, cct_id, flat_id);

		this.beginScope(loopScope);
	}

	/*************************************************************************
	 *	Finishes processing an L (loop) element.
	 ************************************************************************/

	private void end_L()
	{
		this.endScope();
	}

	/*************************************************************************
	 *	Begins processing an LN (line) element.
	 * <!ATTLIST S
                i CDATA #IMPLIED
                s CDATA #IMPLIED
                l CDATA #IMPLIED
                v CDATA #IMPLIED>
	 ************************************************************************/
	private void begin_S(String[] attributes, String[] values)
	{
		begin_S_internal( attributes,  values, false);
	}
	
	
	private void begin_S_internal(String[] attributes, String[] values, boolean isCallSite)
	{
		int cct_id = 0, flat_id = 0;
		// make a new statement-range scope object
		int firstLn = 0;
		int cpid = -1;

		for(int i=0; i<attributes.length; i++) {
			if(attributes[i].equals(ATTRIBUTE_LINE)) {
				firstLn = Integer.parseInt(values[i]);
				
			} else if(attributes[i].equals("s"))  {
				flat_id = Integer.parseInt(values[i]);
				if (cct_id == 0)
					cct_id = flat_id;
				
			} else if(attributes[i].equals(ATTRIBUTE_ID))  {
				cct_id = Integer.parseInt(values[i]);

			} else if(attributes[i].equals("it")) { //the cpid
				cpid = Integer.parseInt(values[i]);
			}
		}

		SourceFile srcFile = this.srcFileStack.peek();
		Scope scope = new LineScope(rootStack.peek(), srcFile, firstLn-1, cct_id, flat_id);
		
		// issue #68: do not record cpid for all line statement
		// just record whenever the cpid is valid only
		if (cpid >= 0) {
			scope.setCpid(cpid);
			mapCpidToCallpath.put(cpid, new CallPath(scope, current_depth));
		}
		
		if (isCallSite) {
			scopeStack.push(scope);
		} else {
			this.beginScope(scope);
		}
	}

	/*************************************************************************
	 *	Finishes processing an S (line) element.
	 ************************************************************************/

	private void end_S()
	{
		this.endScope(); 
	}


	/*************************************************************************
	 * Begin a new CALLSITE
	 * <!ATTLIST C
                i CDATA #IMPLIED
                s CDATA #IMPLIED
                l CDATA #IMPLIED
                v CDATA #IMPLIED>
	 ************************************************************************/
	private void begin_CALLSITE(String[] attributes, String[] values) {  
		this.begin_S_internal(attributes, values, true); // orig: true  
	}



	/*************************************************************************
	 * 	end a callsite.
	 ************************************************************************/
	private void end_CALLSITE() 
	{
		end_S();
	}


	private void end_PGM()
	{
		this.endScope();
		experiment.setMaxDepth(max_depth);
		experiment.setScopeMap(mapCpidToCallpath);
	}


	


	/*************************************************************************
	 *	Finishes processing an LM (load module) element.
	 ************************************************************************/
	private void end_LM()
	{
		this.endScope();
	}



	/*************************************************************************
	 *	Finishes processing an F (file) element.
	 ************************************************************************/
	private void end_F()
	{
		this.endScope();
		this.srcFileStack.pop();
		this.Assert(srcFileStack.peek() == null); // mimic old behavior
	}


	
	
	/*************************************************************************
	 *	Finishes processing a P (procedure) element.
	 ************************************************************************/

	private void end_PF()
	{
		this.srcFileStack.pop();
		this.endScope();
	}




	/*************************************************************************
	 *	Finishes processing a A (alien) element.
	 ************************************************************************/

	private void end_A()
	{
		this.srcFileStack.pop();
		this.endScope();
	}

	
	
	//--------------------------------------------------------------------------------
	// trace database
	//--------------------------------------------------------------------------------
	private void begin_TraceDBTable(String[] attributes, String[] values) 
	{
	}

	
	private void end_TraceDBTable() 
	{
	}

	
	
	/*******
	 * handling trace db
	 * @param attributes
	 * @param values
	 */
	private void do_TraceDB(String[] attributes, String[] values)
	{
		// the database contains trace data. If the caller is hpcviewer, we don't need
		// this xml tags, and we should skip it.
		
		TraceAttribute attribute = new TraceAttribute();
		
		attribute.dbUnitTime = TraceAttribute.PER_NANO_SECOND;
		
		// tallent: Note that the DTD currently only permits one instance of <TraceDB>
		for (int i=0; i<attributes.length; i++) {
			
			if (attributes[i].charAt(0) == 'i') {
			} else if (attributes[i].charAt(0) == 'u') {
				// unit time
				try {
					attribute.dbUnitTime = Long.parseLong(values[i]);
				} catch (Exception e) {
					attribute.dbUnitTime = TraceAttribute.PER_NANO_SECOND;
				}
				
			} else if (attributes[i].equals("db-glob")) {
				attribute.dbGlob = values[i];
				
			} else if (attributes[i].equals("db-min-time")) {
				attribute.dbTimeMin = Long.parseLong(values[i]);
				
			} else if (attributes[i].equals("db-max-time")) {
				attribute.dbTimeMax = Long.parseLong(values[i]);

			} else if (attributes[i].equals("db-header-sz")) {
				attribute.dbHeaderSize = Integer.parseInt(values[i]);
			}
		}
		experiment.setTraceAttribute(attribute);
	}

	
	/****
	 * Add id label to the map
	 * @param attributes
	 * @param values
	 */
	private void do_identifier(String[] attributes, String[] values) {
		int id = 0;
		String val = null;
		for (int i=0; i<attributes.length && i<values.length; i++) {
			switch (attributes[i]) {
			case ATTRIBUTE_ID:
				id = Integer.valueOf(values[i]);
				break;
				
			case ATTRIBUTE_NAME:
				val = values[i];
			}
		}
		idTupleType.add(id, val);
	}
	
	
	private void endIdentifierNameTable() {
		experiment.setIdTupleType(idTupleType);
	}


	//===============================================
	// Utilities that may be used by children of the class
	//===============================================
	
	/*************************************************************************
	 *	Begins processing a profile data (program) element.
	 ************************************************************************/
	protected void begin_SecData(String[] attributes, String[] values) 
	{
		// make the root scope
		rootScope = new RootScope(this.experiment, "Invisible Outer Root Scope", RootScopeType.Invisible, Integer.MAX_VALUE, 1 );
		
		scopeStack.push(this.rootScope);	// don't use 'beginScope'

		final String title;
		final RootScopeType rootType;
		
		if (this.csviewer) {
			title = Experiment.TITLE_TOP_DOWN_VIEW;
			rootType = RootScopeType.CallingContextTree;
		} else {
			title = Experiment.TITLE_FLAT_VIEW;
			rootType = RootScopeType.Flat;
		}
		int cct_id = 0;
		if (experiment.getMajorVersion() <= Constants.EXPERIMENT_DENSED_VERSION)
			cct_id = 1;
		
		this.viewRootScope  = new RootScope(this.experiment, title, rootType, cct_id, 0);
		beginScope(this.viewRootScope);
	}


	/************************************************************************* 
	 * treat XML attributes like a named property list; this is an alternative to a brittle
	 * position-based approach for recognizing attributes
	 *************************************************************************/
	protected String getAttributeByName(String name, String[] attributes, String[] values)
	{
		for (int i = 0; i < attributes.length; i++) 
			if (name.equals(attributes[i])) 
				return values[i];
		return null;
	}


//  ------------------------------------------------------------------- //
//	SCOPE TREE BUILDING													//
//  ------------------------------------------------------------------- //
	
	/*************************************************************************
	 * Add child node to the parent.
	 * We'll verify if the child should be invisible or not. If the child is
	 *  invisible and we need to remove invisible node, we shouldn't add the 
	 *  child to the parent
	 *  
	 * @param parent
	 * @param child
	 *************************************************************************/
	private void addChildToParent(Scope parent, Scope child) {

		if (removeInvisibleProcedure()) {
			if (child instanceof ProcedureScope) {
				if (((ProcedureScope)child).toBeElided()) {
					// don't add to the tree if the scope has to be invisible
					return;
				}
			} else if (child instanceof CallSiteScope) {
				ProcedureScope proc = ((CallSiteScope) child).getProcedureScope();
				if (proc.toBeElided()) {
					return;
				}
			}
		}
		parent.addSubscope(child);
		child.setParent(parent);
	}
	
	/*************************************************************************
	 *	Adds a newly parsed scope to the scope tree.
	 ************************************************************************/
	private void beginScope(Scope scope)
	{
		// add to the tree if the scope is visible
		// if the scope is not visible, we don't add to the parent, and 
		//   thus make it disappear from the tree.
		
		Scope top = getCurrentScope();
		
		addChildToParent(top, scope);
		
		// push the new scope to the stack
		scopeStack.push(scope);
		
		min_cctid = Math.min(min_cctid, scope.getCCTIndex());
		max_cctid = Math.max(max_cctid, scope.getCCTIndex());
		
		if (scope instanceof RootScope) {
			rootStack.push((RootScope)scope);
		}
		if (isScopeTrace(scope)) {
			current_depth++;
			assert(current_depth >= 0);
			max_depth = Math.max(max_depth, current_depth);
		}
	}

	
	/*************************************************************************
	 *	Ends a newly parsed scope.
	 ************************************************************************/
	protected void endScope()
	{
		
		try {
			Scope scope = this.scopeStack.pop();
			
			if (isScopeTrace(scope)) {
				current_depth--;
				assert(current_depth >= 0);
			}

			if (scope instanceof RootScope) {
				rootStack.pop();
			}

		} catch (java.util.EmptyStackException e) {
			System.out.println("End of stack:"+this.parser.getLineNumber());
		}
	}
	
	/***
	 * Check if the scope is part of trace scope. 
	 * A trace scope is either a procedure or a call-site scope.
	 * 
	 * @param scope
	 * @return boolean true if it's a trace scope
	 */
	private boolean isScopeTrace(Scope scope) {
		return (scope instanceof ProcedureScope || 
				scope instanceof CallSiteScope);
	}

	/*************************************************************************
	 *	Returns the current scope.
	 ************************************************************************/
	protected Scope getCurrentScope()
	{
		return this.scopeStack.peek();
	}

	/*************************************************************************
	 *   Get the File for a callsite 	
	 *   Using Hashtable to store the "FileSystemSourceFile" object 
	 *   for the callsite's file attribute.
	 ************************************************************************/
	protected SourceFile getOrCreateSourceFile(String fileLine, int keyFile)
	{
		SourceFile sourceFile=hashSourceFileTable.get(keyFile);
		if (sourceFile == null) {
			String filename = (fileLine.length() == 0 ? FileScope.UNKNOWN_FILE : fileLine);
			File file = new File(filename);
			sourceFile = new FileSystemSourceFile(experiment, file, keyFile);
			this.hashSourceFileTable.put(Integer.valueOf(keyFile), sourceFile);
		}  

		return sourceFile;
	}

	

	/*************************************************************************
	 *	Initializes the build process.
	 ************************************************************************/

	public void begin()
	{
		this.configuration = new ExperimentConfiguration();
	}


	/*************************************************************************
	 *	Takes notice of content characters within an element.
	 *
	 *	None of the elements in an hpcviewer experiment XML file have content
	 *	characters, so this method should never be called.
	 *
	 ************************************************************************/
	public void content(String s)
	{
		Dialogs.notCalled("ExperimentBuilder.content");
	}



	/*************************************************************************
	 *	Finalizes the build process.
	 ************************************************************************/

	public void end()
	{
		// bugs no 224: https://outreach.scidac.gov/tracker/index.php?func=detail&aid=224&group_id=22&atid=169
		try {
			// pop out root scope
			this.scopeStack.pop();
		} catch (EmptyStackException e) {
			System.err.println("ExperimentBuilder: no root scope !");
		}
		
		// check that input was properly nested
		if (!this.scopeStack.empty()) {
			Scope topScope = this.scopeStack.peek();
			System.out.println("Stack is not empty; remaining top scope = " + topScope.getName());
			this.error();
		}

		// copy parse results into configuration
		experiment.setConfiguration(this.configuration);
		experiment.setRootScope(this.rootScope);
		experiment.setMinMaxCCTID(min_cctid, max_cctid);
		
		// supply defaults for missing info
		if( this.configuration.getName(ExperimentConfiguration.NAME_EXPERIMENT) == null )
			this.configuration.setName(ExperimentConfiguration.NAME_EXPERIMENT, this.defaultName);
		
		if( this.configuration.getSearchPathCount() == 0 )
		{
			List<File> paths = new ArrayList<File>();
			paths.add(new File(""));
			paths.add(new File("src"));
			paths.add(new File("compile"));
			this.configuration.setSearchPaths(paths);
		}
		dispose();
	}

		
	//--------------------------------------------------------------------------------
	// Utilities
	//--------------------------------------------------------------------------------

	private void dispose() {		
		// empty variables to allow garbage collection to clean up
		hashLoadModuleTable.clear();
		hashProcedureTable.clear();
		hashSourceFileTable.clear();
		
		statusProcedureMap.clear();
	}
	
	private String getProcedureName(String sProcIndex) {
		String sProcName = PROCEDURE_UNKNOWN;
		boolean hashtableExist = (this.hashProcedureTable.size()>0);
		
		if(hashtableExist) {
			try {
				Integer objProcID = Integer.parseInt(sProcIndex); 
				// get the real name of the procedure from the dictionary
				return this.hashProcedureTable.get(objProcID);
				
			} catch (java.lang.NumberFormatException e) {
				System.err.println("Warning: Procedure index doesn't exist: " + sProcIndex);
			}
		} else {
			// the database of procedure doesn't exist. This can be a flat view.
			sProcName = sProcIndex;
		}
		return sProcName;
	}
	
	/**
	 * Return true if we need to remove invisible procedures from the tree
	 * 
	 * @return
	 */
	private boolean removeInvisibleProcedure() {
		return this.removeInvisibleProcedure;
	}
	
	/**
	 * set true to remove invisible procedures (like <no activity)
	 * 
	 * @param tobeRemoved boolean true if they are to be removed. 
	 *   False otherwise.
	 */
	public void setRemoveInvisibleProcedure(boolean tobeRemoved) {
		this.removeInvisibleProcedure = tobeRemoved;
	}
	
	/*************************************************************************
	 * Class to treat a string of line or range of lines into two lines: first line and last line 
	 *
	 ************************************************************************/
	static private class StatementRange {
		private int firstLn;
		private int lastLn;
		
		public StatementRange(String sLine) {
			// find the range separator
			int iSeparator = sLine.indexOf('-');
			if(iSeparator > 0) {
				// separator exist, it should be a range
				this.firstLn = Integer.parseInt( sLine.substring(0,iSeparator) );
				this.lastLn = Integer.parseInt( sLine.substring(iSeparator+1) );
			} else {
				// no separator: no range
				this.firstLn = Integer.parseInt(sLine);
				this.lastLn = this.firstLn;
			}
		}
		
		public int getFirstLine( ) { return this.firstLn; }
		public int getLastLine( ) { return this.lastLn; }
	}
}
