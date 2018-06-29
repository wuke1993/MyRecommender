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
import java.util.Random;

import org.recommender.cf.preference.VideoSequenceDur;
import org.recommender.data.LearningLog;
import org.recommender.data.TrainingSetTestSet;
import org.recommender.measuring.AccuracyRate;
import org.recommender.measuring.RecallRate;
import org.recommender.utility.MySQLHelper;
import org.recommender.utility.PropertyHelper;

/**
* @author : wuke
* @date   : 20180425 15:51:32
* Title   : VideosSubGraph
* Description : 
*/
public class VideosSubGraph_backup {

	public static void main(String[] args) {
		int[][] cfRec = VideosSubGraph_backup.getCFRecResult("E:\\data\\cf\\recommendation\\rec3.txt", 3); // 视频编号从1开始，1~224
		/*for (int i = 0; i < cfRec.length; i++) {
			for (int j = 0; j < cfRec[0].length; j++) {
				System.out.print(cfRec[i][j] + " ");
			}
			System.out.println();
		}*/
		//System.out.println(VideosSubGraph.countDiversity(cfRec));
		
		double[][] videosRelation = VideosSubGraph_backup.getVideosRelation(PropertyHelper.getProperty("VIDEOS_RELATION_PATH"), " ");
		/*for (int i = 0; i < 224; i++) {
			for (int j = 0; j < 224; j++) {
				System.out.print(videosRelation[i][j] + " ");
			}
			System.out.println();
		}*/
		
		/*double kr = countKRAll(cfRec, videosRelation);
		System.out.println(kr);*/
		
		/*// 基于视频关联度 和 视频学期阶段相似度的乘积生成新的相似度。暂时不使用
		double[][] sim = VideosSubGraph.genSim(videosRelation);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < sim.length; i++) {
			sb.append(sim[i][0]);
			for (int j = 1; j < sim[0].length; j++) {
				sb.append("," + sim[i][j]);
			}
			sb.append("\n");
		}
		StoreStringIntoFile.storeString(sb.toString(), "E:\\data\\videoSim\\sim.txt");
		// double[][] sim = VideosSubGraph.getVideosRelation("e:\\data\\videoSim\\sim.txt", ",");*/
		
		// 训练集 & 测试集
		Connection conn = MySQLHelper.getConn();
        List<LearningLog> logs = TrainingSetTestSet.readLogs(conn);
        List<List<LearningLog>> logsAll = TrainingSetTestSet.genTrainingSetTestSet(logs); // 5份
        
        List<LearningLog> trainSet = logsAll.get(0);
        trainSet.addAll(logsAll.get(1));trainSet.addAll(logsAll.get(2));trainSet.addAll(logsAll.get(3));
        List<LearningLog> testSet = logsAll.get(4);
        
        HashMap<Long, Integer> stunos_sequences = TrainingSetTestSet.getStunoSequence(PropertyHelper.getProperty("PREFERENCE_PATH"));
		HashMap<String, Integer> videos_sequences = VideoSequenceDur.readVideo(conn);
		
		HashMap<Integer, HashSet<Integer>> testSet_stuno_videos = TrainingSetTestSet.selectTestSet(testSet, stunos_sequences, videos_sequences);
		
		// HashSet<Integer> validStuId = FilterLogsByGrade.genValidStu(conn); // 挑选好学生
		
		// 算法性能
		double diversity = 0.0;
		int[][] maRec = null;
		int size = 5; // 推荐视频子图的规模
		int sequence = 3;
		for (int i = 2; i <= size; i++) {
			for (int j = 0; j < sequence; j++) {
				maRec = VideosSubGraph_backup.genVideosSubGraph4(cfRec, videosRelation, i, j);
				// maRec = VideosSubGraph.genVideosSubGraphNew(cfRec, sim, i, j); // 融合 课程视频关联度 与 课程视频学期阶段相似性，暂时不使用
				
				/*double[] accuracy_recall = VideosSubGraph.measuring(maRec, testSet_stuno_videos);
				// double[] accuracy_recall = VideosSubGraph.measuring(maRec, testSet_stuno_videos, validStuId);
				System.out.println(accuracy_recall[0]);
				System.out.println(accuracy_recall[1]);
				
				// 关联度
				double kr = countKRAll(maRec, videosRelation);
				System.out.println(kr);*/
				
				// 分散度
				diversity = VideosSubGraph_backup.countDiversity(maRec);
				System.out.println(diversity);
			}
		}
		
		/*int[][] maRec = VideosSubGraph.genVideosSubGraph4(cfRec, videosRelation, 5, 0);
		for (int i = 0; i < maRec.length; i++) {
			for (int j = 0; j < maRec[0].length; j++) {
				System.out.print(maRec[i][j] + ",");
			}
			System.out.println();
		}*/
	}
	
