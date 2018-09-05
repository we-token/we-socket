package show.we.socket.msg.handler.conn;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import show.we.socket.common.TimeUtils;
import show.we.socket.domain.ClientInfo;
import show.we.socket.domain.UserInfo;
import show.we.socket.msg.handler.chat.ChatLogHandler;
import show.we.system.context.springComponents.DBInit;

public class ConnectionHandler {
	
	private static final Logger log = LoggerFactory.getLogger(ConnectionHandler.class);
	
	public static Lock chatInsert = new ReentrantLock();
	
	private static ConcurrentLinkedQueue<DBObject> saveTimeCache = new ConcurrentLinkedQueue<DBObject>();
	
	public static void  SaveClientTime(UserInfo userInfo,ClientInfo clientInfo){
		Long roomId = clientInfo.getRoomId(), userId = userInfo.getUserId();
		BasicDBObject record = new BasicDBObject();
		record.put("user_id", userId);
		record.put("nick_name", (String) userInfo.getSign().get("nick_name"));
		record.put("room_id", roomId);
		record.put("via",(String) userInfo.getSign().get("s") ==null?"phone":"web" );
		record.put("timestamp", System.currentTimeMillis());
		record.put("start",  userInfo.getSign().get("enterTime"));
		record.put("end",  userInfo.getSign().get("exitTime"));
		record.put("mill", TimeUtils.differTime( userInfo.getSign().get("exitTime"), userInfo.getSign().get("enterTime")));
		saveTimeCache.add(record);
		if(saveTimeCache.size() >= 1000){
			if(ChatLogHandler.chatInsert.tryLock()){
				try {
					insertConnectionLog();
				} catch (Exception e) {
					ChatLogHandler.chatInsert.unlock();
				}
			}
		}
	}

	private static void insertConnectionLog() {
		List<DBObject> logs = new ArrayList<DBObject>();
		for(int i = 0; i < 1000; i++){
			DBObject obj = saveTimeCache.poll();
			if(obj != null)
				logs.add(obj);
			else
				break;
		}
		log.debug("存储断开连接的时长====="+logs.size());
		DB chatLogsDb = DBInit.getChatLogsDb();
		DBCollection collection = chatLogsDb.getCollection("user_room_logs");
		collection.insert(logs);
		log.info("logs+"+logs.size()+"数组"+logs.toArray());
	}
}
