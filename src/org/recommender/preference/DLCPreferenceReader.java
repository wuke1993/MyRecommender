package org.recommender.preference;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
* @author : wuke
* @date   : 20170610 23:48:42
* Title   : DLCPreferenceReader
* Description : 
*/
public class DLCPreferenceReader implements PreferenceMatrixReader {

	@Override
	public double[][] readPreferenceMatrix(String path, int user_num, int item_num) {
		// user_num = Integer.parseInt(GetProperty.getPropertyByName("USER_NUM"));
		// item_num = Integer.parseInt(GetProperty.getPropertyByName("ITEM_NUM_NUM"));
		
		double[][] preferenceMatrix = new double[user_num][item_num];
		
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
		
		return preferenceMatrix;
	}

}
