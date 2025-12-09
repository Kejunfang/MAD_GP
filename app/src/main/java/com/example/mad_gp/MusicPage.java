package com.example.mad_gp;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MusicPage extends AppCompatActivity {

    private static final String TAG = "MusicPage";

    private ImageButton btnBack;
    private ImageButton btnPlayPause;
    private ImageButton btnPrev;
    private ImageButton btnNext;
    private SeekBar seekBar;
    private TextView tvCurrentTime;
    private TextView tvTotalTime;
    private TextView tvTrackTitle;
    private TextView tvArtistName;
    private ImageView ivAlbumArt;
    private RecyclerView recyclerViewUpNext;
    private MusicAdapter upNextAdapter;

    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private boolean isPlaying = false;

    private ArrayList<Music> musicList;
    private int currentPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_page);

        initViews();
        getPlaylistFromIntent();
        setupListeners();
        setupUpNextList();

        if (musicList != null && !musicList.isEmpty()) {
            Log.d(TAG, "Starting playback at position " + currentPosition);
            playMusicAtPosition(currentPosition);
        } else {
            Log.e(TAG, "Music list is empty or null!");
            Toast.makeText(this, "No music to play", Toast.LENGTH_SHORT).show();
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        seekBar = findViewById(R.id.seekBar);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        tvTrackTitle = findViewById(R.id.tvTrackTitle);
        tvArtistName = findViewById(R.id.tvArtistName);
        ivAlbumArt = findViewById(R.id.ivAlbumArt);
        recyclerViewUpNext = findViewById(R.id.recyclerViewUpNext);
    }

    private void getPlaylistFromIntent() {
        Intent intent = getIntent();

        musicList = intent.getParcelableArrayListExtra("music_list");
        currentPosition = intent.getIntExtra("current_position", 0);

        Log.d(TAG, "Received music list size: " + (musicList != null ? musicList.size() : 0));
        Log.d(TAG, "Current position: " + currentPosition);

        if (musicList == null || musicList.isEmpty()) {
            Log.w(TAG, "Creating default music list");
            musicList = new ArrayList<>();

            Music music1 = new Music();
            music1.setTitle("Calm Piano");
            music1.setArtist("Peaceful Mind");
            music1.setCoverUrl("https://picsum.photos/300/300?random=1");
            music1.setAudioUrl("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3");
            music1.setDuration(180);

            Music music2 = new Music();
            music2.setTitle("Ocean Waves");
            music2.setArtist("Nature Sounds");
            music2.setCoverUrl("https://picsum.photos/300/300?random=2");
            music2.setAudioUrl("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3");
            music2.setDuration(240);

            Music music3 = new Music();
            music3.setTitle("Forest Rain");
            music3.setArtist("Relaxing Vibes");
            music3.setCoverUrl("https://picsum.photos/300/300?random=3");
            music3.setAudioUrl("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3");
            music3.setDuration(300);

            musicList.add(music1);
            musicList.add(music2);
            musicList.add(music3);

            currentPosition = 0;

            Toast.makeText(this, "Using default playlist (3 songs)", Toast.LENGTH_LONG).show();
        }
    }

    private void setupUpNextList() {
        if (recyclerViewUpNext == null) {
            Log.e(TAG, "RecyclerView is null - check XML");
            return;
        }

        recyclerViewUpNext.setLayoutManager(new LinearLayoutManager(this));

        upNextAdapter = new MusicAdapter(music -> {
            int clickedPosition = musicList.indexOf(music);
            if (clickedPosition != -1) {
                currentPosition = clickedPosition;
                playMusicAtPosition(currentPosition);
                updateUpNextList();
            }
        });

        recyclerViewUpNext.setAdapter(upNextAdapter);
        updateUpNextList();
    }

    private void updateUpNextList() {
        if (upNextAdapter == null || musicList == null) return;

        List<Music> upNextList = new ArrayList<>();

        for (int i = currentPosition + 1; i < musicList.size() && i < currentPosition + 4; i++) {
            upNextList.add(musicList.get(i));
        }

        Log.d(TAG, "Up Next list size: " + upNextList.size());
        upNextAdapter.setMusicList(upNextList);

        TextView tvUpNextTitle = findViewById(R.id.tvUpNextTitle);
        if (tvUpNextTitle != null) {
            tvUpNextTitle.setVisibility(upNextList.isEmpty() ? View.GONE : View.VISIBLE);
        }
        recyclerViewUpNext.setVisibility(upNextList.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            finish();
        });

        btnPlayPause.setOnClickListener(v -> {
            if (mediaPlayer == null) {
                Toast.makeText(this, "Media player not ready", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isPlaying) {
                pauseMusic();
            } else {
                playMusic();
            }
        });

        btnPrev.setOnClickListener(v -> {
            Log.d(TAG, "Prev clicked. Current: " + currentPosition);

            if (currentPosition > 0) {
                currentPosition--;
                Log.d(TAG, "Going to position: " + currentPosition);
                playMusicAtPosition(currentPosition);
                updateUpNextList();
            } else {
                Toast.makeText(this, "Already at first track", Toast.LENGTH_SHORT).show();
            }
        });

        btnNext.setOnClickListener(v -> {
            Log.d(TAG, "Next clicked. Current: " + currentPosition + "/" + musicList.size());

            if (musicList == null) {
                Toast.makeText(this, "Music list is null!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (currentPosition < musicList.size() - 1) {
                currentPosition++;
                Log.d(TAG, "Going to position: " + currentPosition);
                Music nextMusic = musicList.get(currentPosition);
                Log.d(TAG, "Next song: " + nextMusic.getTitle());

                playMusicAtPosition(currentPosition);
                updateUpNextList();

                Toast.makeText(this, "▶️ " + nextMusic.getTitle(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Already at last track (" + currentPosition + "/" + (musicList.size()-1) + ")", Toast.LENGTH_SHORT).show();
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress * 1000);
                    tvCurrentTime.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void playMusicAtPosition(int position) {
        if (position < 0 || position >= musicList.size()) {
            Log.e(TAG, "Invalid position: " + position);
            return;
        }

        Music music = musicList.get(position);
        Log.d(TAG, "Playing: " + music.getTitle() + " at position " + position);

        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }

        tvTrackTitle.setText(music.getTitle());
        tvArtistName.setText(music.getArtist());
        tvTotalTime.setText(formatTime(music.getDuration()));
        tvCurrentTime.setText("00:00");
        seekBar.setProgress(0);

        Glide.with(this)
                .load(music.getCoverUrl())
                .placeholder(R.drawable.lofimusic)
                .into(ivAlbumArt);

        setupMediaPlayer(music.getAudioUrl(), music.getDuration());
    }

    private void setupMediaPlayer(String audioUrl, int duration) {
        if (audioUrl == null || audioUrl.isEmpty()) {
            Toast.makeText(this, "No audio URL", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Loading audio: " + audioUrl);

        try {
            mediaPlayer = new MediaPlayer();
            // .trim() 会自动删掉链接前后的空格
            mediaPlayer.setDataSource(audioUrl.trim());
            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener(mp -> {
                Log.d(TAG, "Media player prepared");
                Toast.makeText(MusicPage.this, "▶️ Ready!", Toast.LENGTH_SHORT).show();
                seekBar.setMax(mediaPlayer.getDuration() / 1000);
                tvTotalTime.setText(formatTime(mediaPlayer.getDuration() / 1000));
                playMusic();
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                Log.d(TAG, "Song completed");
                if (currentPosition < musicList.size() - 1) {
                    currentPosition++;
                    playMusicAtPosition(currentPosition);
                    updateUpNextList();
                } else {
                    pauseMusic();
                    Toast.makeText(this, "Playlist ended", Toast.LENGTH_SHORT).show();
                }
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "MediaPlayer error: " + what + ", " + extra);
                Toast.makeText(MusicPage.this, "Error playing music", Toast.LENGTH_SHORT).show();
                return false;
            });

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "IOException: " + e.getMessage());
            Toast.makeText(this, "Failed to load: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void playMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
            isPlaying = true;
            btnPlayPause.setImageResource(R.drawable.ic_pause_circle_filled);
            updateSeekBar();
        }
    }

    private void pauseMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            isPlaying = false;
            btnPlayPause.setImageResource(R.drawable.ic_play_circle_filled);
        }
    }

    private void updateSeekBar() {
        if (mediaPlayer != null && isPlaying) {
            seekBar.setProgress(mediaPlayer.getCurrentPosition() / 1000);
            tvCurrentTime.setText(formatTime(mediaPlayer.getCurrentPosition() / 1000));
            handler.postDelayed(this::updateSeekBar, 1000);
        }
    }

    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            pauseMusic();
        }
    }
}