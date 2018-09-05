package show.we.channel.redis.dynamicRecvs;

import org.springframework.data.redis.connection.Message;

/*
 * socket句柄对象抽象 - 要求实现一个onMessage方法，
 * 在监听器获取到消息后就调用该句柄对象的onMessage
 * 方法。
 * */
public interface IMessageHandle {
	public void onMessage(Message message, byte[] pattern);
}
