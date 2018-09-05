package show.we.channel.redis;

import com.ttpod.rest.ext.RestExtension;
import show.we.socket.common.SocketChannel;

/* 约定的key值 */
public  abstract class KeyUtils {
   
	public static byte[] serializer(String string){
		return RestExtension.asBytes(string);
    }

    public  static String decode(byte[] data){
    	return RestExtension.asString(data);
    }
    
    /*redis各种渠道*/
    public static class CHANNEL {
        public static String room(Object roomId) {
            return SocketChannel.ROOM_CHANNEL+roomId;
        }

        public static String user(Object userId) {
            return SocketChannel.USER_CHANNEL+userId;
        }
        
        public static String roomUser(Object roomId, Object userId){
        	return SocketChannel.ROOM_USER_CHANNEL + roomId + ":" + userId;
        }
                
        public static String all() {
            return SocketChannel.ALL_CHANNEL;
        }
    }

}
