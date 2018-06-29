package abandon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import org.recommender.cf.neighborhood.CalNeighbors;
import org.recommender.cf.preference.PreferenceDuration;
import org.recommender.cf.preference.VideoSequenceDur;
import org.recommender.cf.rec.GenRecommendations;
import org.recommender.cf.similarity.CalSimilarityMatrix;
import org.recommender.data.LearningLog;
import org.recommender.data.TrainingSetTestSet;
import org.recommender.measuring.Measuring;
import org.recommender.utility.StoreStringIntoFile;
import org.recommender.utility.MySQLHelper;

/**
* @author : wuke
* @date   : 20170611 02:51:30
* Title   : CFRecommender
* Description : 
*/
public class CFRecommender2 {
	public static double[][] PREFERENCE_MATRIX = null;
	public static int[][] NEIGHBORS_MATRIX = null;
	public static ArrayList<HashMap<Integer, Double>> REC_ARRAY = null;
	public static String pre_path = "E:\\data\\cf\\preference\\new\\preference_times.txt";
	public static String pre_path_dur = "E:\\data\\cf\\preference\\new\\preference_dur.txt";
	
	public static void main(String[] args) {
		Connection conn = MySQLHelper.getConn();
		
		// 训练集 & 测试集
        List<LearningLog> logs = TrainingSetTestSet.readLogs(conn);
        List<List<LearningLog>> logsAll = TrainingSetTestSet.genTrainingSetTestSet(logs); // 5份
        
        List<LearningLog> trainSet = logsAll.get(0);
        trainSet.addAll(logsAll.get(1));trainSet.addAll(logsAll.get(2));trainSet.addAll(logsAll.get(3));
        List<LearningLog> testSet = logsAll.get(4);
        
        // 计算视频学习次数偏好，并存储到文件中
        /*HashMap<Long, HashMap<Integer, Integer>> stuno_video_times = CFRecommender2.calPreference(conn, trainSet);
        CFRecommender2.storePreference(stuno_video_times, pre_path);*/
        
        // 计算视频学习时长偏好，并存储到文件中
        //HashMap<Long, HashMap<Integer, Integer>> stuno_video_totalTlen = PreferenceDuration.calPreference(conn, trainSet);
        
        /*CFRecommender2.genPreferenceMatrix(pre_path, 638, 224);
        		
        CFRecommender2.initPreferenceNeighborsMatrix(conn);
        
        int neighbors_num = 20;
        int recommendation_num = 5;
        CFRecommender2.genRecommendation(neighbors_num, recommendation_num);
        
        CFRecommender2.measure(conn, testSet);*/
	}
	
	public static void measure(Connection conn, List<LearningLog> testSet) {
		for (int i = 13; i <= 13; i++) { // 邻近用户个数
        	for (int j = 1; j <= 10; j++) { // 推荐个数
        		CFRecommender2.genRecommendation(i, j);
        		
        		CFRecommender2.measureRec(conn, testSet);
        	}
        	// System.out.println("==========================");
        }
	}
	
	private static HashMap<Long, HashMap<Integer, Integer>> calPreference(Connection conn, List<LearningLog> logs) {
		HashMap<Long, HashMap<Integer, Integer>> stuno_video_times = new HashMap<Long, HashMap<Integer, Integer>>();
		
		HashMap<String, Integer> videos = VideoSequenceDur.readVideo(conn); // (课程视频名, 课程视频次序)

		long stuno = 0;
		String title = "";
		Integer video_sequence = 0;
		HashMap<Integer, Integer> video_times = null;
		
		for(LearningLog aLearningLog : logs) {
			if (aLearningLog.getOper() == 76) {
				stuno = aLearningLog.getStuno();
				title = aLearningLog.getTitle();
				
				video_sequence = videos.get(title);
				if (video_sequence != null) {
					if (stuno_video_times.containsKey(stuno)) { // old student
						video_times = stuno_video_times.get(stuno);				
						if (video_times.containsKey(video_sequence)) { // watched video
							video_times.put(video_sequence, video_times.get(video_sequence) + 1);
						} else { // new video
							video_times.put(video_sequence, 1);
						}
					} else { // new student with new video
						video_times = new HashMap<Integer, Integer>();
						video_times.put(video_sequence, 1);
						
						stuno_video_times.put(stuno, video_times);
					}
				}
			}
		}
		
		// System.out.println(stuno_video_times.size() + " 个学生");
		
		return stuno_video_times;
	}
	
