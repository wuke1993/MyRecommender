package org.recommender.cf.rec;

import java.util.ArrayList;
import java.util.HashMap;

import org.recommender.utility.PropertyHelper;
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
	 * @param neighbors_num
	 * @param recommendation_num
	 * @return recArr
	 */
	public static ArrayList<HashMap<Integer, Double>> genRecommendationForAll(double[][] preferenceMatrix, 
			int[][] neighborsMatrix, int neighbors_num, int recommendation_num) {
		
		ArrayList<HashMap<Integer, Double>> recArr = new ArrayList<HashMap<Integer, Double>>();
		
		int USER_NUM = Integer.parseInt(PropertyHelper.getProperty("USER_NUM"));
		for (int i = 1; i <= USER_NUM; i++) {
			recArr.add(GenRecommendations.genRecommendationForOne(preferenceMatrix, neighborsMatrix, i, neighbors_num, recommendation_num));
		}
		
		String rec_path = PropertyHelper.getProperty("REC_PATH");
		GenRecommendations.storeRecommendations(recArr, rec_path);
		
		return recArr;
	}
	
	/**
	 * user_sequence, start from 1
	 * @param preferenceMatrix
	 * @param neighborsMatrix
	 * @param user_sequence
	 * @param recommendation_num
	 * @return rec (vedio_sequence, preference)
	 */
	public static HashMap<Integer, Double> genRecommendationForOne(double[][] preferenceMatrix, int[][] neighborsMatrix, 
			int user_sequence, int neighbors_num, int recommendation_num) {
		HashMap<Integer, Double> rec = new HashMap<Integer, Double>();
		
		// neighbors
		int[] neighbors = neighborsMatrix[user_sequence - 1];
		
		// neighbors' preferences
		double[][] k_neighbors_preferences = new double[neighbors_num][];
		for (int i = 0; i < neighbors_num; i++) { // top "neighbors_num" neighbors
			k_neighbors_preferences[i] = preferenceMatrix[neighbors[i]];
		}
		
		// generate prediction preferences for the user by the preferences of his/her neighbors
		double[] own_preferences = preferenceMatrix[user_sequence - 1];
		
		HashMap<Integer, Double> candidate_rec = new HashMap<Integer, Double>();
		double tem_preferences = 0.0;
		for (int i = 0; i < own_preferences.length; i ++) {
			if (doubleEqual(own_preferences[i], 0.0)) { // items' preference is zero	
				for (int j = 0; j < k_neighbors_preferences.length; j++) {
					tem_preferences += k_neighbors_preferences[j][i];
				}
				tem_preferences /= k_neighbors_preferences.length;
				
				candidate_rec.put(i + 1, tem_preferences);
			} else { // TODO 已看过的还要不要推荐
				candidate_rec.put(i + 1, own_preferences[i]);
			}
		}
		
		// find top "recommendation_num" items which have bigger preferences.
		int index = 0;
		for (int i = 0; i < recommendation_num; i++) {
			index = GenRecommendations.findIndexOfMax(candidate_rec);
			rec.put(index, candidate_rec.get(index));
			
			candidate_rec.put(index, Double.MIN_VALUE);
		}
		
		return rec;
	}
	
	private static int findIndexOfMax(HashMap<Integer, Double> candidate_rec) {
		
		int index = 9999;
		 double max = Double.MIN_VALUE;
		for (int itme_sequence : candidate_rec.keySet()) {
			if (max < candidate_rec.get(itme_sequence)) {
				max = candidate_rec.get(itme_sequence);
				index = itme_sequence;
			}
		}
		
		return index;
	}
    
	public static boolean doubleEqual(double a, double b) {
        if (Math.abs(a- b) < 0.00000000000000001) {
            return true;
        } else {
        	return false;
        }
	}      
	
	public static void storeRecommendations(ArrayList<HashMap<Integer, Double>> recArr, String path) {
		StringBuilder sb = new StringBuilder();
		
		HashMap<Integer, Double> hm = null;
		for (int i = 0; i < recArr.size(); i++) {
			hm = recArr.get(i);
			
			for (int video_sequence : hm.keySet()) {
				sb.append(video_sequence);
				sb.append(",");
			}
			sb.append("\n");
		}
		
		StoreStringIntoFile.storeString(sb.toString(), path);
	} 
	
}