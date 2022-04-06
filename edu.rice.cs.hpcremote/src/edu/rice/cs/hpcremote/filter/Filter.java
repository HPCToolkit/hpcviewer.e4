package edu.rice.cs.hpcremote.filter;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.rice.cs.hpcdata.db.IdTuple;


public class Filter {

	private final List<Short> listTypes;
	private final Range []ranks;
	
	public Filter(List<Short> listTypes, String[] patterns){
		assert(listTypes.size() == patterns.length);
		
		this.listTypes = listTypes;
		ranks = new Range[patterns.length];
		for (int i=0; i<patterns.length; i++) {
			String pattern = patterns[i].trim();
			if (pattern == null || pattern.length()==0)
				pattern = "x";
			
			ranks[i] = new Range(patterns[i]);
		}
	}

	/****
	 * Check if an id tuple match the filter or not
	 * @param idTuple
	 * @return
	 */
	boolean matches (IdTuple idTuple) {
		boolean match = true;
		
		// for each kind in id tuple, we need to check if there is filter that match
		for(int i=0; i<idTuple.getLength(); i++) {
			
			for (int j=0; j<listTypes.size(); j++) {
				if (idTuple.getKind(i) == listTypes.get(j)) {
					match = match && ranks[j].matches(idTuple.getPhysicalIndex(i));
					if (!match)
						return false;
					else 
						break;
				}
			}
		}
		return match;
	}
	@Override
	public String toString() {
		return Arrays.toString(ranks);
	}
	
	//Unit test:
	public static void main(String[] args){
		String[] patterns = {"3:7:2,", ",1" ,"1::2,2:4:2", "1:100", ":100", "100:,4"};
		String[] messages = {"should match all threads of ranks 3, 5, and 7.",
				"will match thread 1 of all processes.",
				"will match 1.2, 1.4, 3.2, 3.4, 5.2 ...",
				"will match all threads of ranks 1 to 100",
				"will match all threads of ranks <=100",
		"will match thread 4 of ranks >= 100"};

		TraceName[][] tests = {
				{ new TraceName(2,1), new TraceName(3,2), new TraceName(4,7), new TraceName(10,10)},
				{ new TraceName(0,1), new TraceName(10,2), new TraceName(999,1), new TraceName(1,7)},
				{ new TraceName(5,2), new TraceName(3,4), new TraceName(1,1), new TraceName(2,2)},
				{ new TraceName(0,2), new TraceName(101,4), new TraceName(100,1), new TraceName(201,2)},
				{ new TraceName(0,2), new TraceName(101,4), new TraceName(100,1), new TraceName(201,2)},
				{ new TraceName(0,4), new TraceName(101,4), new TraceName(100,1), new TraceName(201,2)}
		};
		
		List<Short> listTypes = new ArrayList<>();
		listTypes.add((short) 1);
		listTypes.add((short) 2);
		
		for (int i = 0; i < patterns.length; i++) {
			Filter f = new Filter(listTypes, patterns);
			System.out.println(patterns[i] + " decoded to " + f.toString() + " in full form");
			System.out.println(messages[i]);
			TraceName[] theseTests = tests[i];
			for (int j = 0; j < theseTests.length; j++) {
				//System.out.println(theseTests[j] + (f.matches(theseTests[j])? ": matches" : ": does not match."));
			}
		}
	}

	public void serializeSelfToStream(DataOutputStream stream) throws IOException {
		assert(false);
		//process.serializeSelfToStream(stream);
		//thread.serializeSelfToStream(stream);
	}
}

class Range{
	private static final int END = Integer.MAX_VALUE;
	private static final int START = 0;
	final int min, max, stride;
	Range(){//Match everything
		min = START;
		max = END;
		stride = 1;
	}
	public void serializeSelfToStream(DataOutputStream stream) throws IOException {
		stream.writeInt(min);
		stream.writeInt(max);
		stream.writeInt(stride);
	}
	Range(int min, int max, int stride){
		this.min = min;
		this.max = max;
		this.stride = stride;
	}
	public Range(String string) {
		String[] pieces =string.split(":");
		//default value in case they are not specified
		int min = START;
		int max = END;
		int stride = 1;
		switch (pieces.length){//Don't break on the switch so that we can get all the pieces we need
		case 3:
			stride = specialParse(pieces[2], stride);
		case 2:
			max = specialParse(pieces[1], max);
			min = specialParse(pieces[0], min);
			break;
			//The reason for this difference is that "4" means [4,4], which is very different from "4:", which means [4, inf].
		case 1:
			max = specialParse(pieces[0], max);
			min = specialParse(pieces[0], min);
			break;
		}
		this.min = min;
		this.max = max;
		this.stride = stride;
	}
	private static int specialParse(String string, int defaultValue) {
		if (string.length()==0) return defaultValue;
		try{
			return Integer.parseInt(string.trim());
		} catch (NumberFormatException e){
			return defaultValue;
		}

	}
	boolean matches (long index){
		if (index < min) return false;
		if (index > max) return false;
		return ((index - min) % stride) == 0;
	}
	@Override
	public String toString() {
		return (min == START? "start" : min)+ ":" + (max == END ? "end" : max) + ":"
				+ stride;
	}
}

