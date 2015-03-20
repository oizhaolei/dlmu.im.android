package com.ruptech.chinatalk.event;

public class FriendEvent {
	public final String fullname;
	public final long friendId;

	public FriendEvent(String fullname, long friendId) {
		this.fullname = fullname;
		this.friendId = friendId;
	}
}
