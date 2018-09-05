package show.we.socket.common;

public class TimeUtils {
	public static Long differTime(Object startTimes,Object endTimes){
		Long startTimeL = (Long) startTimes;
		Long endTimeL = (Long) endTimes;
		Long  diffrTime = startTimeL  - endTimeL;
	    return diffrTime;
	}
}
