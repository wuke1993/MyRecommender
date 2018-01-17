package org.recommender.cf.similarity;

import org.recommender.cf.preference.DLCPreferenceReader;
import org.recommender.utility.GetProperty;
import org.recommender.utility.StoreStringIntoFile;

/**
* @author : wuke
* @date   : 20170611 00:38:27
* Title   : CalSimilarityMatrix
* Description : Calculate similarity matrix by calling CalTwoUsersSimilarity.calTwoUsersSimilarity().
*/
public class CalSimilarityMatrix {

	public static void main(String[] args) {
		CalSimilarityMatrix calSM = new CalSimilarityMatrix();
		
		// read users' preference
		String preference_path = GetProperty.getPropertyByName("PREFERENCE_PATH");
		int user_num = Integer.parseInt(GetProperty.getPropertyByName("USER_NUM"));
		int item_num = Integer.parseInt(GetProperty.getPropertyByName("ITEM_NUM"));
		
		double[][] preferenceMatrix = new DLCPreferenceReader().readPreferenceMatrix(preference_path, user_num, item_num);
		System.out.println(preferenceMatrix.length + " students, " + preferenceMatrix[0].length + " videos!");
		
		// calculate users' similarity
		double[][] similarityMatrix = calSM.calSimilarityMatrix(preferenceMatrix, user_num);
		
		// store users' similarity
		String similarity_path = GetProperty.getPropertyByName("SIMILARITY_PATH");
		calSM.storeSimilarityMatrix(similarityMatrix, similarity_path);
		
	}
	
	/**
	 * Calculate Similarity Matrix.
	 * @param preferenceMatrix
	 * @param user_num
	 * @return similarityMatrix double[][]
	 */
	public double[][] calSimilarityMatrix(double[][] preferenceMatrix, int user_num) {
		double[][] similarityMatrix = new double[user_num][user_num];
		
		try {
		for (int i = 0; i < user_num; i++) {
			for (int j = 0; j < user_num; j++) {
				if (i == j) {
					similarityMatrix[i][j] = 1;
				} else {
					similarityMatrix[i][j] = CalTwoUsersSimilarity.calTwoUsersSimilarity(preferenceMatrix[i], preferenceMatrix[j], i, j);
				}
			}
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return similarityMatrix;
	}
	
	/**
	 * Store similarity matrix into file.
	 * @param similarityMatrix
	 * @param path
	 */
	private void storeSimilarityMatrix(double[][] similarityMatrix, String path) {
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
			
			StoreStringIntoFile.storeString(similarityMatrixSb.toString(), path, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}