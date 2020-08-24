package com.nurulquran.audio.gcm;

/**
 * Created by NaPro on 10/28/2015.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.nurulquran.audio.R;
import com.google.android.gms.gcm.GcmListenerService;

import com.nurulquran.audio.activity.MainActivity;
import com.nurulquran.audio.activity.PlayerActivity;
import com.nurulquran.audio.config.GlobalValue;
import com.nurulquran.audio.config.WebserviceApi;
import com.nurulquran.audio.object.Song;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Calendar;


public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = MyGcmListenerService.class.getSimpleName();

    private static int REQUEST_CODE = 0;
    private static int NOTIFICATION_ID = 0;

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);

        if (from.startsWith("/topics/")) {
            // message received from some topic.
        } else {
            // normal downstream message.
        }

        // [START_EXCLUDE]
        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
        sendNotification(message);
        // [END_EXCLUDE]
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message) {
        Song song = null;
        String content = "";
        org.json.JSONObject item = null;
        try {
            item = new org.json.JSONObject(message);
            if (item.getInt("type") == 1) {
                song = new Song();
                song.setId(item.getString(WebserviceApi.KEY_ID));
                song.setName(item.getString(WebserviceApi.KEY_NAME));
                String url = (item.getString(WebserviceApi.KEY_LINK));
                url.replaceAll(" ", "%20");
                song.setUrl(url);
                song.setDescription(item.getString(WebserviceApi.DESCRIPTION));
                song.setImage(item.getString(WebserviceApi.KEY_IMAGE));
            }
            content = item.getString("content");


        } catch (JSONException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(Args.NOTIFICATION, true);
        if (song != null) {
            intent.putExtra("Song", song);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        REQUEST_CODE++;
        PendingIntent pendingIntent = PendingIntent.getActivity(this, REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
//        RemoteViews rmView = new RemoteViews(getApplicationContext()
//                .getPackageName(), R.layout.layout_notification_push);
//        notificationBuilder.setContent(rmView);
//        // set
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeInMillis(System.currentTimeMillis());
//        rmView.setTextViewText(R.id.tvTitle,getResources().getString(R.string.app_name));
//        rmView.setTextViewText(R.id.tvContent, content);
//        rmView.setTextViewText(R.id.tvTime,calendar.get(Calendar.HOUR_OF_DAY)+" : "+calendar.get(Calendar.MINUTE));

        Notification notification = notificationBuilder.setSmallIcon(R.drawable.notify_icon)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setContentIntent(pendingIntent).build();

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NOTIFICATION_ID++;
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
}
