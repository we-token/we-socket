package show.we.socket.msg;

import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import show.we.socket.msg.handler.IMsgHandler;

public class MsgDataHandlerManager {
	
	/**
	 * 数据处理器集合
	 */
	private static ConcurrentHashMap<String, IMsgHandler> dataHandlerMap = new ConcurrentHashMap<String, IMsgHandler>();
	
	/**
	 * 数据处理器队列
	 */
	private static Queue<String> dataHanlerqueue = new ConcurrentLinkedQueue<String>();
	
	/**
	 * 注册数据处理器
	 * 
	 * @param operation
	 *            操作类型
	 * @param rptDataHandler
	 *            处理器
	 */
	public static void registHandler(String operation,
			IMsgHandler msgHandler) {
		dataHandlerMap.put(operation, msgHandler);
		dataHanlerqueue.add(operation);
	}

	/**
	 * 获取所有数据处理器
	 * 
	 * @return 返回所有数据处理器
	 */
	public static Queue<String> getAllHandler() {
		return dataHanlerqueue;
	}

	/**
	 * 获取数据处理器
	 * @param operation 操作类型
	 * @return 数据处理器
	 */
	public static IMsgHandler getHandler(String operation)
	{
		return dataHandlerMap.get(operation);
	}

}
