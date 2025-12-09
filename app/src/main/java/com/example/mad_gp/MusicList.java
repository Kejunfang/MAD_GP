package com.example.mad_gp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MusicList extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MusicAdapter adapter;
    private ProgressBar progressBar;
    private FirebaseFirestore db;

    private ArrayList<Music> musicList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list);

        db = FirebaseFirestore.getInstance();

        ImageButton btnBack = findViewById(R.id.btnBack);
        recyclerView = findViewById(R.id.recyclerViewMusic);
        progressBar = findViewById(R.id.progressBar);

        btnBack.setOnClickListener(v -> finish());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new MusicAdapter(new MusicAdapter.OnMusicClickListener() {
            @Override
            public void onPlayClick(Music music) {
                int position = musicList.indexOf(music);

                Intent intent = new Intent(MusicList.this, MusicPage.class);

                intent.putParcelableArrayListExtra("music_list", musicList);
                intent.putExtra("current_position", position);

                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);

        loadMusicFromFirebase();
    }

    private void loadMusicFromFirebase() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("music")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    musicList.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Music music = new Music();
                        music.setId(document.getId());
                        music.setTitle(document.getString("title"));
                        music.setArtist(document.getString("artist"));
                        music.setCategory(document.getString("category"));
                        music.setDuration(document.getLong("duration").intValue());
                        music.setCoverUrl(document.getString("coverUrl"));
                        music.setAudioUrl(document.getString("audioUrl"));

                        musicList.add(music);
                    }

                    adapter.setMusicList(musicList);
                    progressBar.setVisibility(View.GONE);

                    if (musicList.isEmpty()) {
                        Toast.makeText(this, "No music found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading music: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}