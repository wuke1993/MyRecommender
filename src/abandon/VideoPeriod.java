package abandon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.recommender.cf.similarity.PearsonCorrelationSimilarity;
import org.recommender.utility.MySQLHelper;
import org.recommender.utility.PropertyHelper;

/**
* @author : wuke
* @date   : 20180106 20:58:01 & 20180426
* Title   : VideoPeriod
* Description : 课程视频学期阶段属性
*/
public class VideoPeriod {
	private static String VideosPath = "e:\\data\\videoPeriod\\videos.txt";
	
	public static void main(String[] args) {
		Connection conn = MySQLHelper.getConn();
		
		HashMap<String, Integer> videosNameId = VideoPeriod.getVideoId(VideosPath); // 获取 (视频名, 视频编号) TODO 此处编号从 0开始
		/*for (Entry<String, Integer> entry : videosNameId.entrySet()) {
    		System.out.println(entry.getKey() + " " + entry.getValue());
    	}*/
        
        HashMap<String, HashMap<Integer, Integer>> videosWeekTimes = VideoPeriod.initVideosWeekTimes(conn); // 初始化 (视频名, (周次, 0))
        videosWeekTimes = VideoPeriod.genVideosTimes(conn, videosWeekTimes); // 生成 (视频名, (周次, 当前视频在当前周被学习的次数))
        /*for (Entry<String, HashMap<Integer, Integer>> aEntry: videosWeekTimes.entrySet()) {
        	String videoName = aEntry.getKey();
        	System.out.println(videoName);
        }*/
        
        HashMap<Integer, HashMap<Integer, Double>> videosPeriod = VideoPeriod.genVideosPeriod(videosWeekTimes, videosNameId); // (视频名, (周次, 课程视频学期阶段属性的一项))
        for (Entry<Integer, HashMap<Integer, Double>> aEntry: videosPeriod.entrySet()) {
        	int videoId = aEntry.getKey();
        	System.out.print(videoId + "->");
        	HashMap<Integer, Double> weekPeriod = aEntry.getValue();
        	for (Entry<Integer, Double> bEntry : weekPeriod.entrySet()) {
        		int week = bEntry.getKey();
        		double aPeriod = bEntry.getValue();
        		
        		System.out.print(week + "-" + aPeriod + "  ");
        	}
        	System.out.println();
        }
        
        /*double[] simPeriods = VideoPeriod.simPeriod(0, videosPeriod);
        for (double sim : simPeriods) {
        	System.out.print(sim + " ");
        }*/
	}
	
	/**
	 * 返回某视频与其它视频的课程学期阶段属性相似度
	 * @param videoId
	 * @param videosPeriod
	 * @return
	 */
	public static double[] simPeriod(int videoId, HashMap<Integer, HashMap<Integer, Double>> videosPeriod) {
		int itemNum = Integer.parseInt(PropertyHelper.getProperty("ITEM_NUM"));
		double[] simPeriods = new double[itemNum];
		
		PearsonCorrelationSimilarity pearsonCS = new PearsonCorrelationSimilarity();
		
		double[] target = new double[52];
		double[] compare = new double[52];
		for (int i = 0; i < itemNum; i++) {
			for (int j = 0; j < 52; j++) { // 周次
				target[j] = videosPeriod.get(videoId).get(j + 1);
				compare[j] = videosPeriod.get(i).get(j + 1);
			}
			
			simPeriods[i] = 0.5 + 0.5 * pearsonCS.calSimilarity(target, compare);
		}
		
		return simPeriods;
	}
	
	/**
	 * 返回某视频与其它视频的课程学期阶段属性相似度，对外提供接口
	 * @param videoId
	 * @return
	 */
	public static double[] simPeriod(int videoId) {
		Connection conn = MySQLHelper.getConn();
		HashMap<String, Integer> videosNameId = VideoPeriod.getVideoId(VideosPath);
		HashMap<String, HashMap<Integer, Integer>> videosWeekTimes = VideoPeriod.initVideosWeekTimes(conn);
        videosWeekTimes = VideoPeriod.genVideosTimes(conn, videosWeekTimes);
		HashMap<Integer, HashMap<Integer, Double>> videosPeriod = VideoPeriod.genVideosPeriod(videosWeekTimes, videosNameId);
		
		int itemNum = Integer.parseInt(PropertyHelper.getProperty("ITEM_NUM"));
		double[] simPeriods = new double[itemNum];
		
		PearsonCorrelationSimilarity pearsonCS = new PearsonCorrelationSimilarity();
		
		double[] target = new double[52];
		double[] compare = new double[52];
		for (int i = 0; i < itemNum; i++) {
			for (int j = 0; j < 52; j++) { // 周次
				target[j] = videosPeriod.get(videoId).get(j + 1);
				compare[j] = videosPeriod.get(i).get(j + 1);
			}
			
			simPeriods[i] = 0.5 + 0.5 * pearsonCS.calSimilarity(target, compare);
		}
		
		return simPeriods;
	}
	
