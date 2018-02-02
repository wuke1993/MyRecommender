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
		similarityArr[stuno_sequence - 1] = -Double.MAX_VALUE;
		
		int k = (int) parameter;
		int[] neighbors = new int[k];
		
		for(int i = 0; i < k; i++) {
			neighbors[i] = FindKNeighbors.findMaxIndex(similarityArr);
			
			similarityArr[neighbors[i]] = -Double.MAX_VALUE;
		}
		
		return neighbors;
	}

	/**
	 * 返回数组最大值的下标
	 * @param arr
	 * @return
	 */
	private static int findMaxIndex(double[] arr) {
		int index = 0;
		double max = arr[index];
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] > max) {
				index = i;
				max = arr[index];
			}
		}
		
		return index;
	}
}
