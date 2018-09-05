package show.we.channel.kafka.consumer;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.consumer.Whitelist;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import show.we.channel.kafka.KafkaConsumerConnectionFactory;

public class KafkaConsumerListenerContainer implements ListenerContainer, InitializingBean, DisposableBean {


	private static final Logger LOG = LoggerFactory.getLogger(KafkaConsumerListenerContainer.class);
	

	private List<KafkaConsumerListener> consumerListeners = new ArrayList<KafkaConsumerListener>();


	private KafkaConsumerConnectionFactory consumerConnectionFactory = null;


	private final CopyOnWriteArraySet<ConsumerConnector> consumers = new CopyOnWriteArraySet<ConsumerConnector>();

	@Override
	public void register(final KafkaConsumerListener listener) {
		System.out.println("KafkaConsumerListenerContainer.register:"+listener);
		this.addListener(listener);
		this.consumerListeners.add(listener);

	}

	@Override
	public void afterPropertiesSet() throws Exception {
		System.out.println("KafkaConsumerListenerContainer.afterPropertiesSet:"+this.consumerListeners);
		for (KafkaConsumerListener listener : this.consumerListeners) {
			this.addListener(listener);
		}
	}

	/**
	 * 
	 * @param listener
	 */
	private void addListener(KafkaConsumerListener listener) {
		System.out.println("KafkaConsumerListenerContainer.addListener:"+listener);
		ConsumerConnector consumer = createConnector(listener);
		System.out.println("KafkaConsumerListenerContainer.addListener:"+consumer);
		this.consumers.add(consumer);
		this.poll(listener, consumer);
	}

	/**
	 * 
	 * @param listener
	 * @return
	 */
	private ConsumerConnector createConnector(KafkaConsumerListener listener) {
		System.out.println("KafkaConsumerListenerContainer.createConnector:"+listener);
		KafkaConsumerConfig kafkaConsumerConfig = this.consumerConnectionFactory.getConsumerConfig();
		System.out.println("KafkaConsumerListenerContainer.createConnector:"+kafkaConsumerConfig);
		Properties properties = kafkaConsumerConfig.getProperties();
		properties.put("group.id", listener.getGroupId());
		return Consumer.createJavaConsumerConnector(kafkaConsumerConfig.getConsumerConfig());
	}

	/**
	 * 
	 * @param listener
	 * @param consumer
	 */
	private void poll(final KafkaConsumerListener listener, ConsumerConnector consumer) {
		System.out.println("KafkaConsumerListenerContainer.poll:" + listener + ",consumer:" + consumer);
        int threadCount = listener.getProcessThreads();
        String topic = listener.getTopic();
        Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
        topicCountMap.put(topic, threadCount);
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
        final List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);
        ExecutorService threadPool = Executors.newFixedThreadPool(threadCount);
        for (final KafkaStream stream : streams) {
            threadPool.submit(new Runnable() {
                @Override
                public void run() {
                    ConsumerIterator<byte[], byte[]> it = stream.iterator();
                    while (it.hasNext()) {
                        String key, value;
                        MessageAndMetadata<byte[], byte[]> message = it.next();
                        try {
                            key = null;
                            value = null;
                            byte[] byteKey = message.key();
                            if (null != byteKey) {
                                key = new String(byteKey, "UTF-8");
                            }
                            value = new String(message.message(), "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            LOG.debug("kafka fetch message error", e);
                            return;
                        }
                        try {
                            if (null != key) {
                                LOG.debug("kafka poll message key=" + key + ",value=" + value);
                                listener.receiveMessages(key, message.topic(), value);
                            } else {
                                LOG.debug("kafka poll message value=" + value);
                                listener.receiveMessages(message.topic(), value);
                            }
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
	}

	public void setConsumerConnectionFactory(KafkaConsumerConnectionFactory consumerConnectionFactory) {
		System.out.println("KafkaConsumerListenerContainer.setConsumerConnectionFactory:"+consumerConnectionFactory);
		this.consumerConnectionFactory = consumerConnectionFactory;
	}

	public KafkaConsumerConnectionFactory getConsumerConnectionFactory() {
		System.out.println("KafkaConsumerListenerContainer.getConsumerConnectionFactory:"+consumerConnectionFactory);
		return consumerConnectionFactory;
	}

	public List<KafkaConsumerListener> getConsumerListeners() {
		System.out.println("KafkaConsumerListenerContainer.getConsumerListeners:"+consumerListeners);
		return consumerListeners;
	}

	public void setConsumerListeners(List<KafkaConsumerListener> consumerListeners) {
		System.out.println("KafkaConsumerListenerContainer.setConsumerListeners:"+consumerListeners);
		this.consumerListeners = consumerListeners;
	}

	@Override
	public void destroy() throws Exception {
		System.out.println("KafkaConsumerListenerContainer.destroy:"+consumers);
		for (ConsumerConnector consumer : consumers) {
			consumer.shutdown();
			consumer = null;
		}
		this.consumers.clear();
		this.consumerListeners.clear();
	}

}
