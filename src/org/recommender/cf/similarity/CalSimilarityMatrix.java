package org.recommender.cf.similarity;

import java.io.File;
import java.io.FileWriter;

import org.recommender.cf.preference.DLCPreferenceReader;
import org.recommender.utility.GetProperty;

/**
* @author : wuke
* @date   : 20170611 00:38:27
* Title   : CalSimilarityMatrix
* Description : Calculate similarity matrix by calling CalTwoUsersSimilarity.calTwoUsersSimilarity().
*/
public class CalSimilarityMatrix {

	public static void main(String[] args) {
		
		CalSimilarityMatrix calSM = new CalSimilarityMatrix();
		
		long start = System.currentTimeMillis();
		
		// read users' preference
		String preference_path = GetProperty.getPropertyByName("PREFERENCE_PATH");
		int user_num = Integer.parseInt(GetProperty.getPropertyByName("USER_NUM"));
		int item_num = Integer.parseInt(GetProperty.getPropertyByName("ITEM_NUM"));
		
		double[][] preferenceMatrix = new DLCPreferenceReader().readPreferenceMatrix(preference_path, user_num, item_num);
		
		System.out.println("****** Successfully read users' preference! ******");
		System.out.println(preferenceMatrix.length + " students, " + preferenceMatrix[0].length + " videos!");
		long cost = (System.currentTimeMillis() - start) / 1000;
		System.out.println("****** Cost " + cost + "s! ******");
		
		// calculate users' similarity
		double[][] similarityMatrix = calSM.calSimilarityMatrix(preferenceMatrix, user_num);
		
		System.out.println("****** Successfully calculate users' similarity! ******");
		cost = (System.currentTimeMillis() - start) / 1000;
		System.out.println("****** Cost " + cost + "s! ******");
		
		// store users' similarity
		/*String similarity_path = GetProperty.getPropertyByName("SIMILARITY_PATH");
		//String similarity_with_forum_correlation_path = GetProperty.getPropertyByName("SIMILARITY_WITH_FORUM_CORRELATION_PATH");
		
		calSM.storeSimilarityMatrix(similarityMatrix, similarity_path);
		System.out.println("****** Successfully store users' similarity! ******");
		
		cost = (System.currentTimeMillis() - cost - start) / 1000;
		System.out.println("****** Cost " + cost + "s! ******");*/
		
	}
	
	/**
	 * Calculate Similarity Matrix.
	 * @param preferenceMatrix
	 * @param user_num
	 * @return similarityMatrix double[][]
	 */
	public double[][] calSimilarityMatrix(double[][] preferenceMatrix, int user_num) {
		double[][] similarityMatrix = new double[user_num][user_num];
		
		int i = 0;
		int j = 0;
		try {
		for (i = 0; i < user_num; i++) {
			for (j = 0; j < user_num; j++) {
				if (i == j) {
					similarityMatrix[i][j] = 1;
				} else {
					similarityMatrix[i][j] = CalTwoUsersSimilarity.calTwoUsersSimilarity(preferenceMatrix[i], preferenceMatrix[j], i, j);
				}
			}
		}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(i + " " + j);
		}
		
		return similarityMatrix;
	}
	
	/**
	 * Store similarity matrix into file.
	 * @param similarityMatrix
	 * @param path
	 * Too big for StoreStringIntoFile.storeString() to handle.
	 * TODO Values missed in the last line. 
	 */	
	private void storeSimilarityMatrix(double[][] similarityMatrix, String path) {
		
		File file = new File(path);
		FileWriter fw = null;
		try {
			fw = new FileWriter(file, true); // true, append content
			
			StringBuilder similarityMatrixSb = null;
			
			for(int i = 0; i < similarityMatrix.length; i++) {
				similarityMatrixSb = new StringBuilder();
				for(int j = 0; j < (similarityMatrix[i].length - 1); j++) {
					similarityMatrixSb.append(similarityMatrix[i][j]);
					similarityMatrixSb.append(",");
				}
				similarityMatrixSb.append(similarityMatrix[i][similarityMatrix[i].length - 1]);
				
				similarityMatrixSb.append("\n");
				
				fw.write(similarityMatrixSb.toString());
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}