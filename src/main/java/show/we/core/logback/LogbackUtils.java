package show.we.core.logback;

import org.apache.commons.lang.StringUtils;


public class LogbackUtils {
	private static ThreadLocal<String> msgLocal = new ThreadLocal<String>();
	 
	/**
	 * error logback需要一同输出的信息
	 * @param threadName
	 * @param msg
	 */
	public static void modifyLogInfo(String threadName, String msg){
		if(threadName != null)
			Thread.currentThread().setName(threadName);
		msgLocal.set(msg);
	}
	
	public static void clearLogInfo(){
		msgLocal.remove();
	}
	
	public static String getMsgInfo(){
		String msg = msgLocal.get();
		if(StringUtils.isBlank(msg))
			return "";
		else{
			String str = msgLocal.get();
			clearLogInfo();
			return "\n消息:" + str;
		}
	}
}
