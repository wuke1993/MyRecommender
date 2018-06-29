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
import org.recommender.cf.rec.GenRecommendations;
import org.recommender.cf.similarity.CalSimilarityMatrix;
import org.recommender.cf.similarity.supplement.CoursewareSimilarity;
import org.recommender.data.LearningLog;
import org.recommender.data.TrainingSetTestSet;
import org.recommender.measuring.Measuring;
import org.recommender.utility.PropertyHelper;
import org.recommender.utility.MySQLHelper;

/**
* @author : wuke
* @date   : 20170611 02:51:30
* Title   : CFRecommender
* Description : 
*/
public class CFRecommender {
	public static double[][] PREFERENCE_MATRIX = null;
	public static int[][] NEIGHBORS_MATRIX = null;
	public static ArrayList<HashMap<Integer, Double>> REC_ARRAY = null;
	
	public static void main(String[] args) {
		Connection conn = MySQLHelper.getConn();
		
		// 训练集 & 测试集
        List<LearningLog> logs = TrainingSetTestSet.readLogs(conn);
        List<List<LearningLog>> logsAll = TrainingSetTestSet.genTrainingSetTestSet(logs); // 5份
        
        List<LearningLog> trainSet = logsAll.get(0);
        trainSet.addAll(logsAll.get(1));trainSet.addAll(logsAll.get(2));trainSet.addAll(logsAll.get(3));
        List<LearningLog> testSet = logsAll.get(4);
        
        // 分别计算三个偏好，并存储到文件中
        /*double coefficient_times = Double.parseDouble(PropertyHelper.getProperty("WEIGHT_TIMES"));
		double coefficient_pause_drag = Double.parseDouble(PropertyHelper.getProperty("WEIGHT_PAUSE_DRAG"));
		double coefficient_duration = Double.parseDouble(PropertyHelper.getProperty("WEIGHT_DURATION"));
		CFRecommender.initPreference(conn, trainSet, coefficient_times, coefficient_pause_drag, coefficient_duration); // IF-CF
		//CFRecommender.initPreference(conn, trainSet, 1, 0, 0); // Times-CF
		//CFRecommender.initPreference(conn, trainSet, 0, 0, 1); // Dur-CF*/
        
        // 学习者相似度计算
		double weight_pearson = Double.parseDouble(PropertyHelper.getProperty("WEIGHT_PEARSON"));
		double weight_courseware = Double.parseDouble(PropertyHelper.getProperty("WEIGHT_COURSEWARE"));
        CFRecommender.initPreferenceNeighborsMatrix(conn, weight_pearson, weight_courseware);
        // CFRecommender.initPreferenceNeighborsMatrix(conn, 1, 0); // 仅使用Pearson相关系数，对照组（Dur-CF&Times-CF）使用
        
        int neighbors_num = Integer.parseInt(PropertyHelper.getProperty("PARAMETER_K"));
        int recommendation_num = Integer.parseInt(PropertyHelper.getProperty("PARAMETER_N"));
        CFRecommender.genRecommendation(neighbors_num, recommendation_num);
        
        // CFRecommender.measure(conn, testSet);
	}
	
	/**
	 * 不同邻近学习者 k 和 不同推荐视频个数 n 下的推荐准确率和召回率
	 * @param conn
	 * @param testSet
	 */
	public static void measure(Connection conn, List<LearningLog> testSet) {
		for (int i = 5; i <= 5; i++) { // 邻近用户个数
        	for (int j = 2; j <= 5; j++) { // 推荐个数
        		CFRecommender.genRecommendation(i, j);
        		
        		CFRecommender.measureRec(conn, testSet);
        	}
        }
	}
	
	/**
	 * 评分矩阵生成
	 * @param conn
	 * @param trainSet
	 * @param coefficient_times
	 * @param coefficient_pause_drag
	 * @param coefficient_duration
	 */
	public static void initPreference(Connection conn, List<LearningLog> trainSet, double coefficient_times, 
			double coefficient_pause_drag, double coefficient_duration) {
        String path1 = PropertyHelper.getProperty("PREFERENCE_TIMES_DETAIL_PATH");
		String path2 = PropertyHelper.getProperty("PREFERENCE_TIMES_PATH");
		PreferenceTimes.calPreference(conn, trainSet, path1, path2);
		
		String path3 = PropertyHelper.getProperty("PREFERENCE_PAUSE_DRAG_DETAIL_PATH");
		String path4 = PropertyHelper.getProperty("PREFERENCE_PAUSE_DRAG_PATH");
		PreferencePauseDrag.calPreference(conn, trainSet, path3, path4);
		
		String path5 = PropertyHelper.getProperty("PREFERENCE_DURATION_DETAIL_PATH");
		String path6 = PropertyHelper.getProperty("PREFERENCE_DURATION_PATH");
		PreferenceDuration.calPreference(conn, trainSet, path5, path6);
		
		String preference_path = PropertyHelper.getProperty("PREFERENCE_PATH");
		
		CalPreference.calPreference(coefficient_times, coefficient_pause_drag, coefficient_duration, path2, path4, path6, preference_path);
	}
	
