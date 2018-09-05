package show.we.channel.kafka;

import org.springframework.beans.factory.FactoryBean;

import show.we.channel.kafka.consumer.KafkaConsumerConfig;


public class KafkaConsumerConnectionFactory implements FactoryBean {

    private KafkaConsumerConfig consumerConfig;

    @Override
    public Object getObject() throws Exception {
    	System.out.println("KafkaConsumerConnectionFactory.getObject:"+this);
        return this;
    }

    @Override
    public Class<?> getObjectType() {
    	System.out.println("KafkaConsumerConnectionFactory.getObjectType:"+KafkaConsumerConnectionFactory.class);
        return KafkaConsumerConnectionFactory.class;
    }

    @Override
    public boolean isSingleton() {
    	System.out.println("KafkaConsumerConnectionFactory.isSingleton:"+true);
        return true;
    }

    public void setConsumerConfig(KafkaConsumerConfig consumerConfig) {
    	System.out.println("KafkaConsumerConnectionFactory.setConsumerConfig:"+consumerConfig);
        this.consumerConfig = consumerConfig;
    }
    
    public KafkaConsumerConfig getConsumerConfig() {
    	System.out.println("KafkaConsumerConnectionFactory.getConsumerConfig:"+consumerConfig);
        return consumerConfig;
    }
}
