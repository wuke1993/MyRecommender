package org.recommender.subgraph;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.recommender.cf.preference.VideoSequenceDur;
import org.recommender.data.LearningLog;
import org.recommender.data.TrainingSetTestSet;
import org.recommender.utility.MySQLHelper;
import org.recommender.utility.PropertyHelper;

/**
* @author : wuke
* @date   : 20180315 10:15:57
* Title   : VideoCandidates
* Description : 生成候选视频集
*     videoCandidates = learnedCandidates + cfRecCandidates
*/
public class VideoCandidates {
	
	private static Connection conn = MySQLHelper.getConn();
	private static HashMap<Long, Integer> stunos_sequences = TrainingSetTestSet.getStunoSequence(PropertyHelper.getProperty("PREFERENCE_PATH"));
	private static HashMap<String, Integer> videos_sequences = VideoSequenceDur.readVideo(conn);
	private static Map<Integer, Integer> video_dur = VideoSequenceDur.getVideosDur(conn);
	private static int[] classVideoNum = {100, 154, 224}; // OS CN JAVA
	
	public static void main(String[] args) {
		ArrayList<Integer> cfRecArr = VideoCandidates.genCfRec(conn, 1069802314090001L);
		
        List<LearningLog> logs = TrainingSetTestSet.readLogs(conn);
        List<List<LearningLog>> logsAll = TrainingSetTestSet.genTrainingSetTestSet(logs); // 5份
        List<LearningLog> trainSet = logsAll.get(0);
        trainSet.addAll(logsAll.get(1));
        trainSet.addAll(logsAll.get(2));
        trainSet.addAll(logsAll.get(3));
        
        HashMap<Integer, Integer> learnedVideos = VideoCandidates.genLearnedVideos(1069802314090001L, trainSet);
		
        int diversity = 3;
        int[] result = VideoCandidates.genCandidates(cfRecArr, learnedVideos, diversity);
        for (int a : result) {
        	System.out.println(a);
        }
	}
	
	/**
	 * 已学视频：首先，剔除已有效学习过的；再者，若剩余视频个数大于5，依据视频学期属性或累计学习时长来选取5个
	 * 推荐视频 + 已学视频：筛选出5个，多样性因子（学习者多门课程偏好）
	 * @param cfRecArr
	 * @param learnedVideos
	 * @param diversity
	 * @return
	 */
	public static int[] genCandidates(ArrayList<Integer> cfRecArr, HashMap<Integer, Integer> learnedVideos, int diversity) {
		int[] result = new int[5];
		
		// 剔除已有效学习过的视频
		HashSet<Integer> learnedVideoCandidates = new HashSet<Integer>();
		int videoSequence = 0;
		int totalTlen = 0;
		int videoDur = 0;
		for (Entry<Integer, Integer> entry : learnedVideos.entrySet()) {
			videoSequence = entry.getKey();
			totalTlen = entry.getValue();
			
			videoDur = VideoCandidates.video_dur.get(videoSequence);
			if ((totalTlen < 0.8 * videoDur)) {
				learnedVideoCandidates.add(videoSequence);
			}
		}
		
		// 依据多样性因子，从（推荐视频 + 已学视频）中筛选出5个，尽量保存推荐的部分
		ArrayList<Integer> osArr = new ArrayList<Integer>();
		ArrayList<Integer> cnArr = new ArrayList<Integer>();
		ArrayList<Integer> javaArr = new ArrayList<Integer>();
		for (int aVideo : cfRecArr) {
			if (aVideo <= 100) {
				osArr.add(aVideo);
			} else if (aVideo > 154) {
				javaArr.add(aVideo);
			} else {
				cnArr.add(aVideo);
			}
		}		
		int[] flag = new int[3];
		flag[0] = osArr.size();
		flag[1] = cnArr.size();
		flag[2] = javaArr.size();
		
		for (int aVideo : learnedVideoCandidates) {
			if (aVideo <= 100) {
				osArr.add(aVideo);
			} else if (aVideo > 154) {
				javaArr.add(aVideo);
			} else {
				cnArr.add(aVideo);
			}
		}
		
		Random rand = new Random();
		if (diversity == 3) { 
			if (flag[0] != 0 && flag[1] != 0 && flag[2] != 0) { // 全部选择推荐的5个
				int i = 0;
				for (int aVideo : cfRecArr) {
					result[i] = aVideo;
					i++;
				}
			} else { // 尽量选择 2 + 2 + 1，TODO 能不能保证有 5 个？？？ 
				int i = 0;
				int size = osArr.size();
				while (size > 0 && i < 2) { // 最多 2 个
					result[i] = osArr.get(i);
				    i++;
				    size--;
				}
				int j = 0;
				size = cnArr.size();
				while (size > 0 && i < 3) { // 最多 2 个
					result[i] = cnArr.get(j);
				    i++;
				    j++;
				    size--;
				}
				int k = 0;
				size = javaArr.size();
				while (size > 0 && i < 5) { // 最多 2 个
					result[i] = javaArr.get(k);
				    i++;
				    k++;
				    size--;
				}
			}
		} else if (diversity == 2) {
			int abandon = rand.nextInt(3); // 不选择的那门课程
			if (abandon == 0) { // 不选择os
				if (flag[0] == 0) { // 恰好推荐的 5 个视频都不属于 os
					int i = 0;
					for (int aVideo : cfRecArr) {
						result[i] = aVideo;
						i++;
					}
				} else {
					int i = 0;
					int j = 0;
					int size = cnArr.size();
					while (size > 0 && i < 3) { // 最多 2 个
						result[i] = cnArr.get(j);
					    i++;
					    j++;
					    size--;
					}
					int k = 0;
					size = javaArr.size();
					while (size > 0 && i < 5) { // 最多 3 个
						result[i] = javaArr.get(k);
					    i++;
					    k++;
					    size--;
					}
				}
			} else if (abandon == 1) { // 不选择cn
				if (flag[1] == 0) {
					int i = 0;
					for (int aVideo : cfRecArr) {
						result[i] = aVideo;
						i++;
					}
				} else {
					int i = 0;
					int j = 0;
					int size = osArr.size();
					while (size > 0 && i < 3) { // 最多 3 个
						result[i] = osArr.get(j);
					    i++;
					    j++;
					    size--;
					}
					int k = 0;
					size = javaArr.size();
					while (size > 0 && i < 5) { // 最多 2 个
						result[i] = javaArr.get(k);
					    i++;
					    k++;
					    size--;
					}
				}
			} else { // 不选择java
				if (flag[2] == 0) {
					int i = 0;
					for (int aVideo : cfRecArr) {
						result[i] = aVideo;
						i++;
					}
				} else {
					int i = 0;
					int size = osArr.size();
					while (size > 0 && i < 3) { // 最多 3 个
						result[i] = osArr.get(i);
					    i++;
					    size--;
					}
					int j = 0;
					size = cnArr.size();
					while (size > 0 && i < 5) { // 最多 2 个
						result[i] = cnArr.get(j);
					    i++;
					    j++;
					    size--;
					}
				}
			}
		} else { // diversity == 1
			int chosen = rand.nextInt(3);
			int i = 0;
			if (chosen == 0) {
				for (int temp : osArr) {
					result[i] = temp;
					i++;
				}
			} else if (chosen == 1) {
				for (int temp : cnArr) {
					result[i] = temp;
					i++;
				}
			} else {
				for (int temp : javaArr) {
					result[i] = temp;
					i++;
				}
			}
		}
		
		return result;
	}
	
