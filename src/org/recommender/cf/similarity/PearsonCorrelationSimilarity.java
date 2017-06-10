package org.recommender.cf.similarity;

import java.util.ArrayList;
import java.util.List;

/**
* @author : wuke
* @date   : 20170531 22:29:21
* Title   : PearsonCorrelationSimilarity
* Description : https://en.wikipedia.org/wiki/Pearson_correlation_coefficient
*/
public class PearsonCorrelationSimilarity implements UserSimilarity {

	public static final double DOUBLE_ZERO = 0.0000000001;
	
	public static void main(String[] args) {
		
		PearsonCorrelationSimilarity pearsonCS = new PearsonCorrelationSimilarity();
		List<Double> listA = new ArrayList<Double>();
		List<Double> listB = new ArrayList<Double>();
		
		listA.add(1.0);listA.add(2.0);listA.add(3.0);listA.add(5.0);listA.add(8.0);
		listB.add(0.11);listB.add(0.12);listB.add(0.13);listB.add(0.15);listB.add(0.18);
		System.out.println(pearsonCS.calSimilarity(listA, listB));
	}
	
	@Override
	public double calSimilarity(List<Double> listA, List<Double> listB) {
		double similarity = 0.0;
		
		int size = listA.size();
		double sum_product = 0.0; // product, the number you get by multiplying two or more numbers in mathematics
		double sum_listA = 0.0;
		double sum_listB = 0.0;
		double sum_square_listA = 0.0;
		double sum_square_listB = 0.0;
		for(int i = 0; i < size; i++) {
			sum_product += listA.get(i) * listB.get(i);
			sum_listA += listA.get(i);
			sum_listB += listB.get(i);
			sum_square_listA += Math.pow(listA.get(i), 2);
			sum_square_listB += Math.pow(listB.get(i), 2);
		}
		
		double dividend = size * sum_product - sum_listA * sum_listB;
		double divisor = Math.sqrt((size * sum_square_listA - Math.pow(sum_listA, 2)) * (size * sum_square_listB - Math.pow(sum_listB, 2)));
		
		// System.out.println(dividend + " " + divisor);
		
		if(Math.abs(divisor - 0) < DOUBLE_ZERO)
			similarity = 0.0;
		else
			similarity = dividend / divisor;
			
		return similarity;
	}

}
