package show.we.socket.common;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.corundumstudio.socketio.BroadcastOperations;
import com.corundumstudio.socketio.HandshakeData;
import com.corundumstudio.socketio.SocketIOClient;
import com.ttpod.rest.common.util.JSONUtil;
import show.we.channel.redis.KeyUtils;
import show.we.socket.StarSocketLauncher;
import show.we.socket.domain.ClientInfo;
import show.we.socket.domain.UserInfo;
import show.we.system.context.springComponents.RedisInit;
import show.we.system.utils.http.Web;


public class TimeTaskLauncher {
	
	private static final Logger log = LoggerFactory.getLogger(TimeTaskLauncher.class);
	
	/**
	 * 退出房间
	 * key:roomuser
	 */
	public final static ConcurrentHashMap<String, Map<String, Object>> EXIT_DATA = new ConcurrentHashMap<String, Map<String, Object>>();
	
	public final static ConcurrentLinkedQueue<String> EXITING_USERS = new ConcurrentLinkedQueue<String>();
	
	/**
	 * 广播退出房间消息
	 * 		为了避免刷新导致频发进出场提示
	 * 		用户退出并断开socket时会缓存用户id和退出时间戳
	 * 		定时器会扫描已经退出超过5秒的用户 广播退出信息
	 * 		如果用户5秒内再次进入房间时会清除退场缓存的时间戳 实现刷新不广播退出信息
	 * 		ie9一下版本 因为socket 并非使用websocket协议   服务器获知socket断开 而是根据心跳判断 所以ie个别半个 较长时间内进出都不会提示
	 */
	public static void scanExit() {
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				try {
					Thread.currentThread().setName("scan-user-exit");
					long currentTimeMillis = System.currentTimeMillis();
					String key = null;
					long delayTime = 0;
					StringRedisTemplate chatRedis = RedisInit.getChatRedis();
					while ((key = (String) TimeTaskLauncher.EXITING_USERS
							.peek()) != null) {
						String[] groups = key.split("-");
						String roomId = groups[0];
						String userId = groups[1];

						Map sign = (Map) TimeTaskLauncher.EXIT_DATA
								.get(KeyUtils.CHANNEL.roomUser(roomId, userId));
						
						if (sign == null) {
							//两个tab页面 链接同一个房间 直接关闭浏览器时 sign可能为null
							TimeTaskLauncher.EXITING_USERS.poll();
						} else {
							long exitTime = ((Long) sign.get("exitTime")).longValue();
							//退出时间小于5秒  视为未退出
							if ((delayTime = currentTimeMillis - exitTime) <= 5000L)
								break;
							//超过5秒 且 没有再次进入房间 清理用户数据
							TimeTaskLauncher.EXITING_USERS.poll();

							BroadcastOperations roomUser = StarSocketLauncher.server
									.getRoomOperations(KeyUtils.CHANNEL
											.roomUser(roomId, userId));

							if (roomUser.getClients().size() == 0) {
								RedisInit.getMainRedis().opsForSet().remove("room:" + roomId + ":users", userId);
								RedisInit.getMainRedis().opsForHash().delete("room:" + roomId + ":client", userId);
								int priv = Integer.parseInt(sign.get("priv").toString());
								if(priv != 1 && priv != 4){
									HashMap<String, Object> exitInfo = new HashMap<String, Object>();
									exitInfo.put("roomid", roomId);
									exitInfo.put("userid", userId);
									exitInfo.put("userinfo", sign);
									exitInfo.put("room_type", 0);
									HashMap<Object, Object> disData = new HashMap<Object, Object>();
									disData.put("data_d", exitInfo);
									RedisInit.getMainRedis().convertAndSend(SocketChannel.getRoomMsgTopic(), JSONUtil.beanToJson(disData).toString());
								}
								HashMap roomChangeInfo = new HashMap();
								roomChangeInfo.put("action", "room.exit");
								roomChangeInfo.put("data_d", sign);
								chatRedis.convertAndSend(KeyUtils.CHANNEL.room(roomId),JSONUtil.beanToJson(roomChangeInfo).toString());
								TimeTaskLauncher.EXIT_DATA.remove(KeyUtils.CHANNEL.roomUser(roomId, userId));
							}
						}

					}

				} catch (Exception e) {
					TimeTaskLauncher.log.error(e.getMessage());
				}
			}
		}, 0, 5000);
	}
	
	/**
	 * 定时器 3秒后用户仍在线 广播进入房间
	 * @param oldClientSize
	 * @param userInfo
	 * @param clientInfo
	 */
	public static void enterRoom(UserInfo userInfo, ClientInfo clientInfo){
		
		Map<String, Object> exitData = EXIT_DATA.get(KeyUtils.CHANNEL.roomUser(clientInfo.getRoomId(), userInfo.getUserId()));
		//判断是否刚刚退出过
		if(exitData == null){
			BroadcastOperations roomUser = StarSocketLauncher.server.getRoomOperations(KeyUtils.CHANNEL.roomUser(clientInfo.getRoomId(), userInfo.getUserId()));
			if(roomUser.getClients().size() == 1){
				String roomId = clientInfo.getRoomId().toString();
				String userId = userInfo.getUserId().toString();
				StringRedisTemplate redis = RedisInit.getMainRedis();
				Long res = redis.opsForSet().add("room:" + roomId + ":users", userId);
				if(res == 1){
					Map<Object, Object> roomInfo = new HashMap<Object, Object>();
					roomInfo.put("roomid", roomId);
					roomInfo.put("userid", userId);
					roomInfo.put("userinfo", userInfo.getSign());
					roomInfo.put("room_type", 1);
					HashMap<Object, Object> conData = new HashMap<Object, Object>();
					conData.put("data_d", roomInfo);
					//通知后台服务
					redis.convertAndSend(SocketChannel.getRoomMsgTopic(), JSONUtil.beanToJson(conData).toString());
					if(!StringUtils.isBlank(userInfo.getSign().get("s").toString())){
						redis.opsForHash().put("room:" + roomId + ":client", userInfo.getUserId().toString(), userInfo.getSign().get("s"));
					}
				}
				
				StringRedisTemplate chatRedis = RedisInit.getChatRedis();
				HashMap<Object, Object> roomChangeInfo = new HashMap<Object, Object>();
				roomChangeInfo.put("action", "room.change");
				roomChangeInfo.put("data_d", userInfo.getSign());
				chatRedis.convertAndSend(KeyUtils.CHANNEL.room(clientInfo.getRoomId()), JSONUtil.beanToJson(roomChangeInfo).toString());
			}
		}else{
			EXITING_USERS.remove(clientInfo.getRoomId()+ "-" + userInfo.getUserId());
			EXIT_DATA.remove(KeyUtils.CHANNEL.roomUser(clientInfo.getRoomId(), userInfo.getUserId()));
		}
		/**
		 * 判断进入房间数 如果大于3则断开 最早进入的房间
		 * 	ps:只考虑当前服务器的web端连接数   ip_hash 原则上同意终端连接的一台服务器  
		 */
		//判断是否是web端
		String mark = clientInfo.getS();
		if(StringUtils.isBlank(mark)){
			BroadcastOperations userOperations = StarSocketLauncher.server.getRoomOperations(KeyUtils.CHANNEL.user(clientInfo.getUserId()));
			Collection<SocketIOClient> clients = userOperations.getClients();
			
			Iterator<SocketIOClient> iterator = clients.iterator();
			/**
			 * rooms 存放进入的房间  用于判断是否进入数>3
			 * minDate 进入最早进入房间的时间
			 * disRoom 要断开的房间号 和 最早进入的房间 对应 
			 */
			Set<String> rooms = new HashSet<String>();
			String disRoom = "";
			Date minDate = new Date();
			while(iterator.hasNext()){
				SocketIOClient client = iterator.next();
				HandshakeData handshakeData = client.getHandshakeData();
				String roomId = handshakeData.getSingleUrlParam("room_id");
				if(Long.parseLong(roomId) < 1000000000){
					rooms.add(roomId);
					Date loginTime = handshakeData.getTime();
					if(loginTime.getTime() < minDate.getTime()){
						minDate = loginTime;
						disRoom = roomId;
					}
				}
			}
			/**发送roomlimit信息 同时客户端断开连接*/
			if(rooms.size() > 3){
				HashMap<Object, Object> roomLimitInfo = new HashMap<Object, Object>();
				roomLimitInfo.put("action", "room.limit");
				BroadcastOperations roomUserOperations = StarSocketLauncher.server.getRoomOperations(KeyUtils.CHANNEL.roomUser(disRoom, clientInfo.getUserId()));
				roomUserOperations.sendMessage(JSONUtil.beanToJson(roomLimitInfo).toString());
			}
		}
	}
	
	public static void exitRoom(UserInfo userInfo, ClientInfo clientInfo){
		String exiting = clientInfo.getRoomId() + "-" + userInfo.getUserId();
		Map<String, Object> sign = JSONUtil.beanToMap(userInfo.getSign());
		sign.put("exitTime", System.currentTimeMillis());
		EXIT_DATA.put(KeyUtils.CHANNEL.roomUser(clientInfo.getRoomId(), userInfo.getUserId()), sign);
		EXITING_USERS.add(exiting);
		
	}
	
	public static Timer liveStar(UserInfo userInfo, ClientInfo clientInfo){
		Timer timer = new Timer();
		timer.schedule(new PushStarInfo(userInfo, clientInfo), 0, 10000);
		return timer;
	}
	
	public static Timer liveHeart(ClientInfo clientInfo){
		Timer timer = new Timer();
		timer.schedule(new LiveHeart(clientInfo), 0, 15000);
		return timer;
	}
	
	static class EnterRoomTask extends TimerTask{
		private UserInfo userInfo;
		private ClientInfo clientInfo;
		
		public EnterRoomTask(UserInfo userInfo,
				ClientInfo clientInfo) {
			super();
			this.userInfo = userInfo;
			this.clientInfo = clientInfo;
		}

		@Override
		public void run() {
			BroadcastOperations roomUser = StarSocketLauncher.server.getRoomOperations(KeyUtils.CHANNEL.roomUser(clientInfo.getRoomId(), userInfo.getUserId()));
			if(roomUser.getClients().size() == 1){
				StringRedisTemplate chatRedis = RedisInit.getChatRedis();
				HashMap<Object, Object> roomChangeInfo = new HashMap<Object, Object>();
				roomChangeInfo.put("action", "room.change");
				roomChangeInfo.put("data_d", userInfo.getSign());
				chatRedis.convertAndSend(KeyUtils.CHANNEL.room(clientInfo.getRoomId()), JSONUtil.beanToJson(roomChangeInfo).toString());
			}
		}
		
	}
	
	
	static class PushStarInfo extends TimerTask{
		
		private static final Logger log = LoggerFactory.getLogger(PushStarInfo.class);
		
		private String oldInfo = "";
		private UserInfo userInfo;
		private ClientInfo clientInfo;
		
		public PushStarInfo(UserInfo userInfo, ClientInfo clientInfo) {
			super();
			this.userInfo = userInfo;
			this.clientInfo = clientInfo;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public void run() {
			try {
				/**
				 * 检测是否在线 防止因为某些错误 连接虽然断开 但是没有清理定时器
				 */
				SocketIOClient client = StarSocketLauncher.server.getClient(UUID.fromString(clientInfo.getClientId()));
				if(client == null){
					SocketDataStore.removeLiveInfo(clientInfo.getClientId());
					this.cancel();
					return;
				}
				Map info = (Map) Web.api("public/room_star/" + clientInfo.getRoomId());
				if(info != null && !oldInfo.equals(info.toString())){
					BroadcastOperations roomOperations = StarSocketLauncher.server.getRoomOperations(KeyUtils.CHANNEL.roomUser(clientInfo.getClientId(), userInfo.getUserId()));
					Map<String, Object> msg = new HashMap<String, Object>();
					msg.put("action", "room.star");
					msg.put("data_d", info.get("user"));
					roomOperations.sendEvent("message", msg);
					this.oldInfo = info.toString();
				}
			} catch (IOException e) {
				log.error(e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	static class LiveHeart extends TimerTask{
		
		private ClientInfo clientInfo;
		
		public LiveHeart(ClientInfo clientInfo) {
			super();
			this.clientInfo = clientInfo;
		}

		@Override
		public void run() {
			/**
			 * 检测是否在线 防止因为某些错误 连接虽然断开 但是没有清理定时器
			 */
			SocketIOClient client = StarSocketLauncher.server.getClient(UUID.fromString(clientInfo.getClientId()));
			if(client == null){
				SocketDataStore.removeLiveHeart(clientInfo.getClientId());
				this.cancel();
				return;
			}
			StringRedisTemplate redis = RedisInit.getMainRedis();
			redis.expire("room:" + clientInfo.getRoomId() + ":live", 45, TimeUnit.SECONDS);
		}
	}
}
