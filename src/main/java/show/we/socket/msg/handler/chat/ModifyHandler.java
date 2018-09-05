package show.we.socket.msg.handler.chat;

import java.util.Map;

import com.ttpod.rest.common.util.JSONUtil;
import show.we.channel.redis.KeyUtils;
import show.we.socket.domain.ClientInfo;
import show.we.socket.domain.UserInfo;
import show.we.socket.msg.handler.IMsgHandler;

public class ModifyHandler extends IMsgHandler {

	/**
	 * 格式化socket传来的消息
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Map<String, Object> on_handler(Map<String, Object> data) {
		UserInfo userInfo = (UserInfo) data.get("userInfo");
		ClientInfo clientInfo = (ClientInfo)data.get("clientInfo");
		Long roomId = clientInfo.getRoomId();
		Map msgData = JSONUtil.jsonToMap((String)data.get("msgData"));
		Object obj = msgData.get("user_id");
		if(obj != null){
			Long toUserId = Long.parseLong(obj.toString());
			//私聊
			if(toUserId != 0)
				data.put("privateChat", KeyUtils.CHANNEL.roomUser(roomId, toUserId));
		}
		Map msg =(Map) msgData.get("msg");
		msg.remove("action");
		Map<String, Object> sign = JSONUtil.beanToMap(userInfo.getSign());
		sign.put("isStar", clientInfo.isStar());
		msg.put("from", sign);
		msg.put("room_id", clientInfo.getRoomId());
		msg.put("etime", System.currentTimeMillis());
		msgData.put("msg", msg);
		data.put("msgData", JSONUtil.beanToJson(msgData).toString());
		return data;
	}
}
