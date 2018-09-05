package show.we.channel.kafka.consumer;


public interface KafkaConsumerListener {

	/**
	 * @param value
	 */
    void receiveMessages(String key, String topic, String value);
    
    /**
     * @param value
     */
    void receiveMessages(String topic, String value);
    
    /**
     * @return
     */
    int getProcessThreads();

    /**
     * @return
     */
    String getGroupId();

    /**
     * @return
     */
    String getTopic();
    
    String getPatternTopic();

	
    
    
    




}
