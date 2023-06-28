package com.hieu.mp3offline.view.act;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hieu.mp3offline.Mp3Player;
import com.hieu.mp3offline.OnSeekBarChange;
import com.hieu.mp3offline.R;
import com.hieu.mp3offline.databinding.ActivityMainBinding;
import com.hieu.mp3offline.model.Song;
import com.hieu.mp3offline.service.MediaService;
import com.hieu.mp3offline.view.adapter.SongAdapter;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int LEVEL_PLAY = 1;
    private static final int LEVEL_IDLE = 0;
    private ActivityMainBinding binding = null;
    private boolean appRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());
        checkMapPermission();
        initViews();
    }

    @SuppressLint("InlinedApi")
    public void checkMapPermission() {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
        } else {
            Mp3Player.getInstance().loadOffline();
            Mp3Player.getInstance().setCompletionCallBack(new MediaPlayer.OnCompletionListener(){
                @Override
                public void onCompletion(MediaPlayer mp) {
                    updateUI();
                }
            });
            initListSong();
        }
    }

    private void initListSong() {
        binding.include.ivPlay.setOnClickListener(this);
        binding.include.ivNext.setOnClickListener(this);
        binding.include.ivPrevious.setOnClickListener(this);
        binding.include.seekBar.setOnSeekBarChangeListener(new OnSeekBarChange() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Mp3Player.getInstance().seekTo(seekBar.getProgress());
            }
        });
        binding.rvSong.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSong.setAdapter(new SongAdapter(Mp3Player.getInstance().getListSong(), this, v -> {
            v.startAnimation(AnimationUtils.loadAnimation(this, androidx.appcompat.R.anim.abc_fade_in));
            doClickItemSong((Song) v.getTag());
        }));
        appRunning = true;
        new Thread(() -> {
            updateSeekBar();
        }).start();
        updateUI();
    }

    private void updateSeekBar() {
        while (appRunning) {
            try {
                Thread.sleep(500);
                runOnUiThread(() -> {
                    String currentTimeText = Mp3Player.getInstance().getCurrentTimeText();
                    String totalTimeText = Mp3Player.getInstance().getTotalTimeText();
                    int currentTime = Mp3Player.getInstance().getCurrentTime();
                    int totalTime = Mp3Player.getInstance().getTotalTime();

                    binding.include.seekBar.setMax(totalTime);
                    binding.include.seekBar.setProgress(currentTime);
                    binding.include.tvDuration.setText(String.format("%s/%s", currentTimeText, totalTimeText));
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        appRunning = false;
        startService(new Intent(this, MediaService.class));

        super.onDestroy();
    }

    private void doClickItemSong(Song song) {
        Mp3Player.getInstance().play(song);
        updateUI();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Please", Toast.LENGTH_SHORT).show();
        }
    }

    private void initViews() {
        stopService(new Intent(this,MediaService.class));
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_play) {
            Mp3Player.getInstance().play();
        } else if (v.getId() == R.id.iv_next) {
            Mp3Player.getInstance().next();
        } else if (v.getId() == R.id.iv_previous) {
            Mp3Player.getInstance().back();
        }
        updateUI();
    }

    private void updateUI() {
        if (Mp3Player.getInstance().getState() == Mp3Player.STATE_PLAYING) {
            binding.include.ivPlay.setImageLevel(LEVEL_PLAY);
        } else {
            binding.include.ivPlay.setImageLevel(LEVEL_IDLE);
        }
        Song song = Mp3Player.getInstance().getCurrentSong();
        binding.include.tvName.setText(song.title);
        binding.include.tvAlbum.setText(song.album);
        ((SongAdapter) binding.rvSong.getAdapter()).updateUI(Mp3Player.getInstance().getCurrentIndex());
    }
}