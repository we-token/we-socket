package show.we.socket.msg.handler.chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.ttpod.rest.common.util.JSONUtil;
import show.we.socket.common.SocketChannel;
import show.we.socket.domain.ClientInfo;
import show.we.socket.domain.UserInfo;
import show.we.socket.msg.handler.IMsgHandler;
import show.we.system.context.springComponents.DBInit;

public class ChatLogHandler extends IMsgHandler {
	
	private static final Logger log = LoggerFactory.getLogger(ChatLogHandler.class);
	
	public static Lock chatInsert = new ReentrantLock();
	
	private ConcurrentLinkedQueue<DBObject> chatLogCache = new ConcurrentLinkedQueue<DBObject>();

	@SuppressWarnings("rawtypes")
	@Override
	public Map<String, Object> on_handler(Map<String, Object> data) {
		// 首次发言判断
		UserInfo userInfo = (UserInfo) data.get("userInfo");
		ClientInfo clientInfo = (ClientInfo)data.get("clientInfo");
		Long roomId = clientInfo.getRoomId(), userId = userInfo.getUserId();
		Map msgData = JSONUtil.jsonToMap((String)data.get("msgData"));
		Map msg =(Map) msgData.get("msg");
		Map from = (Map) msg.get("from");
		Map to = (Map) msg.get("to");
		BasicDBObject record = new BasicDBObject();
		record.put("i", userId);
		record.put("c", msg.get("content"));
		record.put("t", msg.get("etime"));
		record.put("n", from.get("nick_name"));
		record.put("ri", clientInfo.getRoomId());
		if(to != null){
			record.put("ti", to.get("_id"));
			record.put("tn", to.get("nick_name"));
		}
		if(!userInfo.isHasFirstMsg()){
			DB chatLogsDb = DBInit.getChatLogsDb();
    		DBCollection msgCol = chatLogsDb.getCollection("first_msg");
    		msgCol.insert(record);
    		userInfo.setHasFirstMsg(true);
    		Map<String, Object> taskInfo = new HashMap<String, Object>();
    		taskInfo.put("userid", userInfo.getUserId());
    		taskInfo.put("terminal", "flash");
    		taskInfo.put("roomid", roomId);
    		redis.convertAndSend(SocketChannel.getTaskModifyTopoc(), JSONUtil.beanToJson(taskInfo).toString());
		}
		
		//聊天记录
		chatLogCache.add(record);
		if(chatLogCache.size() >= 1000){
			if(ChatLogHandler.chatInsert.tryLock()){
				try {
					insertChatLog();
				} catch (Exception e) {
					ChatLogHandler.chatInsert.unlock();
				}
			}
		}
		return data;
	}
	
	public void insertChatLog(){
		List<DBObject> logs = new ArrayList<DBObject>();
		for(int i = 0; i < 1000; i++){
			DBObject obj = chatLogCache.poll();
			if(obj != null)
				logs.add(obj);
			else
				break;
		}
		log.debug("存储聊天信息 size:"+logs.size());
		DB chatLogsDb = DBInit.getChatLogsDb();
		DBCollection collection = chatLogsDb.getCollection("logs");
		collection.insert(logs);
	}
}
