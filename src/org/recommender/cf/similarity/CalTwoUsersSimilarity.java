package org.recommender.cf.similarity;

import org.recommender.utility.GetProperty;

/**
* @author : wuke
* @date   : 20170611 00:43:09
* Title   : CalTwoUsersSimilarity
* Description : "similarity = pearson_similarity +  weight_forum_correlation * forum_correlation".
*/
public class CalTwoUsersSimilarity {

	public static double calTwoUsersSimilarity(double[] preferenceArrX, double[] preferenceArrY, int stuno_sequence_x, int stuno_sequence_y) {
		double similarity = 0.0;
		double pearson_similarity = 0.0;
		double forum_correlation = 0.0;
		
		PearsonCorrelationSimilarity pearsonCS = new PearsonCorrelationSimilarity();
		pearson_similarity = pearsonCS.calSimilarity(preferenceArrX, preferenceArrY);
		
		similarity = pearson_similarity;
		/*ForumCorrelation forumCorrelation = new ForumPostAnswerCorrelation();
		forum_correlation = forumCorrelation.calForumCorrelation(stuno_sequence_x, stuno_sequence_y);
		
		double weight_forum_correlation = Double.parseDouble(GetProperty.getPropertyByName("WEIGHT_FORUM_CORRELATION"));
		similarity = pearson_similarity +  weight_forum_correlation * forum_correlation;*/
		
		return similarity;
	}
	
}
