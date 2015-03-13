package com.ruptech.chinatalk;

import com.ruptech.chinatalk.task.impl.RetrieveFriendsTask;
import com.ruptech.chinatalk.utils.Utils;
@Deprecated
public class FriendsRequestReceiver {
	static final String TAG = Utils.CATEGORY
			+ FriendsRequestReceiver.class.getSimpleName();

	public static void doRetrieveNewFriend(final long userId,
			final boolean isNeedNotify) {

		RetrieveFriendsTask mRetrieveFriendsTask = new RetrieveFriendsTask(
				userId, isNeedNotify);
		mRetrieveFriendsTask.execute();
	}

}