package org.recommender.cf;

import java.util.ArrayList;
import java.util.HashMap;

import org.recommender.utility.GetProperty;
import org.recommender.utility.StoreStringIntoFile;

/**
* @author : wuke
* @date   : 20170611 10:53:54
* Title   : GenRecommendations
* Description : 
*/
public class GenRecommendations {
	
	/**
	 * 
	 * @param preferenceMatrix
	 * @param neighborsMatrix
	 * @param recommendation_num
	 * @return
	 */
	public static ArrayList<HashMap<Integer, Double>> genRecommendationForAll(double[][] preferenceMatrix, 
			int[][] neighborsMatrix, int recommendation_num) {
		
		ArrayList<HashMap<Integer, Double>> recArr = new ArrayList<HashMap<Integer, Double>>();
		
		int USER_NUM = Integer.parseInt(GetProperty.getPropertyByName("USER_NUM"));
		for(int i = 1; i <= USER_NUM; i++)
			recArr.add(GenRecommendations.genRecommendationForOne(preferenceMatrix, neighborsMatrix, i, recommendation_num));
		
		return recArr;
	}
	/**
	 * 
	 * @param recommendation_num
	 * @return
	 */
	public static HashMap<Integer, Double> genRecommendationForOne(double[][] preferenceMatrix, int[][] neighborsMatrix, 
			int user_sequence, int recommendation_num) {
		HashMap<Integer, Double> rec = new HashMap<Integer, Double>();
		
		double[] own_preferences = GenRecommendations.getPreferences(preferenceMatrix, user_sequence - 1);
		
		int[] neighbors = GenRecommendations.getKNeighbors(neighborsMatrix, user_sequence);
		int neighbors_num = Integer.parseInt(GetProperty.getPropertyByName("PARAMETER_K"));
		double[][] k_neighbors_preferences = new double[neighbors_num][];
		
		for(int i = 0; i < neighbors.length; i++) {
			k_neighbors_preferences[i] = GenRecommendations.getPreferences(preferenceMatrix, neighbors[i]);
		}
		
		// generate prediction preferences for the user with the preferences of his/her neighbors
		HashMap<Integer, Double> candidate_rec = new HashMap<Integer, Double>();
		double tem_preferences = 0.0;
		for(int i = 0; i < own_preferences.length; i ++) {
			if(own_preferences[i] == 0.0) { // items' preference is zero
				for(int j = 0; j < k_neighbors_preferences.length; j++) {
					tem_preferences += k_neighbors_preferences[j][i];
				}
				tem_preferences /= k_neighbors_preferences.length;
				
				candidate_rec.put(i + 1, tem_preferences);
			}
		}
		
		// find top "recommendation_num" items which have bigger preferences.
		int index = 0;
		for(int i = 0; i < recommendation_num; i++) {
			index = GenRecommendations.findIndexOfMax(candidate_rec);
			rec.put(index, candidate_rec.get(index));
			
			candidate_rec.put(index, Double.MIN_VALUE);
		}
		
		return rec;
	}
	
	/**
	 * Get one user's K-Neighbors.
	 * @param neighbors_num
	 * @return
	 */
	private static int[] getKNeighbors(int[][] neighborsMatrix, int user_sequence) {
		
		return neighborsMatrix[user_sequence - 1];
	}
	
	/**
	 * Get one user's preferences array.
	 * @param user_sequence
	 * @return
	 */
	private static double[] getPreferences(double[][] preferenceMatrix, int user_sequence) {
		
		return preferenceMatrix[user_sequence];
	}
	
	/**
	 * 
	 * @param candidate_rec
	 * @return
	 */
	private static int findIndexOfMax(HashMap<Integer, Double> candidate_rec) {
		
		int index = 9999;
		double max = 0.0;
		for(int itme_sequence : candidate_rec.keySet()) {
			if(max < candidate_rec.get(itme_sequence)) {
				max = candidate_rec.get(itme_sequence);
				index = itme_sequence;
			}
		}
		
		return index;
	}
    
	/**
	 * 
	 * @param recArr
	 * @param path
	 */
	public static void storeRecommendations(ArrayList<HashMap<Integer, Double>> recArr, String path) {
		StringBuilder sb = new StringBuilder();
		
		HashMap<Integer, Double> hm = null;
		for(int i = 0; i < recArr.size(); i++) {
			hm = recArr.get(i);
			
			for(int video_sequence : hm.keySet()) {
				sb.append(video_sequence);
				sb.append(",");
			}
			sb.append("\n");
		}
		
		StoreStringIntoFile.storeString(sb.toString(), path);
	} 
	
}