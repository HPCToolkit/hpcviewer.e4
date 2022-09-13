package edu.rice.cs.hpcdata.db.version4;

import java.nio.ByteBuffer;

import org.eclipse.collections.api.map.primitive.LongObjectMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.LongObjectHashMap;

import edu.rice.cs.hpcdata.experiment.scope.CallSiteScope;
import edu.rice.cs.hpcdata.experiment.scope.CallSiteScopeType;
import edu.rice.cs.hpcdata.experiment.scope.InstructionScope;
import edu.rice.cs.hpcdata.experiment.scope.LineScope;
import edu.rice.cs.hpcdata.experiment.scope.LoadModuleScope;
import edu.rice.cs.hpcdata.experiment.scope.LoopScope;
import edu.rice.cs.hpcdata.experiment.scope.ProcedureScope;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;
import edu.rice.cs.hpcdata.experiment.scope.UnknownScope;
import edu.rice.cs.hpcdata.experiment.source.SourceFile;
import edu.rice.cs.hpcdata.util.Constants;

public class ScopeContextFactory 
{
	/**
	 * Default separator between key component
	 */
	static final String SEPARATOR = ":";
	
	// --------------------------------------------------------------------
	// Constants for Parent-child relation: 
	// --------------------------------------------------------------------
	/**
	 * This context's parent is an enclosing lexical context, eg. source line within a function. 
	 * Specifically, no call occurred
	 */
	private static final int FMT_METADB_RELATION_LEXICAL_NEST = 0;
	
	/**
	 * This context's parent used a typical function call to reach this context. 
	 * The parent context is the source-level location of the call.
	 */
	@SuppressWarnings("unused")
	private static final int FMT_METADB_RELATION_CALL 		  = 1;
	
	/**
	 * This context's parent used an inlined function call (ie. the call was inlined by the compiler). 
	 * The parent context is the source-level location of the original call.
	 */
	private static final int FMT_METADB_RELATION_CALL_INLINED = 2;

	// --------------------------------------------------------------------
	// Constants for lexical type:
	// --------------------------------------------------------------------
	private static final int FMT_METADB_LEXTYPE_FUNCTION = 0;
	private static final int FMT_METADB_LEXTYPE_LOOP = 1;
	private static final int FMT_METADB_LEXTYPE_LINE = 2;
	private static final int FMT_METADB_LEXTYPE_INSTRUCTION = 3;
	
	// --------------------------------------------------------------------
	// local final variables. These variable are not mutable
	// --------------------------------------------------------------------
	private final RootScope rootCCT;
  	
	private final LongObjectMap<LoadModuleScope>    mapLoadModules;
	private final LongObjectHashMap<SourceFile>     mapFileSources;
	private final LongObjectHashMap<ProcedureScope> mapProcedures;

	private final IntIntHashMap mapHashToFlatID;

	
	// --------------------------------------------------------------------
	// local mutable variables
	// --------------------------------------------------------------------
	private int flatID;
	
	/****
	 * Constructor to initialize the parser to create a scope context.
	 * <br/>
	 * It is imperative that the class is instantiated once only to read
	 * the meta.db file.
	 * 
	 * @param mapLoadModules
	 * 			a map from a file offset to a {@code LoadModuleScope}
	 * @param mapFileSources
	 * 			a map from a file offset to a {@code SourceFile}
	 * @param mapProcedures
	 * 			a map from a file offset to a {@code ProcedureScope}
	 * @param rootCCT
	 * 			The main root
	 */
	public ScopeContextFactory(LongObjectMap<LoadModuleScope>    mapLoadModules,
							   LongObjectHashMap<SourceFile>     mapFileSources,
							   LongObjectHashMap<ProcedureScope> mapProcedures,
							   RootScope rootCCT) {
		
		this.mapLoadModules = mapLoadModules;
		this.mapFileSources = mapFileSources;
		this.mapProcedures  = mapProcedures;
		this.rootCCT        = rootCCT;
		
		mapHashToFlatID = new IntIntHashMap();

		flatID = Constants.FLAT_ID_BEGIN + 
				 mapLoadModules.size()   + 
				 mapFileSources.size()   + 
				 mapProcedures.size()    + 1;
	}

