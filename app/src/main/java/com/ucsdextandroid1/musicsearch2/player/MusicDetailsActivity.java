package com.ucsdextandroid1.musicsearch2.player;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;

import com.ucsdextandroid1.musicsearch2.R;
import com.ucsdextandroid1.musicsearch2.data.SongItem;

public class MusicDetailsActivity extends AppCompatActivity {

    private MusicControlsManager musicControls;
    private Toolbar toolbar;

    public static Intent createIntent(Context context) {
        //TODO add the song the intent
        return new Intent(context, MusicDetailsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_details);

        toolbar = findViewById(R.id.amd_toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        musicControls = MusicControlsManager.newUIBuilder()
                .setLoadingView(findViewById(R.id.amd_loading_view))
                .setImageView(findViewById(R.id.amd_image))
                .setTrackTitleView(findViewById(R.id.amd_title))
                .setArtistNameView(findViewById(R.id.amd_subtitle))
                .setPlayPauseView(
                        findViewById(R.id.amd_playpause_view),
                        R.drawable.ic_play_circle_filled,
                        R.drawable.ic_pause_circle_filled
                ).build();

        musicControls.setControlsClickListener(new MusicControlsManager.OnControlClickListener() {
            @Override
            public void onResumeClicked() {
                MusicPlayerService.resume(MusicDetailsActivity.this);
            }

            @Override
            public void onPauseClicked() {
                MusicPlayerService.pause(MusicDetailsActivity.this);
            }
        });

        MusicController.getInstance().getMetadataAndStateLiveData().observe(this, new Observer<MusicMetadataAndState>() {
            @Override
            public void onChanged(MusicMetadataAndState metadataAndState) {
                musicControls.updateViewState(metadataAndState.getState());
                musicControls.updateViewMetadata(metadataAndState.getMetadata());

                toolbar.setTitle(metadataAndState.getMetadata() != null ? metadataAndState.getMetadata().getAlbumName() : "");
            }
        });

        registerBroadcastReceiver();
    }

    private void registerBroadcastReceiver() {
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.registerReceiver(controlsBroadcastReceiver,
                new IntentFilter(MusicPlayerService.ACTION_STATE_CHANGED));
    }

    private void unregisterBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(controlsBroadcastReceiver);
    }

    private BroadcastReceiver controlsBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction() != null && MusicPlayerService.ACTION_STATE_CHANGED.equals(intent.getAction())) {
                int state = intent.getIntExtra(MusicPlayerService.EXTRA_PLAYBACK_STATE, MusicPlayer.STATE_STOPPED);
                SongItem item = intent.getParcelableExtra(MusicPlayerService.EXTRA_SONG);

                if(musicControls != null) {
                    musicControls.updateViewState(state);
                    musicControls.updateViewMetadata(item);
                }
            }
        }

    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterBroadcastReceiver();
    }
}
