package org.recommender.measuring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

/**
* @author : wuke
* @date   : 20170612 4:13:44
* Title   : Measuring
* Description : 准确率 & 召回率
*/
public class Measuring {
	public static double[] calAccuracyRecallRate(ArrayList<HashMap<Integer, Double>> recArr, HashMap<Integer, HashSet<Integer>> stuno_videos) {
		double[] accuracy_recall = new double[2];
		double accuracy = 0.0;
		double recall = 0.0;
		
		HashMap<Integer, Double> hm = null; // 给某用户推荐的视频列表
		HashSet<Integer> hs = null; // 用户实际观看的列表
		int[] arr1 = null;
        int[] arr2 = null;
		for(Entry<Integer, HashSet<Integer>> entry : stuno_videos.entrySet()) {
			hm = recArr.get(entry.getKey() - 1);
			hs = entry.getValue();			
			
			arr1 = new int[hm.size()];
			int index = 0;
			for(int t : hm.keySet())
				arr1[index++] = t;
			
			arr2 = new int[hs.size()];
			int index2 = 0;
			for(int t : hs)
				arr2[index2++] = t;
			
			accuracy += AccuracyRate.calAccuracyRate(arr1, arr2);
			recall += RecallRate.calRecallRate(arr1, arr2);
		}
		
		accuracy /= stuno_videos.size();
		recall /= stuno_videos.size();
		
		accuracy_recall[0] = accuracy;
		accuracy_recall[1] = recall;
		
		/*System.out.println(accuracy);
		System.out.println(recall);*/
		
		return accuracy_recall;
	}
	
	/*public static void test(String[] args) {
		double accuracy = 15.555;
		DecimalFormat df = new DecimalFormat("#.00");
		System.out.println(df.format(accuracy)); // 15.55
		
		double f = 3.105;
		BigDecimal b = new BigDecimal(f);
		double f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue(); // 3.1
		System.out.println(f1);
		
		double d = 3.151;
        BigDecimal bg = new BigDecimal(d).setScale(2, RoundingMode.UP); // 3.16
        System.out.println(bg.doubleValue());
	}*/
}
