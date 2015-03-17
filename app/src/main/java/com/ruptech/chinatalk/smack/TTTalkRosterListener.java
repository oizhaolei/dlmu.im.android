package com.ruptech.chinatalk.smack;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.util.Log;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.sqlite.RosterProvider;
import com.ruptech.chinatalk.event.RosterChangeEvent;
import com.ruptech.chinatalk.sqlite.TableContent.RosterTable;
import com.ruptech.chinatalk.utils.StatusMode;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.utils.XMPPUtils;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;

import java.util.Collection;

/**
 * TTTalk translated
 */
public class TTTalkRosterListener implements RosterListener {
	protected final String TAG = Utils.CATEGORY + TTTalkRosterListener.class.getSimpleName();
	private final Roster mRoster;
	private boolean isFirstTime;
	private final ContentResolver mContentResolver;

	public TTTalkRosterListener(Roster roster, ContentResolver contentResolver) {
		this.mRoster = roster;
		this.mContentResolver = contentResolver;
	}

	@Override
	public void presenceChanged(Presence presence) {
		Log.i(TAG, "presenceChanged(" + presence.getFrom() + "): " + presence);
		String jabberID = XMPPUtils.getJabberID(presence.getFrom());
		RosterEntry rosterEntry = mRoster.getEntry(jabberID);
		updateRosterEntryInDB(rosterEntry);
		App.mBus.post(new RosterChangeEvent());
	}

	@Override
	public void entriesUpdated(Collection<String> entries) {

		Log.i(TAG, "entriesUpdated(" + entries + ")");
		for (String entry : entries) {
			RosterEntry rosterEntry = mRoster.getEntry(entry);
			updateRosterEntryInDB(rosterEntry);
		}
		App.mBus.post(new RosterChangeEvent());
	}

	@Override
	public void entriesDeleted(Collection<String> entries) {
		Log.i(TAG, "entriesDeleted(" + entries + ")");
		for (String entry : entries) {
			deleteRosterEntryFromDB(entry);
		}
		App.mBus.post(new RosterChangeEvent());
	}

	@Override
	public void entriesAdded(Collection<String> entries) {
		Log.i(TAG, "entriesAdded(" + entries + ")");
		ContentValues[] cvs = new ContentValues[entries.size()];
		int i = 0;
		for (String entry : entries) {
			RosterEntry rosterEntry = mRoster.getEntry(entry);
			cvs[i++] = getContentValuesForRosterEntry(rosterEntry);
		}
		mContentResolver.bulkInsert(RosterProvider.CONTENT_URI, cvs);
		if (isFirstTime) {
			isFirstTime = false;
			App.mBus.post(new RosterChangeEvent());
		}
	}

	private void updateRosterEntryInDB(final RosterEntry entry) {
		final ContentValues values = getContentValuesForRosterEntry(entry);

		if (mContentResolver.update(RosterProvider.CONTENT_URI, values,
				RosterTable.Columns.JID + " = ?", new String[]{entry.getUser()}) == 0)
			addRosterEntryToDB(entry);
	}


	private ContentValues getContentValuesForRosterEntry(final RosterEntry entry) {
		final ContentValues values = new ContentValues();

		values.put(RosterTable.Columns.JID, entry.getUser());
		values.put(RosterTable.Columns.ALIAS, getName(entry));

		Presence presence = mRoster.getPresence(entry.getUser());
		values.put(RosterTable.Columns.STATUS_MODE, getStatusInt(presence));
		values.put(RosterTable.Columns.STATUS_MESSAGE, presence.getStatus());
		values.put(RosterTable.Columns.GROUP, getGroup());

		return values;
	}


	private String getGroup() {
		return "";
	}

	private String getName(RosterEntry rosterEntry) {
		String name = rosterEntry.getName();
		if (name != null && name.length() > 0) {
			return name;
		}
		name = StringUtils.parseName(rosterEntry.getUser());
		if (name.length() > 0) {
			return name;
		}
		return rosterEntry.getUser();
	}

	private void addRosterEntryToDB(final RosterEntry entry) {
		ContentValues values = getContentValuesForRosterEntry(entry);
		Uri uri = mContentResolver.insert(RosterProvider.CONTENT_URI, values);
		Log.i(TAG, "addRosterEntryToDB: Inserted " + uri);
	}

	private void deleteRosterEntryFromDB(final String jabberID) {
		int count = mContentResolver.delete(RosterProvider.CONTENT_URI,
				RosterTable.Columns.JID + " = ?", new String[]{jabberID});
		Log.i(TAG, "deleteRosterEntryFromDB: Deleted " + count + " entries");
	}

	private int getStatusInt(final Presence presence) {
		return getStatus(presence).ordinal();
	}

	private StatusMode getStatus(Presence presence) {
		if (presence.getType() == Presence.Type.available) {
			if (presence.getMode() != null) {
				return StatusMode.valueOf(presence.getMode().name());
			}
			return StatusMode.available;
		}
		return StatusMode.offline;
	}

}