package show.we.socket.common;

import java.util.Random;

public class SocketChannel {
	
	public final static String ALL_CHANNEL = "ALLchannel";
	
	public final static String ROOM_CHANNEL = "ROOMchannel:";
	
	public final static String USER_CHANNEL = "USERchannel:";
	
	public final static String  ROOM_USER_CHANNEL = "ROOMUSERchannel:";
	
	public static String  loginStatusTopic = "ROOMUSERchannel:";
	
	private static String[] taskTopic = new String[]{};
	
	private static String[] messageTopic = new String[]{};
	
	public static String[]  roomUserTimeTopic = new String[]{};
	
	public static String[]  roomUserMessageTopic = new String[]{};
	
	
	public static void setTaskTopic(String taskTopic) {
		SocketChannel.taskTopic = taskTopic.split(",");
	}

	public static void setMessageTopic(String messageTopic) {
		SocketChannel.messageTopic = messageTopic.split(",");
	}
	
	public static void setLoginStatusTopic(String loginStatusTopic){
		SocketChannel.loginStatusTopic = loginStatusTopic;
	}
	
	public static void setRoomUserTimeTopic(String roomUserTimeTopic) {
		SocketChannel.roomUserTimeTopic = roomUserTimeTopic.split(",");
	}
	
	public static void setRoomUserMessageTopic(String roomUserMessageTopic) {
		SocketChannel.roomUserMessageTopic = roomUserMessageTopic.split(",");
	}
	

	public static String getRoomMsgTopic(){
		Random random = new Random();
		int index = random.nextInt(messageTopic.length);
		return messageTopic[index];
	}
	
	public static String getTaskModifyTopoc(){
		Random random = new Random();
		int index = random.nextInt(taskTopic.length);
		return taskTopic[index];
	}
	
	public static String getRoomUserTimeTopic() {
		Random random = new Random();
		int index = random.nextInt(roomUserTimeTopic.length);
		return roomUserTimeTopic[index];
	}

	public static String getRoomUserMessageTopic() {
		Random random = new Random();
		int index = random.nextInt(roomUserMessageTopic.length);
		return roomUserMessageTopic[index];
	}



	
}
