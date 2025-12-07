package com.example.mad_gp;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewHolder> {

    private static final String TAG = "MusicAdapter";

    private List<Music> musicList;
    private OnMusicClickListener listener;

    public interface OnMusicClickListener {
        void onPlayClick(Music music);
    }

    public MusicAdapter(OnMusicClickListener listener) {
        this.musicList = new ArrayList<>();
        this.listener = listener;
    }

    public void setMusicList(List<Music> musicList) {
        this.musicList = musicList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_item_music, parent, false);
        return new MusicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {
        Music music = musicList.get(position);
        holder.bind(music, listener);
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }

    static class MusicViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvTitle, tvArtist, tvDuration;
        ImageButton btnPlay;

        public MusicViewHolder(@NonNull View itemView) {
            super(itemView);

            // 查找控件
            ivCover = itemView.findViewById(R.id.ivMusicCover);
            tvTitle = itemView.findViewById(R.id.tvMusicTitle);
            tvArtist = itemView.findViewById(R.id.tvMusicArtist);
            tvDuration = itemView.findViewById(R.id.tvMusicDuration);
            btnPlay = itemView.findViewById(R.id.btnPlayMusic);

            // 👇 添加调试日志，检查哪个控件是 null
            Log.d(TAG, "ViewHolder created:");
            Log.d(TAG, "ivCover: " + (ivCover != null ? "OK" : "NULL"));
            Log.d(TAG, "tvTitle: " + (tvTitle != null ? "OK" : "NULL"));
            Log.d(TAG, "tvArtist: " + (tvArtist != null ? "OK" : "NULL"));
            Log.d(TAG, "tvDuration: " + (tvDuration != null ? "OK" : "NULL"));
            Log.d(TAG, "btnPlay: " + (btnPlay != null ? "OK" : "NULL"));
        }

        public void bind(Music music, OnMusicClickListener listener) {
            // 👇 添加空值检查，防止崩溃
            if (tvTitle != null) {
                tvTitle.setText(music.getTitle());
            } else {
                Log.e(TAG, "tvTitle is NULL!");
            }

            if (tvArtist != null) {
                tvArtist.setText(music.getArtist());
            } else {
                Log.e(TAG, "tvArtist is NULL!");
            }

            if (tvDuration != null) {
                tvDuration.setText(music.getFormattedDuration());
            } else {
                Log.e(TAG, "tvDuration is NULL!");
            }

            // 加载封面图片
            if (ivCover != null) {
                Glide.with(itemView.getContext())
                        .load(music.getCoverUrl())
                        .placeholder(android.R.color.darker_gray)
                        .into(ivCover);
            } else {
                Log.e(TAG, "ivCover is NULL!");
            }

            // 播放按钮点击
            if (btnPlay != null) {
                btnPlay.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onPlayClick(music);
                    }
                });
            } else {
                Log.e(TAG, "btnPlay is NULL!");
            }
        }
    }
}