	/***
	 * Read meta.db file at the given start location, and parse a 
	 * scope context record.
	 * <br/>
	 * The caller needs to check the value of {@code szChildren}. 
	 * If the value is not zero, then the node has children.
	 * 
	 *  <pre>
00:	u64	szChildren          4.0	Total size of *pChildren, in bytes
08:	{Ctx}[...]*	pChildren   4.0	Pointer to the array of child contexts
10:	u32	ctxId               4.0	Unique identifier for this context
14:	{Flags}	flags           4.0	See below
15:	u8	relation            4.0	Relation this context has with its parent
16:	u8	lexicalType         4.0	Type of lexical context represented
17:	u8	nFlexWords          4.0	Size of flex, in u8[8] "words" (bytes / 8)
18:	u16	propagation         4.0	Bitmask for defining propagation scopes
20:	u8[8][nFlexWords] flex  4.0	Flexible data region, see below
	 * </pre>

	 * @param buffer
	 * 			The byte buffer of the meta.db file
	 * @param loc
	 * 			The start location
	 * @param parent
	 * 			The parent's scope node
	 */
	public ScopeContext parse(ByteBuffer buffer, int loc, Scope parent) {
		ScopeContext sc = new ScopeContext();
		
		sc.szChildren = buffer.getLong(loc);
		sc.pChildren  = buffer.getLong(loc + 0x08);
		sc.ctxId      = buffer.getInt (loc + 0x10);
		
		byte flags       = buffer.get(loc + 0x14);
		byte relation    = buffer.get(loc + 0x15);
		byte lexicalType = buffer.get(loc + 0x16);
		sc.nFlexWords  = buffer.get(loc + 0x17);
		sc.propagation = buffer.getShort(loc + 0x18);
		
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
			if (sc.nFlexWords < nwords + 1) 
				return sc;
			pFunction = buffer.getLong(loc + 0x20 + nwords * 8);
			nwords++;
		}
		if ((flags & 0x2) != 0) {
			if (sc.nFlexWords < nwords + 2) 
				return sc;
			pFile = buffer.getLong(loc + 0x20 + nwords * 8);
			line  = buffer.getInt( loc + 0x20 + (nwords+1) * 8) - 1;
			nwords += 2;
		}
		if ((flags & 0x4) != 0) {
			if (sc.nFlexWords < nwords + 2) 
				return sc;
			pModule = buffer.getLong(loc + 0x20 + nwords * 8);
			offset  = buffer.getLong(loc + 0x20 + (nwords+1) * 8);
		}
		var ps = mapProcedures.getIfAbsent (pFunction, ()->ProcedureScope.NONE);
		var fs = mapFileSources.getIfAbsent(pFile,     ()->SourceFile.NONE);
		var lm = mapLoadModules.getIfAbsent(pModule,   ()->LoadModuleScope.NONE);
		
		// linearize the flat id. This is not sufficient and causes collisions for large and complex source code
		// This needs to be computed more reliably.
		int flatId = getKey(parent, lm, fs, ps, line, lexicalType, relation, offset);
		
		switch(lexicalType) {
		case FMT_METADB_LEXTYPE_FUNCTION:
			sc.newScope = createLexicalFunction(parent, ps, sc.ctxId, flatId, line, relation);
			sc.newScope.setRelation(relation);
			
			if (parent instanceof LineScope) {
				var p = parent.getParentScope();
				linkParentChild(p, sc.newScope);
				parent.addScopeReduce(sc.newScope);
				return sc;
			}
			break;
		case FMT_METADB_LEXTYPE_LOOP:
			sc.newScope = new LoopScope(rootCCT, fs, line, line, sc.ctxId, flatId);
			break;
		case FMT_METADB_LEXTYPE_LINE:
			sc.newScope = new LineScope(rootCCT, fs, line, sc.ctxId, flatId);
			break;
		case FMT_METADB_LEXTYPE_INSTRUCTION:
			var instName = InstructionScope.getCanonicalName(lm, offset);
			var procFlatId = ++flatID;
			ProcedureScope procScope = new ProcedureScope(rootCCT, lm, lm.getSourceFile(), 0, 0, instName, false, sc.ctxId, procFlatId, null, ProcedureScope.FEATURE_PROCEDURE);
			sc.newScope = new InstructionScope(rootCCT, lm, procScope, offset, sc.ctxId, flatId);
			sc.newScope.setSourceFile(fs);
			break;
		default:
			sc.newScope = new UnknownScope(rootCCT, fs, flatId);
		}
		sc.newScope.setRelation(relation);
		if (parent != null)
			linkParentChild(parent, sc.newScope);	
		
