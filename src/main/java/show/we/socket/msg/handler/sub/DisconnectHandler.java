package show.we.socket.msg.handler.sub;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.corundumstudio.socketio.BroadcastOperations;
import show.we.socket.StarSocketLauncher;
import show.we.socket.msg.handler.IMsgHandler;

public class DisconnectHandler extends IMsgHandler {
	
	private static final Logger log = LoggerFactory.getLogger(DisconnectHandler.class);

	@Override
	public Map<String, Object> on_handler(Map<String, Object> data) {
		String channel = (String) data.get("channel");
		Object disChannel = data.get("disChannel");
		if(disChannel != null && !"".equals(disChannel.toString()))
			channel = disChannel.toString();
		log.info("断开 " + channel + " scoekt");
		BroadcastOperations operations = StarSocketLauncher.server.getRoomOperations(channel);
		operations.disconnect();
		return data;
	}

}
