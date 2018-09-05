package show.we.channel.kafka.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import show.we.channel.kafka.consumer.KafkaConsumerBaseListener;
import show.we.socket.ChatMsgHandler;

public class KafkaConsumerListener extends KafkaConsumerBaseListener{
	
	public static final Logger log = LoggerFactory.getLogger(KafkaConsumerListener.class);
	
	@Override
	public void onReceiveMessage(String key, String topic, String message) {
		log.debug(topic + "	收到kafka消息："+ message);
        ChatMsgHandler.send(topic, message);
	}
	@Override
	public void onReceiveMessage(String topic, String message) {
		//FIXME socket 接受 kafka消息 暂时没用到
        log.debug(topic + "	收到kafka消息："+ message);
        ChatMsgHandler.send(topic, message);
	}
}
