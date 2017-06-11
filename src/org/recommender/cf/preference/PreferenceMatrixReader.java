package org.recommender.cf.preference;
/**
* @author : wuke
* @date   : 20170610 23:42:17
* Title   : DataReader
* Description : 
*/
public interface PreferenceMatrixReader {
	
	double[][] readPreferenceMatrix(String path, int user_num, int item_num);
}
