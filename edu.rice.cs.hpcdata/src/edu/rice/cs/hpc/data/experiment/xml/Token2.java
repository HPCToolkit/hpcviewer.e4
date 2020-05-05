package edu.rice.cs.hpc.data.experiment.xml;

import java.util.Map;
import java.util.HashMap;

public class Token2 {
    // to add a new token, declare a new element in the enum TokenXML
    // and add an appropriate `put' invocation in the static constructor,
    // which you may find below.  a benefit of doing it this way is that
    // if the element names happen to change, we can simply add aliases
    // for them here.  it also looks a lot cleaner with a Map than

    static public enum TokenXML {
    	T_INVALID_ELEMENT_NAME,
    	
    	T_HPCTOOLKIT_EXPERIMENT,
    	// header
    	T_HEADER, T_INFO, T_NAME_VALUE,
    	// section
    	T_SEC_CALLPATH_PROFILE, T_SEC_HEADER,
    	// data
    	T_SEC_CALLPATH_PROFILE_DATA, 
    	T_CALLPATH_PROFILE_DATA,// @deprecated
    	T_PR, T_PF, T_L, T_C, T_S,
    	// metrics
    	T_METRIC_TABLE, T_METRIC, T_METRIC_FORMULA, T_M,
    	T_METRIC_DB_TABLE, T_METRIC_DB,
		
		// trace database
        T_TRACE_DB_TABLE, T_TRACE_DB,
    	
    	// procedures
    	T_PROCEDURE_TABLE, T_PROCEDURE, 
    	// files
    	T_FILE_TABLE, T_FILE, 
    	// load modules
    	T_LOAD_MODULE_TABLE, T_LOAD_MODULE, 
    	T_SEC_FLAT_PROFILE, T_SEC_FLAT_PROFILE_DATA,
    	// flat data
    	T_LM, T_F, T_A, T_P,
    	
    	// token for old XML file
    	T_CSPROFILE, T_HPCVIEWER,
    	
    	// token for XML v. 3.0
    	T_SUMMARY_DB_FILE, T_TRACE_DB_FILE, 
    	T_PLOT_DB_FILE	 , T_THREAD_ID_FILE
    }
    
    private static Map<String, TokenXML> tokenMap;

    static {
        tokenMap = new HashMap<String, TokenXML>();

        // additional information tags
        tokenMap.put("Info", TokenXML.T_INFO);
        tokenMap.put("NV",   TokenXML.T_NAME_VALUE);
        
        // metrics
        tokenMap.put("Metric", 	 	  TokenXML.T_METRIC);
        tokenMap.put("MetricTable",   TokenXML.T_METRIC_TABLE);
        tokenMap.put("MetricFormula", TokenXML.T_METRIC_FORMULA);
        tokenMap.put("M", 			  TokenXML.T_M);
        tokenMap.put("MetricDBTable", TokenXML.T_METRIC_DB_TABLE);
        tokenMap.put("MetricDB", 	  TokenXML.T_METRIC_DB);
        
        // trace database
        tokenMap.put("TraceDBTable", TokenXML.T_TRACE_DB_TABLE);
        tokenMap.put("TraceDB", 	 TokenXML.T_TRACE_DB);

        // head of xml
        tokenMap.put("HPCToolkitExperiment", TokenXML.T_HPCTOOLKIT_EXPERIMENT);
        tokenMap.put("Header", 				 TokenXML.T_HEADER);
        tokenMap.put("SecCallPathProfile", 	 TokenXML.T_SEC_CALLPATH_PROFILE);
        tokenMap.put("SecHeader", 			 TokenXML.T_SEC_HEADER);
        
        tokenMap.put("LoadModuleTable", TokenXML.T_LOAD_MODULE_TABLE);
        tokenMap.put("LoadModule", 		TokenXML.T_LOAD_MODULE);
        tokenMap.put("FileTable", 		TokenXML.T_FILE_TABLE);
        tokenMap.put("File", 			TokenXML.T_FILE);
        tokenMap.put("ProcedureTable", 	TokenXML.T_PROCEDURE_TABLE);
        tokenMap.put("Procedure", 		TokenXML.T_PROCEDURE);
        
        tokenMap.put("SecCallPathProfileData", TokenXML.T_SEC_CALLPATH_PROFILE_DATA);
        tokenMap.put("CallPathProfileData",    TokenXML.T_CALLPATH_PROFILE_DATA);	// @deprecated
        tokenMap.put("Pr", TokenXML.T_PR);
        tokenMap.put("PF", TokenXML.T_PF);
        tokenMap.put("L",  TokenXML.T_L);
        tokenMap.put("C",  TokenXML.T_C);
        tokenMap.put("S",  TokenXML.T_S);
        
        tokenMap.put("SecFlatProfile", 	   TokenXML.T_SEC_FLAT_PROFILE);
        tokenMap.put("SecFlatProfileData", TokenXML.T_SEC_FLAT_PROFILE_DATA);
        tokenMap.put("LM", TokenXML.T_LM);
        tokenMap.put("F",  TokenXML.T_F);
        tokenMap.put("P",  TokenXML.T_P);
        tokenMap.put("A",  TokenXML.T_A);
        
        // token for old XML
        tokenMap.put("CSPROFILE", TokenXML.T_CSPROFILE);
        tokenMap.put("HPCVIEWER", TokenXML.T_HPCVIEWER);
        
        // token for XML v 3.0
        tokenMap.put("SummaryDBFile", TokenXML.T_SUMMARY_DB_FILE);
        tokenMap.put("TraceDBFile",   TokenXML.T_TRACE_DB_FILE);
        tokenMap.put("PlotDBFile",    TokenXML.T_PLOT_DB_FILE);
        tokenMap.put("ThreadIDFile",    TokenXML.T_THREAD_ID_FILE);
    }

    public static TokenXML map(String element) {
    	TokenXML objToken = tokenMap.get(element);

        if(objToken == null) {
            return TokenXML.T_INVALID_ELEMENT_NAME;
        }
        else {
            return objToken;
        }
    }
}
