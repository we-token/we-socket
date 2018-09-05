package show.we.socket.msg.handler;

import java.util.Map;

import org.springframework.data.redis.core.StringRedisTemplate;

import show.we.system.context.springComponents.RedisInit;

public abstract class IMsgHandler {
	
	protected StringRedisTemplate chatRedis = RedisInit.getChatRedis();
	
	protected StringRedisTemplate redis = RedisInit.getMainRedis();
	
	public abstract Map<String, Object> on_handler(Map<String, Object> data);
	
}
