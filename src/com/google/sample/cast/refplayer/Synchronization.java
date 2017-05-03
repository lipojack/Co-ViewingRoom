/*
 * Copyright (C) 2014 Google Inc. All Rights Reserved. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.google.sample.cast.refplayer;

import android.app.Service;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.support.v7.media.MediaRouter.RouteInfo;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.Cast.ApplicationConnectionResult;
import com.google.android.gms.cast.Cast.MessageReceivedCallback;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.sample.cast.refplayer.RangeSeekBar.OnRangeSeekBarChangeListener;
import com.google.sample.cast.refplayer.browser.VideoItemLoader;
import com.google.sample.cast.refplayer.chatting.ChatMessage;
import com.google.sample.cast.refplayer.chatting.MessagesAdapter;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import wseemann.media.FFmpegMediaMetadataRetriever;



/**
 * Main activity to send messages to the receiver.
 */
public class Synchronization extends AppCompatActivity{


    private List<MediaInfo> videos; // load the video list
    private MediaInfo mSelectedMedia;
    //private static final String mUrl ="http://www2.cs.ccu.edu.tw/~cml100u/CSCLAB/f3.json";
    //private static final String mUrl ="http://smarttv.byethost31.com/f3_v2.json";
    //private static final String mUrl ="https://raw.githubusercontent.com/abq8392/smarttv/master/f3_v2.json";
    private static final String mUrl = "http://m104.nthu.edu.tw/~s104062559/SmartTV/f3_v2.json";

    private static final String TAG = Synchronization.class.getSimpleName();

    private static final int REQUEST_CODE = 1;

    private Toolbar mToolbar;

    private MediaRouter mMediaRouter;
    private MediaRouteSelector mMediaRouteSelector;
    private MediaRouter.Callback mMediaRouterCallback;
    private CastDevice mSelectedDevice;
    private GoogleApiClient mApiClient;
    private Cast.Listener mCastListener;
    private ConnectionCallbacks mConnectionCallbacks;
    private ConnectionFailedListener mConnectionFailedListener;
    private HelloWorldChannel mHelloWorldChannel;
    private boolean mApplicationStarted;
    private boolean mWaitingForReconnect;
    private String mSessionId;
    EditText field_typeMessage;
    View button_sendMessage;
    ListView listMessages;
    MessagesAdapter adapterMessages;

    FFmpegMediaMetadataRetriever mediaRetriever;



    private String srcPath = "android.resource://com.google.sample.cast.refplayer/"+R.raw.shishan;
    private VideoView mVideoView;
    private Handler mHandler;
    private int mCurTime;
    private int pgDuration = 1277000;
    private long mDuration = 1277000;
    private ProgressBar mProgressBar;
    private int mProgressStatus = 0;
    private RangeSeekBar<Integer> seekBar; // Clipping Bar
    private RangeSeekBar<Integer> pseekBar; //Clipping Bar in previewing mode
    private SeekBar previewSeek; // Progressbar in previewing mode
    private Button mSendClip;
    private Button mPreview;
    private Button mSendVideo;
    private TextView mStartText;
    private TextView mDisplayPreviewMode;
    private TextView mClipDuration;
    private TextView mSendClipFeedback;
    private int prev_start_p = 0;
    private int prev_end_p = 0;
    private int prev_start_p_ForPreview;
    private int prev_end_p_ForPreview;
    private long SysStartTime = 0;
    private long CurSysTime = 0;

    private TextView cover_loading;
    /**
     * 1 -> Only whole video
     * 2 -> Clipping
     * 3 -> Snapshot */

    private int condition = 2; // 2 means clipping
    private ImageView mPlayPause; // 1 & 2
    private ImageView mClosebtn;
    //private CountDownTimer tracking; // 1 & 2
    private boolean previewMode = true; // 1 & 2
    private Timer timer;
    private boolean playpause = false;

    /* FTP Settings */
    FTPClient client;
    String localPath = Environment.getExternalStorageDirectory().toString();
    String filePath;
    String imagePath;
    String ftpPath = "/htdocs/upload";
    String ftpServer = "ftp.byethost31.com";
    String ftpUser = "b31_18875121";
    String ftpPassword = "MORRISWANG";
    public int VideoTime = 0;

    ImageView close_snapshot;
    String imageName;
    ImageView screen_snapshot;
    ImageView miniImage;


    public String UserName = "Tina";
    public String FriendName = "Mei-Ling";
    public int UserIndex = 2;
    public int FriendIndex = 1;
    public int Role = 1;
    public int Channel = 0;

    Button button_takeSnapshot;
    Button button_downVolume;
    Button button_upVolume;

    private int pre_length = 0;

    private int EmotionIndex = 1;
    private int FocusIndex = 1;
    private int VolumeIndex = 1;

    // set volume
    public void setVolume(int value){
        Log.d(TAG, "setVolume");
        VideoTime = getVideoTime();
        VolumeIndex = value;
        sendMessage("U&&" + VideoTime + "&&" + UserIndex + "&&" + "V-" + VolumeIndex);
    }
    public void downVolume(View view){
        setVolume(0);
    }
    public void upVolume(View view){
        setVolume(1);
    }

    // switch focus
    public void setFocus(int value){
        Log.d(TAG, "setFocus");
        FocusIndex = value;
        VideoTime = getVideoTime();
        sendMessage("U&&" + VideoTime + "&&" + UserIndex + "&&" + "F-" + FocusIndex);
    }
    public void switchFocus(View view){
        if(FocusIndex==0) FocusIndex = 1;
        else FocusIndex = 0;
        setFocus(FocusIndex);
    }

