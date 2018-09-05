package show.we.socket.msg.handler.conn;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.corundumstudio.socketio.HandshakeData;
import show.we.socket.msg.handler.IMsgHandler;

public class RobotHandler extends IMsgHandler {
	
	private static final Logger log = LoggerFactory.getLogger(RobotHandler.class);
	
	/**
	 * 机器人请求处理（频繁请求）
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> on_handler(Map<String, Object> data) {
		HandshakeData handData = (HandshakeData) data.get("handData");
		Map<Object, Object> session = (Map<Object, Object>) data.get("session");
		String roomId = null, address = null, userId = null;
		roomId = handData.getSingleUrlParam("room_id");
		address = handData.getSingleHeader("X-Forwarded-For");
		userId = (String) session.get("_id");
		
		Boolean isWhite= chatRedis.opsForSet().isMember("robot:white:list", address);
		// 在白名单(公司内网 工作室)不进行判断
		if(!isWhite){
			//60秒内是否登录过
			Boolean isLoged= chatRedis.opsForSet().isMember("robot:"+ address + ":" + roomId + ":users", userId);
			if(!isLoged){
				Object time = chatRedis.opsForHash().get("robot:"+ address + ":" + roomId + ":info", "timestamp");
				long currentTime = System.currentTimeMillis();
				if(time != null && currentTime - Long.parseLong(time.toString()) > 10000){
					chatRedis.delete(Arrays.asList("robot:"+ address + ":" + roomId + ":info", "robot:"+ address + ":" + roomId + ":users"));
				}else{
					Object obj = chatRedis.opsForHash().get("robot:"+ address + ":" + roomId + ":info", "count");
					if(obj != null){
						int count = Integer.parseInt(obj.toString()) +1;
						if(count < 4){
							chatRedis.opsForSet().add("robot:"+ address + ":" + roomId + ":users", userId);
							Map<Object, Object> info = new HashMap<Object,Object>();
							info.put("count", count+"");
							info.put("timestamp", currentTime+"");
							chatRedis.opsForHash().putAll("robot:" + address + ":" + roomId + ":info", info);
						}else{
							data.put("break", true);
							data.put("log", address + " room:" + roomId + " user: "+userId+" robot踢出");
							chatRedis.opsForHash().put("robot:" + address + ":" + roomId + ":info", "timestamp", currentTime+"");
							chatRedis.expire("robot:"+ address + ":" + roomId + ":info", 60, TimeUnit.SECONDS);
							chatRedis.expire("robot:"+ address + ":" + roomId + ":users", 60, TimeUnit.SECONDS);
						}
					}else{
						chatRedis.opsForSet().add("robot:"+ address + ":" + roomId + ":users", userId);
						chatRedis.expire("robot:"+ address + ":" + roomId + ":users", 60, TimeUnit.SECONDS);
						Map<Object, Object> info = new HashMap<Object,Object>();
						info.put("count", 1+"");
						info.put("timestamp", currentTime+"");
						chatRedis.opsForHash().putAll("robot:" + address + ":" + roomId + ":info", info);
						chatRedis.expire("robot:"+ address + ":" + roomId + ":info", 60, TimeUnit.SECONDS);
					}
				}
			}
		}
		return data;
	}
}
