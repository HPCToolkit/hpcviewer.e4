/**
 * 
 */
package edu.rice.cs.hpcmetric.dialog;

// jface
import java.util.Formatter;
import java.util.FormatterClosedException;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.bindings.keys.IKeyLookup;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.LayoutConstants;
// swt
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.graphbuilder.math.Expression;
import com.graphbuilder.math.ExpressionParseException;
import com.graphbuilder.math.ExpressionTree;

// hpcviewer
import edu.rice.cs.hpcbase.map.UserInputHistory;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric.AnnotationType;
import edu.rice.cs.hpcdata.experiment.metric.DerivedMetric;
import edu.rice.cs.hpcdata.experiment.metric.ExtFuncMap;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.metric.MetricFormulaExpression;
import edu.rice.cs.hpcdata.experiment.metric.MetricType;
import edu.rice.cs.hpcdata.experiment.metric.MetricVarMap;
import edu.rice.cs.hpcdata.experiment.metric.format.IMetricValueFormat;
import edu.rice.cs.hpcdata.experiment.metric.format.MetricValuePredefinedFormat;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;


/**
 * Dialog box to enter a math formula to define a derived metric
 * 
 * <b>Warning: </b>
 * <p>
 * Due to issue in #151 (renumbering metric), we need to change the metric index used in the formula. 
 * in this class, the metric index will be converted from the original metric index in experiment.xml file,
 *  to a sequential index (or other thing).
 * </p>
 * It's much simpler to renumbering the metric index inside the derived metric window instead of in the beginning.
 * If we renumber in the beginning, we'll have headache with storing the formula as the metric index will differ from experiment.xml to the stored one.
 *
 * This may not be an issue in prof2 since we don't have metric id. Damn backward compatibility. 
 * Make my life horrible.
 */
public class ExtDerivedMetricDlg extends TitleAreaDialog {

	//------------- Constants
	private static final String FORMAT_PERCENT  = "%.2f %%";
	private static final String LABEL_SEPARATOR = " :  ";

	//------------- GUI variables
	private Combo  cbMetricName;
	private Text   txtMetricFormula;
	private Button btnPercent;
	private Text   txtFormat;
	private Button btnCustomFormat;
	private Button btnPercentFormat;
	private Button btnDefaultFormat;

	// ------------ Metric and math variables

	private final ExtFuncMap fctMap;
	private final MetricVarMap varMap;
	private final RootScope root;

	private DerivedMetric metric;
	private final Map<Integer, Integer> mapMetricNewIndex;
	private final Map<Integer, Integer> mapMetricOldIndex;

	// ------------- Others
	private static final String HISTORY_METRIC_NAME = "metric.formula";	//$NON-NLS-1$
	private static final String HISTORY_SEPARATOR = "|";

	private IMetricManager metricManager;
	private Point expression_position;

	// ------------- object for storing history of formula and metric names
	private UserInputHistory objHistoryName;

	//==========================================================
	// ---- Constructor
	//==========================================================
	/**
	 * Constructor to accept Metrics
	 * @param parentShell
	 * @param listOfMetrics
	 */
	public ExtDerivedMetricDlg(Shell parentShell, IMetricManager mm, RootScope root) {
		this(parentShell, mm, root, root);
	}

	public ExtDerivedMetricDlg(Shell parent, IMetricManager mm, RootScope root, Scope s) {
		super(parent);
		this.metricManager = mm;
		this.root   = root;
		this.fctMap = new ExtFuncMap();
		this.varMap = new MetricVarMap ( root, s, mm );
		
		var indexes = metricManager.getNonEmptyMetricIDs(s);
		mapMetricNewIndex = new HashMap<>(indexes.size());
		mapMetricOldIndex = new HashMap<>(indexes.size());
		
		int newIndex = 0;
		
		for(int index: indexes) {
			mapMetricNewIndex.put(newIndex, index);
			mapMetricOldIndex.put(index, newIndex);
			newIndex++;
		}
	}

	//==========================================================
	// ---- GUI CREATION
	//==========================================================

