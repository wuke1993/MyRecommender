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
        
        //AdjustWeight.adjustPreferenceWeight(conn, trainSet, testSet);
        AdjustWeight.adjustSimilarityWeight(conn, trainSet, testSet);
	}
	
	public static void adjustPreferenceWeight(Connection conn, List<LearningLog> trainSet, List<LearningLog> testSet) {
		double[] accuracy_recall = null;
        double accuracy = 0.0;
        double recall = 0.0;
        
		// 三个占比共 10 份，包含66种情况
        for (int i = 0; i <= 10; i++) {
        	for (int j = 0; j <= (10 - i); j++) {
        		int l = 10 - i -j;
        		
        		if (0 <= l && l <= 10) {
        			accuracy = 0.0;
        			recall = 0.0;
        			
        			CFRecommender.initPreference(conn, trainSet, i, j, l);
        			CFRecommender.initPreferenceNeighborsMatrix(conn);
        			
        			for (int k = 3; k <= 20; k++) {
        				for (int n = 2; n <= 5; n++) {
        					CFRecommender.genRecommendation(k, n);
        					
        					accuracy_recall = CFRecommender.measureRec(conn, testSet);
        					
        					accuracy += accuracy_recall[0];
        					recall += accuracy_recall[1];
        				}
        			}
        			
        			//System.out.println(i + "," + j + "," + l);
        			System.out.println(accuracy / 72 + "," + recall / 72);
        		}
        	}
        }
        
        /*// 1:1:1
        CFRecommender.initPreference(conn, trainSet, 1, 1, 1);
		CFRecommender.initPreferenceNeighborsMatrix(conn);
		for (int k = 3; k <= 20; k++) {
			for (int n = 2; n <= 5; n++) {
			    CFRecommender.genRecommendation(k, n);
			
				accuracy_recall = CFRecommender.measureRec(conn, testSet);
				
				accuracy += accuracy_recall[0];
				recall += accuracy_recall[1];
			}
		}
		System.out.println(accuracy / 72 + "," + recall / 72);*/
	}
	
	public static void adjustSimilarityWeight(Connection conn, List<LearningLog> trainSet, List<LearningLog> testSet) {
		double[] accuracy_recall = null;
        double accuracy = 0.0;
        double recall = 0.0;
        
        // 两个占比共 10 份，包含9种情况
        /*for (int i = 1; i < 10; i++) {
        	int j = 10 - i;
        	
			accuracy = 0.0;
			recall = 0.0;
			
			CFRecommender.initPreferenceNeighborsMatrix(conn, i, j);
			
			for (int k = 3; k <= 20; k++) {
				for (int n = 2; n <= 5; n++) {
					CFRecommender.genRecommendation(k, n);
					accuracy_recall = CFRecommender.measureRec(conn, testSet);
					
					accuracy += accuracy_recall[0];
					recall += accuracy_recall[1];
				}
			}
			
			//System.out.println(i + "," + j);
			System.out.println(accuracy / 72 + "," + recall / 72);
        }*/
        
        // 取值 (1, 0) 和 (0, 1)
        CFRecommender.initPreferenceNeighborsMatrix(conn, 100, 1); // 0.13911740808450124,0.16120759699370302
 		for (int k = 3; k <= 20; k++) {
 			for (int n = 2; n <= 5; n++) {
 				CFRecommender.genRecommendation(k, n);
 				accuracy_recall = CFRecommender.measureRec(conn, testSet);
 				
 				accuracy += accuracy_recall[0];
 				recall += accuracy_recall[1];
 			}
 		}
 		//System.out.println(1 + "," + 0);
 		System.out.println(accuracy / 72 + "," + recall / 72);
	}
}