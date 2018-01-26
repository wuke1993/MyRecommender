package org.recommender.cf.similarity;

/**
* @author : wuke
* @date   : 20170531 22:29:21
* Title   : PearsonCorrelationSimilarity
* Description : https://en.wikipedia.org/wiki/Pearson_correlation_coefficient
*/
public class PearsonCorrelationSimilarity implements Similarity {

	public static final double DOUBLE_ZERO = 0.0000000001;
	
	public static void main(String[] args) {
		
		PearsonCorrelationSimilarity pearsonCS = new PearsonCorrelationSimilarity();
		double[] preferenceArrX = {1.0, 2.0, 3.0, 5.0, 8.0};
		double[] preferenceArrY = {0.18, 0.15, 0.13, 0.12, 0.11};
		
		System.out.println(pearsonCS.calSimilarity(preferenceArrX, preferenceArrY));
	}
	
	@Override
	public double calSimilarity(double[] preferenceArrX, double[] preferenceArrY) {
		double similarity = 0.0;
		
		int size = preferenceArrX.length;
		double sum_product = 0.0; // product, the number you get by multiplying two or more numbers in mathematics
		double sum_listA = 0.0;
		double sum_listB = 0.0;
		double sum_square_listA = 0.0;
		double sum_square_listB = 0.0;
		for (int i = 0; i < size; i++) {
			sum_product += preferenceArrX[i] * preferenceArrY[i];
			sum_listA += preferenceArrX[i];
			sum_listB += preferenceArrY[i];
			sum_square_listA += Math.pow(preferenceArrX[i], 2);
			sum_square_listB += Math.pow(preferenceArrY[i], 2);
		}
		
		double dividend = size * sum_product - sum_listA * sum_listB;
		double divisor = Math.sqrt((size * sum_square_listA - Math.pow(sum_listA, 2)) * (size * sum_square_listB - Math.pow(sum_listB, 2)));
		
		// System.out.println(dividend + " " + divisor);
		
		if (Math.abs(divisor - 0) < DOUBLE_ZERO) {
			similarity = 0.0;
		} else {
			similarity = dividend / divisor;
		}
		
		return similarity;
	}
}
