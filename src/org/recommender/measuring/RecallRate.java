package org.recommender.measuring;

import java.util.HashSet;

/**
* @author : wuke
* @date   : 20170612 02:24:30
* Title   : RecallRate
* Description : 
*/
public class RecallRate {
	
	public static double calRecallRate(int[] rec, int[] test) {
		double recallRate = 0.0;
		
		HashSet<Integer> hs = new HashSet<Integer>();
		for(int tem : rec)
			hs.add(tem);
		
		int count = 0;
		for(int tem : test)
			if(hs.contains(tem))
				count++;
		
		recallRate = count / test.length;
		
		return recallRate;
	}
	
}
