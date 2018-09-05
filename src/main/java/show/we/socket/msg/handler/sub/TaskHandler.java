package show.we.socket.msg.handler.sub;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import show.we.channel.redis.KeyUtils;
import show.we.socket.msg.handler.IMsgHandler;

public class TaskHandler extends IMsgHandler{
	
	private static final Logger log = LoggerFactory.getLogger(TaskHandler.class);
	/**
	 * 新手任务
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Map<String, Object> on_handler(Map<String, Object> data) {
		Map msg  = (Map) data.get("msg");
		String action = (String) data.get("action");
		if("grup.task".equals(action)){
			Map data_d = (Map) msg.get("data_d");
			Long userId = Long.parseLong(data_d.get("userid").toString());
			Object obj = data_d.get("roomid");
			if(obj == null){
				data.put("channel", KeyUtils.CHANNEL.user(userId));
			}else{
				data.put("channel", KeyUtils.CHANNEL.roomUser(Long.parseLong(obj.toString()), userId));
			}
//			log.debug("用户:"+userId+",完成新手任务:"+data_d.get("task_name"));
		}
		return data;
	}

}
