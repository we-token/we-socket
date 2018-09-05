package com.weibo.socket.common;

import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

import com.weibo.channel.redis.dynamicRecvs.DynamicRecvListener;
import com.weibo.socket.domain.ClientInfo;
import com.weibo.socket.domain.UserInfo;

public class SocketDataStore {
	
	/**
	 * 用户信息 需要同步更新
	 * key: userid
	 */
	private final static ConcurrentHashMap<Long, UserInfo> USERINFOS = new ConcurrentHashMap<Long, UserInfo>();
	/**
	 * socket连接信息
	 * key: clientId
	 */
	private final static ConcurrentHashMap<String, ClientInfo> CLIENTINFOS = new ConcurrentHashMap<String, ClientInfo>();
	/**
	 *  redis渠道监听
	 */
	private final static ConcurrentHashMap<String, DynamicRecvListener> REDIS_CHANNEL_LISTENER = new ConcurrentHashMap<String, DynamicRecvListener>();
	
	private final static ConcurrentHashMap<String, Timer> LIVEHEART_TIMER = new ConcurrentHashMap<String, Timer>();
	
	private final static ConcurrentHashMap<String, Timer> LIVEINFO_TIMER = new ConcurrentHashMap<String, Timer>();
	
	
	
	/**
	 * 添加用户信息
	 * @param userId
	 * @param userInfo
	 */
	public static void registUserInfo(long userId, UserInfo userInfo){
		USERINFOS.put(userInfo.getUserId(), userInfo);
	}
	
	public static void registClientInfo(String clientId, ClientInfo clientInfo) {
		CLIENTINFOS.put(clientId, clientInfo);
	}
	
	public static ClientInfo getClientInfo(String clientId){
		return CLIENTINFOS.get(clientId);
	}
	
	/**
	 * 根据userId获取用户信息
	 * @param userId
	 * @return
	 */
	public static UserInfo getUserInfoByUserId(Long userId){
			return USERINFOS.get(userId);
	}
	
	/**
	 * 根据socket连接 获取用户信息
	 * @param clientId
	 * @return
	 */
	public static UserInfo getUserInfoByClient(String clientId){
		Long userId = CLIENTINFOS.get(clientId).getUserId();
		if(userId != null)
			return USERINFOS.get(userId);
		else
			return null;
	}
	
	public static void registChannelListener(String channel, DynamicRecvListener listener){
		REDIS_CHANNEL_LISTENER.put(channel, listener);
	}
	
	public static DynamicRecvListener getChannelListener(String channel){
		return REDIS_CHANNEL_LISTENER.get(channel);
	}
	
	public static void removeChannelListener(String channel){
		REDIS_CHANNEL_LISTENER.remove(channel);
	}
	/**
	 * 清除连接相关信息
	 * @param token
	 */
	public static  void removeClientInfo(String clientUUID){
		CLIENTINFOS.remove(clientUUID);
	}
	/**
	 * 清除用户相关信息
	 * @param token
	 */
	public static void removeUserInfo(Long userId){
		USERINFOS.remove(userId);
	}
	
	public static void registLiveHeart(String clientId, Timer timer){
		LIVEHEART_TIMER.put(clientId, timer);
	}
	
	public static void removeLiveHeart(String clientId){
		Timer timer = LIVEHEART_TIMER.get(clientId);
		if(timer != null){
			timer.cancel();
			LIVEHEART_TIMER.remove(clientId);
		}
	}
	
	public static void registLiveInfo(String clientId, Timer timer){
		LIVEINFO_TIMER.put(clientId, timer);
	}
	
	public static void removeLiveInfo(String clientId){
		Timer timer = LIVEINFO_TIMER.get(clientId);
		if(timer != null){
			timer.cancel();
			LIVEINFO_TIMER.remove(clientId);
		}
	}
}
