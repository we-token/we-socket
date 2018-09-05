package show.we.channel.kafka.consumer;

import java.util.Properties;

import kafka.consumer.ConsumerConfig;

import org.apache.commons.lang.StringUtils;

public class KafkaConsumerConfig {

	private String zkConnect;

	private String zkSessionTimeoutMs;

	private String zkSyncTimeMs;

	private String autoCommitIntervalMs;	
	
	private String autoOffsetReset;
	
	private String rebalanceMs;

	private Properties properties;
	
	public KafkaConsumerConfig() {
		System.out.println("KafkaConsumerConfig.KafkaConsumerConfig:"+properties);
		properties = new Properties();
	}

	public String getZkConnect() {
		System.out.println("KafkaConsumerConfig.getZkConnect:"+zkConnect);
		return zkConnect;
	}

	public void setZkConnect(String zkConnect) {
		System.out.println("KafkaConsumerConfig.setZkConnect:"+zkConnect);
		this.zkConnect = zkConnect;
	}

	public String getZkSessionTimeoutMs() {
		System.out.println("KafkaConsumerConfig.getZkSessionTimeoutMs:"+zkSessionTimeoutMs);
		return zkSessionTimeoutMs;
	}

	public void setZkSessionTimeoutMs(String zkSessionTimeoutMs) {
		System.out.println("KafkaConsumerConfig.setZkSessionTimeoutMs:"+zkSessionTimeoutMs);
		this.zkSessionTimeoutMs = zkSessionTimeoutMs;
	}

	public String getZkSyncTimeMs() {
		System.out.println("KafkaConsumerConfig.getZkSyncTimeMs:"+zkSyncTimeMs);
		return zkSyncTimeMs;
	}

	public void setZkSyncTimeMs(String zkSyncTimeMs) {
		System.out.println("KafkaConsumerConfig.setZkSyncTimeMs:"+zkSyncTimeMs);
		this.zkSyncTimeMs = zkSyncTimeMs;
	}

	public String getAutoCommitIntervalMs() {
		System.out.println("KafkaConsumerConfig.getAutoCommitIntervalMs:"+autoCommitIntervalMs);
		return autoCommitIntervalMs;
	}

	public void setAutoCommitIntervalMs(String autoCommitIntervalMs) {
		System.out.println("KafkaConsumerConfig.setAutoCommitIntervalMs:"+autoCommitIntervalMs);
		this.autoCommitIntervalMs = autoCommitIntervalMs;
	}
	
	public String getRebalanceMs() {
		System.out.println("KafkaConsumerConfig.getrebalanceMs:"+rebalanceMs);
		return rebalanceMs;
	}

	public void setRebalanceMs(String rebalanceMs) {
		System.out.println("KafkaConsumerConfig.SetrebalanceMs:"+rebalanceMs);
		this.rebalanceMs = rebalanceMs;
	}

	public ConsumerConfig getConsumerConfig() {
		
		if (StringUtils.isBlank(zkConnect)) {
			throw new IllegalArgumentException("Blank zkConnect");
		}
		if (StringUtils.isNotBlank(autoOffsetReset)) {
			properties.put("auto.offset.reset", this.autoOffsetReset);
		}
		if (StringUtils.isNotBlank(zkSessionTimeoutMs)) {
			properties.put("zookeeper.session.timeout.ms", this.zkSessionTimeoutMs);
		}
		if (StringUtils.isNotBlank(zkSyncTimeMs)) {
			properties.put("zookeeper.sync.time.ms", this.zkSyncTimeMs);
		}
		if (StringUtils.isNotBlank(autoCommitIntervalMs)) {
			properties.put("auto.commit.interval.ms", this.autoCommitIntervalMs);
		}
		if (StringUtils.isNotBlank(rebalanceMs)) {
			properties.put("rebalance.backoff.ms", this.rebalanceMs);
		}
		
		properties.put("zookeeper.connect", this.zkConnect);
		System.out.println("KafkaConsumerConfig.getConsumerConfig:"+properties);
		return new ConsumerConfig(properties);
	}

	public Properties getProperties() {
		System.out.println("KafkaConsumerConfig.getProperties:"+properties);
		return properties;
	}

	public String getAutoOffsetReset() {
		return autoOffsetReset;
	}

	public void setAutoOffsetReset(String autoOffsetReset) {
		this.autoOffsetReset = autoOffsetReset;
	}
	
	
}
