package com.hieu.mp3offline.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.hieu.mp3offline.Mp3Player;
import com.hieu.mp3offline.R;
import com.hieu.mp3offline.model.Song;

public class MediaService extends Service {
    private static final String TAG = MediaService.class.getName();
    private static final String CHANNEL_ID = "m4u_channel";
    private static final String KEY_EVENT = "KEY_EVENT";
    private static final String PLAY_EVENT = "PLAY_EVENT";
    private static final String NEXT_EVENT = "NEXT_EVENT";
    private static final String BACK_EVENT = "BACK_EVENT";
    private static final String CLOSE_EVENT = "CLOSE_EVENT";
    private Song song;
    private RemoteViews views;
    private boolean appRunning;
    private Notification notify;
    private final Handler handler = new Handler(new Handler.Callback() {
        @SuppressLint("NotificationPermission")
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            song = Mp3Player.getInstance().getCurrentSong();
            views.setTextViewText(R.id.tv_name, song.title);
            views.setTextViewText(R.id.tv_album, song.album);

            String currentTimeText = Mp3Player.getInstance().getCurrentTimeText();
            String totalTimeText = Mp3Player.getInstance().getTotalTimeText();
            int currentTime = Mp3Player.getInstance().getCurrentTime();
            int totalTime = Mp3Player.getInstance().getTotalTime();

            views.setProgressBar(R.id.seek_bar, totalTime, currentTime, false);
            views.setTextViewText(R.id.tv_duration, String.format("%s/%s", currentTimeText, totalTimeText));
            if (Mp3Player.getInstance().getState() == Mp3Player.STATE_PLAYING) {
                views.setImageViewResource(R.id.iv_play,R.drawable.ic_pause);
            } else {
                views.setImageViewResource(R.id.iv_play,R.drawable.ic_play);
            }

            startForeground(10001,notify);
//            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//            manager.notify(10001,notify);
            return false;
        }
    });

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate: ...");
        song = Mp3Player.getInstance().getCurrentSong();
        createNotificationChannel();

        views = new RemoteViews(getPackageName(), R.layout.item_notify_media);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        views.setTextViewText(R.id.tv_name, song.title);
        views.setTextViewText(R.id.tv_album, song.album);

        Intent intentPlay = new Intent(this, MediaService.class);
        intentPlay.putExtra(KEY_EVENT,PLAY_EVENT);
        @SuppressLint("UnspecifiedImmutableFlag")
        PendingIntent piPlay = PendingIntent.getService(this,105,intentPlay,PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.iv_play,piPlay);

        Intent intentBack = new Intent(this, MediaService.class);
        intentBack.putExtra(KEY_EVENT,BACK_EVENT);
        @SuppressLint("UnspecifiedImmutableFlag")
        PendingIntent piBack = PendingIntent.getService(this,106,intentBack,PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.iv_previous,piBack);

        Intent intentNext = new Intent(this, MediaService.class);
        intentNext.putExtra(KEY_EVENT,NEXT_EVENT);
        @SuppressLint("UnspecifiedImmutableFlag")
        PendingIntent piNext = PendingIntent.getService(this,107,intentNext,PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.iv_next,piNext);

        appRunning = true;
        new Thread(() -> {
            updateSeekBar();
        }).start();

        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setAutoCancel(false);
        builder.setCustomBigContentView(views);
        builder.setOngoing(true);
        builder.setOnlyAlertOnce(true);
        builder.setChannelId(CHANNEL_ID);
        builder.setTicker("m4u");

        notify = builder.build();

        startForeground(10001, notify);
    }

    private void updateSeekBar() {
        while (appRunning) {
            try {
                Thread.sleep(500);
                Message.obtain(handler).sendToTarget();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void createNotificationChannel() {
        String description = "Enjoy music :)";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel;
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            channel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String key = intent.getStringExtra(KEY_EVENT);
            if (key != null&&key.equals(PLAY_EVENT)) {
                Mp3Player.getInstance().play();
                Message.obtain(handler).sendToTarget();
            }else if (key != null&&key.equals(NEXT_EVENT)) {
                Mp3Player.getInstance().next();
                Message.obtain(handler).sendToTarget();
            }else if (key != null&&key.equals(BACK_EVENT)) {
                Mp3Player.getInstance().back();
                Message.obtain(handler).sendToTarget();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        appRunning = false;
        stopForeground(true);
        super.onDestroy();
    }
}
