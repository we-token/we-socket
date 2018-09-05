package show.we.channel.kafka.producer;

import java.util.List;

import kafka.producer.KeyedMessage;


public class KafkaProducerTemplate extends KafkaProducerBaseTemplate {

    private List<String> topics;
    
    private String patternTopic;

    public KafkaProducerTemplate() {
        super();
    }
    
    public String getPatternTopic() {
		return patternTopic;
	}



	public void setPatternTopic(String patternTopic) {
		this.patternTopic = patternTopic;
	}

	public List<String> getTopics() {
        return topics;
    }

    public void setTopics(List<String> topics) {
        this.topics = topics;
    }
    
	@Override
	public void sendAsync(String topic, String value) {
		if(topics == null || !topics.contains(topic)){
			if(patternTopic == null || !topic.matches(patternTopic))
    		throw new IllegalArgumentException("Invalid topic value:" + topic);
    	}
        final KeyedMessage<String, String> data = new KeyedMessage<String, String>(topic,value);
        executor.submit(new Runnable() {
            @Override
            public void run() {
                producer.send(data);
            }
        });	
	}
   
    @Override
    public void sendAsync(String topic,String key,String value){
    	if(topics == null || !topics.contains(topic)){
    		if(patternTopic == null || !topic.matches(patternTopic))
    		throw new IllegalArgumentException("Invalid topic value:" + topic);
    	}
        final KeyedMessage<String, String> data = new KeyedMessage<String, String>(topic, key, value);
        executor.submit(new Runnable() {
            @Override
            public void run() {
                producer.send(data);
            }
        });
    }

	@Override
	public void sendSync(String topic, String key, String value) {
		if(topics == null || !topics.contains(topic)){
			if(patternTopic == null || !topic.matches(patternTopic))
    		throw new IllegalArgumentException("Invalid topic value:" + topic);
    	}
		KeyedMessage<String, String> data = new KeyedMessage<String, String>(topic, key, value);
		producer.send(data);
	}

	@Override
	public void sendSync(String topic, String value) {
		if(topics == null || !topics.contains(topic)){
			if(patternTopic == null || !topic.matches(patternTopic))
    		throw new IllegalArgumentException("Invalid topic value:" + topic);
    	}
        KeyedMessage<String, String> data = new KeyedMessage<String, String>(topic,value);
        producer.send(data);
	}
	

}
