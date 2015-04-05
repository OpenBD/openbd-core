package com.bluedragon.vision.engine;

public class BreakPoint {

	public int		line;
	public String uriPath;
	public String realFile;
	
	public BreakPoint(int line, String uriPath, String realFile){
		this.line	= line;
		this.uriPath = uriPath;
		this.realFile = realFile;
	}
	
	public String getKey(){
		return this.realFile + "@" + line;
	}
	
}
