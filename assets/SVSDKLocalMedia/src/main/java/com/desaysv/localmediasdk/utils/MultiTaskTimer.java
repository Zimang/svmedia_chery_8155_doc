package com.desaysv.localmediasdk.utils;


import android.util.SparseArray;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author  ZJP
 * @version 1.0
 * @since   2017.05.03
 */
public class MultiTaskTimer {
	
	private String TAG = "Lib-MultiTaskTimer";
	private ScheduledThreadPoolExecutor mExecutor = new ScheduledThreadPoolExecutor(1);
	private ITimerTaskHandler mTimerTaskHandler = null;
	private SparseArray<Task> mTaskHashMap = new SparseArray<>();
	private SparseArray<ScheduledFuture<?>> mFutureHashMap = new SparseArray<>();
	private final int FIRST_TIMER_ID = -0xFFFF;

	/**
	 * 构造方法
	 * @param timerTaskHandler ITimerTaskHandler
	 */
	public MultiTaskTimer(ITimerTaskHandler timerTaskHandler) {
		mTimerTaskHandler = timerTaskHandler;
		setTimeTask(FIRST_TIMER_ID, 1);
	}
	
	/**
	 * Judge the existence of time task by taskId
	 * @param taskId 任务id
	 * @return boolean
	 */
	public boolean isTaskExist(int taskId) {
		ScheduledFuture<?> future = mFutureHashMap.get(taskId);
		if(future == null) {
			return false;
		} else if(future != null && future.isCancelled()) {
			cancelTimeTask(taskId);
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Set time task
	 * @param taskId 任务id
	 * @param cyclicTime  循环时间
	 */
	public void setTimeTask(int taskId, int cyclicTime) {
		cancelTimeTask(taskId);
		Task task = new Task(taskId);
		ScheduledFuture<?> future = mExecutor.scheduleWithFixedDelay(task, cyclicTime, cyclicTime, TimeUnit.MILLISECONDS);
		mFutureHashMap.put(task.getTaskId(), future);
		mTaskHashMap.put(task.getTaskId(), task);
	}
	
	/**	
	 * Cancel time task by taskId
	 * @param taskId 任务id
	 */
	public void cancelTimeTask(int taskId) {
		ScheduledFuture<?> future = mFutureHashMap.get(taskId);
		if(future != null) {
			if(future.cancel(true)) {
				mFutureHashMap.remove(taskId);
				future = null;
			}	
			
			if(mTaskHashMap.get(taskId) != null) {
				mExecutor.remove(mTaskHashMap.get(taskId));
				mTaskHashMap.remove(taskId);
			}
		}
	}
	
	/**
	 * Kill all timer
	 */
	public void killTimer() {
		try {
			finalize();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		if(mExecutor != null) {
			mExecutor.shutdown();
			mExecutor.purge();
			mExecutor = null;
		}		
		mTimerTaskHandler = null;
	}

	/**
	 * @author junpeng.Zheng
	 * @since  2017.05.03
	 */
	class Task implements Runnable {
		private int taskId = -1;

		public Task(int taskId) {
			this.taskId = taskId;
		}

		public int getTaskId() {
			return taskId;
		}

		@Override
		public void run() {
			//Log.d(TAG, "TimeTask["+taskId+"] was running!");
			if(mTaskHashMap.get(this.taskId) != null) {
				if(mTimerTaskHandler != null) {
					if(FIRST_TIMER_ID == this.taskId){
						cancelTimeTask(FIRST_TIMER_ID);
					} else {
						mTimerTaskHandler.onTimerTaskHandle(this.taskId);
					}
				}
			} else {
				cancelTimeTask(this.taskId);
			}
		}
	}

	/**
	 * Timer task callback
	 */
	public interface ITimerTaskHandler {
		void onTimerTaskHandle(int taskId);
	}
}
