package com.example.musicplayer;

import android.Manifest;
import android.content.ContentResolver;
//import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    int REQUEST_STORAGE_PERMISSION = 1;
    ImageView nextIv,playIv,lastIv;
    TextView singerTv,songTv;
    RecyclerView musicRv;
    List<LocalMusicBean> mDatas;//数据源
    private LocalMusicAdapter adapter;
    //记录是否初始化
    int init = 0;
    //记录当前播放音乐的位置
    int currentPlayPosition = 0;
    //记录暂停音乐时进度条的位置
    int currentPausePositionInSong = 0;
    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        mediaPlayer = new MediaPlayer();
        mDatas = new ArrayList<>();
        //创建适配器对象
        adapter = new LocalMusicAdapter(this, mDatas);
        musicRv.setAdapter(adapter);
        //设置布局管理器
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        musicRv.setLayoutManager(layoutManager);
        //存储权限授予检查
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
        }
        //加载本地数据源
        loadLocalMusicData();
        playMusicInMusicPosition(currentPlayPosition);
        //设置每一项的点击事件
        setEventListener();
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        if(requestCode == REQUEST_STORAGE_PERMISSION){
//            if(grantResults.length == 0||grantResults[0] != PackageManager.PERMISSION_GRANTED){
//                AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                builder.setTitle("权限申请");
//                builder.setMessage("我们需要存储权限来访问您的音乐以播放，请授予该权限以继续使用。");
//                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
//                    }
//                });
//                builder.setPositiveButton("取消", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//
//                    }
//                });
//                AlertDialog dialog = builder.create();
//                dialog.show();
//            }
//        }
//    }

    private void setEventListener() {
        //设置列表中每一项的点击事件
        adapter.setOnItemClickListener(new LocalMusicAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View view, int position) {
                currentPlayPosition = position;
                playMusicInMusicPosition(position);
            }
        });
        //一首播放完后自动播放下一首
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if (currentPlayPosition != 0) {
                    nextPlay();
                }
            }
        });
    }

    private void playMusicInMusicPosition(int position) {
        //从音乐位置开始播放
        LocalMusicBean musicBean = mDatas.get(position);
        //设置底部显示的歌手名称和歌曲名
        singerTv.setText(musicBean.getSinger());
        songTv.setText(musicBean.getSong());
        stopMusic();
        //重置多媒体播放器
        mediaPlayer.reset();
        //设置新的播放路径
        try {
            mediaPlayer.setDataSource(musicBean.getPath());
            if(init != 0)
                playMusic();
            init = 1;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void playMusic() {
        //播放音乐的函数
        if (mediaPlayer!=null&&!mediaPlayer.isPlaying()) {
            if (currentPausePositionInSong == 0) {
                try {
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                mediaPlayer.seekTo(currentPausePositionInSong);
                mediaPlayer.start();
            }

            playIv.setImageResource(R.mipmap.stop);
        }
    }

    private void stopMusic() {
        //停止音乐的函数
        if (mediaPlayer!=null) {
            currentPausePositionInSong = 0;
            mediaPlayer.pause();
            mediaPlayer.seekTo(0);
            mediaPlayer.stop();
            playIv.setImageResource(R.mipmap.play);
        }
    }

    private void pauseMusic() {
        //暂停音乐的函数
        if (mediaPlayer!=null&&mediaPlayer.isPlaying()) {
            currentPausePositionInSong = mediaPlayer.getCurrentPosition();
            mediaPlayer.pause();
            playIv.setImageResource(R.mipmap.play);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMusic();
    }

    private void loadLocalMusicData() {
        //加载本地存储当中的音乐mp3文件到集合中
        //1、获取ContentResolver对象
        ContentResolver resolver = getContentResolver();
        //2、获取本地音乐存储的Uri地址
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        //3、开始查询地址
        Cursor cursor = resolver.query(uri, null, null, null, null);
        //4、遍历cursor
        int id = 0;
        while (cursor.moveToNext()) {
            String song = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            String singer = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
            id++;
            String sid = String.valueOf(id);
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
            SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
            String time = sdf.format(new Date(duration));
            if(time.equals("00:00"))
            {
                id--;
                continue;
            }
            //将数据封装到对象当中
            LocalMusicBean bean = new LocalMusicBean(sid,song,singer,album,time,path);
            mDatas.add(bean);
        }
        cursor.close();
        //数据源变化，提示更新
        adapter.notifyDataSetChanged();
    }

    private void initView() {
        // 初始化控件的函数
        nextIv = findViewById(R.id.local_music_bottom_iv_next);
        playIv = findViewById(R.id.local_music_bottom_iv_play);
        lastIv = findViewById(R.id.local_music_bottom_iv_last);
        singerTv = findViewById(R.id.local_music_bottom_iv_singer);
        songTv = findViewById(R.id.local_music_bottom_iv_song);
        musicRv = findViewById(R.id.local_music_rv);
        nextIv.setOnClickListener(this);
        playIv.setOnClickListener(this);
        lastIv.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.local_music_bottom_iv_last:
                if (currentPlayPosition == 0) {
//                    Toast.makeText(this,"已经是第一首了，没有上一首！",Toast.LENGTH_SHORT).show();
//                    return;
                    currentPlayPosition = mDatas.size() - 1;
                }else {
                    currentPlayPosition--;
                }
                playMusicInMusicPosition(currentPlayPosition);
                break;
            case R.id.local_music_bottom_iv_play:
                if (currentPlayPosition == -1) {
                    //没有选中要播放的音乐
                    Toast.makeText(this,"请选择想要播放的音乐",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mediaPlayer.isPlaying()) {
                    //此时处于播放状态，需要暂停音乐
                    pauseMusic();
                } else {
                    //此时没有播放音乐，需要开始播放
                    playMusic();
                }
                break;
            case R.id.local_music_bottom_iv_next:
                nextPlay();
                break;
        }
    }

    //播放下一首
    private void nextPlay() {
        if (currentPlayPosition == mDatas.size() - 1) {
//                    Toast.makeText(this,"已经是最后一首了，没有下一首！",Toast.LENGTH_SHORT).show();
//                    return;
            currentPlayPosition = 0;
        }else{
            currentPlayPosition++;
        }
        playMusicInMusicPosition(currentPlayPosition);
    }

}