	private static void storePreference(HashMap<Long, HashMap<Integer, Integer>> stuno_video_preference, String path) {
		StringBuilder preferenceSb = new StringBuilder();
		
		int stuno_sequence = 0;
		long stuno = 0;
		int video_sequence = 0;
		double preference = 0;
		
		for (Entry<Long, HashMap<Integer, Integer>> entry : stuno_video_preference.entrySet()) {
			stuno_sequence += 1;
			stuno = entry.getKey();
			HashMap<Integer, Integer> video_preference = entry.getValue();
			
			for (Entry<Integer, Integer> entry2 : video_preference.entrySet()) {				
				video_sequence = entry2.getKey();
				preference = entry2.getValue();
				
				preferenceSb.append(stuno_sequence);
				preferenceSb.append("," + stuno);
				preferenceSb.append("," + video_sequence);
				preferenceSb.append("," + preference);
				preferenceSb.append("\n");
			}
		}
		
		StoreStringIntoFile.storeString(preferenceSb.toString(), path);
	}
	
	private static double[][] genPreferenceMatrix(String path, int user_num, int item_num) {		
		double[][] preferenceMatrix = new double[user_num][item_num];
		
		int stuno_sequence = 0;
		// long stuno = 0.0;
		int item_sequence = 0;
		double preference = 0.0;
		BufferedReader br = null;
		try {
			File file = new File(path);
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);
			String line = "";
			while ((line = br.readLine()) != null) {
				String[] strs = line.split(",");
				
				stuno_sequence = Integer.parseInt(strs[0]);
				// stuno = Long.parseDouble(strs[1]);
				item_sequence = Integer.parseInt(strs[2]);
				preference = Double.parseDouble(strs[3]);
				
				preferenceMatrix[stuno_sequence-1][item_sequence-1] = preference;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return preferenceMatrix;
	}
	
	/**
	 * 评分矩阵 & 邻近用户矩阵生成
	 * PearsonCorrelationSimilarity
	 */
	public static void initPreferenceNeighborsMatrix(Connection conn) {
		// 生成评分矩阵
		int user_num = 638;
		int item_num = 224;
		
		CFRecommender2.PREFERENCE_MATRIX = CFRecommender2.genPreferenceMatrix(pre_path, user_num, item_num);
		
		// 计算相似度矩阵
		double[][] similarityMatrix = CalSimilarityMatrix.calSimilarityMatrix(CFRecommender2.PREFERENCE_MATRIX, user_num);
		
		// 生成邻近用户
		int neighbors_num = 50;
		CFRecommender2.NEIGHBORS_MATRIX = CalNeighbors.calKNeighbors(user_num, neighbors_num, similarityMatrix);
	}
	
	/**
	 * 生成推荐列表
	 * @param neighbors_num
	 * @param recommendation_num
	 */
	public static void genRecommendation(int neighbors_num, int recommendation_num) {
		CFRecommender2.REC_ARRAY = GenRecommendations.genRecommendationForAll(CFRecommender2.PREFERENCE_MATRIX, 
				CFRecommender2.NEIGHBORS_MATRIX, neighbors_num, recommendation_num);
	}
	
	/**
	 *  算法性能评价
	 * @param conn
	 * @param testSet
	 * @return
	 */
	public static double[] measureRec(Connection conn, List<LearningLog> testSet) {
		HashMap<Long, Integer> stunos_sequences = TrainingSetTestSet.getStunoSequence(pre_path);
		HashMap<String, Integer> videos_sequences = VideoSequenceDur.readVideo(conn);
		
		HashMap<Integer, HashSet<Integer>> testSet_stuno_videos = TrainingSetTestSet.selectTestSet(testSet, stunos_sequences, videos_sequences);
		
		double[] accuracy_recall = Measuring.calAccuracyRecallRate(CFRecommender2.REC_ARRAY, testSet_stuno_videos);
		
		System.out.println(accuracy_recall[0]);
		//System.out.println(accuracy_recall[0] + "  " + accuracy_recall[1]);
		
		return accuracy_recall;
	}
	
	private static void test(int neighbors_num, int recommendation_num) {
		// generate recommendations for one learner
		int user_sequence = 1; // 从 1 开始
		HashMap<Integer, Double> rec = GenRecommendations.genRecommendationForOne(CFRecommender2.PREFERENCE_MATRIX, CFRecommender2.NEIGHBORS_MATRIX, 
		    user_sequence, neighbors_num, recommendation_num);
		
		for(Entry<Integer, Double> entry : rec.entrySet()) {
			System.out.println(entry.getKey() + " " + entry.getValue());
		}
	}
}