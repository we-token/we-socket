package show.we.socket.msg.handler.chat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.corundumstudio.socketio.BroadcastOperations;
import com.ttpod.rest.common.util.JSONUtil;
import show.we.channel.redis.KeyUtils;
import show.we.socket.StarSocketLauncher;
import show.we.socket.domain.ClientInfo;
import show.we.socket.domain.UserInfo;
import show.we.socket.msg.handler.IMsgHandler;
import show.we.system.utils.http.Web;

public class ValidHandler extends IMsgHandler{
	
	private static final Logger log = LoggerFactory.getLogger(ValidHandler.class);
	
	private final static Map<Integer, Double>  LEVEL_SPEND = new HashMap<Integer, Double>();
	
	private final static String NUM_REGX = "[^0-9\\u96F6\\u4E00\\u4E8C\\u4E09\\u56DB\\u4E94\\u516D\\u4E03\\u516B\\u4E5D\\u5341\\u58F9\\u8D30\\u53C1\\u8086\\u4F0D\\u9646\\u67D2\\u634C\\u7396\\u62FE\\u2460-\\u2469\\uFF11\\uFF12\\uFF13\\uFF14\\uFF15\\uFF16\\uFF17\\uFF18\\uFF19\\uFF10]";
	
	static{
		LEVEL_SPEND.put(0,0d);
		LEVEL_SPEND.put(1,1000d);
		LEVEL_SPEND.put(2,2000d);
		LEVEL_SPEND.put(3,5000d);
		LEVEL_SPEND.put(4,10000d);
		LEVEL_SPEND.put(5,20000d);
		LEVEL_SPEND.put(6,36000d);
		LEVEL_SPEND.put(7,60000d);
		LEVEL_SPEND.put(8,94000d);
		LEVEL_SPEND.put(9,140000d);
		LEVEL_SPEND.put(10,200000d);
		LEVEL_SPEND.put(11,300000d);
		LEVEL_SPEND.put(12,420000d);
		LEVEL_SPEND.put(13,570000d);
		LEVEL_SPEND.put(14,750000d);
		LEVEL_SPEND.put(15,1000000d);
		LEVEL_SPEND.put(16,1280000d);
		LEVEL_SPEND.put(17,1580000d);
		LEVEL_SPEND.put(18,1900000d);
		LEVEL_SPEND.put(19,2250000d);
		LEVEL_SPEND.put(20,2630000d);
		LEVEL_SPEND.put(21,3030000d);
		LEVEL_SPEND.put(22,3460000d);
		LEVEL_SPEND.put(23,3930000d);
		LEVEL_SPEND.put(24,4440000d);
		LEVEL_SPEND.put(25,4990000d);
		LEVEL_SPEND.put(26,5580000d);
		LEVEL_SPEND.put(27,6220000d);
		LEVEL_SPEND.put(28,6910000d);
		LEVEL_SPEND.put(29,7650000d);
		LEVEL_SPEND.put(30,8440000d);
		LEVEL_SPEND.put(31,9290000d);
		LEVEL_SPEND.put(32,10220000d);
		LEVEL_SPEND.put(33,11270000d);
		LEVEL_SPEND.put(34,12480000d);
		LEVEL_SPEND.put(35,13910000d);
		LEVEL_SPEND.put(36,15640000d);
		LEVEL_SPEND.put(37,17770000d);
		LEVEL_SPEND.put(38,20420000d);
		LEVEL_SPEND.put(39,23730000d);
		LEVEL_SPEND.put(40,27840000d);
		/*LEVEL_SPEND.put(41,32920000d);
		LEVEL_SPEND.put(42,38180000d);
		LEVEL_SPEND.put(43,43720000d);
		LEVEL_SPEND.put(44,49570000d);
		LEVEL_SPEND.put(45,55850000d);
		LEVEL_SPEND.put(46,62620000d);
		LEVEL_SPEND.put(47,70100000d);
		LEVEL_SPEND.put(48,78600000d);
		LEVEL_SPEND.put(49,87880000d);
		LEVEL_SPEND.put(50,99820000d);*/
		
	}
	
	/**
	 * msg 合法性验证
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Map<String, Object> on_handler(Map<String, Object> data) {
		UserInfo userInfo = (UserInfo) data.get("userInfo");
		ClientInfo clientInfo = (ClientInfo)data.get("clientInfo");
		Long roomId = clientInfo.getRoomId(), userId = userInfo.getUserId();
		Map msgData = JSONUtil.jsonToMap((String)data.get("msgData"));
		Map msg =(Map) msgData.get("msg");
		String content = (String) msg.get("content");
		int level = (int) msg.get("level");
		//数字校验
		int priv = userInfo.getPriv();
		if(level == 0 && priv == 3){
			if(content.length() > 20 || content.replaceAll(NUM_REGX, "").length() >= 7){
				data.put("break", true);
				return data;
			}
		}
		//检查禁言
		Long shutup = redis.getExpire("room:" + roomId + ":shutup:" + userId);
		if(shutup > 0){
			BroadcastOperations roomOperations = StarSocketLauncher.server.getRoomOperations(KeyUtils.CHANNEL.roomUser(roomId, userId));
			roomOperations.sendEvent("message", "{action: 'manage.shutup_ttl', data_d: {ttl: " +shutup+ "}}");
			data.put("break", true);
			return data;
		}
		//等级校验
		if(userInfo.getPriv() == 3){
			//用户
			Double spend = userInfo.getSpend();
			if(level < 0 || level >= LEVEL_SPEND.size()){
				log.info("当前用户的等级是：======="+level);
				log.info("用户:" + userInfo + ",等级非法   level:"+level);
				data.put("break", true);
				return data;
			}else{
				Double lrange = LEVEL_SPEND.get(level), rrange = LEVEL_SPEND.get(level+1);
				if((spend < lrange) || (rrange!=null && spend > rrange)){
					try {
						double fact_spend = 0;
						Map info = (Map) Web.api("user/info/" + clientInfo.getToken());
						Map finance = (Map)info.get("finance");
						Object obj = finance.get("coin_spend_total");
						if(obj != null)
							fact_spend = Double.parseDouble(obj.toString());
						 if(fact_spend < lrange || fact_spend > rrange){
							 log.info("等级不符 userid:" + userId + "   token:" + clientInfo.getToken() + "  spend:"+ fact_spend + "    level:" + level);
							 if(fact_spend < LEVEL_SPEND.get(1)){
								 chatRedis.opsForValue().set("seal:user:" + userId, "", 10, TimeUnit.MINUTES);
							 }
							 data.put("break", true);
							 data.put("disconnect", true);
							 return data;
						 }
							
					} catch (Exception e) {
						log.error("api https 错误 user/info/" + clientInfo.getToken());
						log.error(e.getMessage());
						e.printStackTrace();
					}
				}
			}
				
			

				
		}
		return data;
	}
	
}
