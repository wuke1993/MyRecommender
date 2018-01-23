package org.recommender.data;
/**
* @author : wuke
* @date   : 20180117 15:12:14
* Title   : LearningLog
* Description : 
*/
public class LearningLog {
	private long stuno;
	private int oper;
	private String title;
	private int tlen;
	
	public LearningLog(long stuno, int oper, String title, int tlen) {
		super();
		this.stuno = stuno;
		this.oper = oper;
		this.title = title;
		this.tlen = tlen;
	}
	
	public long getStuno() {
		return stuno;
	}

	public int getOper() {
		return oper;
	}

	public String getTitle() {
		return title;
	}

	public int getTlen() {
		return tlen;
	}
}