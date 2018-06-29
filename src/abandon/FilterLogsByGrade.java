package abandon;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import org.recommender.data.TrainingSetTestSet;
import org.recommender.utility.MySQLHelper;
import org.recommender.utility.PropertyHelper;

/**
* @author : wuke
* @date   : 2018426 6:54:49
* Title   : Grade
* Description : 
*/
public class FilterLogsByGrade {

	public static void main(String[] args) {
		Connection conn = MySQLHelper.getConn();
		
		HashSet<Integer> validStuId = FilterLogsByGrade.genValidStu(conn);
		
		for (int t : validStuId) {
			System.out.print(t + " ");
		}
		System.out.println();
		System.out.println(validStuId.size()); // 320
	}

	public static HashSet<Integer> genValidStu(Connection conn) {
		HashMap<Long, Integer> stunos_sequences = TrainingSetTestSet.getStunoSequence(PropertyHelper.getProperty("PREFERENCE_PATH"));
		/*for (Entry <Long, Integer> entry : stunos_sequences.entrySet()) {
			System.out.print(entry.getKey() + ",");
		}*/
		
		
		HashMap<Long, Integer> stunos_scores = new HashMap<Long, Integer>();
		String sql = "SELECT stuno, SUM(kscore) score FROM tms_scores WHERE 1 = 1 AND courseid IN (23, 182, 5) GROUP BY stuno  ORDER BY score DESC";
		ResultSet rs = MySQLHelper.getResultSet(conn, sql);
		long stuno = 0;
		int score = 0;
		try {
			while (rs.next()) {
				stuno = rs.getLong(1);
				score = rs.getInt(2);
				
				stunos_scores.put(stuno, score);
			}
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println(stuno);
		}
		
		HashSet<Integer> validStuId = new HashSet<Integer>();
		int stuId = 0;
		for (Entry <Long, Integer> entry : stunos_sequences.entrySet()) {
			stuno = entry.getKey();
			stuId = entry.getValue();
			
			if (stunos_scores.containsKey(stuno)) {
				//if (stunos_scores.get(stuno) > 255) {
					validStuId.add(stuId);
				//}
			}
		}
		return validStuId;
	}
}
