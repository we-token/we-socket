package show.we.channel.redis.dynamicRecvs;

import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.Topic;

public class DynamicRecvListener{

	/*Socket句柄对象 - 要是实现*/
	private MessageListener messageListener;
	
	/*当前监听器关注的主题*/
	private Topic topic;
	
	/*构造器*/
	public DynamicRecvListener(MessageListener messageListener,Topic _topic){
		this.messageListener = messageListener;
		this.topic = _topic;
	}
	
	public MessageListener retviMessageHandle() {
		return messageListener;
	}

	public Topic retvTopic() {
		return topic;
	}

	/* 和IMessageHandle对象脱离，防止类似A <-> B互相引用造成垃圾对象无法回收，
	 * 句柄对象(要求实现IMessageHandle接口)应该也会持有当前对象实例,以便在句
	 * 柄断开的时候从监听器列表中动态移除当前监听*/
	public void detachIMessageHandle(){
		this.messageListener = null;
	}
	
//	/*监听到消息后 - 触发句柄持有对象的响应方法*/
//	@Override
//	public void onMessage(Message message, byte[] pattern) {
//		iMessageHandle.onMessage(message, pattern);
//	}
	
	public MessageListener retvListener() {
		return messageListener;
	}
	
}
