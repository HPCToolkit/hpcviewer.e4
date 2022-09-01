package edu.rice.cs.hpcdata.db.version4;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.HierarchicalMetric;
import edu.rice.cs.hpcdata.experiment.metric.MetricType;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric.AnnotationType;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric.VisibilityType;

/*********************************************************
 * 
 * Default parser for metrics.yaml (or default.yaml).
 * <br>Only for database version 4 (sparse database).
 * Typical use of this class:<p>
 * <code>
 * var parser  = new MetricYamlParser(...); <br/>
 * var metrics = parser.getListMetrics();
 * </code>
 * </p>
 *
 *********************************************************/
public class MetricYamlParser 
{
	private final static String FILENAME_YAML = File.separator + "metrics" + File.separator + "default.yaml";
	
	private final static String FIELD_METRIC  = "metric";
	private final static String FIELD_NAME    = "name";
	private final static String FIELD_DESC	  = "description";
	private final static String FIELD_VARIANT = "variants";
	
	
	/****
	 * 
	 * Special enumeration for return code in {@code parseVariant}
	 *
	 */
	enum VariantResult {OK, OK_NEW_PARENT, ERROR}
	

	private final List<HierarchicalMetric> listRootMetrics;
	private final List<BaseMetric> listMetrics;
	private final Deque<HierarchicalMetric> stackMetrics;
	
	private final DataSummary  	   dataSummary;
	private final List<BaseMetric> metricsInMetaDB;
	
	private int version;
	private int parentIndex;
	
	private Map<Integer, BaseMetric> mapCodeToMetric; 

	
	/****
	 * Create a parser of metrics' yaml file.<br/>
	 * Use the {@link getListRootMetrics} to retrieve the top-level metrics,
	 *  or the {@link getListMetrics} to retrieve all the leaf metrics.
	 * 
	 * @param directory
	 * 			The database directory
	 * @param dataSummary
	 * 			The profile.db object
	 * @param metricsInMetaDB
	 * 			The list of metrics as specified in meta.db file
	 * 
	 * @throws FileNotFoundException
	 */
	public MetricYamlParser(String directory, DataSummary dataSummary, List<BaseMetric> metricsInMetaDB) 
			throws FileNotFoundException {
		this.dataSummary = dataSummary;
		this.listMetrics = new ArrayList<>(metricsInMetaDB.size());
		listRootMetrics  = new ArrayList<>(1);
		this.metricsInMetaDB = metricsInMetaDB;
		
		parentIndex = -1;
		stackMetrics = new ArrayDeque<>();
		
		final var fname  = directory + FILENAME_YAML;
		
		LoaderOptions loaderOption = new LoaderOptions();
		loaderOption.setMaxAliasesForCollections(1000);
		
		Yaml yaml = new Yaml(loaderOption);
		var data  = yaml.load(new FileInputStream(fname));
		if (!(data instanceof LinkedHashMap<?, ?>))
			return;
		
		// parse the configuration
		parseYaml ((LinkedHashMap<String, ?>) data);
		
		// parse the metric structure
		parseRoots((LinkedHashMap<String, ?>) data);
	}

	
	/****
	 * Return the list of root metrics (the top-level metrics)
	 * if any.
	 * 
	 * @return {@code List} of {@code HierarchicalMetric}.
	 * 			The list can't be null. If there is no root metric, 
	 * 			it returns empty list.
	 */
	public List<HierarchicalMetric> getListRootMetrics() {
		return listRootMetrics;
	}