    // show emotion
    public void showEmotion(int index){
        Log.d(TAG, "showEmotion");
        VideoTime = getVideoTime();
        EmotionIndex = index;
        sendMessage("U&&" + VideoTime + "&&" + UserIndex + "&&" + "E-" + EmotionIndex);
    }
    public void showEmotionHappy(View view){
        showEmotion(1);
    }
    public void showEmotionSurprise(View view){
        showEmotion(2);
    }
    public void showEmotionSad(View view){
        showEmotion(3);
    }
    public void showEmotionAngry(View view){
        showEmotion(4);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //setImmersiveMode();
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.synchronization);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            UserIndex = Integer.parseInt(extras.getString("UserIndex"));
            Log.d(TAG, "UserIndex: "+UserIndex);
        }

        initUI();
        setMediaRouter();
        getVideoURL();
        buildSocketConnection();

    }

    public void buildSocketConnection(){

        Thread socket = new Thread("Client"){
            
            private Socket clientSocket;
            BufferedInputStream in;
            
            @Override
            public void run() {
                Log.d("SocketConnection", "Run");
                try{
                    // set server IP and Port
                    //InetAddress serverIp = InetAddress.getByName("10.0.1.27"); // CSCLab Tina
                    //InetAddress serverIp = InetAddress.getByName("10.0.1.103");   // CSCLab Jack
                    InetAddress serverIp = InetAddress.getByName("192.168.1.100"); // CVLab Sever
                    int serverPort = 7777;
                    clientSocket = new Socket(serverIp, serverPort);
                    Log.d("SocketConnection", "Socket built");
                    
                    BufferedInputStream input = new BufferedInputStream(clientSocket.getInputStream());

                    // repeat
                    Looper.prepare();

                    while (clientSocket.isConnected()) {
                        Log.d("SocketConnection", "Connection built");
                        byte[] data_byte = new byte[1024];
                        String data = "";
                        int length;
                        if((length=input.read(data_byte)) > 0){
                            data += new String(data_byte, 0, length);
                            Log.d("SocketConnection", "Data: " + data);
                            
                            String subdata[] = data.split(",", 2);
                            String type[] = subdata[0].split(":", 2);
                            String value[] = subdata[1].split(":", 2);

                            Log.d("SocketConnection", "type[1]: " + type[1]);
                            
                            if(type[1].equals("\"gesture\"")){
                                Log.d("SocketConnection", "gesture");
                                setVolume(0);
                            }else if(type[1].equals("\"gaze\"")){
                                Log.d("SocketConnection", "gaze");
                                setFocus((Character.getNumericValue(value[1].charAt(0))));
                            }else{
                                Log.d("SocketConnection", "default");
                            }
                        }
                    }

                    Looper.loop();

                }catch(Exception e){
                    // handle disconnection
                    e.printStackTrace();
                    Log.e("SocketConnection", e.toString());

                    // shut down when disconnect
                    finish();
                }
            }
        };

        socket.start();

    }

    public int getVideoTime(){
        CurSysTime = System.currentTimeMillis();
        return (int)(CurSysTime-SysStartTime);
    }

    public void clearMessage(){
        listMessages = (ListView) findViewById(R.id.list_chat);
        adapterMessages = new MessagesAdapter(this);
        listMessages.setAdapter(adapterMessages);
    }

    public void clickLink(View view){
        TextView indexView = (TextView)view.findViewById(R.id.index);
        Log.d(TAG, "clickLink: " + indexView.getText().toString());
        String part[] = indexView.getText().toString().split("-", 2);
        String index = part[0];
        String identity = part[1];
        if(identity.charAt(0)!='0' && !(condition==1 && Role==1)) setVibrate(100);
        VideoTime = getVideoTime();
        sendMessage("R&&" + VideoTime + "&&" + UserIndex + "&&" + index);
    }

    public void takeSnapshot(View view) {
        try {
            int length;
            if (screen_snapshot.getVisibility() == View.VISIBLE){
                length = pre_length;
            }else length = mVideoView.getCurrentPosition();
            Bitmap bitmap = mediaRetriever.getFrameAtTime(length*1000, FFmpegMediaMetadataRetriever.OPTION_CLOSEST);

            //Date currentTime = new Date();
            //android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", currentTime);
            imageName = length + ".jpg";
            imagePath = localPath + "/" + imageName;

            File imageFile = new File(imagePath);
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 50;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();

            screen_snapshot.setImageBitmap(bitmap);
            screen_snapshot.setVisibility(View.VISIBLE);
            screen_snapshot.bringToFront();
            //setSnapshotInvisible();
            close_snapshot.setVisibility(View.VISIBLE);
            close_snapshot.bringToFront();

            button_takeSnapshot.setVisibility(View.VISIBLE);
            button_takeSnapshot.bringToFront();

            VideoTime = getVideoTime();
            sendMessage("S&&" + VideoTime + "&&" + UserIndex + "&&" + length);

            new UploadFile().execute(Integer.toString(length));

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void closeSnapshot(View view) {
        screen_snapshot.setVisibility(View.INVISIBLE);
        close_snapshot.setVisibility(View.INVISIBLE);
    }

    public void getVideoURL(){
        VideoItemLoader vloader = new VideoItemLoader(Synchronization.this, mUrl);
        videos = vloader.loadInBackground();
        // 0: test   1:formal
        mSelectedMedia = videos.get(0);
        mediaRetriever = new FFmpegMediaMetadataRetriever();
        mediaRetriever.setDataSource(mSelectedMedia.getContentId());
    }

    public void setChannel(){
        mSelectedMedia = videos.get(Channel);
        //mediaRetriever = new FFmpegMediaMetadataRetriever();
        mediaRetriever.setDataSource(mSelectedMedia.getContentId());

    }

    /*
    public void setImmersiveMode() {
        final View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    decorView.setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                }
            }
        });
    }*/



    public void setMediaRouter(){
        mMediaRouter = MediaRouter.getInstance(getApplicationContext());
        mMediaRouteSelector = new MediaRouteSelector.Builder()
        .addControlCategory(CastMediaControlIntent.categoryForCast(getResources()
            .getString(R.string.app_id))).build();
        mMediaRouterCallback = new MyMediaRouterCallback();
    }


    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("CSC HOME");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initUI() {
        //mVideoView = (VideoView) findViewById(R.id.videoView1);
        screen_snapshot = (ImageView) findViewById(R.id.screen_snapshot);
        field_typeMessage = (EditText) findViewById(R.id.field_typeMessage);
        listMessages = (ListView) findViewById(R.id.list_chat);
        adapterMessages = new MessagesAdapter(this);
        listMessages.setAdapter(adapterMessages);
        cover_loading = (TextView) findViewById(R.id.cover_loading);
        cover_loading.bringToFront();
        button_takeSnapshot = (Button) findViewById(R.id.button_takeSnapshot);
        button_downVolume = (Button) findViewById(R.id.button_downVolume);
        button_upVolume = (Button) findViewById(R.id.button_upVolume);
        if((UserIndex%2)==0){
            button_downVolume.setVisibility(View.INVISIBLE);
            button_upVolume.setVisibility(View.INVISIBLE);
        }
    }

    public void clickMessageSend(View view) {
        String message = field_typeMessage.getText().toString();
        if (message.length() > 0) {
            VideoTime = getVideoTime();
            sendMessage("M&&" + VideoTime + "&&" + UserIndex + "&&" + message);
            field_typeMessage.setText("");
        }
    }



    public void setClipBar(View v)
    {
        if(condition == 1){
            seekBar.setVisibility(v.VISIBLE);
        }
    }

    /**
     * Setting the progress bar (blue)
     */
    private void SetNormalProgressBar(){
        mProgressBar.setMax(pgDuration);
        Log.d(TAG, "pgDuration: " + pgDuration);
        // Start lengthy operation in a background thread
        Thread thread = new Thread(new Runnable() {
            public void run() {
                while (mProgressStatus < pgDuration) {

                    mProgressStatus = mVideoView.getCurrentPosition();
                    // Update the progress bar
                    mHandler.post(new Runnable() {
                        public void run() {
                            mProgressBar.setProgress(mProgressStatus);
                            mStartText.setText("  "+com.google.android.libraries.cast.companionlibrary.utils.Utils
                                .formatMillis(mProgressStatus)+"  ");
                        }
                    });
                }
            }
        });
        thread.start();
    }


    /**
     *  Setting the Normal Clip bar(orange)
     */
    private void SetNormalClipBar() {
        /**
         *  Setting the clip bar (orange) */
        prev_end_p = (int)mDuration;
        seekBar = new RangeSeekBar(0, (int)mDuration, this);
        seekBar.setOnRangeSeekBarChangeListener(new OnRangeSeekBarChangeListener<Integer>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer start_p, Integer end_p) {
                // handle changed range values
                Log.i(TAG, "Selected new range values: MIN=" + start_p + ", MAX=" + end_p);


                CurSysTime = System.currentTimeMillis();
                if (end_p > (CurSysTime - SysStartTime)) {
                    seekBar.setSelectedMaxValue((int) (CurSysTime - SysStartTime));
                    prev_end_p = (int)(CurSysTime-SysStartTime);
                    seekVideoView();
                    mVideoView.start();
                    SystemClock.sleep(100);
                    mVideoView.pause();
                    mPreview.setVisibility(View.VISIBLE);
                    if(condition==2) mSendClip.setVisibility(View.VISIBLE);
                    SetReturnBtn();
                }
                else if (condition == 2 || condition == 3){
                    // Clip Condition
                    mClipDuration.setText("  "+com.google.android.libraries.cast.companionlibrary.utils.Utils
                        .formatMillis(prev_end_p - prev_start_p)+"  ");
                    mPreview.setVisibility(View.VISIBLE);
                    if(condition  ==2){
                        mSendClip.setVisibility(View.VISIBLE);
                    }
                    SetReturnBtn();

                    if (start_p != prev_start_p) {
                        mCurTime = start_p;
                        seekVideoView();

                        prev_start_p = start_p;
                        mVideoView.start();
                        SystemClock.sleep(100);
                        mVideoView.pause();
                    }
                    if (end_p != prev_end_p) {
                        mCurTime = end_p;
                        seekVideoView();

                        prev_end_p = end_p;
                        mVideoView.start();
                        SystemClock.sleep(100);
                        mVideoView.pause();
                    }
                }

            }
        });
seekVideoView();
        seekBar.setSelectedMaxValue(mCurTime); //Let the rangeseekbar start at the begin

        // add RangeSeekBar to pre-defined layout
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.synchronization);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);

        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, seekBar.getId());
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, seekBar.getId());
        params.addRule(RelativeLayout.BELOW, mVideoView.getId());
        seekBar.setVisibility(View.VISIBLE);
        layout.addView(seekBar, params);
    }

    /**
     *  About ClipFeedback text
     */
    private void SetSendFeedbackText(){
        mSendClipFeedback.setVisibility(View.VISIBLE);
        mSendClipFeedback.postDelayed(new Runnable() {
            public void run() {
                mSendClipFeedback.setVisibility(View.INVISIBLE);

            }
        }, 1000);
    }


    /**
     *  Setting the SendClip button
     */
    private void SetSendClip(){
        mSendClip.setVisibility(View.VISIBLE);
        mSendClip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int start = prev_start_p;
                int end = prev_end_p;
                Log.d(TAG, "clip: " + start + "-" + end);
                VideoTime = getVideoTime();
                sendMessage("C&&" + VideoTime + "&&" + UserIndex + "&&" + start + "-" + end);

                SetSendFeedbackText();

                // And then send the start point & end point to Conversation
            }
        });
    }


    /**
     *  Setting the preview button
     */
    private void SetPreviewBtn() {
         /** The Preview button:
         *  If enter the Preview mode,user can play the clips until its end point.
         *  If enter the Resume mode, resume to the TV progress */

         mPreview.setOnClickListener(new View.OnClickListener() {

             @Override
             public void onClick(View view) {
                 if (previewMode) {

                     /** Entering the preview mode */
                     mDisplayPreviewMode.setText(" Previewing... ");
                     //mPreview.setText("Resume");


                     if (condition == 2 || condition == 3) {
                         SetReturnBtn();
                         if (Role == 1) {
                             seekBar.setVisibility(View.INVISIBLE);
                         }
                         setPreviewClip();
                     }

                     if (Role == 1) {
                         mProgressBar.setVisibility(View.INVISIBLE);
                     }

                     previewSeek.setVisibility(View.VISIBLE);

                     SetPreviewPlayPause();

                     previewMode = false;
                     mCurTime = prev_start_p;
                     previewing();
                     CreatePreviewingSeekbar();

                 } else {
                     /** Continue to editing the clip */
                     if (condition == 2 || condition == 3) {
                         if (timer != null) {
                             timer.cancel();
                             CreatePreviewingSeekbar();
                         }

                         prev_start_p = prev_start_p_ForPreview;
                         prev_end_p = prev_end_p_ForPreview;

                         setPreviewClip();

                         previewSeek.setProgress(prev_start_p);
                         mVideoView.seekTo(prev_start_p);
                         mVideoView.start();
                         SystemClock.sleep(100);
                         mVideoView.pause();

                     }
                 }
             }
         });
}
    /**
     *  Update the Previewing mode seekbar
     */
    private void CreatePreviewingSeekbar(){

        /* Timer for previewing seekbar */
        if(timer != null) {
            timer.cancel();
        }

        timer = new Timer();
        timer.scheduleAtFixedRate(new updatePreviewSeek(), 0, 1000);
        listenPreview();
    }

    /**
     *  Set the close button/ Return button
     */
    private void SetReturnBtn(){
        mClosebtn.setVisibility(View.VISIBLE);
        mClosebtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(Role==1) BackNormalPlay();
                else{
                    if(condition==2){
                        mClosebtn.setVisibility(View.INVISIBLE);
                        mPreview.setVisibility(View.INVISIBLE);
                        mSendClip.setVisibility(View.INVISIBLE);
                    }else if(condition==3){
                        mClosebtn.setVisibility(View.INVISIBLE);
                        button_takeSnapshot.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });
    }

    /*
    public void setSnapshotInvisible(){
        mClosebtn.setVisibility(View.VISIBLE);
        mClosebtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                screen_snapshot.setVisibility(View.INVISIBLE);
                close_snapshot.setVisibility(View.INVISIBLE);
            }
        });
    }
    */

    /**
     *  Back to normal play mode
     */
    private void BackNormalPlay(){
        if(condition == 2 || condition == 3) {
            if (!previewMode) {
                mDisplayPreviewMode.setText("");
                playpause = false;
                mPlayPause.setImageDrawable(
                    getResources().getDrawable(R.drawable.ic_av_play_dark));

                previewMode = true;

                if (timer != null) {
                    timer.cancel();
                }

                if (condition == 2 || condition == 3) {
                    if(Role == 1) {
                        seekBar.setVisibility(View.VISIBLE);
                    }

                    pseekBar.setVisibility(View.INVISIBLE);
                }

                if(Role == 1) {
                    mProgressBar.setVisibility(View.VISIBLE);
                }
                previewSeek.setVisibility(View.INVISIBLE);
                mPlayPause.setVisibility(View.INVISIBLE);
            }

            mPreview.setVisibility(View.INVISIBLE);
            mSendClip.setVisibility(View.INVISIBLE);
        }

        if(Role == 1) {
            CurSysTime = System.currentTimeMillis();
            mCurTime = (int) (CurSysTime - SysStartTime);
            seekVideoView();
            mVideoView.start();
        }
        if(Role == 2){
            mVideoView.seekTo(0);
            mVideoView.start();
            SystemClock.sleep(100);
            mVideoView.pause();
        }
        mClosebtn.setVisibility(View.INVISIBLE);
    }

    /**
     *  Set the play/pause in previewing mode
     */
    private void SetPreviewPlayPause(){
        mPlayPause.setVisibility(View.VISIBLE);
        mPlayPause.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                if (playpause) {

                    mPlayPause.setImageDrawable(
                        getResources().getDrawable(R.drawable.ic_av_play_dark));
                    mVideoView.pause();
                    playpause = false;
                } else {

                    mPlayPause.setImageDrawable(
                        getResources().getDrawable(R.drawable.ic_av_pause_dark));
                    mVideoView.start();
                    //previewCountDown();
                    playpause = true;
                }

            }
        });
    }


    /**
     *  Setting the Previewing mode(Editing mode) clip bar (orange)
     */
    private void setPreviewClip(){

     if(pseekBar != null) {
        pseekBar.setVisibility(View.GONE);
    }

    prev_start_p_ForPreview = prev_start_p;
    prev_end_p_ForPreview = prev_end_p;

    pseekBar = new RangeSeekBar(prev_start_p_ForPreview, prev_end_p_ForPreview, this);
    pseekBar.setOnRangeSeekBarChangeListener(new OnRangeSeekBarChangeListener<Integer>() {
        @Override
        public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer start_p, Integer end_p) {
                // handle changed range values
            Log.i(TAG, "Selected new range values: MIN=" + start_p + ", MAX=" + end_p);
            mClipDuration.setText("  "+com.google.android.libraries.cast.companionlibrary.utils.Utils
                .formatMillis(prev_end_p_ForPreview - prev_start_p_ForPreview)+"  ");

            playpause = false;
            mPlayPause.setImageDrawable(
                getResources().getDrawable(R.drawable.ic_av_play_dark));


            if (start_p != prev_start_p_ForPreview) {
                mVideoView.seekTo(start_p);
                mVideoView.start();
                SystemClock.sleep(100);
                mVideoView.pause();
                prev_start_p_ForPreview = start_p;
                prev_start_p = start_p;
            }
            if (end_p != prev_end_p_ForPreview) {
                mVideoView.seekTo(end_p);
                mVideoView.start();
                SystemClock.sleep(100);
                mVideoView.pause();
                prev_end_p_ForPreview = end_p;
                prev_end_p = end_p;
            }
        }
    });

        // add RangeSeekBar to pre-defined layout