	/**
	 * Creates the dialog's contents
	 * 
	 * @param parent the parent composite
	 * @return Control
	 */
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);

		// Set the title: if the metric hasn't been set, its a new metric creation
		// 	otherwise we want to modify the metric
		if (metric == null)
			setTitle("Creating a derived metric");
		else
			setTitle("Updating metric '" + metric.getDisplayName()+"'");

		setMessage("A derived metric is a spreadsheet-like formula using other metrics (variables), operators, functions,\n"
					+ "and numerical constants.\n");

		return contents;
	}

	/*
	 * {@docRoot org.eclipse.jface.dialogs.TitleAreaDialog}
	 * @see {@link org.eclipse.jface.dialogs.TitleAreaDialog} 
	 */
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		Group grpBase = new Group(composite, SWT.NONE);
		grpBase.setText("Derived metric definition");

		Point ptMargin = LayoutConstants.getMargins(); 
		ptMargin.x = 5;
		ptMargin.y = 5;

		Composite expressionArea = new Composite(grpBase, SWT.NONE);
		{
			Group grpExpression = new Group(expressionArea, SWT.NONE);

			//--------------------------------------------
			// name of the metric
			//--------------------------------------------
			final Composite nameArea = new Composite(grpExpression, SWT.NONE);
			final Label lblName = new Label(nameArea, SWT.LEFT);
			lblName.setText("Name:");	// $NON-SLS

			cbMetricName = new Combo(nameArea, SWT.NONE);
			cbMetricName.setToolTipText("Type the displayed name of the derived metric");	// $NON-SLS
			objHistoryName = new UserInputHistory( ExtDerivedMetricDlg.HISTORY_METRIC_NAME );

			final List<String> nameHistory = objHistoryName.getHistory();
			for(String name: nameHistory) {
				String label = name.replace(HISTORY_SEPARATOR, LABEL_SEPARATOR);
				cbMetricName.add(label);
			}

			if (metric != null) {
				cbMetricName.setText(metric.getDisplayName());
			}
			cbMetricName.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					String select = cbMetricName.getText();
					int idxSep = select.indexOf(LABEL_SEPARATOR);
					if (idxSep > 0) {
						String formula = select.substring(idxSep + 4, select.length());
						txtMetricFormula.setText(formula);
					}
					cbMetricName.setText(select.substring(0, idxSep));
				}
			});

			List<Integer> indexes = this.metricManager.getNonEmptyMetricIDs(root);
			final String[]metricNames = new String[indexes.size()];
			final BaseMetric[]metrics = new BaseMetric[indexes.size()];

			int i=0;
			for(Integer index: indexes) {				
				BaseMetric m = metricManager.getMetric(index);
				metricNames[i] = mapMetricOldIndex.get(m.getIndex()) + LABEL_SEPARATOR + m.getDisplayName();
				metrics[i] = m;
				i++;
			}

			//--------------------------------------------
			// formula
			//--------------------------------------------	    	
			Label lblFormula = new Label(nameArea, SWT.NONE);
			lblFormula.setText("Formula: ");

			txtMetricFormula = new Text(nameArea, SWT.NONE);
			txtMetricFormula.setToolTipText("A spreadsheet-like formula using other metrics (variables), arithmetic operators, functions, and numerical constants");

			var proposals = new MetricContentProposalProvider(metrics, mapMetricOldIndex);
			KeyStroke keystroke = null;
			try {
				keystroke = KeyStroke.getInstance(IKeyLookup.CTRL_NAME + "+" + IKeyLookup.SPACE_NAME);
			} catch (ParseException e1) {
				// keystroke is not 
			}

			new ContentProposalAdapter(txtMetricFormula, new TextContentAdapter(), proposals, keystroke, new char[] {'$', '@'}) ;

			if (metric != null) {
				String formula = metric.getFormula();
				Expression exp = ExpressionTree.parse(formula);
				MetricFormulaExpression.rename(exp, mapMetricOldIndex, null);
				txtMetricFormula.setText( exp.toString() );
			}

			GridLayoutFactory.fillDefaults().numColumns(2).generateLayout(nameArea);

			Label lbl = new Label(grpExpression, SWT.WRAP);
			lbl.setText("There are two kinds of metric variables: point-wise and aggregate.  The former is like a spreadsheet cell, the "
					+ "latter is like a spreadsheet-column sum.  To form a variable, prepend '$' and '@', respectively, to a metric id.  "
					+ "For instance, the formula\n"
					+ "    (($2 - $1) * 100.0) / @1\n"
					+ "divides the scaled difference of the point-wise metrics 2 and 1 by the aggregate value of metric 1.");

			expression_position = new Point(0,0);
			txtMetricFormula.addKeyListener( new KeyAdapter(){

				public void keyReleased(KeyEvent e) {
					expression_position = txtMetricFormula.getSelection();
				}

			});

			txtMetricFormula.addMouseListener( new MouseAdapter(){
				public void mouseUp(MouseEvent e)  {
					if (txtMetricFormula.getClientArea().contains(e.x, e.y)) {
						expression_position = txtMetricFormula.getSelection();
					}
				}
			});

			GridLayoutFactory.fillDefaults().numColumns(1).generateLayout(grpExpression);

			//--------------- inserting metric
			Group grpInsertion = new Group(expressionArea, SWT.NONE);
			grpInsertion.setText("Assistance:");

			Label lblMetric = new Label(grpInsertion, SWT.NONE);
			lblMetric.setText("Metrics:");

			// combo box that lists the metrics
			final Combo cbMetric = new Combo(grpInsertion, SWT.READ_ONLY);

			cbMetric.setItems(metricNames);
			cbMetric.select(0);

			//---------------------------------------------------------------
			// button to insert the metric code into the expression field
			//---------------------------------------------------------------

			final Composite buttonArea = new Composite(grpInsertion, SWT.NONE);
			final Button btnMetric = new Button(buttonArea, SWT.PUSH);
			btnMetric.setText("Point-wise");
			btnMetric.setToolTipText("Insert the metric as point-wise variable in the formula by prepending with '$' sign");

			btnMetric.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					insertMetricToFormula("$", cbMetric.getSelectionIndex());
				}
				public void widgetDefaultSelected(SelectionEvent e) {

				}
			});

			final Button btnAggregate = new Button(buttonArea, SWT.PUSH);
			btnAggregate.setText("Aggregate");
			btnAggregate.setToolTipText("Insert the metric as aggregate variable in the formula by prepending with '@' sign");

			btnAggregate.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					insertMetricToFormula("@", cbMetric.getSelectionIndex());
				}
				public void widgetDefaultSelected(SelectionEvent e) {

				}
			});
			GridLayoutFactory.fillDefaults().numColumns(2).generateLayout(buttonArea);

			//---------------- inserting function
			Label lblFunc = new Label(grpInsertion, SWT.NONE);
			lblFunc.setText("Functions:");

			final Combo cbFunc = new Combo(grpInsertion, SWT.READ_ONLY);

			// create the list of the name of the function
			// list of the name  of the function and its arguments
			final String []arrFunctions = fctMap.getFunctionNamesWithType();
			// the list of the name of the function to be inserted
			final String []arrFuncNames = fctMap.getFunctionNames();

			// insert the name of the function into the combo box
			if(arrFunctions != null && arrFunctions.length>0) {
				cbFunc.setItems(arrFunctions);
				// by default insert the toplist function
				cbFunc.setText(arrFunctions[0]);
			}

			final Button btnFunc = new Button(grpInsertion, SWT.PUSH);
			btnFunc.setText("Insert function");
			btnFunc.addSelectionListener(new SelectionListener() {
				// action to insert the name of the function into the formula text
				public void widgetSelected(SelectionEvent e) {
					Point p = expression_position;
					String sFunc = arrFuncNames[cbFunc.getSelectionIndex()];
					StringBuilder sb = new StringBuilder( txtMetricFormula.getText() );
					int iLen = sFunc.length();
					sb.insert( p.x, sFunc );
					sb.insert( p.x + iLen, "()" );
					p.x = p.x + iLen + 1;
					p.y = p.x;
					txtMetricFormula.setText( sb.toString() );
					txtMetricFormula.setSelection( p );
				}
				public void widgetDefaultSelected(SelectionEvent e) {

				}
			});

			final Label lblOperators = new Label(grpInsertion, SWT.NONE);
			lblOperators.setText("Operators: ");
			final Label lblOpValues  = new Label(grpInsertion, SWT.NONE);
			lblOpValues.setText("( ) + - * / ^");


			// do not expand the group
			GridDataFactory.fillDefaults().grab(false, false).applyTo(grpInsertion);
			GridLayoutFactory.fillDefaults().numColumns(3).generateLayout(grpInsertion);

			GridLayoutFactory.fillDefaults().numColumns(1).generateLayout(expressionArea);
		}
		GridLayoutFactory.fillDefaults().margins(ptMargin).generateLayout(grpBase);

		//-------
		// options
		//-------
		ExpandBar barOptions = new ExpandBar(composite,SWT.V_SCROLL);
		{
			Composite cOptions = new Composite (barOptions, SWT.NONE);
			GridLayoutFactory.fillDefaults().numColumns(1).margins(ptMargin).generateLayout(cOptions);

			// percent option
			this.btnPercent = new Button(cOptions, SWT.CHECK);
			this.btnPercent.setText("Augment metric value display with a percentage relative to column total");
			this.btnPercent.setToolTipText("For each metric value, display the annotation of percentage relative to aggregate metric value");

			// format option
			//final Composite cFormat = new Composite( cOptions, SWT.NONE );
			final Composite cCustomFormat = new Composite( cOptions, SWT.NONE );

			btnDefaultFormat = new Button(cCustomFormat, SWT.RADIO);
			btnDefaultFormat.setText("Default format");
			new Label( cCustomFormat, SWT.NONE );

			btnPercentFormat = new Button(cCustomFormat, SWT.RADIO);
			btnPercentFormat.setText("Display metric value as percent");
			new Label( cCustomFormat, SWT.NONE );

			btnCustomFormat = new Button(cCustomFormat, SWT.RADIO);
			btnCustomFormat.setText("Custom format");

			txtFormat = new Text(cCustomFormat, SWT.BORDER);
			final String txtCustomFormat = "The format is based on java.util.Formatter class which is almost equivalent to C's printf format. "; 
			txtFormat.setToolTipText(txtCustomFormat);
			btnCustomFormat.addSelectionListener(new SelectionAdapter(){

				@Override
				public void widgetSelected(SelectionEvent e) {
					txtFormat.setEnabled(true); 
				}
			});

			btnPercentFormat.addSelectionListener(new SelectionAdapter(){

				@Override
				public void widgetSelected(SelectionEvent e) {
					txtFormat.setEnabled(false);
					txtFormat.setFocus();
				}
			});
			btnDefaultFormat.setSelection(true);

			// when we are provided a metric, we should select the correct option based
			// on the type of the metric. i.e., we need to make sure:
			// - percent option is checked
			// - type of metric is correctly selected

			if (metric != null) {
				boolean bPercent = metric.getAnnotationType() == BaseMetric.AnnotationType.PERCENT;
				btnPercent.setSelection( bPercent );

				IMetricValueFormat format = metric.getDisplayFormat();
				// check if the metric has a custom format. If it is the case, check if it only displays
				// percentage (with specific format) or user-customized format
				if (format instanceof MetricValuePredefinedFormat) {
					String strFormat = ((MetricValuePredefinedFormat)format ).getFormat();
					txtFormat.setText( strFormat  );

					if (strFormat.equals( FORMAT_PERCENT )) {
						btnPercentFormat.setSelection(true);						
					} else {
						btnCustomFormat.setSelection(true);
					}
					// only one option is allowed. If one is enabled, the other is disabled
					btnDefaultFormat.setSelection(false);
				}
			} else {
				// make sure to initialize the state of the text
				txtFormat.setEnabled(false); 
			}

			final Label lblCustomFormat = new Label( cOptions, SWT.NONE );
			lblCustomFormat.setText(txtCustomFormat + "\n"
					+ "Example: '%6.2f ' will display 6 digit floating-points with 2 digit precision. ");

			GridLayoutFactory.fillDefaults().numColumns(2).generateLayout(cCustomFormat);

			// item for expansion bar
			ExpandItem eiOptions = new ExpandItem(barOptions, SWT.NONE, 0);
			eiOptions.setText("Advanced options");
			eiOptions.setHeight(cOptions.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
			eiOptions.setControl(cOptions);
			eiOptions.setExpanded(true);

			barOptions.setToolTipText("Optional settings");
		}
		GridLayoutFactory.fillDefaults().numColumns(1).margins(ptMargin).generateLayout(composite);
		return composite;
	}


	/*****
	 * insert the selected metric from the combo box into the formula field
	 * 
	 * @param signToPrepend: either '$' or '@'
	 * @param selection_index
	 */
	private void insertMetricToFormula(String signToPrepend, int selection_index) {
		final String sText = txtMetricFormula.getText();
		final int iSelIndex = expression_position.x; 
		StringBuilder sBuff = new StringBuilder(sText);

		// insert the metric variable ( i.e.: $ + metric index)
		List<Integer> indexes = metricManager.getNonEmptyMetricIDs(root);
		BaseMetric m = metricManager.getMetric(indexes.get(selection_index));

		int newIndex = mapMetricOldIndex.get(m.getIndex());
		final String sMetricIndex = signToPrepend + newIndex;
		
		sBuff.insert(iSelIndex, sMetricIndex );
		txtMetricFormula.setText(sBuff.toString());

		// put cursor after the metric variable
		Point p = new Point(iSelIndex + sMetricIndex.length(), iSelIndex + sMetricIndex.length());
		txtMetricFormula.setSelection( p );
		expression_position = txtMetricFormula.getSelection();
	}
	

	/**
	 * check if the expression is correct
	 * @return boolean {@code true} if the expression is valid. false otherwise.
	 */
	private boolean checkExpression() {
		String expFormula = this.txtMetricFormula.getText();
		if(expFormula.length() == 0) {
			MessageDialog.openError(getShell(), 
					"Error: empty expression", 
					"An expression can not be empty.");
			return false;
		}
		try {
			Expression newFormula = ExpressionTree.parse(expFormula);
			MetricFormulaExpression.rename(newFormula, mapMetricNewIndex, null);
			
			return DerivedMetric.evaluateExpression(newFormula.toString(), varMap, fctMap);
		} catch (ExpressionParseException e) {
			MessageDialog.openError(getShell(), "Error: incorrect expression", e.getDescription());
		} catch (Exception e) {
			MessageDialog.openError(getShell(), "Error detected", e.getMessage());
		}
		return false;
	}

	/***
	 * verify the validity of custom format
	 * @return
	 */
	private boolean checkFormat() {
		boolean bResult = true;
		String sError = null;
		if (this.btnCustomFormat.getSelection()) {
			String sFormat = txtFormat.getText();

			// custom format if selected, cannot be null
			if (sFormat.length()==0) {
				bResult = false;
				sError = "Custom format cannot be empty.";
			}
			try {
				Formatter format = new Formatter();
				format.format(sFormat, 1.0);
				format.close();

			} catch (IllegalFormatException e) {
				sError = "Format is incorrect: " + e.getMessage();
				bResult = false;
			} catch (FormatterClosedException e) {
				bResult = false;
				sError = "Illegal format: " + e.getMessage();
			}
		}
		if (!bResult)
			MessageDialog.openError(getShell(), "Format syntax error", sError);
		return bResult;
	}



	/*****
	 * perform metric creation or update (depending whether the metric has been 
	 * 	created or not)
	 * 
	 * @return the new metric
	 */
	private DerivedMetric doAction() {

		AnnotationType annType = AnnotationType.NONE;

		// -----------------------------
		// display the percentage ?
		// -----------------------------

		if (btnPercent.getSelection()) {
			annType = AnnotationType.PERCENT;
		}

		// -----------------------------
		// create or update a metric ?
		// -----------------------------

		if (metric == null) {

			// create a new metric

			List<BaseMetric> list = metricManager.getMetricList();
			int maxIndex = 0;
			for (BaseMetric metric: list) {
				int index = metric.getIndex();
				maxIndex  = Math.max(maxIndex, index);
			}
			BaseMetric metricLast = metricManager.getMetric(maxIndex);

			String metricLastID = metricLast.getShortName();
			maxIndex = Integer.valueOf(metricLastID) + 1;
			metricLastID = String.valueOf(maxIndex);

			Expression newFormula = ExpressionTree.parse(txtMetricFormula.getText());
			MetricFormulaExpression.rename(newFormula, mapMetricNewIndex, null);

			metric = new DerivedMetric(root, metricManager, newFormula.toString(), 
					cbMetricName.getText(), metricLastID, maxIndex, 
					annType, MetricType.UNKNOWN);

		} else {
			// update the existing metric
			metric.setDisplayName( cbMetricName.getText() );
			metric.setAnnotationType(annType);
			
			Expression newFormula = ExpressionTree.parse(txtMetricFormula.getText());
			MetricFormulaExpression.rename(newFormula, mapMetricNewIndex, null);

			metric.setExpression(newFormula.toString());
		}

		// -----------------------------
		// set the displayed format (case of custom format)
		// -----------------------------

		final String sFormat = txtFormat.getText();
		IMetricValueFormat objFormat;

		if ( btnCustomFormat.getSelection() && (sFormat != null) ) {

			// user has specified specific format. Let's set it to the metric
			objFormat = new MetricValuePredefinedFormat(sFormat);
			metric.setDisplayFormat(objFormat);

		} else if (btnPercentFormat.getSelection()) {
			objFormat = new MetricValuePredefinedFormat(FORMAT_PERCENT);
			metric.setDisplayFormat(objFormat);
		}

		return metric;
	}

	//==========================================================
	// ---- PUBLIC METHODS
	//==========================================================


	/***
	 * set the default metric to modify
	 * @param metric to modify
	 */
	public void setMetric( DerivedMetric metric ) {
		this.metric = metric;
	}

	/******
	 * return the new derived or updated metric
	 * @return
	 */
	public DerivedMetric getMetric() {
		return metric;
	}


	/**
	 * Call back method when the OK button is pressed
	 */
	public void okPressed() {
		if (cbMetricName.getText().isEmpty()) {
			MessageDialog.openError(getShell(), "Error", "Metric's name cannot be empty");
			return;
		}
		if(this.checkExpression() && this.checkFormat()) {
			// save the options for further usage (required by the caller)
			doAction();

			// save user history
			objHistoryName.addLine( cbMetricName.getText() + HISTORY_SEPARATOR + txtMetricFormula.getText());

			super.okPressed();
		}
	}	


	/****************************************
	 * 
	 * Customized content proposal provider.
	 * This class proposes content assist with label:
	 * <pre>
	 *  metric_index : metric_name
	 * </pre>
	 * It will return metric_index to the control (if selected)
	 * 
	 ****************************************/
	private static class MetricContentProposalProvider implements IContentProposalProvider
	{
		private final ContentProposal []proposals;

		public MetricContentProposalProvider(BaseMetric []metrics, Map<Integer, Integer> mapMetricIndex) {
			proposals = new ContentProposal[metrics.length];
			int i=0;

			for(BaseMetric m: metrics ) {
				int newIndex = mapMetricIndex.get(m.getIndex());
				String index = String.valueOf(newIndex);
				proposals[i] = new ContentProposal(index, index + LABEL_SEPARATOR + m.getDisplayName(), m.getDescription());			
				i++;
			}
		}

		@Override
		public IContentProposal[] getProposals(String contents, int position) {
			return proposals;
		}
	}
}
