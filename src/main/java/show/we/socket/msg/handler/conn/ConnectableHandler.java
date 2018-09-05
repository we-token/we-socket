package show.we.socket.msg.handler.conn;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.corundumstudio.socketio.HandshakeData;
import show.we.socket.msg.handler.IMsgHandler;

public class ConnectableHandler extends IMsgHandler{
	
	private static final Logger log = LoggerFactory.getLogger(ConnectableHandler.class);

	/**
	 * 验证是否被封ip
	 * 是否被提出房间
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> on_handler(Map<String, Object> data) {
		HandshakeData handData = (HandshakeData) data.get("handData");
		Map<Object, Object> session = (Map<Object, Object>) data.get("session");
		String roomId = null, userId = null;
		roomId = handData.getSingleUrlParam("room_id");
		userId = (String) session.get("_id");
		Long sealTime = chatRedis.getExpire("seal:user:" + userId);
		if(sealTime > 0){
			data.put("break", true);
			log.info(userId + ":已被封," + sealTime + " 秒后才能再次链接 .");
		}else{
			Long kickTime = redis.getExpire("room:" + roomId + ":kick:" + userId);
			if(kickTime > 0){
				data.put("break", true);
				log.info(userId + ":," + kickTime + " 秒后才能再次进入该房间:" + roomId + ".");
			}
		}
		return data;
	}

}
