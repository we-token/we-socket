package com.weibo.system.context.springComponents;

import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;

public class RedisInit {

	private static JdkSerializationRedisSerializer serialization;
	private static RedisMessageListenerContainer redisContainer;
	private static StringRedisTemplate mainRedis = null;
	private static StringRedisTemplate chatRedis = null;
	
	public static JdkSerializationRedisSerializer getSerialization() {
		return serialization;
	}
	public static StringRedisTemplate getMainRedis() {
		return mainRedis;
	}
	public static StringRedisTemplate getChatRedis() {
		return chatRedis;
	}
	
	public static RedisMessageListenerContainer getRedisContainer() {
		return redisContainer;
	}
	
	public static void init(ApplicationContext ctx){
		RedisInit.chatRedis = (StringRedisTemplate) ctx.getBean("chatRedis");
		
		RedisInit.mainRedis = (StringRedisTemplate) ctx.getBean("mainRedis");
		/*RedisInit.liveRedis = (StringRedisTemplate) ctx.getBean("liveRedis");
		RedisInit.infoRedis = (StringRedisTemplate) ctx.getBean("infoRedis");*/
		
		serialization = (JdkSerializationRedisSerializer) ctx.getBean("serialization");
		redisContainer =  (RedisMessageListenerContainer) ctx.getBean("redisContainer");
	}
}
