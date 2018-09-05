package show.we.core.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class MsgConverter extends ClassicConverter {

	@Override
	public String convert(ILoggingEvent event) {
		Level level = event.getLevel();
		if(level.toInt() == Level.ERROR_INT){
			return LogbackUtils.getMsgInfo();
		}
		return "";
	}

}
