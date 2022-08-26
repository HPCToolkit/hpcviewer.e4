package edu.rice.cs.hpcdata.experiment.metric;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import edu.rice.cs.hpcdata.experiment.Experiment;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric.AnnotationType;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric.VisibilityType;
import edu.rice.cs.hpcdata.experiment.metric.format.IMetricValueFormat;
import edu.rice.cs.hpcdata.experiment.metric.format.SimpleMetricValueFormat;

/*********************************************************
 * 
 * Default parser for metrics.yaml (or default.yaml).
 * 
 *
 *********************************************************/
public class MetricYamlParser 
{
	private final Experiment       experiment;
	private final List<BaseMetric> listMetrics;

	private int version;
	private List<Map<String, ?>> listInputs; 

	
	/****
	 * Directly parse the metrics yaml file during the class construction.
	 * 
	 * @param experiment
	 * @throws FileNotFoundException
	 */
	public MetricYamlParser(Experiment experiment) throws FileNotFoundException {
		this.experiment  = experiment;
		this.listMetrics = new ArrayList<>();
		
		final var dbPath = experiment.getDirectory();
		final var fname  = dbPath + File.separator + "metrics" + File.separator + "default.yaml";
		
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
	

	/***
	 * Retrieve the list of metric descriptors specified in metrics.yaml
	 * 
	 * @return {@code List} of {@code BaseMetric}
	 */
	public List<BaseMetric> getListMetrics() {
		return listMetrics;
	}

	private void parseYaml(LinkedHashMap<String, ?> data) {
		version = (int)data.get("version");				
		parseInputs(data);
	}
	
	
	private void parseInputs(LinkedHashMap<String, ?> data) {
		var inputs = data.get("inputs");
		if (!(inputs instanceof ArrayList<?>))
			return;
		
		listInputs = (List<Map<String, ?>>)inputs;
	}
	
	private void parseRoots(LinkedHashMap<String, ?> data) {
		var roots = data.get("roots");
		if (!(roots instanceof List<?>)) {
			return;
		}
		List<Map<String, ?>> listRoots = (List<Map<String, ?>>) roots;
		for(var r: listRoots) {
			var name = r.get("name");
			var desc = r.get("description");
			var variants = r.get("variants");
			
			if (!(variants instanceof LinkedHashMap<?, ?>)) 
				continue;
			
			var children = r.get("children");
			if (children instanceof List<?>) {
				List<?> listChildren = (List<?>)children;
				listChildren.forEach(child -> {
					if (child instanceof LinkedHashMap<?, ?>) {
						parseChild((LinkedHashMap<String, ?>)child);
					}
				});
			}
		}
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
	private void parseChild(LinkedHashMap<String, ?> childAttributes) {
		var name = childAttributes.get("name");
		var desc = childAttributes.get("description");
		var variants = childAttributes.get("variants");
		if (variants instanceof LinkedHashMap<?, ?>)
			parseVariants((String)name, (String)desc, (LinkedHashMap<String, ?>)variants);
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
	 */
	private void parseVariants(String name, String desc, LinkedHashMap<String, ?> variants) {
		var metrics  = experiment.getMetricList();
		var iterator = variants.entrySet().iterator();
		
		// traverse for each variant within "variants" field
		// A variant can be Sum, Min or Max
		while(iterator.hasNext()) {
			var entry   = iterator.next();
			var combine = entry.getKey();
			
			var filteredMetrics = metrics.stream()
					 .filter(m -> ((HierarchicalMetric)m).getName().equals(name))
					 .filter(m -> ((HierarchicalMetric)m).getCombineTypeLabel().equals(combine))
					 .collect(Collectors.toList());
			
			if (filteredMetrics.isEmpty()) {
				throw new IllegalStateException(name + "/" + combine + ": metric does not exist.");
			}
			var attr = entry.getValue();
			if (!(attr instanceof LinkedHashMap<?, ?>)) {
				throw new IllegalStateException(name + "/" + combine + " has illegal attribute class: " + attr.getClass());
			}
			LinkedHashMap<String, ?> mapAttributes = (LinkedHashMap<String, ?>) attr;
			var formula = mapAttributes.get("formula");
			
			if (formula instanceof LinkedHashMap<?, ?>) {
				LinkedHashMap<String, ?> mapFormula = (LinkedHashMap<String, ?>) formula;
				var formulaIterator = mapFormula.entrySet().iterator();
				while(formulaIterator.hasNext()) {
					var formulaItem = formulaIterator.next();
					var formulaType = getMetricFormulaType(formulaItem.getKey());
					
					for(var metric: filteredMetrics) {
						if (metric.getMetricType().equals(formulaType)) {
							var format = getMetricFormat(metric, mapAttributes.get("render"));			
							metric.setDisplayFormat(format);
							metric.setDescription(desc);
							break;
						}
					}
				}
			}
			listMetrics.addAll(filteredMetrics);
		}
	}
	
	private MetricType getMetricFormulaType(String formulaType) {
		if (formulaType.equals("inclusive")) {
			return MetricType.INCLUSIVE;
		} else if (formulaType.equals("exclusive")) {
			return MetricType.EXCLUSIVE;
		}
		throw new IllegalArgumentException("unknown formula type: " + formulaType);
	}
	
	private IMetricValueFormat getMetricFormat(BaseMetric metric, Object render) {
		List<String> formats;
		if (render instanceof String) {
			formats = new ArrayList<>(1);
			formats.add((String) render);
		} else if (render instanceof List<?>) {
			formats = (List<String>) render;
		} else {
			throw new IllegalArgumentException("Unknown class rendering: " + render.getClass());
		}
		IMetricValueFormat format = SimpleMetricValueFormat.getInstance();
		for(var fmt: formats) {
			if (fmt.equals("number")) {
				// nothing
			} else if (fmt.equals("percent")) {
				metric.setAnnotationType(AnnotationType.PERCENT);
			} else if (fmt.equals("hidden")) {
				metric.setDisplayed(VisibilityType.HIDE);
			}
		}

		return format;
	}


	public int getVersion() {
		return version;
	}
}
