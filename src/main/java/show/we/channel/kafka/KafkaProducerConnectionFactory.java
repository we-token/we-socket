package show.we.channel.kafka;

import org.springframework.beans.factory.FactoryBean;

import show.we.channel.kafka.producer.KafkaProducerConfig;

public class KafkaProducerConnectionFactory implements FactoryBean {

    private KafkaProducerConfig producerConfig;

    @Override
    public Object getObject() throws Exception {
        return this;
    }

    @Override
    public Class<?> getObjectType() {
        return KafkaProducerConnectionFactory.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public void setProducerConfig(KafkaProducerConfig producerConfig) {
        this.producerConfig = producerConfig;
    }
    
    public KafkaProducerConfig getProducerConfig() {
    	System.out.println("KafkaConsumerConnectionFactory.getConsumerConfig:"+producerConfig);
        return producerConfig;
    }
    
}
