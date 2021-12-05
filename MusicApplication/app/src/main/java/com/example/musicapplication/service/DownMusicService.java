package com.example.musicapplication.service;

import android.Manifest;
import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.example.musicapplication.activity.DisplayActivity;
import com.example.musicapplication.activity.SearchNetActivity;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;

public class DownMusicService extends IntentService {
   // String uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString();/Music/qqmusic/song
   public static final File PATH = Environment.getExternalStoragePublicDirectory("/qqmusic/song");
    //File folDirectory = new File(uri);////如果文件不存在，则自动创建

    protected void onHandleIntent(Intent intent) {
        String url = intent.getStringExtra("path");//接收SearchNetActivity里传递的url
        String actname=intent.getStringExtra("actname");//歌手名
        String songtitle=intent.getStringExtra("songname");//歌名
        String songname= actname+" - "+songtitle+".mp3";//路径
        String duration=intent.getStringExtra("duration");


        ContentValues contentValues=new ContentValues();//建立对象
        contentValues.put(MediaStore.Audio.Media.TITLE, songtitle);//设置歌名
        contentValues.put(MediaStore.Audio.Media.ARTIST,actname);//设置歌手
        contentValues.put(MediaStore.Audio.Media.DATA,"/storage/emulated/0/qqmusic/song/"+songname);//设置路径
        contentValues.put(MediaStore.Audio.Media.DURATION,duration);//设置时长
        contentValues.put(MediaStore.Audio.Media.IS_MUSIC,1);//设置是音乐
        getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues);
        //向数据库插入
        System.out.println(url);
        Request request = new Request.Builder().url(url).build();
        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 下载失败
                e.printStackTrace();
                Log.i("DOWNLOAD","download failed");
                Handler handler=new Handler(Looper.getMainLooper());
                handler.post(new Runnable(){
                    public void run(){
                        Toast.makeText(getApplicationContext(), "下载失败", Toast.LENGTH_LONG).show();
                    }
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Sink sink = null;
                BufferedSink bufferedSink = null;
                try {
                    File dest = new File(PATH, songname);//设置路径
                    sink = Okio.sink(dest);//得到一个Sink输出流
                    //buffer中的内容写入到sink成员变量中去，然后将自身返回
                    bufferedSink = Okio.buffer(sink);
                    bufferedSink.writeAll(response.body().source());
                  //  bufferedSink.close();
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), "下载成功!", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i("DOWNLOAD", "download failed");
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), "下载失败!", Toast.LENGTH_LONG).show();
                        }
                    });
                } finally {
                    if (bufferedSink != null) {
                        bufferedSink.close();
                    }
                }
            }
        });
        Intent intentNew = new Intent(DownMusicService.this, DisplayActivity.class);//跳转至播放页面
        intentNew.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
        startActivity(intentNew);
    }
    public DownMusicService() {
        super("/qqmusic/song");
    }
}
