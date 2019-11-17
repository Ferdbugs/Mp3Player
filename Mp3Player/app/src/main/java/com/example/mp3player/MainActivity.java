package com.example.mp3player;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    //Declare Variables
    MusicService musicService;
    boolean isBound = false;
    private SeekBar seekBar;
    private Runnable runnable;
    private Handler handler = new Handler();
    private ImageView Play;
    private ImageView Pause;

    //Uses customAdapter for formatting the list
    private class CustomAdapter extends ArrayAdapter<File> {
        File[] data;

        public CustomAdapter(@NonNull Context context, int resource, @NonNull File[] objects) {
            super(context, resource, objects);
            data = objects;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            TextView view;
            if (convertView == null) {
                view = (TextView) getLayoutInflater().inflate(android.R.layout.simple_list_item_1, null);
                String text = data[position].getName();
                view.setText(text.substring(0, text.length() - 4));
                return view;
            } else return convertView;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ListView lv = findViewById(R.id.listView);
        seekBar = findViewById(R.id.seekbar);
        Play = findViewById(R.id.Play);
        Pause = findViewById(R.id.pause);
        //OnClick Listener for Play Button
        Play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(musicService.mp3.mediaPlayer!=null){
                    musicService.mp3.play();                //Uses musicService to call mp3 to play selected song
                    Play.setVisibility(View.GONE);          //Makes Pause button replace Play button
                    Pause.setVisibility(View.VISIBLE);
                    seek();
                }
            }
        });
        //OnClick listener for Pause Button
        Pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(musicService.mp3.mediaPlayer!=null){
                    musicService.mp3.pause();
                    Pause.setVisibility(View.GONE);
                    Play.setVisibility(View.VISIBLE);
                    seek();
                }
            }
        });

        //Fetches List from the Music Directory
        File musicDir = new File(
                Environment.getExternalStorageDirectory().getPath() + "/Music/");
        File list[] = musicDir.listFiles();
        if (list != null) {
            lv.setAdapter(new CustomAdapter(this, android.R.layout.simple_list_item_1, list));
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> myAdapter, View myView, int myItemInt, long mylng) {
                    File selectedFromList = (File) (lv.getItemAtPosition(myItemInt));
                    musicService.mp3.stop();
                    musicService.mp3.load(selectedFromList.getAbsolutePath());
                    musicService.mp3.play();
                    Play.setVisibility(View.GONE);
                    Pause.setVisibility(View.VISIBLE);
                    seek();
                }
            });
        }
        //Function for seeking through song on User Click
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    if(musicService.mp3.mediaPlayer!=null) {
                        musicService.mp3.setProgress(progress);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //Starts the MusicService
        Intent intent = new Intent(MainActivity.this, MusicService.class);
        bindService(intent, myConnection, 0);
        startService(intent);
    }

    //Recursive function to update Seekbar while song plays
    private void seek(){
        seekBar.setMax(musicService.mp3.getDuration());
        if(musicService!=null) {
            if (musicService.mp3.getState() == MP3Player.MP3PlayerState.PLAYING) {
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        seekBar.setProgress(musicService.mp3.getProgress());
                        seek();
                    }
                };
                handler.postDelayed(runnable, 1000);
            }
        }
    }

    //Initiates and defines the service connection from Main Activity to MusicService
    private ServiceConnection myConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MyLocalBinder binder = (MusicService.MyLocalBinder) service;
            musicService = binder.getService();
            isBound = true;
            if(musicService!=null && musicService.mp3.getState() == MP3Player.MP3PlayerState.PLAYING){
                Play.setVisibility(View.GONE);
                Pause.setVisibility(View.VISIBLE);
                seek();
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

}
