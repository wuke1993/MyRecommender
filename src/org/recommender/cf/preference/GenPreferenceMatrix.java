package org.recommender.cf.preference;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.recommender.utility.PropertyHelper;
import org.recommender.utility.StoreStringIntoFile;

/**
* @author : wuke
* @date   : 20170610 23:48:42
* Title   : GenPreferenceMatrix
* Description : 利用之前操作产生的评分记录文件生成评分矩阵
*/
public class GenPreferenceMatrix {

	/*public static void main(String[] args) {
		String preference_path = GetProperty.getPropertyByName("PREFERENCE_PATH");
		int user_num = Integer.parseInt(GetProperty.getPropertyByName("USER_NUM"));
		int item_num = Integer.parseInt(GetProperty.getPropertyByName("ITEM_NUM"));
		
		double[][] preferenceMatrix = GenPreferenceMatrix.genPreferenceMatrix(preference_path, user_num, item_num);
	}*/
	
	public static double[][] genPreferenceMatrix(String path, int user_num, int item_num) {		
		double[][] preferenceMatrix = new double[user_num][item_num];
		
		int stuno_sequence = 0;
		// long stuno = 0.0;
		int item_sequence = 0;
		double preference = 0.0;
		BufferedReader br = null;
		try {
			File file = new File(path);
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);
			String line = "";
			while ((line = br.readLine()) != null) {
				String[] strs = line.split(",");
				
				stuno_sequence = Integer.parseInt(strs[0]);
				// stuno = Long.parseDouble(strs[1]);
				item_sequence = Integer.parseInt(strs[2]);
				preference = Double.parseDouble(strs[3]);
				
				preferenceMatrix[stuno_sequence-1][item_sequence-1] = preference;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		GenPreferenceMatrix.storePreferenceMatrix(preferenceMatrix, PropertyHelper.getProperty("PREFERENCE_MATRIX_PATH"));
		
		return preferenceMatrix;
	}
	
	private static void storePreferenceMatrix(double[][] preferenceMatrix, String path) {
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < preferenceMatrix.length; i++) {
			for (int j = 0; j < (preferenceMatrix[i].length - 1); j++) {
				sb.append(preferenceMatrix[i][j]);
				sb.append(",");
			}
			sb.append(preferenceMatrix[i][preferenceMatrix[i].length - 1]);
			sb.append("\n");
		}
		
		StoreStringIntoFile.storeString(sb.toString(), path);
	}
}