	public static double countKRAll(int[][] maRec, double[][] videosRelation) {
		double r = 0.0;
		
		for (int i = 0; i < maRec.length; i++) {
			r += countKR(maRec[i], videosRelation);
		}
		
		return (r / maRec.length);
	}
	
	public static double countKR(int[] list, double[][] videosRelation) {
		double r = 0.0;
		for (int i = 0; i < list.length; i++) {
			for (int j = 0; j < list.length; j++) {
				if (i != j) {
					r += videosRelation[i][j];
				}
			}
		}
		double num = list.length * (list.length - 1);
		return (r / num);
	}
	
	/**
	 * 当前方法   ！！！！！
	 * @param cfRec
	 * @param videosRelation
	 * @param size
	 * @param sequence
	 * @return
	 */
	public static int[][] genVideosSubGraph4(int[][] cfRec, double[][] videosRelation, int size, int sequence) {
		int[][] maRec = new int[638][size];
		
		int seed = 0;
		for (int i = 0; i < maRec.length; i++) {			
			seed = cfRec[i][sequence] - 1; // 取协同过滤推荐列表的第sequence个,sequence从 0 开始
			
			ArrayList<Integer> seletedNodes = genVideosSubGraph(size, seed, videosRelation); // 从 0 开始
			
			for (int j = 0; j < seletedNodes.size(); j++) {
				maRec[i][j] = seletedNodes.get(j) + 1; // 加 1 保持视频编号从 1 开始
			}
		}
		
		return maRec;
	}
	
	public static ArrayList<Integer> genVideosSubGraph(int size, int seed, double[][] videosRelation) {
		ArrayList<Integer> seletedNodes = new ArrayList<Integer>();
		ArrayList<Integer> unSeletedNodes = new ArrayList<Integer>();
		ArrayList<int[]> edges = new ArrayList<int[]>();
		
		seletedNodes.add(seed);
		
		for (int i = 0; i < videosRelation.length; i++) {
			unSeletedNodes.add(i);
		}
		unSeletedNodes.remove((Integer)seed); // 减去对应元素

		while(seletedNodes.size() < size) {
			double maxR = 0.0;
			int candidateNode = 0;
			int[] candidateEdge = new int[2];
			for (int i : seletedNodes) {
				for (int j : unSeletedNodes) {
					if (videosRelation[i][j] > maxR) {
						maxR = videosRelation[i][j];
						candidateNode = j;
						candidateEdge[0] = i;
						candidateEdge[1] = j;
					}
				}
			}
			
			seletedNodes.add(candidateNode);
			unSeletedNodes.remove((Integer)candidateNode); // 减去对应元素
			edges.add(candidateEdge);
		}
		
		return seletedNodes;
	}
	
	/**
	 * 根据视频关联度生成推荐视频子图，视频编号保持跟CF一致，从 1 开始
	 * @param cfRec
	 * @param videosRelation
	 * @return
	 */
	public static int[][] genVideosSubGraph3(int[][] cfRec, double[][] videosRelation, int size, int sequence) {
		int[][] maRec = new int[638][size];
		
		int seed = 0;
		double[][] row = new double[224][224];
		for (int i = 0; i < maRec.length; i++) {
			maRec[i][0] = cfRec[i][sequence];
			
			seed = cfRec[i][sequence] - 1; // 取协同过滤推荐列表的第sequence个,sequence从 0 开始
			
			for (int k = 0; k < 224; k++) {
				for (int l = 0; l < 224; l++) {
					row[k][l] = videosRelation[k][k];
				}
			}
			
			int videoId = -1;
			for (int j = 1; j < size; j++) {
				row[seed][seed] = -Double.MAX_VALUE; // 将关联度中自身与自身的关联度从 1 （最大值）设置为最小
				
				videoId = VideosSubGraph_backup.findIndexOfMax(row[seed]);
				
				maRec[i][j] = videoId + 1; // 加 1 保持视频编号从 1 开始
				
				//row[videoId] = -Double.MAX_VALUE; // 赋值操作
			}
		}
		
		return maRec;
	}
	
