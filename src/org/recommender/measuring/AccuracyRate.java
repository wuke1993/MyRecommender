package org.recommender.measuring;

import java.util.HashSet;

/**
* @author : wuke
* @date   : 20170612 02:24:10
* Title   : AccuracyRate
* Description : 
*/
public class AccuracyRate {

	public static double calAccuracyRate(int[] rec, int[] test) {
		double accuracyRate = 0.0;
		
		HashSet<Integer> hs = new HashSet<Integer>();
		for(int tem : test)
			hs.add(tem);
		
		int count = 0;
		for(int tem : rec)
			if(hs.contains(tem))
				count++;
		
		accuracyRate = count / rec.length;
		
		return accuracyRate;
	}
}
