package org.recommender.cf.preference;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map.Entry;

import org.recommender.utility.GetProperty;
import org.recommender.utility.StoreStringIntoFile;

/**
* @author : wuke
* @date   : 20170609 20:37:23
* Title   : CalPreference
* Description : TODO 组合三种评分。线性加权 & 向量 
*/
public class CalPreference {

	public static void main(String[] args) {

		CalPreference calculator= new CalPreference();
		
		double coefficient_times = Double.parseDouble(GetProperty.getPropertyByName("WEIGHT_TIMES"));
		double coefficient_pause_drag = Double.parseDouble(GetProperty.getPropertyByName("WEIGHT_PAUSE_DRAG"));
		double coefficient_duration = Double.parseDouble(GetProperty.getPropertyByName("WEIGHT_DURATION"));
		
		String path1 = GetProperty.getPropertyByName("PREFERENCE_TIMES_PATH");
		String path2 = GetProperty.getPropertyByName("PREFERENCE_PAUSE_DRAG_PATH");
		String path3 = GetProperty.getPropertyByName("PREFERENCE_DURATION_PATH");
		String path4 = GetProperty.getPropertyByName("PREFERENCE_PATH");
		calculator.calPreference(coefficient_times, coefficient_pause_drag, coefficient_duration, path1, path2, path3, path4);
	}
	
	public void calPreference(double coefficient_times, double coefficient_pause_drag, double coefficient_duration, 
			String path1, String path2, String path3, String path4) {
		HashMap<Long, HashMap<Integer, Double>> stuno_video_preference = new HashMap<Long, HashMap<Integer, Double>>();
		
		HashMap<Long, HashMap<Integer, Double>> stuno_video_preferenceTimes = this.readPreference(path1);
		HashMap<Long, HashMap<Integer, Double>> stuno_video_preferencePauseDrag = this.readPreference(path2);
		HashMap<Long, HashMap<Integer, Double>> stuno_video_preferenceDuration = this.readPreference(path3);
		
		long stuno = 0;
		HashMap<Integer, Double> video_preferenceTimes = null;
		for (Entry<Long, HashMap<Integer, Double>> entry : stuno_video_preferenceTimes.entrySet()) {
			stuno = entry.getKey();
			video_preferenceTimes = entry.getValue();
			
			int video_sequence = 0;
			Double preference = 0.0;
			Double preferenceTimes = 0.0;
			Double preferencePauseDrag = 0.0;
			Double preferenceDuration = 0.0;
			
			HashMap<Integer, Double> video_preference = new HashMap<Integer, Double>();
			for (Entry<Integer, Double> entry2 : video_preferenceTimes.entrySet()) {
				video_sequence = entry2.getKey();
				preferenceTimes = entry2.getValue();
				
				if (stuno_video_preferencePauseDrag.get(stuno) == null || stuno_video_preferencePauseDrag.get(stuno).get(video_sequence) == null) {
					preferencePauseDrag = 0.0;
				} else {
					preferencePauseDrag = stuno_video_preferencePauseDrag.get(stuno).get(video_sequence);
				}
				
				preferenceDuration = stuno_video_preferenceDuration.get(stuno).get(video_sequence);
				
				preference = coefficient_times * preferenceTimes + coefficient_pause_drag * preferencePauseDrag + coefficient_duration * preferenceDuration;
				
				video_preference.put(video_sequence, preference);
			}
			stuno_video_preference.put(stuno, video_preference);
		}
		
		this.storePreferenceTimes(stuno_video_preference, path4);
	}
	
	/**
	 * 从文件中读取评分，存入 HashMap(stuno, (viedo_sequence, preference))
	 * @param path
	 * @return
	 */
	private HashMap<Long, HashMap<Integer, Double>> readPreference(String path) {
		HashMap<Long, HashMap<Integer, Double>> stuno_video_preference = new HashMap<Long, HashMap<Integer, Double>>();
		
		long stuno = 0;
		int video_sequence = 0;
		double preference = 0;
		BufferedReader reader = null;
		try {
			FileInputStream fileInputStream = new FileInputStream(path);
			InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
			reader = new BufferedReader(inputStreamReader);
			
			String str = "";
			HashMap<Integer, Double> video_preference = null;
			while ((str = reader.readLine()) != null) {
				String[] strArr = str.split(",");
				
				stuno = Long.parseLong(strArr[0]);
				video_sequence = Integer.parseInt(strArr[1]);
				preference = Double.parseDouble(strArr[2]);
				
				if (stuno_video_preference.containsKey(stuno)) { // old user
					video_preference = stuno_video_preference.get(stuno);
					
					video_preference.put(video_sequence, preference);
				} else { // new user
					video_preference = new HashMap<Integer, Double>();
					video_preference.put(video_sequence, preference);
					
					stuno_video_preference.put(stuno, video_preference);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return stuno_video_preference;
	}
	
	/**
	 * 存储评分矩阵(stuno_sequence, stuno, video_sequence, preference)
	 * @param stuno_video_preference
	 * @param path
	 */
	private void storePreferenceTimes(HashMap<Long, HashMap<Integer, Double>> stuno_video_preference, String path) {
		StringBuilder preferenceSb = new StringBuilder();
		
		int stuno_sequence = 0;
		long stuno = 0;
		int video_sequence = 0;
		double preference = 0;
		
		for (Entry<Long, HashMap<Integer, Double>> entry : stuno_video_preference.entrySet()) {
			stuno_sequence += 1;
			stuno = entry.getKey();
			HashMap<Integer, Double> video_preference = entry.getValue();
			
			for (Entry<Integer, Double> entry2 : video_preference.entrySet()) {				
				video_sequence = entry2.getKey();
				preference = entry2.getValue();
				
				preferenceSb.append(stuno_sequence);
				preferenceSb.append("," + stuno);
				preferenceSb.append("," + video_sequence);
				preferenceSb.append("," + preference);
				preferenceSb.append("\n");
			}
		}
		
		StoreStringIntoFile.storeString(preferenceSb.toString(), path, false);
	}
	
}
