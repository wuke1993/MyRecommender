package org.recommender.measuring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

/**
* @author : wuke
* @date   : 2017年6月12日上午4:13:44
* Title   : Measuring
* Description : 
*/
public class Measuring {

	public static void calAccuracyRate(ArrayList<HashMap<Integer, Double>> recArr, HashMap<Integer, HashSet<Integer>> stuno_videos) {
		
		double accuracy = 0.0;
		double recall = 0.0;
		
		HashMap<Integer, Double> hm = null;
		HashSet<Integer> hs = null;
		int[] arr1 = null;
        int[] arr2 = null;
		for(Entry<Integer, HashSet<Integer>> entry : stuno_videos.entrySet()) {
			hm = recArr.get(entry.getKey());
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
		
		System.out.println(accuracy);
		System.out.println(recall);
	}
}
