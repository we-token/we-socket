package show.we.channel.kafka.consumer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import show.we.socket.StarSocketLauncher;

public abstract class KafkaConsumerBaseListener  implements KafkaConsumerListener, InitializingBean, DisposableBean {


	private int processThreads = -1;

	protected ExecutorService executor;

	private String groupId;

    private String topic;
    
    private String patternTopic;

	public int getProcessThreads() {
		return processThreads;
	}

	public ExecutorService getExecutor() {
		return executor;
	}

	public void setProcessThreads(int processThreads) {
		if (processThreads < 0) {
			throw new IllegalArgumentException("Invalid processThreads value:" + processThreads);
		}
		this.processThreads = processThreads;
	}

	@Override
	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
        if (null == groupId || "".equals(groupId)) {
            throw new IllegalArgumentException("Invalid groupId value:" + groupId);
        }
		this.groupId = groupId;
	}


    @Override
    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic){
    	if(patternTopic != null)
    		throw new IllegalArgumentException("patternTopic is setted value");
        if (null == topic || "".equals(topic)) {
            throw new IllegalArgumentException("Invalid topic value:" + topic);
        }
        this.topic = topic;
    }
    
    @Override
	public String getPatternTopic() {
		return patternTopic;
	}

	public void setPatternTopic(String patternTopic) {
		if(topic != null)
    		throw new IllegalArgumentException("topic is setted value");
		if (null == patternTopic || "".equals(patternTopic)) {
            throw new IllegalArgumentException("Invalid patternTopic value:" + patternTopic);
        }
		this.patternTopic = patternTopic;
	}

	public abstract void onReceiveMessage(String key,String topic, String message);
	
	public abstract void onReceiveMessage(String topic, String message);

	@Override
	public void receiveMessages(String key, String topic, String value) {
		onReceiveMessage(key, topic, value);
	}
	
	
	@Override
	public void receiveMessages(String topic, String value) {
		onReceiveMessage(topic, value);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.groupId = StarSocketLauncher.getGroupid();
		if (this.processThreads > 0) {
			this.executor = Executors.newFixedThreadPool(this.processThreads);
		}
	}

	@Override
	public void destroy() throws Exception {
		if (this.executor != null) {
			this.executor.shutdown();
			this.executor = null;
		}
	}

}
