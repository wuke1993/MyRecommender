package org.recommender.cf.similarity;

import java.util.HashMap;

import org.recommender.cf.similarity.forum.ForumCorrelation;
import org.recommender.utility.PropertyHelper;
import org.recommender.utility.StoreStringIntoFile;

/**
* @author : wuke
* @date   : 20170611 00:38:27
* Title   : CalSimilarityMatrix
* Description : 计算用户相似度矩阵
*/
public class CalSimilarityMatrix {	
	/**
	 * Calculate Similarity Matrix. PearsonCorrelationSimilarity
	 * @param preferenceMatrix
	 * @param user_num
	 * @return similarityMatrix double[][]
	 */
	public static double[][] calSimilarityMatrix(double[][] preferenceMatrix, int user_num) {
		double[][] similarityMatrix = new double[user_num][user_num];
		
		for (int i = 0; i < user_num; i++) {
			for (int j = 0; j < user_num; j++) {
				if (i == j) {
					similarityMatrix[i][j] = 1;
				} else {
					similarityMatrix[i][j] = CalSimilarityMatrix.calTwoUsersSimilarity(preferenceMatrix[i], preferenceMatrix[j]);
				}
			}
		}
		
		String similarity_path = PropertyHelper.getProperty("SIMILARITY_PATH");
		CalSimilarityMatrix.storeSimilarityMatrix(similarityMatrix, similarity_path);
		
		return similarityMatrix;
	}
	
	/**
	 * Calculate Similarity Matrix. (pearson_similarity + coursewareTimes_similarity)
	 * @param preferenceMatrix
	 * @param user_num
	 * @param stuno_coursewareTimes
	 * @param weight_pearson
	 * @param weight_courseware
	 * @return
	 */
	public static double[][] calSimilarityMatrix(double[][] preferenceMatrix, int user_num, HashMap<Integer, Integer> stuno_coursewareTimes,
			double weight_pearson, double weight_courseware) {
		double[][] similarityMatrix = new double[user_num][user_num];
		
		for (int i = 0; i < user_num; i++) {
			for (int j = 0; j < user_num; j++) {
				if (i == j) {
					similarityMatrix[i][j] = 1;
				} else {
					similarityMatrix[i][j] = CalSimilarityMatrix.calTwoUsersSimilarity(preferenceMatrix[i], preferenceMatrix[j], stuno_coursewareTimes, 
							i + 1, j + 1, weight_pearson, weight_courseware);
				}
			}
		}
		
		String similarity_path = PropertyHelper.getProperty("SIMILARITY_PATH");
		CalSimilarityMatrix.storeSimilarityMatrix(similarityMatrix, similarity_path);
		
		return similarityMatrix;
	}
	
	/**
	 * PearsonCorrelationSimilarity
	 * @param preferenceArrX
	 * @param preferenceArrY
	 * @return
	 */
	public static double calTwoUsersSimilarity(double[] preferenceArrX, double[] preferenceArrY) {
		double similarity = 0.0;
		
		double pearson_similarity = 0.0;
		PearsonCorrelationSimilarity pearsonCS = new PearsonCorrelationSimilarity();
		pearson_similarity = pearsonCS.calSimilarity(preferenceArrX, preferenceArrY);
		
		similarity = pearson_similarity;
		
		return similarity;
	}
	
	/**
	 * pearson_similarity + coursewareTimes_similarity
	 * @param preferenceArrX
	 * @param preferenceArrY
	 * @param stuno_coursewareTimes
	 * @param stuno_sequence_x 从1开始
	 * @param stuno_sequence_y 从1开始
	 * @return
	 */
	public static double calTwoUsersSimilarity(double[] preferenceArrX, double[] preferenceArrY, HashMap<Integer, Integer> stuno_coursewareTimes, 
			int stuno_sequence_x, int stuno_sequence_y, double weight_pearson, double weight_courseware) {
		double similarity = 0.0;
		double pearson_similarity = 0.0;
		
		PearsonCorrelationSimilarity pearsonCS = new PearsonCorrelationSimilarity();
		pearson_similarity = pearsonCS.calSimilarity(preferenceArrX, preferenceArrY);
		
		// 观看课件次数的相似性 (-1, 1]
		int stuno_x_coursewareTimes = 0;
		int stuno_y_coursewareTimes = 0;
		double coursewareTimes_similarity = 0.0;
		
		stuno_x_coursewareTimes = stuno_coursewareTimes.get(stuno_sequence_x);
		stuno_y_coursewareTimes = stuno_coursewareTimes.get(stuno_sequence_y);
		if (stuno_x_coursewareTimes == stuno_y_coursewareTimes) {
			coursewareTimes_similarity = 1;
		} else if (Math.abs(stuno_x_coursewareTimes - stuno_y_coursewareTimes) >= 112) {
			coursewareTimes_similarity = 1 / (Math.abs(stuno_x_coursewareTimes - stuno_y_coursewareTimes)) - 1;
		} else {
			coursewareTimes_similarity = 1 / (Math.abs(stuno_x_coursewareTimes - stuno_y_coursewareTimes));
		}
		
		similarity = weight_pearson * pearson_similarity +  weight_courseware * coursewareTimes_similarity;
		
		return similarity;
	}
	
	/**
	 * TODO pearson_similarity + forum_correlation
	 * @param preferenceArrX
	 * @param preferenceArrY
	 * @param stuno_sequence_x  从1开始
	 * @param stuno_sequence_y  从1开始
	 * @param weight_pearson
	 * @param weight_forum_correlation
	 * @return
	 */
	public static double calTwoUsersSimilarity(double[] preferenceArrX, double[] preferenceArrY, int stuno_sequence_x, int stuno_sequence_y, 
			double weight_pearson, double weight_forum_correlation) {
		double similarity = 0.0;
		double pearson_similarity = 0.0;
		double forum_correlation = 0.0;
		
		PearsonCorrelationSimilarity pearsonCS = new PearsonCorrelationSimilarity();
		pearson_similarity = pearsonCS.calSimilarity(preferenceArrX, preferenceArrY);
		
		forum_correlation = ForumCorrelation.calForumCorrelation(stuno_sequence_x, stuno_sequence_y);
		
		similarity = weight_pearson * pearson_similarity +  weight_forum_correlation * forum_correlation;
		
		return similarity;
	}
	
	/**
	 * Store similarity matrix into file.
	 * @param similarityMatrix
	 * @param path
	 */
	private static void storeSimilarityMatrix(double[][] similarityMatrix, String path) {
		StringBuilder similarityMatrixSb = new StringBuilder();
		try {
			for (int i = 0; i < similarityMatrix.length; i++) {
				for (int j = 0; j < (similarityMatrix[i].length - 1); j++) {
					similarityMatrixSb.append(similarityMatrix[i][j]);
					similarityMatrixSb.append(",");
				}
				similarityMatrixSb.append(similarityMatrix[i][similarityMatrix[i].length - 1]);
				similarityMatrixSb.append("\n");
			}
			
			StoreStringIntoFile.storeString(similarityMatrixSb.toString(), path);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}