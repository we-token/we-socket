package show.we.core.logback;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.spi.PropertyDefiner;
import ch.qos.logback.core.status.Status;

public class GroupPropertyDefiner implements PropertyDefiner{
	
	private static String group;

	@Override
	public void setContext(Context context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Context getContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addStatus(Status status) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addInfo(String msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addInfo(String msg, Throwable ex) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addWarn(String msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addWarn(String msg, Throwable ex) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addError(String msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addError(String msg, Throwable ex) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getPropertyValue() {
		return getGroup();
	}

	public static String getGroup() {
		return group;
	}

	public static void setGroup(String group) {
		GroupPropertyDefiner.group = group;
	}
	
	
}
