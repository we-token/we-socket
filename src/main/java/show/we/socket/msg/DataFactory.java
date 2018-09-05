package show.we.socket.msg;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ttpod.rest.common.util.JSONUtil;
import show.we.socket.msg.handler.conn.ConnectableHandler;
import show.we.socket.msg.handler.conn.RobotHandler;
import show.we.socket.msg.handler.chat.ChatLogHandler;
import show.we.socket.msg.handler.chat.ModifyHandler;
import show.we.socket.msg.handler.chat.ValidHandler;
import show.we.socket.msg.handler.sub.DisconnectHandler;
import show.we.socket.msg.handler.sub.KickFrezzHandler;
import show.we.socket.msg.handler.sub.PushMsgHandler;
import show.we.socket.msg.handler.sub.TaskHandler;

public class DataFactory {
	
	private static final Logger log = LoggerFactory.getLogger(DataFactory.class);
	
	static{
		/**
		 * ps:事件处理顺序根据注册顺序执行
		 */
		//connection 验证
		MsgDataHandlerManager.registHandler(DataOperation.ROBOT, new RobotHandler());
		MsgDataHandlerManager.registHandler(DataOperation.CONNECTABLE, new ConnectableHandler());
		//socket message验证  在socket端进行 发言间隔 禁言 等验证 如果验证不通过 直接通过socket返回  不进行redis广播
		MsgDataHandlerManager.registHandler(DataOperation.VALID, new ValidHandler());
		MsgDataHandlerManager.registHandler(DataOperation.MODIFY, new ModifyHandler());
		MsgDataHandlerManager.registHandler(DataOperation.CHATLOG, new ChatLogHandler());
		//redis message验证
		MsgDataHandlerManager.registHandler(DataOperation.FREEZE, new KickFrezzHandler());
		MsgDataHandlerManager.registHandler(DataOperation.KICK, new KickFrezzHandler());
		MsgDataHandlerManager.registHandler(DataOperation.TASK, new TaskHandler());
		MsgDataHandlerManager.registHandler(DataOperation.PUSH, new PushMsgHandler());
		MsgDataHandlerManager.registHandler(DataOperation.DISCONNECTION, new DisconnectHandler());
		
	}
	
	public static Map<String, Object> getFinalData(List<String> operations, Map<String, Object> data) {
		if(data == null){
			data = new HashMap<String,Object>(); 
		}
		// 处理器链 重新排序
		Queue<String> operationList = MsgDataHandlerManager.getAllHandler();
		
		Iterator<String> operas = operationList.iterator();
		while(operas.hasNext())
		{
			String operation = operas.next();
			if(operations.contains(operation))
			{
//				log.debug("Operation : " + operation + " start");
				data = MsgDataHandlerManager.getHandler(operation).on_handler(data);
				if(data.get("break") != null && (boolean)data.get("break") == true){
//					log.debug("------------------"+operation+":被拦截------------------"+System.getProperty("line.separator")+JSONUtil.beanToJson(data).toString());
					break;
				}
			}
		}
		return data;
	}
}
