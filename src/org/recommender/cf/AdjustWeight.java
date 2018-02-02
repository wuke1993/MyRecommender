package org.recommender.cf;

import java.sql.Connection;
import java.util.List;

import org.recommender.data.LearningLog;
import org.recommender.data.TrainingSetTestSet;
import org.recommender.utility.MySQLHelper;

/**
* @author : wuke
* @date   : 20180202 17:51:01
* Title   : AdjustWeight
* Description : 
*/
public class AdjustWeight {

	public static void main(String[] args) {
        Connection conn = MySQLHelper.getConn();
		
		// 训练集 & 测试集
        List<LearningLog> logs = TrainingSetTestSet.readLogs(conn);
        List<List<LearningLog>> logsAll = TrainingSetTestSet.genTrainingSetTestSet(logs); // 5份
        
        List<LearningLog> trainSet = logsAll.get(0);
        trainSet.addAll(logsAll.get(1));trainSet.addAll(logsAll.get(2));trainSet.addAll(logsAll.get(3));
        List<LearningLog> testSet = logsAll.get(4);
        
        AdjustWeight.adjustSimilarityWeight(conn, trainSet, testSet);
	}
	
	public static void adjustPreferenceWeight(Connection conn, List<LearningLog> trainSet, List<LearningLog> testSet) {
		double[] accuracy_recall = null;
        double accuracy = 0.0;
        double recall = 0.0;
        
        /*int i = 1;
        int j = 1;
        int l = 1;
        CFRecommender.initPreference(conn, trainSet, i, j, l);
		CFRecommender.initPreferenceNeighborsMatrix(conn);
		for (int n = 3; n <= 9; n++) {
			for (int k = 2; k <= 5; k++) {
				accuracy_recall = CFRecommender.measureRec(conn, n, k, testSet);
				
				accuracy += accuracy_recall[0];
				recall += accuracy_recall[1];
			}
		}
		System.out.println(i + "," + j + "," + l);
		System.out.println(accuracy + "," + recall);*/
        
        for (int i = 0; i <= 10; i++) {
        	for (int j = 0; j <= (10 - i); j++) {
        		int l = 10 - i -j;
        		
        		if (0 <= l && l <= 10) {
        			
        			accuracy = 0.0;
        			recall = 0.0;
        			
        			CFRecommender.initPreference(conn, trainSet, i, j, l);
        			
        			CFRecommender.initPreferenceNeighborsMatrix(conn);
        			
        			for (int n = 3; n <= 9; n++) {
        				for (int k = 2; k <= 5; k++) {
        					accuracy_recall = CFRecommender.measureRec(conn, n, k, testSet);
        					
        					accuracy += accuracy_recall[0];
        					recall += accuracy_recall[1];
        				}
        			}
        			
        			System.out.println(i + "," + j + "," + l);
        			System.out.println(accuracy + "," + recall);
        		}
        	}
        }
	}
	
	public static void adjustSimilarityWeight(Connection conn, List<LearningLog> trainSet, List<LearningLog> testSet) {
		double[] accuracy_recall = null;
        double accuracy = 0.0;
        double recall = 0.0;
        
        /*double i = 0;
        double j = 1;
		CFRecommender.initPreferenceNeighborsMatrix(conn, i, j);
		for (int n = 3; n <= 20; n++) {
			for (int k = 2; k <= 5; k++) {
				accuracy_recall = CFRecommender.measureRec(conn, n, k, testSet);
				
				accuracy += accuracy_recall[0];
				recall += accuracy_recall[1];
			}
		}
		System.out.println(i + "," + j);
		System.out.println(accuracy + "," + recall);*/
        
        for (int i = 1; i < 10; i++) {
        	int j = 10 - i;
        	
			accuracy = 0.0;
			recall = 0.0;
			
			CFRecommender.initPreferenceNeighborsMatrix(conn, i, j);
			
			for (int n = 3; n <= 20; n++) {
				for (int k = 2; k <= 5; k++) {
					accuracy_recall = CFRecommender.measureRec(conn, n, k, testSet);
					
					accuracy += accuracy_recall[0];
					recall += accuracy_recall[1];
				}
			}
			
			System.out.println(i + "," + j);
			System.out.println(accuracy + "," + recall);
        }
	}
}
