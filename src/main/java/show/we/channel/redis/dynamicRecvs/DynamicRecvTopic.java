package show.we.channel.redis.dynamicRecvs;

import org.springframework.data.redis.listener.Topic;

public class DynamicRecvTopic implements Topic{

	private String topic;
	public DynamicRecvTopic(String _topic) {
		this.topic = _topic;
	}

	@Override
	public String getTopic() {
		return this.topic;
	}
}
