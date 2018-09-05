package com.weibo.socket;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.BroadcastOperations;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.HandshakeData;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.ttpod.rest.common.util.JSONUtil;
import com.weibo.channel.redis.KeyUtils;
import com.weibo.core.logback.LogbackUtils;
import com.weibo.socket.common.SocketChannel;
import com.weibo.socket.common.SocketDataStore;
import com.weibo.socket.common.TimeTaskLauncher;
import com.weibo.socket.common.TimeUtils;
import com.weibo.socket.domain.ClientInfo;
import com.weibo.socket.domain.UserInfo;
import com.weibo.socket.msg.DataFactory;
import com.weibo.socket.msg.DataOperation;
import com.weibo.socket.msg.handler.chat.ChatLogHandler;
import com.weibo.socket.msg.handler.conn.ConnectionHandler;
import com.weibo.socket.msg.handler.conn.ConnectionRoomTimesHandler;
import com.weibo.system.context.springComponents.RedisInit;

public class StarSocketLauncher{
	
	private static final Logger log = LoggerFactory.getLogger(StarSocketLauncher.class);
	
	public static SocketIOServer server = null;
	
	private MessageListener redisRecvListener;
	/**
	 * kafka 组名 
	 * 	根据socket ip和port 拼接组名 并不是在kafka.xml指定
	 */
	private static String groupid = "";

	/**
	 * 启动socket服务
	 */
	public void start(String... args) {
		
		/** 1.socket配置*/
		Configuration config = new Configuration();
		config.setAllowCustomRequests(true);
		config.setHostname(args[0]);
		config.setPort(Integer.parseInt(args[1]));
		config.setAuthorizationListener(new StarAuthorization());
		
		server = new SocketIOServer(config);
		
		/** 2.socket事件绑定*/
		connection(server);
		message(server);
		disconnection(server);
		
		/** 3.socket启动*/
		server.start();
		
		/**
		 * 4.启动定时器
		 *  退出房间延迟清理数据(避免刷新页面频发广播进出房间消息)
		 */
		TimeTaskLauncher.scanExit();
	}
	
	public void stop(){
		if(server != null)
			server.stop();
	}
	
	/**
	 * 绑定socket connection监听
	 * 	ps: userChannel:广播同一用户(如果用户进入多个房间都会广播) roomUserChannel:广播某个房间的某个用户
	 */
	private void connection(final SocketIOServer server){
		
		server.addConnectListener(new ConnectListener() {

			@Override
			public void onConnect(SocketIOClient client) {
				//FIXME 3、socket 创建连接
				LogbackUtils.modifyLogInfo(null, client.getHandshakeData().getUrl());
				/** 初始化数据*/
				HandshakeData data = client.getHandshakeData();
				String  token = null, roomChannel = null, userChannel = null, roomUserChannel = null;
				Long userId = null, roomId = null;
				roomId = Long.parseLong(data.getSingleUrlParam("room_id"));
				token = data.getSingleUrlParam("access_token");
				List<String> userIds = data.getHeaders().get("userId");
				roomChannel= KeyUtils.CHANNEL.room(roomId);
				
				/** socket分组	socket通过分组实现房间广播 用户广播*/
				client.joinRoom(roomChannel);
				client.joinRoom(SocketChannel.ALL_CHANNEL);
				if(StringUtils.isBlank(token) || userIds == null || userIds.size() == 0){
					//游客
					ClientInfo clientInfo = new ClientInfo(client.getSessionId().toString(), token, roomId);
					SocketDataStore.registClientInfo(client.getSessionId().toString(), clientInfo);
				}else{
					//用户
					UserInfo userInfo = SocketDataStore.getUserInfoByUserId(Long.parseLong(userIds.get(0)));
					if(userInfo != null){
						userId = userInfo.getUserId();
						userChannel = KeyUtils.CHANNEL.user(userId);
						roomUserChannel = KeyUtils.CHANNEL.roomUser(roomId, userId);
						
						/** socket分组	socket通过分组实现房间广播 用户广播*/
						client.joinRoom(userChannel);
						client.joinRoom(roomUserChannel);
						
						/**记录当前的socket 的连接的用户 在此房间的时长**/
						
						
						/** 缓存clientInfo*/
						ClientInfo clientInfo = new ClientInfo(client.getSessionId().toString(), token, roomId, userInfo);
						SocketDataStore.registClientInfo(client.getSessionId().toString(), clientInfo);
						
						Integer priv = userInfo.getPriv();
						if(1 != priv && 4 != priv){
							BroadcastOperations roomUser = server.getRoomOperations(roomUserChannel);
							if(roomUser.getClients().size() == 1){
								TimeTaskLauncher.enterRoom(userInfo, clientInfo);
							}
						}
						/** 启动主播心跳定时器  并缓存  socket断开时需要清除*/
						if(clientInfo.isStar()){
							Timer liveStar = TimeTaskLauncher.liveStar(userInfo, clientInfo);
							SocketDataStore.registLiveInfo(clientInfo.getClientId(), liveStar);
							Timer liveHeart = TimeTaskLauncher.liveHeart(clientInfo);
							SocketDataStore.registLiveHeart(clientInfo.getClientId(), liveHeart);
						}
						
						ConnectionRoomTimesHandler.SaveRoomNum(userId, roomId);
					    Map timeInfo = new HashMap<String, Object>();
					    timeInfo.put("userid", userInfo.getUserId());
					    timeInfo.put("enterTime", data.getTime());
					    timeInfo.put("roomid", clientInfo.getRoomId());
					    timeInfo.put("type", 1);
						RedisInit.getMainRedis().convertAndSend(SocketChannel.getRoomUserTimeTopic(), JSONUtil.beanToJson(timeInfo).toString());
					}
				}
				int size = server.getAllClients().size();
				log.info(" 连接成功,当前连接数:" + size + "	roomId:"+ roomId + " token:" + token + " session:" + client.getSessionId());
				LogbackUtils.clearLogInfo();
			}
		});
	}
	
