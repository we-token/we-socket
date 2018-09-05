package show.we.socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ttpod.rest.common.util.JSONUtil;
import show.we.socket.msg.DataFactory;
import show.we.socket.msg.DataOperation;

public class ChatMsgHandler {
	
	private static final Logger log = LoggerFactory.getLogger(ChatMsgHandler.class);
	
	
	/**
	 *通过此类推送消息给client
	 *1、api服务推送的消息
	 *2、socket广播的消息
	 *3、kafka的消息(暂时没用到)
	 */
	@SuppressWarnings("rawtypes")
	public static void send(String channel, String msgStr){
		//FIXME redis kafka消息处理类
		Map msg = JSONUtil.jsonToMap(msgStr);
		Object action = msg.get("action");
		//消息处理链 传递的数据
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("msg", msg);
		data.put("channel", channel);
		//消息处理链
		List<String> handlers = new ArrayList<String>();
		if(action != null){
			data.put("action", action);
			//需要特殊处理的
			if(DataOperation.KICK.equals(action)){
				handlers.add(DataOperation.KICK);
				handlers.add(DataOperation.DISCONNECTION);
			}else if(DataOperation.FREEZE.equals(action)){
				handlers.add(DataOperation.FREEZE);
				handlers.add(DataOperation.DISCONNECTION);
			}else if(DataOperation.TASK.equals(action)){
				handlers.add(DataOperation.TASK);
			}
		}
		handlers.add(DataOperation.PUSH);
		
		//XXX 根据hanlder包装后的  dataMap 做其他处理
		Map<String, Object> dataMap = DataFactory.getFinalData(handlers, data);
			
	}
}
