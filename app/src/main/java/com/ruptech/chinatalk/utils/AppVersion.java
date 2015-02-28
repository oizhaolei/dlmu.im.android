package com.ruptech.chinatalk.utils;

import java.io.Serializable;

public class AppVersion implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6055705418910210761L;
	public String apkname = "";
	public String comment = "";
	public int verCode = 0;
	public int fileSize = 0;
	public String verName = "";

	@Override
	public String toString() {
		return verCode + ", " + verName;
	}

}
