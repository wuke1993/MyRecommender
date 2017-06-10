package org.recommender.preference;

import java.sql.Connection;

/**
* @author : wuke
* @date   : 20170608 16:55:55
* Title   : Preference
* Description : 
*/
public interface Preference {

	void calPreference(Connection conn, String sql, String path1, String path2);
}
