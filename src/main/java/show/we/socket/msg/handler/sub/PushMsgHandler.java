package show.we.socket.msg.handler.sub;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.corundumstudio.socketio.BroadcastOperations;
import com.ttpod.rest.common.util.JSONUtil;
import show.we.socket.StarSocketLauncher;
import show.we.socket.msg.handler.IMsgHandler;

public class PushMsgHandler extends IMsgHandler{
	
	private static final Logger log = LoggerFactory.getLogger(PushMsgHandler.class);

	/**
	 * 向socket推送消息
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Map<String, Object> on_handler(Map<String, Object> data) {
		String channel = (String) data.get("channel");
		BroadcastOperations operations = StarSocketLauncher.server.getRoomOperations(channel);
		Map msg  = (Map) data.get("msg");
		String msgStr = JSONUtil.beanToJson(msg).toString();
//		log.debug("send " + channel + " client msg:	" + JSONUtil.beanToJson(msg).toString());
		operations.sendMessage(msgStr);
		return data;
	}

}
