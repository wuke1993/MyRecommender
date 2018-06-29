package org.recommender.cf.preference;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.recommender.data.LearningLog;

/**
* @author : wuke
* @date   : 20170608 16:57:11
* Title   : PreferenceDrag
* Description : 学习者（暂停+拖动）某个视频的总次数/该视频被单个学习者（暂停+拖动）的最大次数
*/
public class PreferencePauseDrag {
	public static void calPreference(Connection conn, List<LearningLog> logs, String path1, String path2) {
        Map<Long, HashMap<Integer, Integer>> stuno_video_pause_drag = new HashMap<Long, HashMap<Integer, Integer>>();
		
		HashMap<String, Integer> videos = VideoSequenceDur.readVideo(conn); // (课程视频名, 课程视频次序)

		long stuno = 0;
		String title = "";
		Integer video_sequence = 0;
		HashMap<Integer, Integer> video_times = null;
		
		for(LearningLog aLearningLog : logs) {
			if (aLearningLog.getOper() != 76) { // 78 & 79
				stuno = aLearningLog.getStuno();
				title = aLearningLog.getTitle();
				
				video_sequence = videos.get(title);
				if (video_sequence != null) {
					if (stuno_video_pause_drag.containsKey(stuno)) { // old student
						video_times = stuno_video_pause_drag.get(stuno);				
						if (video_times.containsKey(video_sequence)) { // watched video
							video_times.put(video_sequence, video_times.get(video_sequence) + 1);
						} else { // new video
							video_times.put(video_sequence, 1);
						}
					} else { // new student with new video
						video_times = new HashMap<Integer, Integer>();
						video_times.put(video_sequence, 1);
						
						stuno_video_pause_drag.put(stuno, video_times);
					}
				}
			}
		}
		
		//System.out.println(stuno_video_pause_drag.size() + " 个学生");
		
		PreferenceTimes.storePreferenceTimes(stuno_video_pause_drag, path1, path2); // 存储
	}
	
	/**
	 * 弃用。从数据库中读取日志数据
	 * @param conn
	 * @param sql
	 * @param path1
	 * @param path2
	 */
	public static void calPreference(Connection conn, String sql, String path1, String path2) {
		PreferenceTimes.calPreference(conn, sql, path1, path2);
	}
}