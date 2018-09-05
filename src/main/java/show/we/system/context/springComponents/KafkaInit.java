package com.weibo.system.context.springComponents;

import org.springframework.context.ApplicationContext;

import com.weibo.channel.kafka.consumer.KafkaConsumerListenerContainer;
import com.weibo.channel.kafka.producer.KafkaProducerTemplate;

public class KafkaInit {
	private static  KafkaConsumerListenerContainer container;
	
	private static	KafkaProducerTemplate producer;
	
	public static KafkaConsumerListenerContainer getContainer() {
		return container;
	}
	
	public static KafkaProducerTemplate getProducer() {
		return producer;
	}


	public static void init(ApplicationContext ctx){
		KafkaInit.container = (KafkaConsumerListenerContainer) ctx.getBean("kafkaConsumerListenerContainer");
		KafkaInit.producer = (KafkaProducerTemplate) ctx.getBean("kafkaProducerTemplate");
	}
}
