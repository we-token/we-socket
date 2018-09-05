package show.we.socket.common;

import java.util.HashMap;
import java.util.Map;

public class DeviceMark {
	
	public static Map<String, String> S;
	
	static{
		S = new HashMap<String, String>();
		S.put("s200", "2");
		S.put("s310", "10");
		S.put("s320", "20");
		S.put("s330", "30");
	}
	
	public static String getMark(String s){
		if(s != null){
			String mark = S.get(s);
			if(mark != null);
				return mark;
		}
		return "";
	}
}