	/**
	 * 根据视频关联度生成推荐视频子图，视频编号保持跟CF一致，从 1 开始
	 * @param cfRec
	 * @param videosRelation
	 * @return
	 */
	public static int[][] genVideosSubGraph1(int[][] cfRec, double[][] videosRelation, int size, int sequence) {
		int[][] maRec = new int[638][size];
		
		int seed = 0;
		double[] row = new double[224]; // 存放某一视频和其它视频的关联度
		for (int i = 0; i < maRec.length; i++) {
			maRec[i][0] = cfRec[i][sequence];
			
			seed = cfRec[i][sequence] - 1; // 取协同过滤推荐列表的第sequence个,sequence从0开始
			
			// row = videosRelation[seed]; // 若这样做，下面的赋值操作会改变 videosRelation
			for (int k = 0; k < 219; k++) {
				row[k] = videosRelation[seed][k];
			}
			
			// System.out.println(seed);
			
			int videoId = -1;
			for (int j = 1; j < size; j++) {
				row[seed] = -Double.MAX_VALUE; // 将关联度中自身与自身的关联度从 1 （最大值）设置为最小
				
				videoId = VideosSubGraph_backup.findIndexOfMax(row);
				
				// 因为删除了 5 个总复习视频，因此此处要进行处理
				if (videoId < 98) {
					maRec[i][j] = videoId + 1; // 加 1 保持视频编号从 1 开始
				} else if (videoId > 149) {
					maRec[i][j] = videoId + 1 + 4; // 对应 Java 的课程视频，前面删除了操作系统 2 个总复习视频，删除了计算机网络 2 个总复习视频，共计 4 个
				} else {
					maRec[i][j] = videoId + 1 + 2; // 对应计算机网络 课程视频，前面删除了操作系统 2个总复习
				}
				
				row[videoId] = -Double.MAX_VALUE; // 赋值操作
			}
		}
		
		return maRec;
	}
	
	/**
	 * 
	 * @param maRec
	 * @param testSet_stuno_videos
	 * @return
	 */
	private static double[] measuring(int[][] maRec, HashMap<Integer, HashSet<Integer>> testSet_stuno_videos) {
		double[] accuracy_recall = new double[2];
		double accuracy = 0.0;
		double recall = 0.0;
		
		int[] rec = null; // 给某用户推荐的视频列表
		HashSet<Integer> hs = null; // 用户实际观看的列表
        int[] real = null;
		for(Entry<Integer, HashSet<Integer>> entry : testSet_stuno_videos.entrySet()) {
			rec = maRec[entry.getKey() - 1]; // TODO 用户编号从 1 开始，所以需要减一
			hs = entry.getValue();
			
			real = new int[hs.size()];
			int index = 0;
			for(int t : hs)
				real[index++] = t;
			
			accuracy += AccuracyRate.calAccuracyRate(rec, real);
			recall += RecallRate.calRecallRate(rec, real);
		}
		
		accuracy /= testSet_stuno_videos.size();
		recall /= testSet_stuno_videos.size();
		
		accuracy_recall[0] = accuracy;
		accuracy_recall[1] = recall;
		
		return accuracy_recall;
	}
	
	private static double countDiversity(int[][] maRec) {
		double diversity = 0.0;
		
		HashSet<Integer> hs = new HashSet<Integer>();
		for (int i = 0; i < maRec.length; i++) {
			for (int j = 0; j < maRec[0].length; j++) {
				hs.add(maRec[i][j]);
			}
		}
		
		double hsSize = hs.size();
		diversity = hsSize / 224;
		
		return diversity;
	}    
	
