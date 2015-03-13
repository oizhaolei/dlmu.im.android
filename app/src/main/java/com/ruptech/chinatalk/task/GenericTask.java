package com.ruptech.chinatalk.task;

import android.os.AsyncTask;
import android.util.Log;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.BuildConfig;
import com.ruptech.chinatalk.http.NetworkException;
import com.ruptech.chinatalk.http.ServerSideException;
import com.ruptech.chinatalk.utils.Utils;

import java.util.Observable;
import java.util.Observer;

public abstract class GenericTask extends AsyncTask<Object, Object, TaskResult> implements Observer {
	protected final String TAG = "TaskManager";
	private String msg;

	private boolean isCancelable = true;

	private TaskListener mListener = null;

	abstract protected TaskResult _doInBackground() throws Exception;

	private void addTaskToTaskManager() {
		if (App.taskManager != null)
			App.taskManager.addTask(this);
	}

	private void deleteTaskFromTaskManager() {
		if (App.taskManager != null)
			App.taskManager.deleteObserver(this);
	}

	@Override
	protected TaskResult doInBackground(Object... params) {
		TaskResult result;
		try {
			result = _doInBackground();
		} catch (Exception e) {
			handleException(e);
			return TaskResult.FAILED;
		}
		return result;
	}

	public void doPublishProgress(Object... values) {
		super.publishProgress(values);
	}

	public TaskListener getListener() {
		return mListener;
	}

	public String getMsg() {
		return msg;
	}

	public Object[] getMsgs() {
		return new Object[0];
	}

	protected void handleException(Throwable e) {
		if (BuildConfig.DEBUG)
			Log.e(TAG, e.getMessage(), e);
		if (e instanceof ServerSideException) {
			msg = e.getMessage();
			publishProgress(msg);
		} else if (e instanceof NetworkException) {
			msg = e.getMessage();
			publishProgress(msg);
		}
		if (!(e instanceof NetworkException)) {
			Utils.sendClientException(e, getMsgs());
		}
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
		deleteTaskFromTaskManager();
		if (mListener != null) {
			mListener.onCancelled(this);
		}
	}

	@Override
	protected void onPostExecute(TaskResult result) {
		super.onPostExecute(result);
		deleteTaskFromTaskManager();
		if (mListener != null) {
			mListener.onPostExecute(this, result);
		}

	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		addTaskToTaskManager();

		if (mListener != null) {
			mListener.onPreExecute(this);
		}

	}

	@Override
	protected void onProgressUpdate(Object... values) {
		super.onProgressUpdate(values);

		if (mListener != null) {
			if (values != null && values.length > 0) {
				mListener.onProgressUpdate(this, values[0]);
			}
		}

	}

	public void setCancelable(boolean flag) {
		isCancelable = flag;
	}

	public void setListener(TaskListener taskListener) {
		mListener = taskListener;
	}

	@Override
	public void update(Observable o, Object arg) {
		if (TaskManager.CANCEL_ALL == (Integer) arg && isCancelable) {
			if (getStatus() == GenericTask.Status.RUNNING) {
				cancel(true);
			}
		}
	}
}
