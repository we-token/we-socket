package show.we.socket.msg.handler.sub;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ttpod.rest.common.util.JSONUtil;
import show.we.channel.redis.KeyUtils;
import show.we.socket.common.SocketChannel;
import show.we.socket.msg.DataOperation;
import show.we.socket.msg.handler.IMsgHandler;

public class KickFrezzHandler extends IMsgHandler{
	
	private static final Logger log = LoggerFactory.getLogger(KickFrezzHandler.class);
	/**
	 * 房间踢人 用户冻结响应
	 */
	@Override
	public Map<String, Object> on_handler(Map<String, Object> data) {
		Map msg  = (Map) data.get("msg");
		String action = (String) data.get("action");
		String channel = (String) data.get("channel");
		if(channel.startsWith(SocketChannel.ROOM_CHANNEL)){
			if(DataOperation.KICK.equals(action)){
				Map data_d = (Map) msg.get("data_d");
				String roomId = channel.replace(SocketChannel.ROOM_CHANNEL, "");
				String kickUserId = data_d.get("xy_user_id").toString();
				redis.opsForSet().remove("room:" + roomId + ":users", kickUserId);
				data.put("disChannel", KeyUtils.CHANNEL.roomUser(roomId, kickUserId));
				log.info("用户:"+kickUserId+",被房间："+roomId+" 踢出.	" + JSONUtil.beanToJson(msg).toString());
			}else if(DataOperation.FREEZE.equals(action)){
				//TODO
			}
			
		}
		return data;
	}

}
