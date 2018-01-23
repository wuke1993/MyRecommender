package org.recommender.cf.similarity;

import org.recommender.cf.preference.GenPreferenceMatrix;
import org.recommender.cf.similarity.forum.ForumCorrelation;
import org.recommender.cf.similarity.forum.ForumPostAnswerCorrelation;
import org.recommender.utility.PropertyHelper;
import org.recommender.utility.StoreStringIntoFile;

/**
* @author : wuke
* @date   : 20170611 00:38:27
* Title   : CalSimilarityMatrix
* Description : Calculate similarity matrix by calling CalTwoUsersSimilarity.calTwoUsersSimilarity().
*/
public class CalSimilarityMatrix {

	/*public static void main(String[] args) {
		// read users' preference
		String preference_path = GetProperty.getPropertyByName("PREFERENCE_PATH");
		int user_num = Integer.parseInt(GetProperty.getPropertyByName("USER_NUM"));
		int item_num = Integer.parseInt(GetProperty.getPropertyByName("ITEM_NUM"));
		
		double[][] preferenceMatrix = GenPreferenceMatrix.genPreferenceMatrix(preference_path, user_num, item_num);
		System.out.println(preferenceMatrix.length + " students, " + preferenceMatrix[0].length + " videos!");
		
		// calculate users' similarity
		double[][] similarityMatrix = CalSimilarityMatrix.calSimilarityMatrix(preferenceMatrix, user_num);
		
		// store users' similarity
		String similarity_path = GetProperty.getPropertyByName("SIMILARITY_PATH");
		CalSimilarityMatrix.storeSimilarityMatrix(similarityMatrix, similarity_path);
		
	}*/
	
	/**
	 * Calculate Similarity Matrix.
	 * @param preferenceMatrix
	 * @param user_num
	 * @return similarityMatrix double[][]
	 */
	public static double[][] calSimilarityMatrix(double[][] preferenceMatrix, int user_num) {
		double[][] similarityMatrix = new double[user_num][user_num];
		
		try {
		for (int i = 0; i < user_num; i++) {
			for (int j = 0; j < user_num; j++) {
				if (i == j) {
					similarityMatrix[i][j] = 1;
				} else {
					similarityMatrix[i][j] = CalSimilarityMatrix.calTwoUsersSimilarity(preferenceMatrix[i], preferenceMatrix[j]);
				}
			}
		}
		} catch (Exception e) {
			e.printStackTrace();
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
	 * TODO pearson_similarity +  weight_forum_correlation * forum_correlation
	 * @param preferenceArrX
	 * @param preferenceArrY
	 * @param stuno_sequence_x
	 * @param stuno_sequence_y
	 * @param weight_forum_correlation
	 * @return
	 */
	public static double calTwoUsersSimilarity(double[] preferenceArrX, double[] preferenceArrY, int stuno_sequence_x, int stuno_sequence_y, 
			double weight_forum_correlation) {
		double similarity = 0.0;
		double pearson_similarity = 0.0;
		double forum_correlation = 0.0;
		
		PearsonCorrelationSimilarity pearsonCS = new PearsonCorrelationSimilarity();
		pearson_similarity = pearsonCS.calSimilarity(preferenceArrX, preferenceArrY);
		
		similarity = pearson_similarity;
		ForumCorrelation forumCorrelation = new ForumPostAnswerCorrelation();
		forum_correlation = forumCorrelation.calForumCorrelation(stuno_sequence_x, stuno_sequence_y);
		
		similarity = pearson_similarity +  weight_forum_correlation * forum_correlation;
		
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