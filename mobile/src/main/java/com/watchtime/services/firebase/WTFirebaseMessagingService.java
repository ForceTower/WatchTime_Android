package com.watchtime.services.firebase;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.app.RemoteInput;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.watchtime.R;
import com.watchtime.activities.MainActivity;
import com.watchtime.base.utils.VersionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class WTFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = WTFirebaseMessagingService.class.getSimpleName();
    private static int id = 0;

    public WTFirebaseMessagingService() {
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.i(TAG, "From: " + remoteMessage.getFrom());

        if (remoteMessage.getData().size() > 0) {
            Log.i(TAG, "Message data payload: " + remoteMessage.getData());
        }

        String notificationTitle;
        String notificationBody;
        String icon;
        String sound;
        if (remoteMessage.getNotification() != null) {
            notificationTitle = remoteMessage.getNotification().getTitle();
            notificationBody = remoteMessage.getNotification().getBody();
            icon = remoteMessage.getNotification().getIcon();
            sound = remoteMessage.getNotification().getSound();

            if (remoteMessage.getNotification() != null) {
                Log.i(TAG, "Message Notification Body: " + notificationBody);
            }
        } else {
            notificationTitle = remoteMessage.getData().get("title");
            notificationBody = remoteMessage.getData().get("message");
            icon = remoteMessage.getData().get("icon");
            sound = "default";
        }
        showNotification(notificationTitle, notificationBody, icon, sound);
    }

    private void showNotification(String title, String message, String icon, String sound) {
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_ONE_SHOT);
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Bitmap largeImage = getBitmapFromURL(icon);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.mipmap.app_logo)
                .setLargeIcon(largeImage != null ? largeImage : largeIcon)
                .setSound(defaultSound)
                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.primary))
                .setContentIntent(pendingIntent);

        if (VersionUtils.isNougat()) {
            RemoteInput remoteInput = new RemoteInput.Builder("key_text_remote")
                    .setLabel("Reply")
                    .build();

            NotificationCompat.Action acceptRecommend = new NotificationCompat.Action.Builder(R.drawable.ic_mark_watched, "Reply", pendingIntent)
                    .addRemoteInput(remoteInput)
                    .build();

            builder.addAction(acceptRecommend);
        }

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        manager.notify(id++, builder.build());
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }

    private Bitmap getCircleBitmap(Bitmap bitmap) {
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = Color.RED;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        bitmap.recycle();

        return output;
    }
}
