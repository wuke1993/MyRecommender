package org.recommender.utility;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
* @author : wuke
* @date   : 20170602 17:19:13
* Title   : StoreStringIntoFile
* Description : 
*/
public class StoreStringIntoFile {
	
	public static void storeString(String str, String path) {
		File file = new File(path);
		FileWriter fw = null;
		
		try {
			fw = new FileWriter(file);
			fw.write(str);
			
			System.out.println("Successfully store file in " + path);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