	/**
	 * 绑定socket message监听
	 */
	private void message(final SocketIOServer server){
		server.addMessageListener(new DataListener<String>(){
			
			@Override
			public void onData(SocketIOClient client, String data,AckRequest ackSender) throws Exception {
				//FIXME 3、socket 聊天
				/** 初始化数据*/
				LogbackUtils.modifyLogInfo(null, data + "	url:" + client.getHandshakeData().getUrl());
				String clientId = client.getSessionId().toString();
            	ClientInfo clientInfo = SocketDataStore.getClientInfo(clientId);
            	UserInfo userInfo = SocketDataStore.getUserInfoByClient(client.getSessionId().toString());
            	
            	if(data == null || clientInfo == null || userInfo == null){
            		log.error("聊天消息缺少必要信息,msgData:"+ data + "	clientInfo:" + clientInfo + "	userInfo:" + userInfo);
            		return;
            	}
            	/** 通过处理链DataFactory  处理聊天信息*/
            	List<String> handlers = new ArrayList<String>();
            	HashMap<String, Object> map = new HashMap<String, Object>();
            	map.put("msgData", data);
            	map.put("clientInfo", clientInfo);
            	map.put("userInfo", userInfo);
            	handlers.add(DataOperation.VALID);
            	handlers.add(DataOperation.MODIFY);
            	handlers.add(DataOperation.CHATLOG);
            	Map<String, Object> dataMap = DataFactory.getFinalData(handlers, map);
            	if(dataMap.get("break") != null && (boolean)dataMap.get("break") == true){
            		if(dataMap.get("disconnect") != null && (boolean)dataMap.get("disconnect") == true)
            			client.disconnect();
            		return;
            	}
            	
            	/** 通过redis 分发聊天信息到各个服务器*/
            	StringRedisTemplate chatRedis = RedisInit.getChatRedis();
            	String priChatChannel = (String)dataMap.get("privateChat");
            	String object = (String) dataMap.get("msgData");
            	Map<String, Object> msgData = JSONUtil.jsonToMap(object);
            	if(!StringUtils.isBlank(priChatChannel)){
            		//私聊 to
					chatRedis.convertAndSend(priChatChannel, JSONUtil.beanToJson(msgData.get("msg")));
					//log.info("聊天的渠道+===="+priChatChannel+"聊天信息的字符串：+==="+JSONUtil.beanToJson(msgData.get("msg")));
            	}else{
            		//房间
					chatRedis.convertAndSend(KeyUtils.CHANNEL.room(clientInfo.getRoomId()), JSONUtil.beanToJson(msgData.get("msg")));
					//log.info("聊天的渠道+===="+KeyUtils.CHANNEL.room(clientInfo.getRoomId())+"聊天信息的字符串：+==="+JSONUtil.beanToJson(msgData.get("msg")));
            	}
        		Map info = new HashMap<String, Object>();
			    info.put("userid", userInfo.getUserId());
			    info.put("roomid", clientInfo.getRoomId());
				System.out.println("发言数据json" + JSONUtil.beanToJson(info).toString());
				RedisInit.getMainRedis().convertAndSend(SocketChannel.getRoomUserMessageTopic(), JSONUtil.beanToJson(info).toString());
            	LogbackUtils.clearLogInfo();
			}}
		);
	}
	
