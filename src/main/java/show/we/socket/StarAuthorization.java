package show.we.socket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.corundumstudio.socketio.AuthorizationListener;
import com.corundumstudio.socketio.HandshakeData;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.ttpod.rest.common.util.JSONUtil;
import show.we.socket.common.DeviceMark;
import show.we.socket.common.SocketDataStore;
import show.we.socket.domain.UserInfo;
import show.we.socket.msg.DataFactory;
import show.we.socket.msg.DataOperation;
import show.we.system.context.springComponents.DBInit;
import show.we.system.context.springComponents.RedisInit;

public class StarAuthorization implements AuthorizationListener{
	
	private static final Logger log = LoggerFactory.getLogger(StarAuthorization.class);
	
	private StringRedisTemplate redis = RedisInit.getMainRedis();
	
	/**
	 * 1、roomId ip token完整性验证
	 * 2、如果为用户登录 取出用户相关信息放到SocketDataStore工具类中
	 */
	@Override
	public boolean isAuthorized(HandshakeData data) {
		//FIXME 2、socket 权限验证
		String roomId = null, token = null, address = null;
		roomId = data.getSingleUrlParam("room_id");
		token = data.getSingleUrlParam("access_token");
		address = data.getSingleHeader("X-Forwarded-For");
		
		if(roomId == null || address == null){
			log.error("roomId:"+ roomId + " address:" + address + " 缺少连接信息");
			return false;
		}else{
			log.info("roomId:"+ roomId + " token:" + token + " address:" + address + " 尝试连接");
			if(StringUtils.isBlank(token))
				return true;
			Map<Object, Object> session = redis.opsForHash().entries("token:"+token);
			if(session == null || session.size() == 0){
				log.error("redis 缺少session信息,token:"+token);
				return true;
			}else{
				//可连接性校验
				List<String> handlers = new ArrayList<String>();
            	HashMap<String, Object> map = new HashMap<String, Object>();
            	map.put("handData", data);
            	map.put("session", session);
            	handlers.add(DataOperation.ROBOT);
            	handlers.add(DataOperation.CONNECTABLE);
            	Map<String, Object> dataMap = DataFactory.getFinalData(handlers, map);
            	if(dataMap.get("break") != null && (boolean)dataMap.get("break") == true){
					if(dataMap.get("log") != null)
						log.info(dataMap.get("log").toString());
					return false;
            	}
            	Map<String, Object> sign = new HashMap<String, Object>();
            	
            	//缓存用户信息
            	UserInfo userInfo = new UserInfo();
            	//首次发言信息
            	/**	这里缓存首次发言的标示 避免判断完成首次发言任务 频繁查库*/
            	UserInfo oldUserInfo = SocketDataStore.getUserInfoByUserId(Long.parseLong(session.get("_id").toString()));
            	if(oldUserInfo == null || !oldUserInfo.isHasFirstMsg()){
            		DB chatLogsDb = DBInit.getChatLogsDb();
            		DBCollection msgCol = chatLogsDb.getCollection("first_msg");
            		BasicDBObject cond = new BasicDBObject();
            		cond.put("i", Integer.parseInt(session.get("_id").toString()));
            		DBCursor find = msgCol.find(cond);
            		int count = find.count();
            		if(count > 0)
            			userInfo.setHasFirstMsg(true);
            		find.close();
            	}
            	Map<Object, Object> nobilityInfo = null;
				//贵族信息
				String nobility = redis.opsForValue().get("user:" + session.get("_id") + ":nobility");
				if(nobility != null){
					 nobilityInfo =new HashMap<Object, Object>();
					 String nobilityLevel = nobility.split("\\|")[0];
					 String nobilityTag = nobility.split("\\|")[1];
					 nobilityInfo.put("level", nobilityLevel);
					 nobilityInfo.put("type", nobilityTag);
					 sign.put("nobilityInfo", nobilityInfo);
				}
				
				
				//vip信息
				String vip = redis.opsForValue().get("user:" + session.get("_id") + ":vip");
				if(vip != null){
					sign.put("vip", Integer.parseInt(vip));
					if("2".equals(vip)){
						String vipHiding = redis.opsForValue().get("user:" + session.get("_id") + ":vip_hiding");
						sign.put("vip_hiding", Integer.parseInt(vipHiding));
					}
				}
				
				//是否守护信息
				String guard = redis.opsForValue().get("guards:" + roomId+ ":" + session.get("_id"));
				if(guard == null){
					DBCollection guards = DBInit.getMainMongo().getCollection("guards");
					BasicDBObject param = new BasicDBObject();
					param.append("user_id", Integer.parseInt(session.get("_id").toString()));
					param.append("room_id", Integer.parseInt(roomId));
					param.append("valid_flag", true);
					DBObject guardObj = guards.findOne(param);
					if(guardObj != null){
						sign.put("guard", true);
					}
				}else{
					sign.put("guard", true);
				}
				
				
				
				
				//座驾信息
				String car = redis.opsForValue().get("user:" + session.get("_id") + ":car");
				if(car != null){
					sign.put("car", Integer.parseInt(car));
				}
				//徽章信息
				Map<Object, Object> badges = redis.opsForHash().entries("user:" + session.get("_id") + ":badge");
				if(badges != null){
					long currentTimeMillis = System.currentTimeMillis();
					badges.remove("commongift");
					badges.remove("supergift");
					//判断有效时间
					Iterator<Entry<Object, Object>> it = badges.entrySet().iterator();
					while(it.hasNext()){
						Entry<Object, Object> next = it.next();
						Map badge = JSONUtil.jsonToMap(next.getValue().toString());
						Collection<Map> values = badge.values();
						for(Map item : values){
							long expire= Long.parseLong(item.get("expire_at").toString());
							if(currentTimeMillis > expire){
								it.remove();
							}else{
								badges.put(next.getKey(), badge);
							}
						}
					}
				}
				
				sign.put("_id", session.get("_id"));
				sign.put("spend", session.get("spend")==null?0:Double.parseDouble(session.get("spend").toString()));
				sign.put("nick_name", session.get("nick_name"));
				sign.put("priv", session.get("priv"));
				sign.put("s", DeviceMark.getMark(data.getSingleHeader("s")));
				sign.put("badges", badges);
				sign.put("pic", session.get("pic"));
				sign.put("enterTime", System.currentTimeMillis());
				sign.put("ip", address);
				userInfo.setSign(sign);
				data.getHeaders().put("userId", Arrays.asList(userInfo.getUserId().toString()));/**权限验证完后会执行connection方法 将userId放入HandshakeData 传给connection方法*/
				//将userInfo 放入内存中
				SocketDataStore.registUserInfo(userInfo.getUserId(), userInfo);
				System.out.println("用户ip为"+sign.get("ip").toString());
				
				return true;
			}
			
		}
	}
	

}
