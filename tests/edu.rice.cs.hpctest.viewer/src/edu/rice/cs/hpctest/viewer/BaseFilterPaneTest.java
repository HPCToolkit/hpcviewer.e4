package edu.rice.cs.hpctest.viewer;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.rice.cs.hpcdata.db.IdTupleType;
import edu.rice.cs.hpcfilter.AbstractFilterPane;
import edu.rice.cs.hpcfilter.BaseFilterPane;
import edu.rice.cs.hpcfilter.FilterDataItem;
import edu.rice.cs.hpcfilter.FilterInputData;
import edu.rice.cs.hpcfilter.StringFilterDataItem;

import edu.rice.cs.hpctest.viewer.util.*;

class BaseFilterPaneTest extends ViewerTestCase
{
	private Random random = new Random();
	private BaseFilterPane<String> pane;
	private FilterInputData<String> data;
	
	@BeforeEach
	@Override
	public void setUp() {
		super.setUp();
		
		List<FilterDataItem<String>> items = new ArrayList<>();
		
		for(int i=0; i<20; i++) {
			int rank = random.nextInt(10);
			int thread = random.nextInt(100);
			String label = IdTupleType.LABEL_RANK   + " " + rank + " " +
					   	   IdTupleType.LABEL_THREAD + " " + thread;
			
			FilterDataItem<String> obj = new StringFilterDataItem(label, i<6, i>3);
			items.add(obj);
		}		
		data = new FilterInputData<>(items);
		pane = new BaseFilterPane<>(shell, AbstractFilterPane.STYLE_INDEPENDENT, data) ;
	}

	
	@Test
	void testGetEventList() {
		showWindow();
		List<FilterDataItem<String>> clist = pane.getEventList(); 
		clist.stream().forEach(item -> {
			assertNotNull(item);
			assertNotNull(item.data);
		});
	}
}