	/**
	 * 绑定socket disconnection监听
	 */
	private void disconnection(final SocketIOServer server){
		
		server.addDisconnectListener(new DisconnectListener(){

			@Override
			public void onDisconnect(SocketIOClient client) {
				//FIXME 3、socket 断开连接
				LogbackUtils.modifyLogInfo(null, client.getHandshakeData().getUrl());
				/** 初始化数据*/
				String clientId = client.getSessionId().toString();
				Long userId = null, roomId = null;
				ClientInfo clientInfo = SocketDataStore.getClientInfo(clientId);
				UserInfo userInfo = SocketDataStore.getUserInfoByClient(clientId);
				roomId = clientInfo.getRoomId();
				
				String roomChannel = KeyUtils.CHANNEL.room(roomId);
				client.leaveRoom(roomChannel);
				client.leaveRoom(SocketChannel.ALL_CHANNEL);
				if(userInfo != null){
					userId = userInfo.getUserId();
					TimeTaskLauncher.exitRoom(userInfo, clientInfo);
					
					String roomUserChannel = KeyUtils.CHANNEL.roomUser(roomId, userId);
					String userChannel = KeyUtils.CHANNEL.user(userId);
					
					BroadcastOperations userOperations = server.getRoomOperations(userChannel);
					if(userOperations.getClients().size() == 0){
						SocketDataStore.removeUserInfo(userId);
						log.debug("[ LEVAE ] user : " + userId);
					}
					client.leaveRoom(userChannel);
					client.leaveRoom(roomUserChannel);
					if(clientInfo.isStar()){
						SocketDataStore.removeLiveInfo(clientId);
						SocketDataStore.removeLiveHeart(clientId);
					}
					userInfo.getSign().put("exitTime", System.currentTimeMillis());
					ConnectionHandler.SaveClientTime(userInfo,clientInfo);
					Long mill = TimeUtils.differTime( userInfo.getSign().get("exitTime"), userInfo.getSign().get("enterTime"));
					HashMap<String, Object> taskInfo =null;
					if(mill>=300000){
					    taskInfo = new HashMap<String, Object>();
						taskInfo.put("taskid", 48);
						taskInfo.put("userid", userInfo.getUserId());
						taskInfo.put("timestamp", System.currentTimeMillis());
						taskInfo.put("roomid", clientInfo.getRoomId());
						log.info("在房间待的时间大于10000"+userInfo.getUserId());
						System.out.println("发送消息的数据json" + JSONUtil.beanToJson(taskInfo).toString());
						RedisInit.getMainRedis().convertAndSend(SocketChannel.getTaskModifyTopoc(), JSONUtil.beanToJson(taskInfo).toString());
					}
					Map timeInfo = new HashMap<String, Object>();
				    timeInfo.put("userid", userInfo.getUserId());
				    timeInfo.put("exitTime", new Date().getTime());
				    timeInfo.put("roomid", clientInfo.getRoomId());
				    timeInfo.put("type", 2);
					RedisInit.getMainRedis().convertAndSend(SocketChannel.getRoomUserTimeTopic(), JSONUtil.beanToJson(timeInfo).toString());
			}
				long time = new Date().getTime();
				long conTime = time - client.getHandshakeData().getTime().getTime();
				log.info(" 断开连接,连接时长:"+(conTime)/1000+"s	roomId:"+ roomId + " token:" + clientInfo.getToken() + " sessionId:" + client.getSessionId());
				SocketDataStore.removeClientInfo(clientId);
				LogbackUtils.clearLogInfo();
			}
		});
	}
	
	public MessageListener getRedisRecvListener() {
		return redisRecvListener;
	}

	public void setRedisRecvListener(MessageListener redisRecvListener) {
		this.redisRecvListener = redisRecvListener;
	}

	public static String getGroupid() {
		return groupid;
	}

	public static void setGroupid(String groupid) {
		StarSocketLauncher.groupid = groupid;
	}
	
}
