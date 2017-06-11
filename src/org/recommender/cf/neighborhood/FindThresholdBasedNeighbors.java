package org.recommender.cf.neighborhood;
/**
* @author : wuke
* @date   : 20170611 07:52:27
* Title   : FindThresholdBasedNeighbors
* Description : Find threhold based neighbors by calling method FindKNeighbors.genNeighbors().
*/
public class FindThresholdBasedNeighbors implements FindNeighbors {

	@Override
	public int[] genNeighbors(double parameter, int stuno_sequence, double[] similarityArr) {
		double threshold = parameter;
		int size = (int) (threshold * similarityArr.length);
		
		int[] neighbors = new FindKNeighbors().genNeighbors(size, stuno_sequence, similarityArr);
		
		return neighbors;
	}
	
}
