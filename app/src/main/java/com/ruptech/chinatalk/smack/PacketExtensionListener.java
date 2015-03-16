package com.ruptech.chinatalk.smack;


import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;

import java.util.Collection;

public abstract class PacketExtensionListener<T> implements PacketListener {
	private Class<T> extClass;

	protected PacketExtensionListener(Class<T> extClass) {
		this.extClass = extClass;
	}

	@Override
	final public void processPacket(Packet packet) {
		Collection<PacketExtension> extensions = packet.getExtensions();
		for (PacketExtension ext : extensions) {
			if (ext.getClass().equals(extClass)) {
				Message msg = (Message) packet;

				processExtension(msg, (T) ext);
			}
		}
	}

	abstract void processExtension(Message msg , T ext);
}
