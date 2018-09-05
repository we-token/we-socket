package com.weibo.system.context;

import javax.servlet.ServletContextEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

import com.weibo.socket.StarSocketLauncher;
import com.weibo.system.context.springComponents.DBInit;
import com.weibo.system.context.springComponents.KafkaInit;
import com.weibo.system.context.springComponents.RedisInit;
import com.weibo.web.BaseController;


/**
 * spring容器启动监听器
 * */
public class SpringContextListener extends ContextLoaderListener{

	public static final Logger log = LoggerFactory.getLogger(BaseController.class);
	private static WebApplicationContext wac = null;
	
	@Override
	public void contextInitialized(ServletContextEvent event) {
		/*调用父类方法继续初始化容器super.contextInitialized(event);*/
		wac = initWebApplicationContext(event.getServletContext());
		
		/*将组件以静态属性的形式注入到响应的工具类中,方便某些非spring组件可以直接使用*/
		DBInit.init(wac);
		RedisInit.init(wac);
		KafkaInit.init(wac);
		wac.getBean("socketLauncher", StarSocketLauncher.class).start(new String[]{"127.0.0.1","9999"});
		log.info("spring 容器初始化完毕!"+wac.toString());
		
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		// TODO Auto-generated method stub
		wac.getBean("socketLauncher", StarSocketLauncher.class).stop();;
		super.contextDestroyed(event);
		log.info("spring 容器销毁完毕!"+wac.toString());
	}
	
	/**
	 * 根据名称获取spring中组件
	 * @param beanName
	 * @return
	 * */
	public static Object getBean(String beanName){
		return wac.getBean(beanName);
	}
	
}
