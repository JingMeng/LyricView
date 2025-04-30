package com.lauzy.freedom.lyricview;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.lauzy.freedom.library.Lrc;
import com.lauzy.freedom.library.LrcHelper;
import com.lauzy.freedom.library.LrcView;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private MediaPlayer mMediaPlayer = new MediaPlayer();
    private LrcView mLrcView;
    private Handler mHandler = new Handler();
    private SeekBar mSeekBar;
    private TextView mTvStart;
    private TextView mTvEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        play();
    }

    private void play() {


        if (false) {
            // 1. 保证播放器回到 Idle
            if (mMediaPlayer == null) {
                mMediaPlayer = new MediaPlayer();
            } else {
                mMediaPlayer.reset();
            }

            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Log.e("PlayerError", "what=" + what + " extra=" + extra);
                    return true; // 我们已处理，避免系统再走 onCompletion()
                }
            });
        }

        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.i("MainActivity123", "----------是否准备好了----" + Math.random());
                mMediaPlayer.start();
                mSeekBar.setMax(mMediaPlayer.getDuration());
                mTvEnd.setText(LrcHelper.formatTime(mMediaPlayer.getDuration()));
                mHandler.post(mRunnable);
            }
        });

        try {
            AssetFileDescriptor afd = getAssets().openFd("Rolling In The Deep.mp3");

            /**
             * 主要是现在的这个api影响的是否播放
             */
            if (true) {
                mMediaPlayer.setDataSource(
                        afd.getFileDescriptor(),
                        afd.getStartOffset(),
                        afd.getLength()
                );
            } else {
                mMediaPlayer.setDataSource(
                        afd.getFileDescriptor()
                );
            }
            afd.close();

            mMediaPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            int currentPosition = mMediaPlayer.getCurrentPosition();
            mLrcView.updateTime(currentPosition);
            mSeekBar.setProgress(currentPosition);
            mTvStart.setText(LrcHelper.formatTime(currentPosition));
            mHandler.postDelayed(this, 100);
        }
    };

    private void init() {
        List<Lrc> lrcs = LrcHelper.parseLrcFromAssets(this, "Rolling in the Deep-Adele.lrc");
        mLrcView = findViewById(R.id.lrc_view);
        mSeekBar = findViewById(R.id.seek_play);
        mTvStart = findViewById(R.id.tv_start);
        mTvEnd = findViewById(R.id.tv_end);
        findViewById(R.id.btn_play).setOnClickListener(this);
        findViewById(R.id.btn_pause).setOnClickListener(this);
        mLrcView.setLrcData(lrcs);
        mLrcView.setOnPlayIndicatorLineListener(new LrcView.OnPlayIndicatorLineListener() {
            @Override
            public void onPlay(long time, String content) {
                mMediaPlayer.seekTo((int) time);
            }
        });
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mHandler.removeCallbacks(mRunnable);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mHandler.post(mRunnable);
                mMediaPlayer.seekTo(seekBar.getProgress());
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_play:
                Log.i("MainActivity123", "----------是否正在播放----" + mMediaPlayer.isPlaying());
                if (!mMediaPlayer.isPlaying()) {
                    mMediaPlayer.start();
                    mLrcView.resume();
                }
                break;
            case R.id.btn_pause:
                mMediaPlayer.pause();
                mLrcView.pause();
                break;
        }
    }
}