		return sc;
	}
	
	
	private int getKey(Scope parent,
					   LoadModuleScope lms, 
					   SourceFile sf, 
					   ProcedureScope ps, 
					   int line, 
					   int lexicalType, 
					   int relation,
					   long offset) {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("L" + lexicalType);
		sb.append(SEPARATOR);

		// --------------------------------------------------------
		// reconstruct the normal structure of load-module and file source
		// the "normal" key should be:
		// 		<load_module, file_source, line_number>
		//
		// this should be sufficient in normal cases, but not all cases.
		// --------------------------------------------------------
		sb.append(lms.getFlatIndex());
		sb.append(SEPARATOR);

		sb.append("O" + offset);
		sb.append(SEPARATOR);
					
		sb.append(sf.getFileID());
		sb.append(SEPARATOR);		
		
		// warning: line number can be unknown if debug information
		// doesn't exist
		sb.append(line);
		
		// --------------------------------------------------------
		// sometimes, the line number information doesn't exist.
		// in this case we can't trust just "normal" structure,
		// but we need to include the procedure as well.
		// Now, the key would be:
		// 		<load_module, file_source, procedure, line_number>
		// --------------------------------------------------------
		
		if (ps != ProcedureScope.NONE) {
			sb.append(SEPARATOR);
			sb.append(ps.getFlatIndex());
		}

		// --------------------------------------------------------
		// For inlined nodes: like inline function or nested functions,
		// they are linked to the parent regardless if they are defined
		// at the same place.
		//
		// For call sites: need to add the line scope of the call as 
		//   part of the key. We want to differentiate between:
		//
		//   13: call to foo
		//   17: call to foo
		//
		// Where function foo is exactly the same for each call.
		// Without adding the line scope, we'll end up having the same
		// flat id for each calls.
		// --------------------------------------------------------

		if (relation == FMT_METADB_RELATION_LEXICAL_NEST ||
			relation == FMT_METADB_RELATION_CALL_INLINED) {
			
			sb.append(getProcedureParentFlatId(parent));
		} else if (relation == FMT_METADB_RELATION_CALL) {
			if (lexicalType == FMT_METADB_LEXTYPE_INSTRUCTION) {
				sb.append(getProcedureParentFlatId(parent));
			} else if (parent instanceof LineScope || 
					   parent instanceof InstructionScope) {
				sb.append(SEPARATOR);
				sb.append(parent.getFlatIndex());
			}
		}
		
		int hash = sb.toString().hashCode();
		
		if (mapHashToFlatID.containsKey(hash))
			return mapHashToFlatID.get(hash);
		
		flatID++;
		mapHashToFlatID.put(hash, flatID);
		
		return flatID;
	}

	private String getProcedureParentFlatId(Scope parent) {
		Scope procParent = parent;
		if (parent instanceof CallSiteScope) 
			procParent = ((CallSiteScope)parent).getProcedureScope();
		
		return SEPARATOR + procParent.getFlatIndex();
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
										int flatId,
										int line, 
										int relation) {
		
		boolean alien = relation == FMT_METADB_RELATION_CALL_INLINED;
		LineScope ls;
		
		if (parent instanceof LineScope) {
			ls = (LineScope)parent;
		} else {
			ls = new LineScope(rootCCT, ps.getSourceFile(), line, ctxId, flatId);
		}

		// Case for nested functions?
		// need to find an example from the database
		if (relation == FMT_METADB_RELATION_LEXICAL_NEST)
			return new ProcedureScope(rootCCT, 
									  ps.getLoadModule(), 
									  ps.getSourceFile(), 
									  line, 
									  line, 
									  ps.getName(), 
									  alien, 
									  ctxId, 
									  ps.getFlatIndex(), 
									  null, 
									  ProcedureScope.FEATURE_PROCEDURE);				

		ProcedureScope procScope = ps;
		if (alien) {
			// without duplicating here, there is no [i] notation in the top-down view.
			procScope = new ProcedureScope(rootCCT, 
										   ps.getLoadModule(), 
										   ps.getSourceFile(), 
										   ps.getFirstLineNumber(), 
										   ps.getLastLineNumber(), 
										   ps.getName(), 
										   true, 
										   ctxId, 
										   ps.getFlatIndex(), 
										   null, 
										   ProcedureScope.FEATURE_PROCEDURE);
		}
		ps.setAlien(alien);
		var cs = new CallSiteScope(ls, procScope, CallSiteScopeType.CALL_TO_PROCEDURE, ctxId, flatId);
		cs.setRootScope(rootCCT);
		
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
