package org.recommender.preference;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.recommender.utility.GetProperty;
import org.recommender.utility.StoreStringIntoFile;

/**
* @author : wuke
* @date   : 20170610 23:48:42
* Title   : DLCPreferenceReader
* Description : 
*/
public class DLCPreferenceReader implements PreferenceMatrixReader {

	public static void main(String[] args) {
		String preference_path = GetProperty.getPropertyByName("PREFERENCE_PATH");
		int user_num = Integer.parseInt(GetProperty.getPropertyByName("USER_NUM"));
		int item_num = Integer.parseInt(GetProperty.getPropertyByName("ITEM_NUM"));
		
		new DLCPreferenceReader().readPreferenceMatrix(preference_path, user_num, item_num);
	}
	@Override
	public double[][] readPreferenceMatrix(String path, int user_num, int item_num) {
		// user_num = Integer.parseInt(GetProperty.getPropertyByName("USER_NUM"));
		// item_num = Integer.parseInt(GetProperty.getPropertyByName("ITEM_NUM_NUM"));
		
		double[][] preferenceMatrix = new double[user_num][item_num];
		
		for(int i = 0; i < preferenceMatrix.length; i++) {
			for(int j = 0; j < (preferenceMatrix[i].length - 1); j++) {
				preferenceMatrix[i][j] = 0.0;
			}
		}
		
		// read from file
		int stuno_sequence = 0;
		// double stuno = 0.0;
		int item_sequence = 0;
		double preference = 0.0;
		BufferedReader br = null;
		try {
			File file = new File(path);
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);
			String line = "";
			while((line = br.readLine()) != null) {
				String[] strs = line.split(",");
				
				stuno_sequence = Integer.parseInt(strs[0]);
				// stuno = Double.parseDouble(strs[1]);
				item_sequence = Integer.parseInt(strs[2]);
				preference = Double.parseDouble(strs[3]);
				
				preferenceMatrix[stuno_sequence-1][item_sequence-1] = preference;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String path2 = GetProperty.getPropertyByName("PREFERENCE_MATRIX_PATH");
		DLCPreferenceReader.storePreferenceMatrix(preferenceMatrix, path2);
		
		return preferenceMatrix;
	}

	/**
	 * Store the preference matrix.
	 * @param preferenceMatrix
	 * @param path
	 */
	private static void storePreferenceMatrix(double[][] preferenceMatrix, String path) {
		
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < preferenceMatrix.length; i++) {
			for(int j = 0; j < (preferenceMatrix[i].length - 1); j++) {
				sb.append(preferenceMatrix[i][j]);
				sb.append(",");
			}
			sb.append(preferenceMatrix[i][preferenceMatrix[i].length - 1]);
			sb.append("\n");
		}
		
		StoreStringIntoFile.storeString(sb.toString(), path);
	}
	
}
