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

	public static void main(String[] args) {
		String preference_path = PropertyHelper.getProperty("PREFERENCE_PATH");
		int user_num = Integer.parseInt(PropertyHelper.getProperty("USER_NUM"));
		int item_num = Integer.parseInt(PropertyHelper.getProperty("ITEM_NUM"));
		
		double[][] preferenceMatrix = GenPreferenceMatrix.genPreferenceMatrix(preference_path, user_num, item_num);
	}
	
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
	
	/**
	 * 20180602
	 * @param path
	 * @param path2
	 * @param user_num
	 * @param item_num
	 * @return
	 */
	public static double[][] genPreferenceMatrix(String path, String path2,int user_num, int item_num) {		
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
				
				stuno_sequence = Integer.parseInt(strs[0]); // 从 1 开始
				// stuno = Long.parseDouble(strs[1]);
				item_sequence = Integer.parseInt(strs[2]); // 从 1 开始
				preference = Double.parseDouble(strs[3]);
				
				preferenceMatrix[stuno_sequence - 1][item_sequence - 1] = preference;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// 第一次IF-CF推荐的列表，加入到“学习者已学”，修改评分矩阵
		try {
			File file = new File(path2);
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);
			String line = "";
			int lineNum = 1;
			int item1, item2, item3, item4, item5;
			while ((line = br.readLine()) != null) {
				String[] strs = line.split(",");
				
				item1 = Integer.parseInt(strs[0]); // 从 1 开始
				item2 = Integer.parseInt(strs[1]);
				item3 = Integer.parseInt(strs[2]);
				item4 = Integer.parseInt(strs[3]);
				item5 = Integer.parseInt(strs[4]);
				
				// 计算评分的取值
				double sumP = 0.0;
				int count = 0;
				for (double p : preferenceMatrix[lineNum - 1]) {
					if (Math.abs(p - 0) > 0.00000000001) {
						sumP += p;
						count++;
					}
				}
				double temP = sumP / count;
				
				// 可以看是否已有评分，已有的话：1. 保留原始；2. 叠加；3. 赋新值 √
				preferenceMatrix[lineNum - 1][item1 - 1] = temP;
				preferenceMatrix[lineNum - 1][item2 - 1] = temP;
				preferenceMatrix[lineNum - 1][item3 - 1] = temP;
				preferenceMatrix[lineNum - 1][item4 - 1] = temP;
				preferenceMatrix[lineNum - 1][item5 - 1] = temP;
				
				lineNum++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		GenPreferenceMatrix.storePreferenceMatrix(preferenceMatrix, PropertyHelper.getProperty("NEW_PREFERENCE_MATRIX_PATH"));
		
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
