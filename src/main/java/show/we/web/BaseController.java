package com.weibo.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.ttpod.rest.common.util.JSONUtil;
import com.ttpod.rest.web.StaticSpring;
import com.weibo.channel.redis.KeyUtils;
import com.weibo.channel.redis.RedisSendService;

@Controller
@RequestMapping(value="/base")
public class BaseController {

	@RequestMapping(value="/welcome")
	public ModelAndView welcome(HttpServletRequest request){
		ModelAndView view = new ModelAndView("/base/welcome");
		view.addObject("name","  hi 看到我就说明当前系统启动成功！"
				+StaticSpring.getContext().getBean("userMongo").toString());

		Map<String,String> map = new HashMap<String,String>();
		map.put("type", "star-socket-new-test");
		map.put("from", "star-socket-new:baseController");
		map.put("timemili", ""+System.currentTimeMillis());
		RedisSendService.publishToChat(KeyUtils.CHANNEL.all(),JSONUtil.beanToJson(map));
		
		return view;
	}
}
