package org.recommender.cf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import org.recommender.cf.neighborhood.CalNeighbors;
import org.recommender.cf.preference.CalPreference;
import org.recommender.cf.preference.DLCPreferenceReader;
import org.recommender.cf.preference.PreferenceDrag;
import org.recommender.cf.preference.PreferenceDuration;
import org.recommender.cf.preference.PreferenceTimes;
import org.recommender.cf.similarity.CalSimilarityMatrix;
import org.recommender.measuring.Measuring;
import org.recommender.measuring.TestDataReader;
import org.recommender.utility.GetProperty;

/**
* @author : wuke
* @date   : 20170611 02:51:30
* Title   : InitCFRecommender
* Description : 
*/
public class InitCFRecommender {

	public static void main(String[] args) {
		
		// calculate preference
		/*PreferenceTimes.main(args);
		PreferenceDrag.main(args);
		PreferenceDuration.main(args);
				
		double coefficient_times = Double.parseDouble(GetProperty.getPropertyByName("WEIGHT_TIMES"));
		double coefficient_drag = Double.parseDouble(GetProperty.getPropertyByName("WEIGHT_DRAG"));
		double coefficient_duration = Double.parseDouble(GetProperty.getPropertyByName("WEIGHT_DURATION"));
		String path1 = GetProperty.getPropertyByName("PREFERENCE_TIMES_PATH");
		String path2 = GetProperty.getPropertyByName("PREFERENCE_DRAG_PATH");
		String path3 = GetProperty.getPropertyByName("PREFERENCE_DURATION_PATH");
		String path4 = GetProperty.getPropertyByName("PREFERENCE_PATH");
		
		CalPreference calculator= new CalPreference();
		calculator.calPreference(coefficient_times, coefficient_drag, coefficient_duration, 
				path1, path2, path3, path4);*/
		
		// calculate similarity, first read users' preference matrix
		String preference_path = GetProperty.getPropertyByName("PREFERENCE_PATH");
		int user_num = Integer.parseInt(GetProperty.getPropertyByName("USER_NUM"));
		int item_num = Integer.parseInt(GetProperty.getPropertyByName("ITEM_NUM"));
		
		double[][] preferenceMatrix = new DLCPreferenceReader().readPreferenceMatrix(preference_path, user_num, item_num);
		
		CalSimilarityMatrix calSM = new CalSimilarityMatrix();
		double[][] similarityMatrix = calSM.calSimilarityMatrix(preferenceMatrix, user_num);
		
		// generate neighbors
		int parameter_k = Integer.parseInt(GetProperty.getPropertyByName("PARAMETER_K"));
		int[][] neighborsMatrix = CalNeighbors.calKNeighbors(user_num, parameter_k, similarityMatrix);
		
		// generate recommendations for one
		int recommendation_num = 25;
		/*int user_sequence = 1;
		HashMap<Integer, Double> rec = GenRecommendations.genRecommendationForOne(preferenceMatrix, neighborsMatrix, user_sequence, recommendation_num);
		
		for(Entry<Integer, Double> entry : rec.entrySet()) {
			System.out.println(entry.getKey() + " " + entry.getValue());
		}*/
		
		// generate recommendations for all
		ArrayList<HashMap<Integer, Double>> recArr = GenRecommendations.genRecommendationForAll(preferenceMatrix, neighborsMatrix, recommendation_num);
		
		// store recommendations into file
		String rec_path = GetProperty.getPropertyByName("REC_PATH");
		GenRecommendations.storeRecommendations(recArr, rec_path);
		
		// measuring, first read test data
		HashMap<Integer, HashSet<Integer>> stuno_videos = TestDataReader.readStunoVideos();
		
		Measuring.calAccuracyRate(recArr, stuno_videos);
		
	}
}
