package com.ladjzero.uzlee.model;

import java.util.ArrayList;

/**
 * Created by chenzhuo on 16-3-26.
 */
public class Version {
	private String v;
	private ArrayList<String> logs;
	private String url;

	public String getV() {
		return v;
	}

	public void setV(String v) {
		this.v = v;
	}

	public ArrayList<String> getLogs() {
		return logs;
	}

	public void setLogs(ArrayList<String> logs) {
		this.logs = logs;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
