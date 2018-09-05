package show.we.channel.redis.dynamicRecvs;

import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.connection.MessageListener;

import show.we.system.context.springComponents.RedisInit;

/**
 * @author cyan
 * 动态监听门面 - 目前只定义了两个操作，添加和删除
 * */
public class DynamicRecvFacade {

	private static RedisMessageListenerContainer redisContainer = RedisInit.getRedisContainer();
	
//	/*监听器列表中动态太假*/
//	public static DynamicRecvListener addRecv(IMessageHandle iMessageHandle,String channel){
//		DynamicRecvListener dynamicRecvListener = new DynamicRecvListener(iMessageHandle,new DynamicRecvTopic(channel));
//		redisContainer.addMessageListener(dynamicRecvListener, dynamicRecvListener.retvTopic());
//		return dynamicRecvListener;
//	}
//	
//	/*从监听器列表中动态移除当前监听器*/
//	public static void rmvRecv(DynamicRecvListener dynamicRecvListener){
//		dynamicRecvListener.detachIMessageHandle();
//		redisContainer.removeMessageListener(dynamicRecvListener, dynamicRecvListener.retvTopic());
//	}
	
	/*监听器列表中动态太假*/
	public static DynamicRecvListener addRecv(MessageListener messageListener,String channel){
		DynamicRecvListener dynamicRecvListener = new DynamicRecvListener(messageListener, new ChannelTopic(channel));
		redisContainer.addMessageListener(dynamicRecvListener.retviMessageHandle(), dynamicRecvListener.retvTopic());
		return dynamicRecvListener;
	}
	
	/*从监听器列表中动态移除当前监听器*/
	public static void rmvRecv(DynamicRecvListener dynamicRecvListener){
		if(dynamicRecvListener != null){
			dynamicRecvListener.detachIMessageHandle();
			redisContainer.removeMessageListener(dynamicRecvListener.retviMessageHandle(), dynamicRecvListener.retvTopic());
		}
	}
	
}