	/***
	 * Retrieve the list of metric descriptors specified in metrics.yaml
	 * 
	 * @return {@code List} of {@code BaseMetric}.
	 * 			The list can't be null. If there is no root metric, 
	 * 			it returns empty list.
	 */
	public List<BaseMetric> getListMetrics() {
		return listMetrics;
	}

	
	/****
	 * Parse the "header" (first common level) of the yaml file,
	 * which include the version and inputs fields. <br/>
	 * Example:
	 * <pre>
version: 0
inputs: 
    . . .
	 * </pre>
	 * 
	 * @param mapFields
	 * 			The main map object
	 */
	private void parseYaml(LinkedHashMap<String, ?> mapFields) {
		version = (int)mapFields.get("version");				
		parseInputs(mapFields);
	}
	
	
	/****
	 * Parse the inputs fields, including its descendants.
	 * This will store the map between the anchor and the metric from meta.db
	 * <br/>
	 * Example:
	 * <pre>
inputs: ArrayList<E>  (id=98)	
  - &cycles-sum-x5b_0x0x5d_-execution
    metric: cycles
    scope: execution
    formula: $$
    combine: sum
  - &cycles-sum-x5b_0x0x5d_-function
    metric: cycles
    scope: function
    formula: $$
    combine: sum
	 * </pre>
	 * 
	 * @param data
	 */
	private void parseInputs(LinkedHashMap<String, ?> data) {
		var inputs = data.get("inputs");
		if (!(inputs instanceof ArrayList<?>))
			return;
		
		var metrics     = metricsInMetaDB;
		var listInputs  = (List<Map<String, ?>>)inputs;
		mapCodeToMetric = new HashMap<>(listInputs.size());
		
		for(var input: listInputs) {
			var metric  = input.get(FIELD_METRIC);
			var scope   = input.get("scope");
			var combine = (String)input.get("combine");

			final MetricType mtype = scope.equals("execution") ? MetricType.INCLUSIVE : MetricType.EXCLUSIVE;
			
			// try to find metric in meta.db that match the metric in yaml file
			// We should find a matched metric, otherwise there is something wrong
			var filteredMetrics = metrics.stream()
					 .filter(m -> ((HierarchicalMetric)m).getName().equals(metric))
					 .filter(m -> ((HierarchicalMetric)m).getCombineTypeLabel().equalsIgnoreCase(combine))
					 .filter(m -> m.getMetricType() == mtype)
					 .collect(Collectors.toList());

			if (filteredMetrics.isEmpty()) {
				// something wrong: there is no correspondent metric in meta.db
				throw new IllegalStateException(metric + "/" + combine + ": metric does not exist.");
			}
			if (filteredMetrics.size() > 1) {
				// found more than one matched metrics. that's impossible
				throw new IllegalStateException(metric + "/" + combine + ": metric has more than 1 matched.");
			}
			mapCodeToMetric.put(input.hashCode(), filteredMetrics.get(0));
		}
	}
	
	
	/*****
	 * Parsing the metric roots and their descendants.
	 * This will create a hierarchy of metrics.
	 * Example:
	 * <pre>
roots:
  - name: GPU
    description: GPU-accelerated performance metrics
    variants:
      Sum:
        render: [number, percent, colorbar]
        formula: sum
    children:
      - name: Kernel Execution
        description: Time spent running kernels on a GPU.
        variants:
          Sum:
            render: [number, percent]
            formula:
              inclusive: *GKERx20_x28_secx29_-sum-x5b_0x0x5d_-execution
              exclusive: *GKERx20_x28_secx29_-sum-x5b_0x0x5d_-function
      - name: Synchronization
        description: Time spent idle waiting for actions from the host / other GPU operations.
        variants:
          Sum:
            render: [number, percent, colorbar]
            formula: sum
        children:
          - name: GSYNC:STR (sec)
            description: "GPU synchronizations: stream"
            variants:
              Sum:
                render: [number, percent]
                formula:
                  inclusive: *GSYNCx3a_STRx20_x28_secx29_-sum-x5b_0x0x5d_-execution
                  exclusive: *GSYNCx3a_STRx20_x28_secx29_-sum-x5b_0x0x5d_-function
	 * </pre>
	 * @param data
	 * 			The map of the current metrics yaml data structure
	 */
	private void parseRoots(LinkedHashMap<String, ?> data) {
		var roots = data.get("roots");
		if (roots == null) {
			return;
		}
		List<Map<String, ?>> listRoots = (List<Map<String, ?>>) roots;
		if (roots instanceof Map<?, ?>) {
			listRoots = Arrays.asList((Map<String, ?>) roots);
		}
		
		for(var aRoot: listRoots) {
			//
			// create the root of metrics
			//
			String name = (String) aRoot.get(FIELD_NAME);
			String desc = (String) aRoot.get(FIELD_DESC);
			
			var variants = aRoot.get(FIELD_VARIANT);
			LinkedHashMap<String, Object> listMapVariants;
			
			if (variants instanceof LinkedHashMap) {
				listMapVariants = (LinkedHashMap<String, Object>) variants;
			} else {
				listMapVariants =  new LinkedHashMap<>(0);
			}
			var result = parseVariants(name, desc, listMapVariants);
			
			//
			// traverse the children of this root metric
			//
			parseMetricChildren(aRoot);
			
			if (result == VariantResult.OK_NEW_PARENT)
				stackMetrics.pop();
		}
	}
	
	
	/***
	 * Parse the render field
	 * 
	 * @param metric
	 * 			the current metric
	 * @param mapAttribute
	 * 			the current yaml map 
	 */
	private void parseRender(BaseMetric metric, Map<String, ?> mapAttribute) {
		var render = mapAttribute.get("render");
		if (render == null || metric == null)
			return;
		
		List<String> renderAttr;
		if (render instanceof List<?>) {
			renderAttr = (List<String>) render;
		} else if (render instanceof String) {
			renderAttr = new ArrayList<>(1);
			renderAttr.add((String) render);
		} else {
			throw new IllegalStateException("Invalid render class: " + render);
		}
		for(var attr: renderAttr) {
			if (attr.equalsIgnoreCase("hidden")) 
				metric.setDisplayed(VisibilityType.HIDE);
			else if (attr.equalsIgnoreCase("percent"))
				metric.setAnnotationType(AnnotationType.PERCENT);
			else if (attr.equals("colorbar"))
				// TODO: not supported at the moment
				metric.setAnnotationType(AnnotationType.PERCENT);
		}
	}
	
	
	/****
	 * Parsing the metrics
	 * 
	 * @param mapParent
	 */
	private void parseMetricChildren(Map<String, ?> mapParent) {
		var children = mapParent.get("children");
		if (children == null)
			return;
		
		List<?> listChildren;
		if (children instanceof List<?>) {
			listChildren = (List<?>)children;
		} else {
			listChildren = Arrays.asList(children);
		}
		listChildren.forEach(child -> {
			if (child instanceof LinkedHashMap<?, ?>) {
				parseMetric((LinkedHashMap<String, ?>)child);
			}
		});
	}
	
