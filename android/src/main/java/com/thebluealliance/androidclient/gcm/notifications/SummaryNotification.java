package com.thebluealliance.androidclient.gcm.notifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.gson.JsonParseException;
import com.thebluealliance.androidclient.R;
import com.thebluealliance.androidclient.activities.RecentNotificationsActivity;
import com.thebluealliance.androidclient.database.Database;
import com.thebluealliance.androidclient.database.tables.NotificationsTable;
import com.thebluealliance.androidclient.gcm.GCMMessageHandler;
import com.thebluealliance.androidclient.listeners.NotificationDismissedListener;
import com.thebluealliance.androidclient.models.StoredNotification;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by phil on 2/5/15.
 */
public class SummaryNotification extends BaseNotification {
    /**
     * Limit the summary's list to avoid taking up the whole notification shade and to work around
     * <a hreaf="https://code.google.com/p/android/issues/detail?id=168890">an Android 5.1 bug</a>.
     */
    static final int MAX = 7;

    public SummaryNotification() {
        super(NotificationTypes.SUMMARY, "");
    }

    @Override
    public Notification buildNotification(Context context) {
        NotificationsTable table = Database.getInstance(context).getNotificationsTable();

        List<StoredNotification> active = table.getActive();
        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
        int size = active.size();
        int count = 0;

        for (StoredNotification n : active) {
            if (++count == MAX && size > MAX) {
                style.addLine(context.getString(R.string.notification_summary_more, size + 1 - MAX));
                break;
            }
            style.addLine(n.getTitle());
        }

        String notificationTitle = context.getString(R.string.notification_summary, size);
        style.setBigContentTitle(notificationTitle);
        style.setSummaryText(context.getString(R.string.app_name));

        Intent instance = getIntent(context);
        PendingIntent intent = makeNotificationIntent(context, instance);
        PendingIntent onDismiss = PendingIntent.getBroadcast(context, 0, new Intent(context, NotificationDismissedListener.class), 0);

        return new NotificationCompat.Builder(context)
                .setContentTitle(notificationTitle)
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(getLargeIconFormattedForPlatform(context, R.drawable.ic_info_outline_white_24dp))
                .setContentIntent(intent)
                .setDeleteIntent(onDismiss)
                .setAutoCancel(true)
                .setGroup(GCMMessageHandler.GROUP_KEY)
                .setGroupSummary(true)
                .setStyle(style).build();
    }

    @Override
    public void parseMessageData() throws JsonParseException {
        /* Nothing to do */
    }

    @Override
    public Intent getIntent(Context c) {
        return RecentNotificationsActivity.newInstance(c);
    }

    @Override
    public void updateDataLocally(Context c) {
        /* Nothing to store */
    }

    @Override
    public int getNotificationId() {
        /* All have the same ID so future notifications replace it */
        return 1337;
    }

    /* Checks if we've already posted a notification */
    public static boolean isNotificationActive(Context context) {
        NotificationsTable table = Database.getInstance(context).getNotificationsTable();
        return table.getActive().size() > 1;
        // The newest notification has already been added to the table, so we're checking if there are 2+ active
    }
}
