package show.we.channel.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.ttpod.rest.web.StaticSpring;
import show.we.system.context.springComponents.RedisInit;

/**
 * @author cyan
 * push消息到redis缓存
 * */
public class RedisSendService {
	
	public static final Logger log = LoggerFactory.getLogger(RedisSendService.class);
    public static final StringRedisTemplate mainRedis = RedisInit.getMainRedis();
    public static final StringRedisTemplate chatRedis = RedisInit.getChatRedis();
	
    /**发送到聊天缓存*/
	public static void publishToChat(final String channel, final String json) {
		 StaticSpring.execute(new Runnable() {
	            @SuppressWarnings({ "unchecked", "rawtypes" })
				public void run() {
	                final byte[] data = KeyUtils.serializer(json);
	                log.info("推送 : "+json);
	                chatRedis.execute(new RedisCallback() {
	                    @Override
	                    public Object doInRedis(RedisConnection connection)
	                            throws DataAccessException {
	                        Object obj = connection.publish(KeyUtils.serializer(channel),data);
	                        log.info("结果 : "+obj.toString());
	                        return obj;
	                    }
	                });
	            }
		 });
	}
	
	/**发送至main缓存*/
	private static void publishToMain(final String channel, final String json) {
		 StaticSpring.execute(new Runnable() {
	            @SuppressWarnings({ "unchecked", "rawtypes" })
				public void run() {
	                final byte[] data = KeyUtils.serializer(json);
	                log.info("推送 : "+json);
	                mainRedis.execute(new RedisCallback() {
	                    @Override
	                    public Object doInRedis(RedisConnection connection)
	                            throws DataAccessException {
	                        Object obj = connection.publish(KeyUtils.serializer(channel),data);
	                        log.info("结果 : "+obj.toString());
	                        return obj;
	                    }
	                });
	            }
		 });
	}
	
}