RelativeLayout layout = (RelativeLayout) findViewById(R.id.synchronization);
RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
    ViewGroup.LayoutParams.WRAP_CONTENT);

params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, pseekBar.getId());
params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, pseekBar.getId());
params.addRule(RelativeLayout.BELOW, mVideoView.getId());
pseekBar.setVisibility(View.VISIBLE);
layout.addView(pseekBar, params);
}


    /**
     *  Listen to the preview seekbar
     */
    private void listenPreview(){

        previewSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            //private Timer tracking;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mVideoView.pause();
                playpause = false;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mVideoView.seekTo((previewSeek.getProgress()) + prev_start_p);
            }
        });
    }

    private class updatePreviewSeek extends TimerTask {

        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    int currentPos = 0;
                    currentPos = mVideoView.getCurrentPosition();
                    if(currentPos > prev_end_p)
                    {
                        mVideoView.pause();
                        mDisplayPreviewMode.setText("  Previewing Done ");
                    }
                    previewSeek.setProgress(currentPos-prev_start_p);
                    previewSeek.setMax(prev_end_p - prev_start_p);
                }
            });
        }
    }

    private void previewing(){
        mVideoView.seekTo(mCurTime);
        mVideoView.start();
        SystemClock.sleep(100);
        mVideoView.pause();
    }

    private void seekVideoView(){
        Thread thread = new Thread(new Runnable(){
         @Override
         public void run(){
             mVideoView.seekTo(mCurTime);
         }
     });
        thread.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item);
        MediaRouteActionProvider mediaRouteActionProvider
        = (MediaRouteActionProvider) MenuItemCompat
        .getActionProvider(mediaRouteMenuItem);
        // Set the MediaRouteActionProvider selector for device discovery.
        mediaRouteActionProvider.setRouteSelector(mMediaRouteSelector);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadViews(){

        mVideoView = (VideoView) findViewById(R.id.videoView1);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar2);
        mStartText = (TextView) findViewById(R.id.startText);
        mDisplayPreviewMode = (TextView) findViewById(R.id.previewMode);

        /**Components in Condition 1 : Whole Video*/
        if(condition == 1)
        {
            mSendVideo = (Button) findViewById(R.id.send_video);
            mPreview = (Button) findViewById(R.id.clipPreview);
            previewSeek = (SeekBar) findViewById(R.id.previewSeek);
            mPlayPause = (ImageView) findViewById(R.id.imageView);
            mSendClipFeedback = (TextView) findViewById(R.id.SendClipfeedback);
        }

        /** Components in Condition 2 : Clipping*/
        /** Components in Condtion 3 : Snapshot*/
        if(condition == 2 || condition == 3){
            mSendClip = (Button) findViewById(R.id.SendClipBtn);
            mClipDuration = (TextView) findViewById(R.id.clipDuration);
            mSendClipFeedback = (TextView) findViewById(R.id.SendClipfeedback);
            mPreview = (Button) findViewById(R.id.clipPreview);
            previewSeek = (SeekBar) findViewById(R.id.previewSeek);
            mPlayPause = (ImageView) findViewById(R.id.imageView);
            mClosebtn = (ImageView) findViewById(R.id.close_btn);
        }
        if(condition == 3) close_snapshot = (ImageView) findViewById(R.id.close_snapshot);
    }

    /*
     * Handle the voice recognition response
     *
     * @see android.support.v4.app.FragmentActivity#onActivityResult(int, int,
     * android.content.Intent)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(
                RecognizerIntent.EXTRA_RESULTS);
            if (matches.size() > 0) {
                Log.d(TAG, matches.get(0));
                //sendMessage(matches.get(0));
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Start media router discovery
        mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback,
            MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
        if(mMediaRouter.getRoutes().size() >= 1) {
            mMediaRouter.selectRoute(mMediaRouter.getRoutes().get(1));
        }
    }

    @Override
    protected void onStop() {
        // End media router discovery
        mMediaRouter.removeCallback(mMediaRouterCallback);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        teardown(true);
        super.onDestroy();
    }

    /**
     * Callback for MediaRouter events
     */
    public class MyMediaRouterCallback extends MediaRouter.Callback {

        @Override
        public void onRouteSelected(MediaRouter router, RouteInfo info) {
            // Handle the user route selection.
            mSelectedDevice = CastDevice.getFromBundle(info.getExtras());
            launchReceiver();
        }

        @Override
        public void onRouteUnselected(MediaRouter router, RouteInfo info) {
            Log.d(TAG, "onRouteUnselected");
            teardown(false);
            mSelectedDevice = null;
        }
    }

    /**
     * Start the receiver app
     */
    public void launchReceiver() {
        try {
            mCastListener = new Cast.Listener() {
                @Override
                public void onApplicationDisconnected(int errorCode) {
                    Log.d(TAG, "application has stopped");
                    teardown(true);
                }
            };
            // Connect to Google Play services
            mConnectionCallbacks = new ConnectionCallbacks();
            mConnectionFailedListener = new ConnectionFailedListener();
            Cast.CastOptions.Builder apiOptionsBuilder = Cast.CastOptions
            .builder(mSelectedDevice, mCastListener);
            mApiClient = new GoogleApiClient.Builder(this)
            .addApi(Cast.API, apiOptionsBuilder.build())
            .addConnectionCallbacks(mConnectionCallbacks)
            .addOnConnectionFailedListener(mConnectionFailedListener)
            .build();
            mApiClient.connect();
        } catch (Exception e) {
            Log.e(TAG, "Failed launchReceiver", e);
        }
    }

    /**
     * Google Play services callbacks
     */
    private class ConnectionCallbacks implements GoogleApiClient.ConnectionCallbacks {

        @Override
        public void onConnected(Bundle connectionHint) {

            if (mApiClient == null) {
                // We got disconnected while this runnable was pending
                // execution.
                return;
            }

            try {
                if (mWaitingForReconnect) {
                    mWaitingForReconnect = false;
                    // Check if the receiver app is still running
                    if ((connectionHint != null)
                        && connectionHint.getBoolean(Cast.EXTRA_APP_NO_LONGER_RUNNING)) {
                        Log.d(TAG, "App  is no longer running");
                    teardown(true);
                } else {
                        // Re-create the custom message channel
                    try {
                        Cast.CastApi.setMessageReceivedCallbacks(
                                mApiClient,
                                mHelloWorldChannel.getNamespace(),
                                mHelloWorldChannel);
                    } catch (IOException e) {
                        Log.e(TAG, "Exception while creating channel", e);
                    }
                }
            } else {
                    // Launch the receiver app
                Cast.CastApi.launchApplication(mApiClient, getString(R.string.app_id), false)
                .setResultCallback(
                        new ResultCallback<Cast.ApplicationConnectionResult>() {
                            @Override
                            public void onResult(
                                    ApplicationConnectionResult result) {
                                Status status = result.getStatus();
                                Log.d(TAG,
                                        "ApplicationConnectionResultCallback.onResult:"
                                                + status.getStatusCode());
                                if (status.isSuccess()) {
                                    ApplicationMetadata applicationMetadata = result
                                            .getApplicationMetadata();
                                    mSessionId = result.getSessionId();
                                    String applicationStatus = result
                                            .getApplicationStatus();
                                    boolean wasLaunched = result.getWasLaunched();
                                    Log.d(TAG, "application name: "
                                            + applicationMetadata.getName()
                                            + ", status: " + applicationStatus
                                            + ", sessionId: " + mSessionId
                                            + ", wasLaunched: " + wasLaunched);
                                    mApplicationStarted = true;

                                    // Create the custom message
                                    // channel
                                    mHelloWorldChannel = new HelloWorldChannel();
                                    try {
                                        Cast.CastApi.setMessageReceivedCallbacks(
                                                mApiClient,
                                                mHelloWorldChannel.getNamespace(),
                                                mHelloWorldChannel);
                                    } catch (IOException e) {
                                        Log.e(TAG, "Exception while creating channel",
                                                e);
                                    }


                                    initialMessage();

                                } else {
                                    Log.e(TAG, "application could not launch");
                                    teardown(true);
                                }
                            }
                        });
            }
            } catch (Exception e) {
                Log.e(TAG, "Failed to launch application", e);
            }
        }

        @Override
        public void onConnectionSuspended(int cause) {
            mWaitingForReconnect = true;
        }

        public void initialMessage(){
            clearMessage();
            sendMessage("I&&" + "0" + "&&" + UserIndex + "&&-");
        }
    }

    /**
     * Google Play services callbacks
     */
    private class ConnectionFailedListener implements GoogleApiClient.OnConnectionFailedListener {

        @Override
        public void onConnectionFailed(ConnectionResult result) {
            teardown(false);
        }
    }

    /**
     * Tear down the connection to the receiver
     */
    private void teardown(boolean selectDefaultRoute) {
        if (mApiClient != null) {
            if (mApplicationStarted) {
                if (mApiClient.isConnected() || mApiClient.isConnecting()) {
                    try {
                        Cast.CastApi.stopApplication(mApiClient, mSessionId);
                        if (mHelloWorldChannel != null) {
                            Cast.CastApi.removeMessageReceivedCallbacks(
                                mApiClient,
                                mHelloWorldChannel.getNamespace());
                            mHelloWorldChannel = null;
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception while removing channel", e);
                    }
                    mApiClient.disconnect();
                }
                mApplicationStarted = false;
            }
            mApiClient = null;
        }
        if (selectDefaultRoute) {
            mMediaRouter.selectRoute(mMediaRouter.getDefaultRoute());
        }
        mSelectedDevice = null;
        mWaitingForReconnect = false;
        mSessionId = null;
    }

    /**
     * Send a text message to the receiver
     */
    public void sendMessage(String message) {

        if (mApiClient != null && mHelloWorldChannel != null) {
            try {
                Cast.CastApi.sendMessage(mApiClient,
                    mHelloWorldChannel.getNamespace(), message).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status result) {
                            if (!result.isSuccess()) {
                                Log.e(TAG, "Sending message failed");
                            }
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Exception while sending message", e);
                }
            } else {
                Toast.makeText(Synchronization.this, message, Toast.LENGTH_SHORT).show();
            }
        }

    /**
     * Custom message channel
     */
    class HelloWorldChannel implements MessageReceivedCallback {

        /**
         * @return custom namespace
         */
        public String getNamespace() {
            return getString(R.string.namespace);
        }

        @Override
        public void onMessageReceived(CastDevice castDevice, String namespace,
            String message) {
            Log.d(TAG, "onMessageReceived: "+message);
            // Message: type && time && user index && content && index
            if(message.toLowerCase().contains("&&")){
                String[] parts = message.split("&&", 5);
                String type = parts[0];
                String time = parts[1];
                int user = 0;
                if(parts[2]!="") user = Integer.parseInt(parts[2]);
                String index = parts[3];
                String content = parts[4];
                switch(type.charAt(0)){

                    case 'I':
                    Log.d(TAG, "initial: "+message);
                    String[] I_subparts = type.split("-", 2);
                    if(Integer.parseInt(I_subparts[1])==UserIndex){
                        String[] I_content_subparts = content.split("-", 6);
                        UserName = I_content_subparts[0];
                        FriendName = I_content_subparts[1];
                        FriendIndex = Integer.parseInt(I_content_subparts[2]);
                        condition = Integer.parseInt(I_content_subparts[3]);
                        Role = Integer.parseInt(I_content_subparts[4]);
                        Channel = Integer.parseInt(I_content_subparts[5]);
                        setChannel();
                        setVideo();
                        cover_loading.setVisibility(View.INVISIBLE);
                    }
                    break;

                    case 'M':
                    if(user==UserIndex) setChatMessage(UserName, content, false, time, index);
                    else setChatMessage(FriendName, content, true, time, index);
                    break;

                    case 'C':
                    Log.d(TAG, "clip: "+message);
                    String[] C_subparts = type.split("-", 2);
                    if(Integer.parseInt(C_subparts[1])==UserIndex){
                        String[] C_content_subparts = content.split("-", 3);
                        int start = Integer.parseInt(C_content_subparts[0]);
                        int end = Integer.parseInt(C_content_subparts[1]);
                        showClip(start, end);
                    }
                    break;

                    case 'S':
                    Log.d(TAG, "snapshot: "+message);
                    String[] S_subparts = type.split("-", 2);
                    if(Integer.parseInt(S_subparts[1])==UserIndex){
                        showSnapshot(content);
                    }
                    break;

                    case 'P':
                    String[] P_subparts = type.split("-", 2);
                    if(Integer.parseInt(P_subparts[1])==UserIndex){
                        if(user==UserIndex) setChatMessage(UserName, content, false, time, index);
                        else setChatMessage(FriendName, content, true, time, index);
                    }
                    
                    break;
                }
            }
        }
    }

    public void setVideo(){
        mHandler = new Handler();
        loadViews();
        
        if(Channel==0) srcPath = "android.resource://com.google.sample.cast.refplayer/"+R.raw.shishan;
        else if(Channel==1) srcPath = "android.resource://com.google.sample.cast.refplayer/"+R.raw.smarttv_35_2;
        else if(Channel==2) srcPath = "android.resource://com.google.sample.cast.refplayer/"+R.raw.smarttv_10;
        

        mVideoView.setVideoURI(Uri.parse(srcPath));

        SystemClock.sleep(1000);

        if ( Role == 1 ) { mVideoView.start(); }
        if ( Role == 2 ) { mProgressBar.setVisibility(View.INVISIBLE); }


        SysStartTime = System.currentTimeMillis();
        CurSysTime = System.currentTimeMillis();

        mProgressBar.setProgress(0);
        SetNormalProgressBar();

        // Condition 1: Video Condition
        if(condition == 1){
            if( Role == 1 ){
               mSendVideo.setVisibility(View.VISIBLE);
               mSendVideo.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View view) {
                    int start = 0;
                    int end = pgDuration;
                    Log.d(TAG, "clip: " + start + "-" + end);
                    VideoTime = getVideoTime();
                    sendMessage("C&&" + VideoTime + "&&" + UserIndex + "&&" + start + "-" + end);
                    SetSendFeedbackText();
                }
            });
           }

           if(Role == 2) {
            //mPreview.setVisibility(View.INVISIBLE);
            //SetPreviewBtn();
        }
    }

        // Condition 2: Clip Condition
    if(condition == 2 || condition == 3) {
        if(condition == 3 && Role == 2) {

            /* Assume receiver get the prev_end_p time point */
                /*
                mVideoView.seekTo(prev_end_p);
                mVideoView.start();
                mVideoView.pause();*/

            }
            else {
                if(condition == 2 && Role == 1){
                    SetNormalClipBar();
                }
                if(condition == 3 && Role == 1){
                    SetNormalClipBar();
                    button_takeSnapshot.setVisibility(View.VISIBLE);
                }
                if(condition == 2 && Role == 2){
                    mPreview.setVisibility(View.INVISIBLE);
                }
                SetPreviewBtn();

                if(condition == 2){
                    SetSendClip();
                    mSendClip.setVisibility(View.INVISIBLE);
                }
            }
        }

        //if(condition==3) button_takeSnapshot.setVisibility(View.VISIBLE);
        // Condition 3: Snapshot Condition
    }

    // Set Message
    public void setChatMessage(String user, String content, boolean incoming, String time, String index){
        ChatMessage message_public = new ChatMessage();
        message_public.setUsername(user);
        message_public.setMessage(content);
        message_public.setIncomingMessage(incoming);
        message_public.setDate(time);
        message_public.setIndex(index);
        adapterMessages.add(message_public);
        if(incoming) setVibrate(500);

        String part[] = index.split("-", 2);
        String identity = part[1];
        message_public.setIdentity(identity);
    }

    public void showSnapshot(String imageName){
        int length = Integer.parseInt(imageName);
        pre_length = length;
        Bitmap bitmap = mediaRetriever.getFrameAtTime(length*1000, FFmpegMediaMetadataRetriever.OPTION_CLOSEST);
        screen_snapshot.setImageBitmap(bitmap);
        screen_snapshot.setVisibility(View.VISIBLE);
        screen_snapshot.bringToFront();
        close_snapshot.setVisibility(View.VISIBLE);
        close_snapshot.bringToFront();
        button_takeSnapshot.setVisibility(View.VISIBLE);
        button_takeSnapshot.bringToFront();
        //setSnapshotInvisible();
    }

    public void showClip(int start, int end){
        if(condition == 1 && Role == 1){

        }
        else{
            prev_start_p = start;
            prev_end_p = end;

            if(condition==2) {
                mClosebtn.setVisibility(View.INVISIBLE);
                mSendClip.setVisibility(View.VISIBLE);
                mPreview.setVisibility(View.VISIBLE);
            }
            screen_snapshot.setVisibility(View.INVISIBLE);

            /** Entering the preview mode */
            mDisplayPreviewMode.setText("  Previewing... ");
        //mPreview.setText("Resume");


            if (condition == 2) {
                SetReturnBtn();
                if(Role==1) seekBar.setVisibility(View.INVISIBLE);
                setPreviewClip();
            }
            if(Role==1) mProgressBar.setVisibility(View.INVISIBLE);

            previewSeek.setVisibility(View.VISIBLE);

            SetPreviewPlayPause();

            previewMode = false;
            mCurTime = start;
            previewing();
            CreatePreviewingSeekbar();

            Log.d(TAG, "showClip: "+start+"-"+end);
        }   
    }

    // Vibrate
    public void setVibrate(int time){
        Vibrator myVibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
        myVibrator.vibrate(time);
    }

    class UploadFile extends AsyncTask<String, Integer, Boolean> {

        String length;

        @Override
        protected Boolean doInBackground(String... params) {
            length = params[0];
            imageName = length + ".jpg";
            filePath = localPath + "/" + imageName;
            
            try {

                client = new FTPClient();
                Log.d("FTP","Connecting to " + ftpServer);

                try {
                    client.setType(FTPClient.TYPE_BINARY);
                    client.connect(ftpServer);
                    client.login(ftpUser, ftpPassword);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    client.changeDirectory(ftpPath);
                } catch(Exception e) {
                    client.createDirectory(ftpPath);
                    client.changeDirectory(ftpPath);
                }

                if (client != null) {
                    try {

                        File fileUpload = new File(filePath);

                        Log.d("FTPSync", "Uploading the " + filePath + "to" + ftpServer);

                        client.upload(fileUpload, new FTPDataTransferListener() {

                            @Override
                            public void started() {
                                Log.d("FTP", "TRANSFER-STATUS: File transfer started...");
                            }

                            @Override
                            public void transferred(int length) {
                            }

                            @Override
                            public void completed() {
                                Log.d("FTP", "TRANSFER-STATUS: File transfer completed...");
                            }

                            @Override
                            public void aborted() {
                                Log.d("FTP", "TRANSFER-STATUS: File transfer aborted...");
                            }

                            @Override
                            public void failed() {
                                Log.d("FTP", "TRANSFER-STATUS: File transfer failed...");
                            }

                        });

Log.d("FTPSync", "Successfully Uploaded the " + filePath + " File to " + ftpServer);

} catch (Exception e) {
    e.printStackTrace();
}
}
} catch (Exception e) {
    e.printStackTrace();
}

finally {
    if (client != null) {
        try {
            client.disconnect(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

return true;
}

@Override
protected void onPreExecute() {}

@Override
protected void onProgressUpdate(Integer... values) {}

@Override
protected void onPostExecute(Boolean result) {
}
}

}