	/**
	 * 返回给定学习者的推荐列表。从文件中读取
	 * @param conn
	 * @param learnerId
	 * @return cfRecArr, ArrayList<Integer>
	 */
	public static ArrayList<Integer> genCfRec(Connection conn, long stuno) {
		ArrayList<Integer> cfRecArr = new ArrayList<Integer>();
		
		int row = VideoCandidates.stunos_sequences.get(stuno);
		
		int target = 1;
		BufferedReader reader = null;
		try {
			FileInputStream fileInputStream = new FileInputStream(PropertyHelper.getProperty("REC_PATH"));
			InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
			reader = new BufferedReader(inputStreamReader);
			
			String str = "";
			int videoSeq = 0;
			while((str = reader.readLine()) != null) {
				if (row == target) {
					String[] strArr = str.split(",");
					for (String aStr :strArr) {
						videoSeq = Integer.parseInt(aStr);
						if (videoSeq <= 224) {
							cfRecArr.add(videoSeq);
						}
					}
					break;
				}
				else {
					target++;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return cfRecArr;
	}

	/**
	 * 生成用户已学视频列表，<视频编号, 累计学习时长>
	 * @param stuno
	 * @param logs
	 * @return
	 */
	public static HashMap<Integer, Integer> genLearnedVideos(long stuno, List<LearningLog> logs) {
		HashMap<Integer, Integer> videos_totalTlen = new HashMap<Integer, Integer>();
		
		long aStuno = 0;
		String aTitle = "";
		int aTlen = 0;
		Integer video_sequence = 0;
		for(LearningLog aLearningLog : logs) {
			if (aLearningLog.getOper() == 76) {
				aStuno = aLearningLog.getStuno();
				aTitle = aLearningLog.getTitle();
				aTlen = aLearningLog.getTlen();
					
				video_sequence = videos_sequences.get(aTitle);
				if (video_sequence != null) {
					if (aStuno == stuno) {
						if (videos_totalTlen.containsKey(video_sequence)) { // old videos
							videos_totalTlen.put(video_sequence, videos_totalTlen.get(video_sequence) + aTlen);
						} else {
							videos_totalTlen.put(video_sequence, aTlen);
						}						
					}
				}
			}
		}
		
		return videos_totalTlen;
	}
	
	private static void test() {
		for (Entry<Long, Integer> entry : stunos_sequences.entrySet()) {
		    System.out.println(entry.getKey() + "  " + entry.getValue());
		}
		for (Entry<String, Integer> entry : videos_sequences.entrySet()) {
			System.out.println(entry.getKey() + "  " + entry.getValue());
		}
		for (Entry<Integer, Integer> entry : video_dur.entrySet()) {
			System.out.println(entry.getKey() + " " + entry.getValue());
		}
	}
}