	/**
	 * 生成课程视频学期阶段属性， 一维行向量
	 * @param videosWeekTimes (视频名, (周次, 课程视频学期阶段属性的一项))
	 * @param videosNameId
	 * @return
	 */
	public static HashMap<Integer, HashMap<Integer, Double>> genVideosPeriod(HashMap<String, HashMap<Integer, Integer>> videosWeekTimes,
			HashMap<String, Integer> videosNameId) {
		HashMap<Integer, HashMap<Integer, Double>> videosPeriod = new HashMap<Integer, HashMap<Integer, Double>>(); // (视频名, (周次, 课程视频学期阶段属性的一项))
		int videoId = 0; // 课程视频编号，从 0 开始
		HashMap<Integer, Double> oneVideoWeekPeriod = null; // 某课程视频的 (周次, 课程视频学期阶段属性的一项)
		
		// 
		Set<Entry<String, HashMap<Integer, Integer>>> aEntrySet = videosWeekTimes.entrySet(); // (视频名, (周次, 当前视频在当前周被学习的次数))
        String videoName = null; // 视频名
        HashMap<Integer, Integer> weekTimes = null; // 某课程视频的 (周次, 当前视频在当前周被学习的次数)
        for (Entry<String, HashMap<Integer, Integer>> aEntry : aEntrySet){
        	videoName = aEntry.getKey();
        	weekTimes = aEntry.getValue();
        	
        	oneVideoWeekPeriod = new HashMap<Integer, Double>();
        	
        	// 某个视频 52 个周总的学习次数 TODO 修改成 24 个周
        	Set<Entry<Integer, Integer>> bEntrySet = weekTimes.entrySet();
        	double totalTimes = 0;
        	for (Entry<Integer, Integer> bEntry : bEntrySet) { // 计算某视频在全部周次的总学习次数
        		totalTimes += bEntry.getValue();
        	}
        	
        	// 课程视频学期阶段属性计算
        	double period = 0.0;
        	for (Entry<Integer, Integer> bEntry : bEntrySet) {
        		period = bEntry.getValue() / totalTimes;
        		
        		oneVideoWeekPeriod.put(bEntry.getKey(), period);
        	}
        	
        	videoId = videosNameId.get(videoName);
        	videosPeriod.put(videoId, oneVideoWeekPeriod);
        }
		
		return videosPeriod;
	}
	
	/**
	 * (视频名, (周次, 当前视频在当前周被学习的次数))
	 * @param conn
	 * @param videosWeekTimes
	 * @return
	 */
	private static HashMap<String, HashMap<Integer, Integer>> genVideosTimes(Connection conn, HashMap<String, HashMap<Integer, Integer>> videosWeekTimes) {
		String sql = "SELECT rtime, title FROM my_cs_log_stulearns_4th";
		PreparedStatement ps;
		try {
			ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			
			String date = null;
			String videoName = null;
			int weekOfYear = 0;
			HashMap<Integer, Integer> weekTimes = null;
			int times = 0;
			while (rs.next()) {
				date = rs.getString(1).substring(0, 10);
				videoName = rs.getString(2);
				
				weekOfYear = VideoPeriod.getWeekOfYear(date);
				
				weekTimes = videosWeekTimes.get(videoName);
				
				if (weekTimes != null) {
					times = weekTimes.get(weekOfYear);
					
					weekTimes.put(weekOfYear,  times + 1);
					
					videosWeekTimes.put(videoName, weekTimes);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return videosWeekTimes;
	}
	
	/**
	 * (视频名, (周次, 0))
	 * @param conn
	 * @return
	 */
	private static HashMap<String, HashMap<Integer, Integer>> initVideosWeekTimes(Connection conn) {
		HashMap<String, HashMap<Integer, Integer>> videosWeekTimes = new HashMap<String, HashMap<Integer, Integer>>();
		
		String sql = "SELECT title FROM kj_courseitems WHERE cid IN (485, 658, 862)";
		PreparedStatement ps;
		try {
			ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			
			HashMap<Integer, Integer> weekTimes = null;
			String videoName = null;
			while (rs.next()) {
				weekTimes = new HashMap<Integer, Integer>();
				videoName = rs.getString(1);
				
				videosWeekTimes.put(videoName, weekTimes);
				for (int i = 1; i < 53; i++) {
					videosWeekTimes.get(videoName).put(i, 0);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return videosWeekTimes;
	}
	
	/**
	 * 根据日期返回其在某年中的第几周
	 * @param arg
	 * @return
	 */
	public static int getWeekOfYear(String arg) {
		int result = 0;
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
			date = sdf.parse(arg);
		} catch (ParseException e) {
			e.printStackTrace();
		}
        
        Calendar calendar = Calendar.getInstance();  
        calendar.setFirstDayOfWeek(Calendar.MONDAY);  
        calendar.setTime(date);  
          
        result = calendar.get(Calendar.WEEK_OF_YEAR);
		
		return result;
	}
	
	/**
	 * 获取课程视频的 id ，从 0 开始
	 * @return
	 */
	public static HashMap<String, Integer> getVideoId(String path) {
		HashMap<String, Integer> video_Name_Id = new HashMap<String, Integer>();
        File file = new File(path);
		BufferedReader reader = null;
		int i = 0;
		try {
			reader = new BufferedReader(new FileReader(file));
			String str = null;
			while((str = reader.readLine()) != null) {
				video_Name_Id.put(str, i);
				i++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return video_Name_Id;
	}
}
