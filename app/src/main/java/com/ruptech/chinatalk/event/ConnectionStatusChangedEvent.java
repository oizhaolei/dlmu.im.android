package com.ruptech.chinatalk.event;

import android.support.annotation.NonNull;

public class ConnectionStatusChangedEvent {
	@NonNull
	public final int connectedState;
	public final String reason;

	public ConnectionStatusChangedEvent(int connectedState, String reason) {
		this.connectedState = connectedState;
		this.reason = reason;
	}
}
