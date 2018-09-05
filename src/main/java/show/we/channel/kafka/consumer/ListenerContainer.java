package show.we.channel.kafka.consumer;

import java.io.Serializable;


public interface ListenerContainer extends Serializable {


    void register(KafkaConsumerListener listener);
}
