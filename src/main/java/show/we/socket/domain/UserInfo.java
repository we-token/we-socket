package com.weibo.socket.domain;

import java.util.HashMap;
import java.util.Map;

public class UserInfo {
	
	private Map<String, Object> sign = new HashMap<String, Object>();
	
	private boolean hasFirstMsg = false;
	
	public Map<String, Object> getSign() {
		return sign;
	}

	public void setSign(Map<String, Object> sign) {
		this.sign = sign;
	}

	public Long getUserId(){
		return Long.parseLong(sign.get("_id").toString());
	}
	
	public int getPriv(){
		return Integer.parseInt(sign.get("priv").toString());
	}

	public boolean isHasFirstMsg() {
		return hasFirstMsg;
	}

	public void setHasFirstMsg(boolean hasFirstMsg) {
		this.hasFirstMsg = hasFirstMsg;
	}
	
	public double getSpend(){
		return (double) sign.get("spend");
	}
	
	public void setSpend(double spend){
		this.sign.put("spend", spend);
	}
}
