package org.recommender.subgraph;

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
public class VideosSubGraph {
	private static int itemTotalSize = Integer.parseInt(PropertyHelper.getProperty("ITEM_NUM"));

	public static void main(String[] args) {
		int cfRecN = 5; // 协同过滤推荐个数，取值范围 [2,5]
		//int[][] cfRec = VideosSubGraph.getCFRecResult("E:\\data\\cf\\recommendation\\Times-CF\\rec-" + cfRecN + ".txt", cfRecN); // 视频编号从1开始，1~224
		// int[][] cfRec = VideosSubGraph.getCFRecResult("E:\\data\\cf\\recommendation\\Dur-CF\\rec-" + cfRecN + ".txt", cfRecN); // 视频编号从1开始，1~224
		int[][] cfRec = VideosSubGraph.getCFRecResult("E:\\data\\cf\\recommendation\\rec.txt", cfRecN); // 视频编号从1开始，1~224
		System.out.println(cfRec[0].length);
		
		double[][] videosRelation = VideosSubGraph.getVideosRelation(PropertyHelper.getProperty("VIDEOS_RELATION_PATH"), " "); // 课程视频关联度
		
		VideosSubGraph.measureCFRec(cfRec, videosRelation);
		
		// 训练集 & 测试集
		/*Connection conn = MySQLHelper.getConn();
        List<LearningLog> logs = TrainingSetTestSet.readLogs(conn);
        List<List<LearningLog>> logsAll = TrainingSetTestSet.genTrainingSetTestSet(logs); // 5份
        
        List<LearningLog> trainSet = logsAll.get(0);
        trainSet.addAll(logsAll.get(1));trainSet.addAll(logsAll.get(2));trainSet.addAll(logsAll.get(3));
        List<LearningLog> testSet = logsAll.get(4);
        
        HashMap<Long, Integer> stunos_sequences = TrainingSetTestSet.getStunoSequence(PropertyHelper.getProperty("PREFERENCE_PATH"));
		HashMap<String, Integer> videos_sequences = VideoSequenceDur.readVideo(conn);
		
		HashMap<Integer, HashSet<Integer>> testSet_stuno_videos = TrainingSetTestSet.selectTestSet(testSet, stunos_sequences, videos_sequences);*/
		
		// 算法性能
		double diversity = 0.0;
		int[][] maRec = null;
		int size = 5; // 推荐视频子图的规模，取值范围 [2,5]
		int sequence = 3; // TODO
		/*for (int i = 2; i <= size; i++) {
			for (int j = 0; j < sequence; j++) {
				maRec = VideosSubGraph.genVideosSubGraphForAll(cfRec, videosRelation, i, j);
				for (int k = 0; k < maRec.length; k++) {
					for (int l = 0; l < maRec[0].length; l++) {
						System.out.print(maRec[k][l] + ",");
					}
					System.out.println();
				}
				
				double[] accuracy_recall = VideosSubGraph.measuring(maRec, testSet_stuno_videos);
				System.out.println(accuracy_recall[0]);
				System.out.println(accuracy_recall[1]);
				
				// 关联度
				double kr = countKRAll(maRec, videosRelation);
				System.out.println("基于图谱关联的推荐的知识关联性为：" + kr);
				
				// 覆盖率
				diversity = VideosSubGraph.countDiversity(maRec, itemTotalSize);
				System.out.println("基于图谱关联的推荐的覆盖率为：" + diversity);
			}
		}*/
	}
	
	/**
	 * 为全体学习者生成推荐视频子图集
	 * @param cfRec
	 * @param videosRelation
	 * @param size
	 * @param sequence
	 * @return
	 */
	public static int[][] genVideosSubGraphForAll(int[][] cfRec, double[][] videosRelation, int size, int sequence) {
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
	
	/**
	 * 根据给定的种子视频生成单个推荐视频子图
	 * @param size
	 * @param seed
	 * @param videosRelation
	 * @return
	 */
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
	 * MA算法的准确率 & 召回率
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
			rec = maRec[entry.getKey() - 1]; // 用户编号从 1 开始，所以需要减一 TODO 后续可统一整个项目的ID，保证从0开始
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
	
	/**
	 * 多个子图的知识关联性的平均值
	 * @param maRec
	 * @param videosRelation
	 * @return
	 */
	public static double countKRAll(int[][] maRec, double[][] videosRelation) {
		double r = 0.0;
		
		for (int i = 0; i < maRec.length; i++) {
			r += countKR(maRec[i], videosRelation);
		}
		
		return (r / maRec.length);
	}
	
	/**
	 * 单个子图的知识关联性
	 * @param list
	 * @param videosRelation
	 * @return
	 */
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
	 * 从文件中读取对应CF的推荐结果
	 * @param path
	 * @param size
	 * @return
	 */
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
	
	/**
	 * 协同过滤推荐的知识关联性&覆盖率
	 * @param cfRec
	 * @param videosRelation
	 */
	public static void measureCFRec(int[][] cfRec, double[][] videosRelation) {
		double kr = VideosSubGraph.countKRAll(cfRec, videosRelation);
		System.out.println("协同过滤推荐的知识关联性为：" + kr);
		
		double diversity = VideosSubGraph.countDiversity(cfRec, itemTotalSize);
		System.out.println("协同过滤推荐的覆盖率为：" + diversity);
	}
	
	/**
	 * 从文件中读取课程视频关联度
	 * 因为文件中存在“总复习”视频被删除的情况，所以要特殊处理
	 * @param path
	 * @param separator
	 * @return
	 */
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
	 * 覆盖率
	 * @param maRec
	 * @param itemTotalSize
	 * @return
	 */
	private static double countDiversity(int[][] maRec, int itemTotalSize) {
		double diversity = 0.0;
		
		HashSet<Integer> hs = new HashSet<Integer>();
		for (int i = 0; i < maRec.length; i++) {
			for (int j = 0; j < maRec[0].length; j++) {
				hs.add(maRec[i][j]);
			}
		}
		
		double hsSize = hs.size();
		diversity = hsSize / itemTotalSize;
		
		return diversity;
	}
}