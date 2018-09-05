package show.we.channel.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

import show.we.core.logback.LogbackUtils;
import show.we.socket.ChatMsgHandler;

public class RedisRecvListener implements MessageListener{

	public static final Logger log = LoggerFactory.getLogger(RedisRecvListener.class);
	
	@Override
	public void onMessage(Message message, byte[] pattern) {
		//FIXME socket 接受 redis消息
		String channel = new String(message.getChannel());
		String msgStr = message.toString();
		LogbackUtils.modifyLogInfo("c:" + channel + ":r:sub", msgStr);
        ChatMsgHandler.send(channel, msgStr);
	}
	
}