	/***
	 * Parsing a child of a root
	 * <pre>
     children:
      - name: cycles
        description: PERF_COUNT_HW_CPU_CYCLES
        variants:
          Sum:
            render: [number, percent]
            formula:
              inclusive: *cycles-sum-x5b_0x0x5d_-execution
              exclusive: *cycles-sum-x5b_0x0x5d_-function

	 * </pre>
	 * @param childAttributes
	 */
	private VariantResult parseMetric(LinkedHashMap<String, ?> childAttributes) {
		var name = childAttributes.get(FIELD_NAME);
		var desc = childAttributes.get(FIELD_DESC);
		var variants = childAttributes.get(FIELD_VARIANT);
		if (!(variants instanceof LinkedHashMap<?, ?>))
			throw new IllegalStateException("No list of children");

		var result = parseVariants((String)name, (String)desc, (LinkedHashMap<String, ?>)variants);
		
		// parsing if the children of this metric if any
		parseMetricChildren(childAttributes);
				
		if (result == VariantResult.OK_NEW_PARENT)
			stackMetrics.pop();

		return result;
	}
	
	
	/***
	 * Parsing all the variants of a child
	 * <pre>
	 variants:
          Sum:
            render: [number, percent]
            formula:
              inclusive: *cycles-sum-x5b_0x0x5d_-execution
              exclusive: *cycles-sum-x5b_0x0x5d_-function
	 </pre>
	 * @param name
	 * @param desc
	 * @param variants
	 * 
	 * @return VariantResult
	 */
	private VariantResult parseVariants(String name, String desc, LinkedHashMap<String, ?> variants) {
		var result = VariantResult.OK;
		var iterator = variants.entrySet().iterator();
		
		// traverse for each variant within "variants" field
		// A variant can be Sum, Min or Max
		while(iterator.hasNext()) {
			var entry   = iterator.next();
			var attr = entry.getValue();

			LinkedHashMap<String, ?> mapAttributes = (LinkedHashMap<String, ?>) attr;
			LinkedHashMap<String, ?> mapFormula;
			
			var formula = mapAttributes.get("formula");
			
			if (formula instanceof LinkedHashMap<?, ?>) {
				mapFormula = (LinkedHashMap<String, ?>) formula;
			} else {
				createParentMetric(name, desc);
				result = VariantResult.OK_NEW_PARENT;
				continue;
			}
			var formulaIterator = mapFormula.entrySet().iterator();
			
			while(formulaIterator.hasNext()) {
				var formulaItem = formulaIterator.next();
				var formulaType = getMetricFormulaType(formulaItem.getKey());
				
				var mapMetrics  = formulaItem.getValue();
				var metric = mapCodeToMetric.get(mapMetrics.hashCode());
				HierarchicalMetric hm;
				
				if (metric == null) {
					// no correspondent metric: this may be a new parent metric
					// or an error?
					hm = createParentMetric(name, desc);
					result = VariantResult.OK_NEW_PARENT;
					
				} else if (metric.getMetricType() == formulaType &&
						   metric instanceof HierarchicalMetric) {
					// it's a normal metric visible to user (mostly)
					hm = (HierarchicalMetric) metric;					
					hm.setDescription(desc);
					linkParentChild(hm);

					listMetrics.add(metric);
				} else {
					throw new IllegalStateException("Unknown metric: ");
				}

				parseRender(hm, mapAttributes);
			}
		}
		return result;
	}
	
	
	private void linkParentChild(HierarchicalMetric metric) {
		var parent = stackMetrics.peek();		
		metric.setParent(parent);
		
		if (parent == null)
			listRootMetrics.add(metric);
		else
			parent.addChild(metric);	

	}
	
	private HierarchicalMetric createParentMetric(String name, String desc) {
		var metric = new HierarchicalMetric(dataSummary, parentIndex, name);		
		metric.setDescription(desc);

		linkParentChild(metric);
		
		// this metric is a parent
		parentIndex--;
		stackMetrics.push(metric);

		return metric;
	}
	
	private MetricType getMetricFormulaType(String formulaType) {
		if (formulaType.equals("inclusive")) {
			return MetricType.INCLUSIVE;
		} else if (formulaType.equals("exclusive")) {
			return MetricType.EXCLUSIVE;
		}
		throw new IllegalArgumentException("unknown formula type: " + formulaType);
	}
	
	public int getVersion() {
		return version;
	}
}
