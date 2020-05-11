package edu.rice.cs.hpc.data.experiment.merge;

final class Constants {
	static final float MIN_DISTANCE_METRIC = (float) 0.15;
	
	// init value of score. 
	// score can be incremented if the confidence of similarity is high,
	// score is decreased if the confidence is lower
	static final int SCORE_INIT = 100;
	
	// ------------------------------------------------------------------------
	// weight of different parameters: metric, location, name, children, ..
	// the higher the weight, the more important in similarity comparison
	// ------------------------------------------------------------------------
	// maximum score of metric similarity
	static final int WEIGHT_METRIC = 100;
	
	// score if the two scopes are the same
	static final int WEIGHT_NAME = 30;
	
	// score for the case with the same children
	static final int WEIGHT_CHILDREN = 80;
	
	// same types (loop vs. loop, line vs. line, ...)
	static final int WEIGHT_TYPE = 20;
	
	// weight (the importance) of the distance of the location
	// for some application, the distance is not important (due to inlining)
	static final float WEIGHT_LOCATION_COEF = (float) 0.2;
	static final int WEIGHT_LOCATION = 20;
	
	// ------------------------------------------------------------------------
	// pruning the search of descendants to avoid long useless search
	// ------------------------------------------------------------------------
	
	// Maximum length of breadth first search
	static final int MAX_LEN_BFS = 4;
	
	// maximum length of depth first search
	static final int MAX_LEN_DFS = 4;
	
	// ------------------------------------------------------------------------
	// minimum score for confidently the same or similar node
	// ------------------------------------------------------------------------
	
	static final int SCORE_SAME = 260;
	
	static final int SCORE_SIMILAR = 200;
}
