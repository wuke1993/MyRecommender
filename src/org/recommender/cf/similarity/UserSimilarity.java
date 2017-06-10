package org.recommender.cf.similarity;

import java.util.List;

/**
* @author : wuke
* @date   : 20170531 22:27:47
* Title   : UserSimilarity
* Description : 
*/
public interface UserSimilarity {

	double calSimilarity(List<Double> listA, List<Double> listB);
	
}
