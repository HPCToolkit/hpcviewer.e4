/**
 * 
 */
package edu.rice.cs.hpcmetric.dialog;

// jface
import java.util.Formatter;
import java.util.FormatterClosedException;
import java.util.IllegalFormatException;
import java.util.List;

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

import com.graphbuilder.math.ExpressionParseException;

// hpcviewer
import edu.rice.cs.hpcbase.map.UserInputHistory;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric;
import edu.rice.cs.hpcdata.experiment.metric.BaseMetric.AnnotationType;
import edu.rice.cs.hpcdata.experiment.metric.DerivedMetric;
import edu.rice.cs.hpcdata.experiment.metric.ExtFuncMap;
import edu.rice.cs.hpcdata.experiment.metric.IMetricManager;
import edu.rice.cs.hpcdata.experiment.metric.MetricType;
import edu.rice.cs.hpcdata.experiment.metric.MetricVarMap;
import edu.rice.cs.hpcdata.experiment.metric.format.IMetricValueFormat;
import edu.rice.cs.hpcdata.experiment.metric.format.MetricValuePredefinedFormat;
import edu.rice.cs.hpcdata.experiment.scope.RootScope;
import edu.rice.cs.hpcdata.experiment.scope.Scope;

/**
 * @author la5
 * Dialog box to enter a math formula to define a derived metric
 */
public class ExtDerivedMetricDlg extends TitleAreaDialog {

	//------------- Constants
	final private String FORMAT_PERCENT = "%.2f %%";

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
			lblName.setText("Name:");

			this.cbMetricName = new Combo(nameArea, SWT.NONE);
			this.cbMetricName.setToolTipText("Name of the derived metric");
			objHistoryName = new UserInputHistory( ExtDerivedMetricDlg.HISTORY_METRIC_NAME );

			final String labelSeparator = " :  ";
			final List<String> nameHistory = objHistoryName.getHistory();
			for(String name: nameHistory) {
				String label = name.replace(HISTORY_SEPARATOR, labelSeparator);
				cbMetricName.add(label);
			}

			if (metric != null) {
				cbMetricName.setText(metric.getDisplayName());
			}
			cbMetricName.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					String select = cbMetricName.getText();
					int idxSep = select.indexOf(labelSeparator);
					if (idxSep > 0) {
						String formula = select.substring(idxSep + 4, select.length());
						txtMetricFormula.setText(formula);
					}
				}
			});

			List<Integer> indexes = this.metricManager.getNonEmptyMetricIDs(root);
			final String[]metricNames = new String[indexes.size()];
			final BaseMetric[]metrics = new BaseMetric[indexes.size()];

			int i=0;
			for(Integer index: indexes) {				
				BaseMetric m = metricManager.getMetric(index);
				metricNames[i] = m.getShortName() + ": " + m.getDisplayName();
				metrics[i] = m;
				i++;
			}

			//--------------------------------------------
			// formula
			//--------------------------------------------	    	
			Label lblFormula = new Label(nameArea, SWT.NONE);
			lblFormula.setText("Formula: ");

			this.txtMetricFormula = new Text(nameArea, SWT.NONE);
			txtMetricFormula.setToolTipText("A spreadsheet-like formula using other metrics (variables), arithmetic operators, functions, and numerical constants");

			var proposals = new MetricContentProposalProvider(metrics);
			KeyStroke keystroke = null;
			try {
				keystroke = KeyStroke.getInstance("ctrl+space");
			} catch (ParseException e1) {
			}

			new ContentProposalAdapter(txtMetricFormula, new TextContentAdapter(), proposals, keystroke, new char[] {'$', '@'}) ;

			if (metric != null) {
				txtMetricFormula.setText( metric.getFormula() );
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
					StringBuffer sb = new StringBuffer( txtMetricFormula.getText() );
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

				public void widgetSelected(SelectionEvent e) {
					txtFormat.setEnabled(true); 
				}
			});

			btnPercentFormat.addSelectionListener(new SelectionAdapter(){

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

		GridLayoutFactory.fillDefaults().numColumns(1).margins(ptMargin).generateLayout(
				composite);
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
		StringBuffer sBuff = new StringBuffer(sText);

		// insert the metric variable ( i.e.: $ + metric index)
		List<Integer> indexes = metricManager.getNonEmptyMetricIDs(root);
		BaseMetric m = metricManager.getMetric(indexes.get(selection_index));

		final String sMetricIndex = signToPrepend + m.getShortName() ; 
		sBuff.insert(iSelIndex, sMetricIndex );
		txtMetricFormula.setText(sBuff.toString());

		// put cursor after the metric variable
		Point p = new Point(iSelIndex + sMetricIndex.length(), iSelIndex + sMetricIndex.length());
		txtMetricFormula.setSelection( p );
		expression_position = txtMetricFormula.getSelection();
	}
	

	/**
	 * check if the expression is correct
	 * @return
	 */
	private boolean checkExpression() {
		boolean bResult = false;
		String expFormula = this.txtMetricFormula.getText();
		if(expFormula.length() > 0) {
			try {
				bResult = DerivedMetric.evaluateExpression(expFormula, varMap, fctMap);
			} catch (ExpressionParseException e) {
				MessageDialog.openError(getShell(), "Error: incorrect expression", e.getDescription());
			} catch (Exception e) {
				MessageDialog.openError(getShell(), "Error detected", e.getMessage());
			}
		} else {
			MessageDialog.openError(getShell(), 
									"Error: empty expression", 
									"An expression can not be empty.");
		}
		return bResult;
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

			metric = new DerivedMetric(root, metricManager, txtMetricFormula.getText(), 
					cbMetricName.getText(), metricLastID, maxIndex, 
					annType, MetricType.UNKNOWN);

		} else {
			// update the existing metric
			metric.setDisplayName( cbMetricName.getText() );
			metric.setAnnotationType(annType);
			metric.setExpression(txtMetricFormula.getText());
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
		private final ContentProposal proposals[];

		public MetricContentProposalProvider(BaseMetric []metrics) {
			proposals = new ContentProposal[metrics.length];
			int i=0;

			for(BaseMetric m: metrics ) {
				final String index = m.getShortName();
				proposals[i] = new ContentProposal(index, index + ": " + m.getDisplayName(), m.getDescription());			
				i++;
			}
		}

		@Override
		public IContentProposal[] getProposals(String contents, int position) {
			return proposals;
			/*
			  List<IContentProposal> result = new ArrayList<>();
			  String userContent = contents.substring(1, contents.length());
			  for(ContentProposal proposal: proposals) {
				  final String label = proposal.getLabel();
				  if (userContent.length()==0 || 
						  (label.length() >= userContent.length() && 
						   label.substring(0, label.length()).equalsIgnoreCase(userContent)))
					  result.add(proposal);
			  }
			  System.out.println("c: " + contents + ", uc: " + userContent + ", p: " + result.size());
			  return result.toArray(new IContentProposal[result.size()]);
			 */
		}
	}
}