	public static int[][] getCFRecResult(String path, int size) {
		int[][] cfRec = new int[638][size];
		
		File file = new File(path);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
			
			int lineNum = 0;
			String str = "";
			String[] strArr = null;
			int videoId = 0;
			while((str = br.readLine()) != null) {
				strArr = str.split(",");
				for (int i = 0; i < size; i++) {
					videoId = Integer.parseInt(strArr[i]);
					cfRec[lineNum][i] = videoId;
				}
				
				lineNum++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return cfRec;
	}
	
	public static double[][] getVideosRelation0(String path, String separator) {
		double[][] videosRelation = new double[224][224];
		
		File file = new File(path);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
			
			int lineNum = 0;
			String str = "";
			String[] strArr = null;
			double value = 0;
			while((str = br.readLine()) != null) {
				strArr = str.split(separator);
				for (int j = 0; j < strArr.length; j++) { // 219
					videosRelation[lineNum][j] = Double.parseDouble(strArr[j]);
				}
				
				lineNum++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return videosRelation;
	}
	
	public static double[][] getVideosRelation(String path, String separator) {
		double[][] videosRelation = new double[224][224];
		
		int[] revison = {98, 99, 152, 153, 223};
		HashSet<Integer> video_revision  = new HashSet<Integer>();
		for (int aVideo : revison) {
			video_revision.add(aVideo);
		}
		
		File file = new File(path);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
			
			int lineNum = 0;
			String str = "";
			String[] strArr = null;
			double value = 0;
			while((str = br.readLine()) != null) {
				strArr = str.split(separator);
				for (int j = 0; j < strArr.length; j++) { // 219
					if (lineNum < 98) {
						if (j < 98) {
							value = Double.parseDouble(strArr[j]);
							videosRelation[lineNum][j] = value;
						} else if (98 <= j && j < 150) {
							value = Double.parseDouble(strArr[j]);
							videosRelation[lineNum][j + 2] = value;
						} else if (150 <= j) {
							value = Double.parseDouble(strArr[j]);
							videosRelation[lineNum][j + 4] = value;
						}
					} else if (98 <= lineNum && lineNum < 150) {
						if (j < 98) {
							value = Double.parseDouble(strArr[j]);
							videosRelation[lineNum + 2][j] = value;
						} else if (98 <= j && j < 150) {
							value = Double.parseDouble(strArr[j - 2]);
							videosRelation[lineNum + 2][j + 2] = value;
						} else if (150 <= j) {
							value = Double.parseDouble(strArr[j - 4]);
							videosRelation[lineNum + 2][j + 4] = value;
						}
					} else if (150 <= lineNum) {
						if (j < 98) {
							value = Double.parseDouble(strArr[j]);
							videosRelation[lineNum + 4][j] = value;
						} else if (98 <= j && j < 150) {
							value = Double.parseDouble(strArr[j - 2]);
							videosRelation[lineNum + 4][j + 2] = value;
						} else if (150 <= j) {
							value = Double.parseDouble(strArr[j - 4]);
							videosRelation[lineNum + 4][j + 4] = value;
						}
					}
				}
				
				lineNum++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return videosRelation;
	}
	
	/**
	 * 随机推荐
	 * @param userNum
	 * @param recNum
	 * @return
	 */
	public static int[][] randomRec(int userNum, int recNum) {
		int[][] random = new int[userNum][recNum];
		
		Random seed = new Random();
		
		for (int i = 0; i < userNum; i++) {
			for (int j = 0; j < recNum; j++) {
				random[i][j] = seed.nextInt(224); // 0 ~ 223
				
				// System.out.println(random[i][j]);
			}
		}
		
		return random;		
	}
	
	public static void testRandomRec(HashMap<Integer, HashSet<Integer>> testSet_stuno_videos) {
		double diversity = 0.0;
		for (int j = 2; j <=5; j++) {
			diversity = 0.0;
			for (int i = 0 ; i < 10000; i++) {
			    int[][] randomRec = VideosSubGraph_backup.randomRec(638, j);
			    diversity += countDiversity(randomRec);
		    }
		    System.out.println(diversity / 10000);
		}
		
		double accuracy = 0.0;
		double recall = 0.0;
		for (int j = 2; j <=5; j++) {
			accuracy = 0.0;
			recall = 0.0;
			for (int i = 0 ; i < 10000; i++) {
			    int[][] randomRec = VideosSubGraph_backup.randomRec(638, j);
			    double[] accuracy_recall = VideosSubGraph_backup.measuring(randomRec, testSet_stuno_videos);
			    accuracy += accuracy_recall[0];
			    recall += accuracy_recall[1];
		    }
		    System.out.println(accuracy / 10000 + " " +  recall / 10000);
		}
	}
	
	private static int findIndexOfMax(double[] row) {
		int index = -2;
		double max = -Double.MAX_VALUE;
		for (int i = 0; i < row.length; i++) {
			if (max < row[i]) {
				max = row[i];
				index = i;
			}
		}
		
		return index;
	}
	
	/**
	 * 弃用 本想利用成绩高的学生，看看能不能提升，结果没提升
	 * @param maRec
	 * @param testSet_stuno_videos
	 * @param validStuId
	 * @return
	 */
	private static double[] measuring(int[][] maRec, HashMap<Integer, HashSet<Integer>> testSet_stuno_videos, HashSet<Integer> validStuId) {
		double[] accuracy_recall = new double[2];
		double accuracy = 0.0;
		double recall = 0.0;
		
		int[] rec = null; // 给某用户推荐的视频列表
		HashSet<Integer> hs = null; // 用户实际观看的列表
        int[] real = null;
		for(Entry<Integer, HashSet<Integer>> entry : testSet_stuno_videos.entrySet()) {
			rec = maRec[entry.getKey() - 1]; // TODO 用户编号从 1 开始，所以需要减一
			if (validStuId.contains(entry.getKey() - 1)) {
				hs = entry.getValue();
			
				real = new int[hs.size()];
				int index = 0;
				for(int t : hs)
					real[index++] = t;
				
				accuracy += AccuracyRate.calAccuracyRate(rec, real);
				recall += RecallRate.calRecallRate(rec, real);
			}
			
		}
		
		accuracy /= validStuId.size();
		recall /= validStuId.size();
		
		accuracy_recall[0] = accuracy;
		accuracy_recall[1] = recall;
		
		return accuracy_recall;
	}
	
	/**
	 * 融合 课程视频关联度 与 课程视频学期阶段相似性，相乘，暂时不使用
	 * @param videosRelation
	 * @return
	 */
	private static double[][] genSim(double[][] videosRelation) {
		double[][] sim = new double[224][224];
		
		double tem = 0.0;
		for (int i = 0; i < 224; i++) {
			double[] simPeriods = VideoPeriod.simPeriod(i);
			for (int j = 0; j < i; j++) {
				if (i < 97) {
					tem = simPeriods[j] * videosRelation[i][j];
					sim[i][j] = tem;
					sim[j][i] = tem;
				} else if (i > 153 && i < 223) {
					if (j < 98) { // 操作系统
						tem = simPeriods[j] * videosRelation[i - 4][j];
						sim[i][j] = tem;
						sim[j][i] = tem;
					} else if (j > 153 && j < 223) { // Java
						tem = simPeriods[j] * videosRelation[i - 4][j - 4];
						sim[i][j] = tem;
						sim[j][i] = tem; // 对应 Java 的课程视频，前面删除了操作系统 2 个总复习视频，删除了计算机网络 2 个总复习视频，共计 4 个
					} else if (j > 99 && j < 152) { // 计算机网络原理
						tem = simPeriods[j] * videosRelation[i - 4][j - 2];
						sim[i][j] = tem;
						sim[j][i] = tem; // 对应计算机网络 课程视频，前面删除了操作系统 2个总复习
					}
				} else if (i > 99 && i < 152) {
					if (j < 98) { // 操作系统
						tem = simPeriods[j] * videosRelation[i - 2][j];
						sim[i][j] = tem;
						sim[j][i] = tem;
					} else if (j > 153 && j < 223) { // Java
						tem = simPeriods[j] * videosRelation[i - 2][j - 4];
						sim[i][j] = tem;
						sim[j][i] = tem; // 对应 Java 的课程视频，前面删除了操作系统 2 个总复习视频，删除了计算机网络 2 个总复习视频，共计 4 个
					} else if (j > 99 && j < 152) { // 计算机网络原理
						tem = simPeriods[j] * videosRelation[i - 2][j - 2];
						sim[i][j] = tem;
						sim[j][i] = tem; // 对应计算机网络 课程视频，前面删除了操作系统 2个总复习
					}
				}
			}
		}
		
		return sim;
	}
	
	/**
	 * 融合 课程视频关联度 与 课程视频学期阶段相似性，暂时不使用
	 * 视频编号保持跟CF一致，从 1 开始
	 * @param cfRec
	 * @param sim
	 * @param size
	 * @param sequence
	 * @return
	 */
	public static int[][] genVideosSubGraph2(int[][] cfRec, double[][] sim, int size, int sequence) {
		int[][] maRec = new int[638][size];
		
		int seed = 0;
		double[] row = new double[224]; // 存放某一视频和其它视频的关联度
		for (int i = 0; i < maRec.length; i++) {
			maRec[i][0] = cfRec[i][sequence];
			
			seed = cfRec[i][sequence] - 1; // 取协同过滤推荐列表的第sequence个,sequence从0开始
			
			// row = videosRelation[seed]; // 若这样做，下面的赋值操作会改变 videosRelation
			for (int k = 0; k < 224; k++) {
				row[k] = sim[seed][k];
			}
			
			int videoId = -1;
			for (int j = 1; j < size; j++) {
				row[seed] = -Double.MAX_VALUE; // 将关联度中自身与自身的关联度从 1 （最大值）设置为最小
				videoId = VideosSubGraph_backup.findIndexOfMax(row);
				
				maRec[i][j] = videoId + 1; // 加 1 保持视频编号从 1 开始
				
				row[videoId] = - Double.MAX_VALUE; // 赋值操作
			}
		}
		
		return maRec;
	}
}