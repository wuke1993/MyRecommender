package org.recommender.cf;

import org.recommender.cf.similarity.CalSimilarityMatrix;
import org.recommender.preference.CalPreference;
import org.recommender.preference.PreferenceDrag;
import org.recommender.preference.PreferenceDuration;
import org.recommender.preference.PreferenceTimes;

/**
* @author : wuke
* @date   : 20170611 02:51:30
* Title   : InitCFRecommender
* Description : 
*/
public class InitCFRecommender {

	public static void main(String[] args) {
		
		// calculate preference
		PreferenceTimes.main(args);
		PreferenceDrag.main(args);
		PreferenceDuration.main(args);
		
		CalPreference.main(args);
		
		// calculate similarity
		CalSimilarityMatrix.main(args);
		
	}
}