	/**
	 * 评分矩阵 & 邻近用户矩阵生成
	 * pearson_similarity + coursewareTimes_similarity
	 */
	public static void initPreferenceNeighborsMatrix(Connection conn, double weight_pearson, double weight_courseware) {
		// 生成评分矩阵
		int user_num = Integer.parseInt(PropertyHelper.getProperty("USER_NUM"));
		int item_num = Integer.parseInt(PropertyHelper.getProperty("ITEM_NUM"));
		
		String preference_path = PropertyHelper.getProperty("PREFERENCE_PATH");
		CFRecommender.PREFERENCE_MATRIX = GenPreferenceMatrix.genPreferenceMatrix(preference_path, user_num, item_num);
		/* 弃用
		 * String rec_path = PropertyHelper.getProperty("REC_PATH");
		CFRecommender.PREFERENCE_MATRIX = GenPreferenceMatrix.genPreferenceMatrix(preference_path, rec_path, user_num, item_num);*/
		
		// 计算相似度矩阵
		HashMap<Integer, Integer> stuno_coursewareTimes = CoursewareSimilarity.genCoursewareTimes(conn);
		double[][] similarityMatrix = CalSimilarityMatrix.calSimilarityMatrix(CFRecommender.PREFERENCE_MATRIX, user_num, 
				stuno_coursewareTimes, weight_pearson, weight_courseware);
		
		// 生成邻近用户
		int neighbors_num = Integer.parseInt(PropertyHelper.getProperty("PARAMETER_K"));
		CFRecommender.NEIGHBORS_MATRIX = CalNeighbors.calKNeighbors(user_num, neighbors_num, similarityMatrix);
	}
	
	/**
	 * 评分矩阵 & 邻近用户矩阵生成
	 * PearsonCorrelationSimilarity
	 */
	public static void initPreferenceNeighborsMatrix(Connection conn) {
		// 生成评分矩阵
		int user_num = Integer.parseInt(PropertyHelper.getProperty("USER_NUM"));
		int item_num = Integer.parseInt(PropertyHelper.getProperty("ITEM_NUM"));
		
		String preference_path = PropertyHelper.getProperty("PREFERENCE_PATH");
		CFRecommender.PREFERENCE_MATRIX = GenPreferenceMatrix.genPreferenceMatrix(preference_path, user_num, item_num);
		
		// 计算相似度矩阵
		double[][] similarityMatrix = CalSimilarityMatrix.calSimilarityMatrix(CFRecommender.PREFERENCE_MATRIX, user_num);
		
		// 生成邻近用户
		int neighbors_num = Integer.parseInt(PropertyHelper.getProperty("PARAMETER_K"));
		CFRecommender.NEIGHBORS_MATRIX = CalNeighbors.calKNeighbors(user_num, neighbors_num, similarityMatrix);
	}
	
	/**
	 * 生成推荐列表
	 * @param neighbors_num
	 * @param recommendation_num
	 */
	public static void genRecommendation(int neighbors_num, int recommendation_num) {
		CFRecommender.REC_ARRAY = GenRecommendations.genRecommendationForAll(CFRecommender.PREFERENCE_MATRIX, 
				CFRecommender.NEIGHBORS_MATRIX, neighbors_num, recommendation_num);
	}
	
	/**
	 *  算法性能评价
	 * @param conn
	 * @param testSet
	 * @return
	 */
	public static double[] measureRec(Connection conn, List<LearningLog> testSet) {
		HashMap<Long, Integer> stunos_sequences = TrainingSetTestSet.getStunoSequence(PropertyHelper.getProperty("PREFERENCE_PATH")); // TODO 学习者编号从1开始的 
		HashMap<String, Integer> videos_sequences = VideoSequenceDur.readVideo(conn);
		
		HashMap<Integer, HashSet<Integer>> testSet_stuno_videos = TrainingSetTestSet.selectTestSet(testSet, stunos_sequences, videos_sequences);
		
		double[] accuracy_recall = Measuring.calAccuracyRecallRate(CFRecommender.REC_ARRAY, testSet_stuno_videos);
		
		/*System.out.print(accuracy_recall[0] + ",");
		System.out.print(accuracy_recall[1] + ",");*/
		
		System.out.println(accuracy_recall[0]);
		System.out.println(accuracy_recall[1]);
		
		return accuracy_recall;
	}
	
	private static void test(int neighbors_num, int recommendation_num) {
		// generate recommendations for one learner
		int user_sequence = 1; // 从 1 开始
		HashMap<Integer, Double> rec = GenRecommendations.genRecommendationForOne(CFRecommender.PREFERENCE_MATRIX, CFRecommender.NEIGHBORS_MATRIX, 
		    user_sequence, neighbors_num, recommendation_num);
		
		for(Entry<Integer, Double> entry : rec.entrySet()) {
			System.out.println(entry.getKey() + " " + entry.getValue());
		}
	}
}