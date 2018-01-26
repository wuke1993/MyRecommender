package org.recommender.data;

import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.recommender.utility.MySQLHelper;

/**
* @author : wuke
* @date   : 20180119 17:57:28
* Title   : AnalysisDataSet
* Description : TODO 分析测试集中学习者的视频数量
*/
public class AnalysisDataSet {

	public static void main(String[] args) {
		Connection conn = MySQLHelper.getConn();
		
		List<LearningLog> logs = TrainingSetTestSet.readLogs(conn);
        List<List<LearningLog>> logsAll = TrainingSetTestSet.genTrainingSetTestSet(logs); // 分为 5 份
        
        List<LearningLog> testSet = logsAll.get(4);
        
        HashMap<Long, HashSet<String>> stunoTitle = AnalysisDataSet.learnedTitles(testSet);
        System.out.println(stunoTitle.size()); // 616 人
        
        int[] countArr = new int[616];
        int i = 0;
        for (HashSet<String> titles : stunoTitle.values()) {
        	countArr[i] = titles.size();
        	System.out.print(countArr[i] + " ");
        	
        	i++;
        }
	}
	
	public static HashMap<Long, HashSet<String>> learnedTitles(List<LearningLog> testSet) {
		HashMap<Long, HashSet<String>> stunoTitle = new HashMap<Long, HashSet<String>>();
		HashSet<String> titles = null;
		
		LearningLog aLearningLog = null;
		long stuno = 0L;
		String title = null;
		for (int i = 0; i < testSet.size(); i++) {
			aLearningLog = testSet.get(i);
			
			stuno = aLearningLog.getStuno();
			title = aLearningLog.getTitle();
			
			if (stunoTitle.containsKey(stuno)) {
				stunoTitle.get(stuno).add(title);
			} else {
				titles = new HashSet<String>();
				titles.add(title);
				
				stunoTitle.put(stuno, titles);
			}
		}
		
		return stunoTitle;
	}
}
