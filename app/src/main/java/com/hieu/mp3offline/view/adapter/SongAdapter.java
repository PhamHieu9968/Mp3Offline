package com.hieu.mp3offline.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hieu.mp3offline.R;
import com.hieu.mp3offline.model.Song;

import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongHolder> {
    private final List<Song> listSong;
    private final Context context;
    private final View.OnClickListener event;
    private int currentSong;

    public SongAdapter(List<Song> listSong, Context context, View.OnClickListener event) {
        this.listSong = listSong;
        this.context = context;
        this.event = event;
    }

    @NonNull
    @Override
    public SongHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_song, parent, false);
        return new SongHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SongHolder holder, int position) {
        Song song = listSong.get(position);
        if (position == currentSong) {
            holder.view.setBackgroundResource(R.color.gray_light );
        } else {
            holder.view.setBackgroundResource(R.color.white);
        }
        if (song.cover != null) {
            holder.ivCover.setImageURI(song.cover);
        } else {
            holder.ivCover.setImageResource(R.drawable.ic_song);
        }
        holder.tvSong.setText(song.title);
        holder.tvSong.setTag(song);
    }

    @Override
    public int getItemCount() {
        return listSong.size();
    }

    public void updateUI(int currentSong) {
        this.currentSong = currentSong;
        notifyItemRangeChanged(0, listSong.size());
    }

    public class SongHolder extends RecyclerView.ViewHolder {
        TextView tvSong;
        View view;
        ImageView ivCover;

        public SongHolder(@NonNull View v) {
            super(v);
            ivCover = v.findViewById(R.id.iv_song);
            view = v.findViewById(R.id.view);
            tvSong = v.findViewById(R.id.tv_song);
            tvSong.setOnClickListener(event);
        }
    }

}
