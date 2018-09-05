package show.we.socket.msg.handler.conn;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import show.we.socket.common.TimeUtils;
import show.we.socket.domain.ClientInfo;
import show.we.socket.domain.UserInfo;
import show.we.socket.msg.handler.chat.ChatLogHandler;
import show.we.system.context.springComponents.DBInit;
import show.we.system.context.springComponents.RedisInit;

public class ConnectionRoomTimesHandler {
	
	private static final Logger log = LoggerFactory.getLogger(ConnectionRoomTimesHandler.class);
	
	public static Lock chatInsert = new ReentrantLock();
	
	private static ConcurrentHashMap<Long,DBObject> saveCache = new ConcurrentHashMap<Long,DBObject>();
	
	public static void  SaveRoomNum(Long userId,Long roomId){
		DB chatLogsDb = DBInit.getChatLogsDb();
		DBCollection collection = chatLogsDb.getCollection("user_room_logs");
		DBCollection numCollection = chatLogsDb.getCollection("user_room_num_logs");
		BasicDBObject cond = new BasicDBObject();  
		cond.put("user_id", userId);
		cond.put("room_id", roomId);
		DBCursor cursor = collection.find(cond);
		DBObject data = cursor.hasNext()?cursor.next():null;
	
		if(data==null){
			DBObject numData = saveCache.get("userId");
			if(numData==null){
				numData =numCollection.findOne(userId);
			}
			if(numData==null){ 
				numData = new BasicDBObject();
				numData.put("_id", userId);
				numData.put("timestamp", System.currentTimeMillis());
				numData.put("room_nums", 1);
				numCollection.insert(numData);
				saveCache.put(userId,numData);
			}else{
				int num = (int) numData.get("room_nums");
				num = num+1;
				numData.put("room_nums", num);
				saveCache.put(userId,numData);
				if(saveCache.size() >= 1000){
					if(chatInsert.tryLock()){//修改
						try {
							updateConnectionLog(numCollection);
						} finally {
							chatInsert.unlock();
						}
					}
				}
			}
			
			
		}
		
	}

	private static void updateConnectionLog(DBCollection collection) {
		log.debug("开始更新库");
		Iterator<Map.Entry<Long,DBObject>> iterator = saveCache.entrySet().iterator();
		while(iterator.hasNext()){
			Map.Entry<Long,DBObject> entry = iterator.next();
			Long userId = entry.getKey();
			int num = (int) entry.getValue().get("room_nums");
			collection.update(new BasicDBObject("_id",userId), new BasicDBObject("$set",new BasicDBObject("room_nums",num)));
			saveCache.clear();
		}
	}	
}
