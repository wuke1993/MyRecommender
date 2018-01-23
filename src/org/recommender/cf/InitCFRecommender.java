package org.recommender.cf;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import org.recommender.cf.neighborhood.CalNeighbors;
import org.recommender.cf.preference.CalPreference;
import org.recommender.cf.preference.GenPreferenceMatrix;
import org.recommender.cf.preference.PreferencePauseDrag;
import org.recommender.cf.preference.PreferenceDuration;
import org.recommender.cf.preference.PreferenceTimes;
import org.recommender.cf.preference.VideoSequenceDur;
import org.recommender.cf.similarity.CalSimilarityMatrix;
import org.recommender.data.LearningLog;
import org.recommender.data.TrainingSetTestSet;
import org.recommender.measuring.Measuring;
import org.recommender.utility.PropertyHelper;
import org.recommender.utility.MySQLHelper;

/**
* @author : wuke
* @date   : 20170611 02:51:30
* Title   : InitCFRecommender
* Description : 
*/
public class InitCFRecommender {

	public static void main(String[] args) {
		Connection conn = MySQLHelper.getConn();
		
		// 训练集 & 测试集
        List<LearningLog> logs = TrainingSetTestSet.readLogs(conn);
        List<List<LearningLog>> logsAll = TrainingSetTestSet.genTrainingSetTestSet(logs); // 5份
        
        List<LearningLog> trainSet = logsAll.get(0);
        trainSet.addAll(logsAll.get(1));trainSet.addAll(logsAll.get(2));trainSet.addAll(logsAll.get(3));
        List<LearningLog> testSet = logsAll.get(4);
        
        /*InitCFRecommender.initPreference(conn, trainSet);*/
        
        for (int i = 20; i <= 20; i++) {
        	for (int j = 10; j <= 10; j++) {
        		InitCFRecommender.init(conn, i, j, testSet);
        		System.out.println("***********");
        	}
        	System.out.println("==========================");
        }
	}
	
	/**
	 * 第一步 评分生成
	 * 此处注意打印出的三个学生数量，取最大值，手动赋给 USER_NUM TODO 需改进
	 * @param conn
	 * @param trainSet
	 */
	public static void initPreference(Connection conn, List<LearningLog> trainSet) {
        String path1 = PropertyHelper.getProperty("PREFERENCE_TIMES_DETAIL_PATH");
		String path2 = PropertyHelper.getProperty("PREFERENCE_TIMES_PATH");
		PreferenceTimes.calPreference(conn, trainSet, path1, path2);
		String path3 = PropertyHelper.getProperty("PREFERENCE_PAUSE_DRAG_DETAIL_PATH");
		String path4 = PropertyHelper.getProperty("PREFERENCE_PAUSE_DRAG_PATH");
		PreferencePauseDrag.calPreference(conn, trainSet, path3, path4);
		String path5 = PropertyHelper.getProperty("PREFERENCE_DURATION_DETAIL_PATH");
		String path6 = PropertyHelper.getProperty("PREFERENCE_DURATION_PATH");
		PreferenceDuration.calPreference(conn, trainSet, path5, path6);
		
		double coefficient_times = Double.parseDouble(PropertyHelper.getProperty("WEIGHT_TIMES"));
		double coefficient_pause_drag = Double.parseDouble(PropertyHelper.getProperty("WEIGHT_PAUSE_DRAG"));
		double coefficient_duration = Double.parseDouble(PropertyHelper.getProperty("WEIGHT_DURATION"));
		String preference_path = PropertyHelper.getProperty("PREFERENCE_PATH");
		
		CalPreference.calPreference(coefficient_times, coefficient_pause_drag, coefficient_duration, path2, path4, path6, preference_path);
	}
	
	public static void init(Connection conn, int neighbors_num, int recommendation_num, List<LearningLog> testSet) {		
		// 第二步，计算相似度，首先生成评分矩阵
		int user_num = Integer.parseInt(PropertyHelper.getProperty("USER_NUM"));
		int item_num = Integer.parseInt(PropertyHelper.getProperty("ITEM_NUM"));
		
		String preference_path = PropertyHelper.getProperty("PREFERENCE_PATH");
		double[][] preferenceMatrix = GenPreferenceMatrix.genPreferenceMatrix(preference_path, user_num, item_num);
		
		double[][] similarityMatrix = CalSimilarityMatrix.calSimilarityMatrix(preferenceMatrix, user_num);
		
		// 第三步，生成邻近用户
		// int neighbors_num = Integer.parseInt(PropertyHelper.getProperty("PARAMETER_K"));
		int[][] neighborsMatrix = CalNeighbors.calKNeighbors(user_num, neighbors_num, similarityMatrix);
		
		// 第四步，生成推荐列表
		// int recommendation_num = Integer.parseInt(PropertyHelper.getProperty("PARAMETER_N"));
		// int neighbors_num = Integer.parseInt(PropertyHelper.getProperty("PARAMETER_K"));
		
		// generate recommendations for one
		/*int user_sequence = 1; // 从 1 开始
		HashMap<Integer, Double> rec = GenRecommendations.genRecommendationForOne(preferenceMatrix, neighborsMatrix, user_sequence, neighbors_num, recommendation_num);
		
		for(Entry<Integer, Double> entry : rec.entrySet()) {
			System.out.println(entry.getKey() + " " + entry.getValue());
		}*/
		
		// generate recommendations for all
		ArrayList<HashMap<Integer, Double>> recArr = GenRecommendations.genRecommendationForAll(preferenceMatrix, neighborsMatrix, neighbors_num, recommendation_num);
		
		// store recommendations into file
		String rec_path = PropertyHelper.getProperty("REC_PATH");
		GenRecommendations.storeRecommendations(recArr, rec_path);
		
		// 第五步，算法评价
		HashMap<Long, Integer> stunos_sequences = TrainingSetTestSet.getStunoSequence(preference_path);
		HashMap<String, Integer> videos_sequences = VideoSequenceDur.readVideo(conn);
		HashMap<Integer, HashSet<Integer>> testSet_stuno_videos = TrainingSetTestSet.processTestSet(testSet, stunos_sequences, videos_sequences);
		
		Measuring.calAccuracyRate(recArr, testSet_stuno_videos);
	}
}