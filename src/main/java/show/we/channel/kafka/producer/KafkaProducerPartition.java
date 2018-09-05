package show.we.channel.kafka.producer;

import java.util.Random;

import org.apache.commons.lang.StringUtils;

import kafka.producer.Partitioner;
import kafka.utils.VerifiableProperties;


public class KafkaProducerPartition implements Partitioner {
	
	public KafkaProducerPartition(VerifiableProperties props) {
		 
    }
	
	@Override
	public int partition(Object key,int numPartitions) {
        if (StringUtils.isBlank((String)key)) {
            Random random = new Random();
            return random.nextInt(numPartitions);
        }
        else {
            int result = Math.abs(key.hashCode())%numPartitions; //很奇怪，
                     //hashCode 会生成负数，奇葩，所以加绝对值
            return result;
        }
	}

}
