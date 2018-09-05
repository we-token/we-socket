package com.weibo.system.context.springComponents;

import org.springframework.context.ApplicationContext;
import com.mongodb.DB;

/**
 * @author cyan
 * 系统
 * */
public class DBInit {

   /* 
    private static  DB adminMongo;
    private static  DB topicMongo;
    private static  DB logMongo;
    private static  DB unionMongo;
    private static  DB activeMongo;
    private static  DB singMongo;
    private static  DB rankMongo;
    private static  DB adminDb;*/
	private static  DB mainMongo;
    private static	DB chatLogsDb;
    
	/*
	public static DB getAdminMongo() {
		return adminMongo;
	}
	public static DB getTopicMongo() {
		return topicMongo;
	}
	public static DB getLogMongo() {
		return logMongo;
	}
	public static DB getUnionMongo() {
		return unionMongo;
	}
	public static DB getActiveMongo() {
		return activeMongo;
	}
	public static DB getSingMongo() {
		return singMongo;
	}
	public static DB getRankMongo() {
		return rankMongo;
	}
	public static DB getAdminDb() {
		return adminDb;
	}*/
    public static DB getMainMongo() {
		return mainMongo;
	}
	public static DB getChatLogsDb() {
		return chatLogsDb;
	}
	
	public static void init(ApplicationContext ctx){
	  /*  
	    DBInit.adminMongo = (DB) ctx.getBean("adminMongo");
	    DBInit.topicMongo = (DB) ctx.getBean("topicMongo");
	    DBInit.logMongo = (DB) ctx.getBean("logMongo");
	    DBInit.unionMongo = (DB) ctx.getBean("unionMongo");
	    DBInit.activeMongo = (DB) ctx.getBean("activeMongo");
	    DBInit.singMongo = (DB) ctx.getBean("singMongo");
	    DBInit.rankMongo = (DB) ctx.getBean("rankMongo");
	    DBInit.adminDb = (DB) ctx.getBean("adminDb");*/
		DBInit.mainMongo = (DB) ctx.getBean("mainMongo");
	    DBInit.chatLogsDb = (DB) ctx.getBean("chatLogsDb");
	}
}
