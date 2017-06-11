package org.recommender.cf.neighborhood;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.recommender.utility.GetProperty;

/**
* @author : wuke
* @date   : 20170611 08:53:21
* Title   : CalNeighbors
* Description : Calculate all users' neighbors, which can be K-Neighbors or Threshold-based-Neighbors.
*/
public class CalNeighbors {

	public static void main(String[] args) {		
		String similarity_path = GetProperty.getPropertyByName("SIMILARITY_PATH");
		int user_num = Integer.parseInt(GetProperty.getPropertyByName("USER_NUM"));
		double[][] similarityMatrix = CalNeighbors.readSimilarityMatrix(similarity_path, user_num);
		
		int parameter_k = Integer.parseInt(GetProperty.getPropertyByName("PARAMETER_K"));
		int[][] neighborsMatrix = CalNeighbors.calKNeighbors(user_num, parameter_k, similarityMatrix);
		
		System.out.println(neighborsMatrix.toString());
	}
	
    /**
     * Calculate all users' K-Neighbors.
     * @param user_num
     * @param parameter
     * @param similarityMatrix
     * @return
     */
	public static int[][] calKNeighbors(int user_num, double parameter, double[][] similarityMatrix) {
		int[][] neighborsMatrix = new int[user_num][];
		
		for(int i = 0; i < user_num; i++)
			neighborsMatrix[i] = new FindKNeighbors().genNeighbors(parameter, i + 1, similarityMatrix[i]);
		
		return neighborsMatrix;
	}
	
	/**
	 * Calculate all users' Threshold-based-Neighbors.
	 * @param user_num
	 * @param parameter
	 * @param similarityMatrix
	 * @return
	 */
	public static int[][] calThresholdBasedeighbors(int user_num, double parameter, double[][] similarityMatrix) {
		int[][] neighborsMatrix = new int[user_num][];
		
		for(int i = 0; i < user_num; i++)
			neighborsMatrix[i] = new FindThresholdBasedNeighbors().genNeighbors(parameter, i + 1, similarityMatrix[i]);
		
		return neighborsMatrix;
	}

	/**
	 * Read similarity matrix from file.
	 * @param path
	 * @param user_num
	 * @return similarityMatrix double[][]
	 */
	public static double[][] readSimilarityMatrix(String path, int user_num) {
		
		double[][] similarityMatrix = new double[user_num][user_num];
		
		// read from file
		BufferedReader br = null;
		int row = 0;
		try {
			File file = new File(path);
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);
			String line = "";
			while((line = br.readLine()) != null) {
				String[] strs = line.split(",");
				
				for(int i = 0; i < user_num; i ++)
					similarityMatrix[row][i] = Double.parseDouble(strs[i]);
				
				row++;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(row);
		}
		
		return similarityMatrix;
	}
	
}
