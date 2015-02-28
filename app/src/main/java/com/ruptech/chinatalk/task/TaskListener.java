package com.ruptech.chinatalk.task;

public interface TaskListener {
	void onCancelled(GenericTask task);

	void onPostExecute(GenericTask task, TaskResult result);

	void onPreExecute(GenericTask task);

	void onProgressUpdate(GenericTask task, Object param);
}
