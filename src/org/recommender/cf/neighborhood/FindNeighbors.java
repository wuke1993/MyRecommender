package org.recommender.cf.neighborhood;
/**
* @author : wuke
* @date   : 20170611 07:30:43
* Title   : FindNeighbors
* Description : 
*/
public interface FindNeighbors {
	int[] genNeighbors(double parameter, int stuno_sequence, double[] similarityArr);
}
