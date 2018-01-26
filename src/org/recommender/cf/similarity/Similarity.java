package org.recommender.cf.similarity;

/**
* @author : wuke
* @date   : 20170531 22:27:47
* Title   : UserSimilarity
* Description : 
*/
public interface Similarity {
	double calSimilarity(double[] listA, double[] listB);
}