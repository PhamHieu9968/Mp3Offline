package com.hieu.mp3offline;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.hieu.mp3offline.model.Song;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Mp3Player {
    public final static String TAG = Mp3Player.class.getName();
    public static final int STATE_IDLE = 1;
    public static final int STATE_PLAYING = 2;
    public static final int STATE_PAUSED = 3;
    public static Mp3Player instance;
    private int state = STATE_IDLE;
    private MediaPlayer player ;
    private final List<Song> listSong = new ArrayList<>();
    private int currentSong;
    private MediaPlayer.OnCompletionListener completionEvent;



    public int getState() {
        return state;
    }

    private Mp3Player() {
        // for singleton
        player =  new MediaPlayer();

        player.setAudioAttributes(new AudioAttributes.Builder()
                .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build());
    }

    public static Mp3Player getInstance() {
        if (instance == null) {
            instance = new Mp3Player();
        }
        return instance;
    }

    public List<Song> getListSong() {
        return listSong;
    }

    public void loadOffline() {
        Cursor c = App.getInstance().getContentResolver()
                .query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                , null, null, null
                        , MediaStore.Audio.Media.TITLE + " ASC");
//        App.getInstance().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
//                ,null,"name = 'Thanh'",null,
//                MediaStore.Audio.Media.TITLE+" ASC");
        c.moveToFirst();
        int cTitle = c.getColumnIndex(MediaStore.Audio.Media.TITLE);
        int cPath = c.getColumnIndex(MediaStore.Audio.Media.DATA);
        int cAlbum = c.getColumnIndex(MediaStore.Audio.Media.ALBUM);
        int cArtist = c.getColumnIndex(MediaStore.Audio.Media.ARTIST);
        listSong.clear();

        while (!c.isAfterLast()) {
            String title = c.getString(cTitle);
            String path = c.getString(cPath);
            String album = c.getString(cAlbum);
            String artist = c.getString(cArtist);
            Uri uri = getArtUriFromMusicFile(App.getInstance(),new File(path));
            Song song = new Song(title,path,album,artist,uri);
            listSong.add(song);
            c.moveToNext();
        }
        c.close();
        Log.i(TAG, "loadOffline: " +listSong.size());
    }

    public static Uri getArtUriFromMusicFile(Context context, File file) {
        final Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        final String[] cursor_cols = { MediaStore.Audio.Media.ALBUM_ID };

        final String where = MediaStore.Audio.Media.IS_MUSIC + "=1 AND " + MediaStore.Audio.Media.DATA + " = '"
                + file.getAbsolutePath() + "'";
        final Cursor cursor = context.getApplicationContext().getContentResolver().query(uri, cursor_cols, where, null, null);

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            long albumId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));

            Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
            Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);
            cursor.close();
            return albumArtUri;
        }
        return null;
    }

    public void play(){
        if (state == STATE_IDLE) {
            player.reset();
            try {
                player.setDataSource(listSong.get(currentSong).path);
                player.prepare();
                player.start();
                state = STATE_PLAYING;
                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        next();
                        completionEvent.onCompletion(null);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (state == STATE_PAUSED) {
            player.start();
            state = STATE_PLAYING;
        }else {
            player.pause();
            state = STATE_PAUSED;
        }
    }

    public void next() {
        currentSong++;
        if (currentSong >= listSong.size()-1) {
            currentSong=0;
        }
        state = STATE_IDLE;
        play();
    }
    public void back() {
        currentSong--;
        if (currentSong <= 0) {
            currentSong=listSong.size()-1;
        }
        state = STATE_IDLE;
        play();
    }

    public int getCurrentIndex() {
        return currentSong;
    }
    public Song getCurrentSong() {
        return listSong.get(currentSong);
    }
    public void play(Song song) {
        currentSong = listSong.indexOf(song);
        state = STATE_IDLE;
        play();
    }

    public String getCurrentTimeText() {
        try {
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat df = new SimpleDateFormat("mm:ss");
            return df.format(new Date(player.getCurrentPosition()));
        }catch (Exception e){
            e.printStackTrace();
        }
        return "--";
    }
    public String getTotalTimeText() {
        try {
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat df = new SimpleDateFormat("mm:ss");
            return df.format(new Date(player.getDuration()));
        }catch (Exception e){
            e.printStackTrace();
        }
        return "--";
    }
    public int getTotalTime(){
        return player.getDuration();
    }
    public int getCurrentTime(){
        return player.getCurrentPosition();
    }

    public void seekTo(int progress) {
        try {
            player.seekTo(progress);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public void setCompletionCallBack(MediaPlayer.OnCompletionListener event) {
        completionEvent = event;
    }
}
