package com.weibo;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.weibo.core.logback.GroupPropertyDefiner;
import com.weibo.socket.StarSocketLauncher;
import com.weibo.system.context.springComponents.DBInit;
import com.weibo.system.context.springComponents.RedisInit;

public class Socket {
	
	public static void main(String[] args) {
		/**
		 * 验证args参数
		 * args[0]:ip
		 * args[1]:port
		 */
		if(args == null || args.length != 2){
			throw new RuntimeException("socket ip and port is vaild "+args);
		}
		
		/**logback日志文件根据端口区分*/
		GroupPropertyDefiner.setGroup(args[1]);
		
		/**
		 * kafka 消费者 根据端口分组
		 * KafkaConsumerBaseListener afterPropertiesSet()处 进行分组
		 */
		try {
			StarSocketLauncher.setGroupid(InetAddress.getLocalHost().getHostAddress() + "-" + args[1]);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		/**初始化spring容器*/
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring/application.xml");
		DBInit.init(context);
		RedisInit.init(context);
//		KafkaInit.init(context);
		
		/**启动socket*/ //FIXME 1、socket 启动
		context.getBean("socketLauncher", StarSocketLauncher.class).start(args);
	}
	
}
