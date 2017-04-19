package com.nituv.lbi;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.nituv.common.exceptions.LbiParameterException;
import com.nituv.common.util.NtvDateUtils;

public class LbiScheduledThreadsPoolMgr {
	private static Object m_lockObject = new Object(); 
	private static ScheduledThreadPoolExecutor threadPool;
	static
	{
		init();
	}
	private static void init() {
		synchronized (m_lockObject) {
			if (threadPool != null) {
				return;
			}
			threadPool = new ScheduledThreadPoolExecutor(10);//,50,30, TimeUnit.SECONDS, workQue);
			threadPool.setKeepAliveTime(60, TimeUnit.SECONDS);
			threadPool.setMaximumPoolSize(50);
			threadPool.allowCoreThreadTimeOut(true);
			threadPool.setRemoveOnCancelPolicy(true); // on cancel remove tasks from queue on shutdown
			threadPool.setExecuteExistingDelayedTasksAfterShutdownPolicy(false); // on cancel remove scheduled tasks from queue on shutdown
		}
	}
	
	public static ScheduledThreadPoolExecutor getScheduledThreadPoolExecutor()
	{
		return threadPool;
	}
	
	public static void execute(Runnable runnable)
	{
		threadPool.execute(runnable);
	}
	
	public static <V> void execute(Callable<V> callable)
	{
		threadPool.schedule(callable,0,TimeUnit.MILLISECONDS);
	}
	
	public static void schedule(Runnable runnable, int seconds)
	{
		threadPool.schedule(runnable, seconds, TimeUnit.SECONDS);
	}

	public static void schedule(Runnable runnable, Date timeToExecute) throws LbiParameterException
	{
		schedule(runnable, timeToExecute,0);
	}

	public static void schedule(Runnable runnable, Date timeToExecute, int secondsTolorance) throws LbiParameterException
	{
		int secGap = NtvDateUtils.getSecondsGapFromNow(timeToExecute);
		if (secGap  < 0 - secondsTolorance) {
			throw new LbiParameterException("timeToExecute is in the past:" + timeToExecute);
		}
		threadPool.schedule(runnable, secGap, TimeUnit.SECONDS);
	}

	
	public static void remove(Runnable runnable) {
		threadPool.remove(runnable);
	}

	
	public static void shutdown() 
	{
		threadPool.shutdown();
	}

	public static void waitTillReady() // need to call this only on start;
	{
		init();
		
	}
}
