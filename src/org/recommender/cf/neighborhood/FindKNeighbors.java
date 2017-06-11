package org.recommender.cf.neighborhood;

/**
* @author : wuke
* @date   : 20170611 07:49:43
* Title   : FindKNeighbors
* Description : 
*/
public class FindKNeighbors implements FindNeighbors {

	@Override
	public int[] genNeighbors(double parameter, int stuno_sequence, double[] similarityArr) {
		similarityArr[stuno_sequence - 1] = Double.MIN_VALUE;
		
		int k = (int) parameter;
		int[] neighbors = new int[k];
		
		for(int i = 0; i < k; i++) {
			neighbors[i] = FindKNeighbors.findMaxIndex(similarityArr);
			similarityArr[neighbors[i]] = Double.MIN_VALUE;
		}
		
		return neighbors;
	}

	/**
	 * Find the index of the max value in a array.
	 * @param arr
	 * @return
	 */
	private static int findMaxIndex(double[] arr) {

		int index = 0;
		double max = arr[index];
		for(int i = 0; i < arr.length; i++) {
			if(arr[i] > max) {
				index = i;
				max = arr[index];
			}
		}
		
		return index; // TODO
	}

}
