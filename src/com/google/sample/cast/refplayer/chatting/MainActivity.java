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

package com.google.sample.cast.refplayer.chatting;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.Cast.ApplicationConnectionResult;
import com.google.android.gms.cast.Cast.MessageReceivedCallback;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.sample.cast.refplayer.R;
import com.google.sample.cast.refplayer.VideoBrowserActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.support.v7.media.MediaRouter.RouteInfo;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.app.Service;
import android.os.Vibrator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import java.util.HashMap;
import java.util.Map;



/**
 * Main activity to send messages to the receiver.
 */
public class MainActivity extends AppCompatActivity {
//public class MainActivity extends ActionBarActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

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

    
    public String UserName = "Mei-Ling";
    public String FriendName = "Tina";
    public int UserIndex = 1;
    public int FriendIndex = 2;


    
    /*
    public String UserName = "Tina";
    public String FriendName = "Mei-Ling";
    public int UserIndex = 2;
    public int FriendIndex = 1;
    */



    public void backVideoBrowser(View view){
        backVideoBrowser();
        //finish();
    }

    public void backVideoBrowser() {
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, VideoBrowserActivity.class);
        startActivity(intent);
    }

    public void clearMessage(){
        listMessages = (ListView) findViewById(R.id.list_chat);
        adapterMessages = new MessagesAdapter(this);
        listMessages.setAdapter(adapterMessages);
    }

    public void loadMessage(View view){
        clearMessage();
        sendMessage("L&&" + UserIndex + "&&" + FriendIndex);
    }

    public void clickUserSet() {
        setContentView(R.layout.chatting);
    }

    public void clickLink(View view){

        TextView v = (TextView)view.findViewById(R.id.label_message);
        String m = v.getText().toString();
        Log.d(TAG, "clickLink: "+m);
    }


    public void takeScreenshot(View view) {
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        try {
            // image naming and path  to include sd card  appending name you choose for file
            //String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";
            String mPath = Environment.getExternalStorageDirectory().toString() + "/test3.jpg";

            // create bitmap screen capture
            //View v1 = getWindow().getDecorView().getRootView();
            View v1 = (RelativeLayout)findViewById(R.id.area_Screenshot);
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

            File imageFile = new File(mPath);
            
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();

            //uploadImage("" + now);
            //uploadImage();
            
            openScreenshot(imageFile);
            
            
        } catch (Throwable e) {
            // Several error may come out with file handling or OOM
            e.printStackTrace();
        }
    }
    public void openScreenshot(File imageFile) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(imageFile);
        intent.setDataAndType(uri, "image/*");
        startActivity(intent);
    }

    public void uploadImage(){
        //new DataUploader2().execute("");
        Log.d(TAG, "====== Volley uploadImage");

        //String url = "http://tinatu.byethost7.com/SmartTV/DataUploader4.php";
        String url = "http://smarttv.byethost31.com/DataUploader.php";
        // /String url = "http://10.0.1.18/DataUploader/DataUploader4.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "====== Volley onResponse"+response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "======Volley VolleyError"+error);
                    }
                }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("params1","TEST1");
                params.put("params2","TEST2");
                Log.d(TAG, "====== Volley getParams");
                return params;
            }
 
        };
 
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);




        //imageUploader = new DataUploader2();
        //imageUploader.execute("");
        
        //imageUploader = new DataUploader();
        //imageUploader.postFile(imageName);
        /*
        String url = "http://tinatu.byethost7.com/SmartTV/DataUploader.php";
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), imageName+".jpg");
        try {
            HttpClient httpclient = new DefaultHttpClient();

            HttpPost httppost = new HttpPost(url);

            InputStreamEntity reqEntity = new InputStreamEntity(
                    new FileInputStream(file), -1);
            reqEntity.setContentType("binary/octet-stream");
            reqEntity.setChunked(true); // Send in multiple parts if needed
            httppost.setEntity(reqEntity);
            HttpResponse response = httpclient.execute(httppost);
            //Do something with response...

        } catch (Exception e) {
            // show error
            Log.d(TAG, "Fail to upload image");

        }
        */
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "~~~~~~~~~~ onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatting);
        
        ActionBar actionBar = getSupportActionBar();
                //actionBar.setBackgroundDrawable(new ColorDrawable(
                //getResources().getColor(android.R.color.transparent)));
        //mToolbar = (Toolbar) findViewById(R.id.toolbar);
        //mToolbar.setLogo(R.drawable.actionbar_logo_castvideos);
        //mToolbar.setTitle(R.string.app_name);
        //ActionBar actionBar = setSupportActionBar(mToolbar);
                
        // When the user clicks on the button, use Android voice recognition to
        // get text
        /*
        Button voiceButton = (Button) findViewById(R.id.voiceButton);
        voiceButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startVoiceRecognitionActivity();
            }
        });
        */
        // Configure Cast device discovery
        mMediaRouter = MediaRouter.getInstance(getApplicationContext());
        mMediaRouteSelector = new MediaRouteSelector.Builder()
                .addControlCategory(CastMediaControlIntent.categoryForCast(getResources()
                        .getString(R.string.app_id))).build();
        mMediaRouterCallback = new MyMediaRouterCallback();

        setupActionBar();
        initUI();

        //setDialog();
    }




    private void setupActionBar() {
        Log.d(TAG, "~~~~~~~~~~ setupActionBar");


        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("CSC HOME");
        //mToolbar.setTitle(mSelectedMedia.getMetadata().getString(MediaMetadata.KEY_TITLE));
        setSupportActionBar(mToolbar);
        //getSupportActionBar().show();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }



    private void initUI() {
        field_typeMessage = (EditText) findViewById(R.id.field_typeMessage);
        listMessages = (ListView) findViewById(R.id.list_chat);
        adapterMessages = new MessagesAdapter(this);
        listMessages.setAdapter(adapterMessages);
    }

    public void clickMessageSend(View view) {
        String message = field_typeMessage.getText().toString();
        if (message.length() > 0) {
            sendMessage("M&&"+UserIndex+"&&"+message);
            field_typeMessage.setText("");
        }
    }

    // Android voice recognition
    private void startVoiceRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.message_to_cast));
        startActivityForResult(intent, REQUEST_CODE);
    }

    /*
     * Handle the voice recognition response
     *
     * @see android.support.v4.app.FragmentActivity#onActivityResult(int, int,
     * android.content.Intent)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "~~~~~~~~~~ onActivityResult");
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            if (matches.size() > 0) {
                Log.d(TAG, matches.get(0));
                sendMessage(matches.get(0));
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "~~~~~~~~~~ onStart");
        super.onStart();
        // Start media router discovery
        mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback,
                MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);

        //launchReceiver();
        //mSelectedDevice = CastDevice.getFromBundle(info.getExtras());
        //launchReceiver();
        //MediaRouter router = "android.support.v7.media.MediaRouter@41c21b38";
        //RouteInfo info = (RouteInfo)"MediaRouter.RouteInfo{ uniqueId=com.google.android.gms/.cast.media.CastMediaRouteProviderService:b5d65006fa48b3c0dcfcd1cb309e31a7, name=CSCLab_Living_Room, description=Chromecast, enabled=true, connecting=false, canDisconnect=false, playbackType=1, playbackStream=-1, volumeHandling=0, volume=0, volumeMax=20, presentationDisplayId=-1, extras=Bundle[mParcelledData.dataSize=584], settingsIntent=null, providerPackageName=com.google.android.gms }";
        //MyMediaRouterCallback.onRouteSelected(router, info);
        

        //mSelectedDevice = CastDevice.getFromBundle(info.getExtras());

        //launchReceiver();

    }

    @Override
    protected void onStop() {
        Log.d(TAG, "~~~~~~~~~~ onStop");
        // End media router discovery
        mMediaRouter.removeCallback(mMediaRouterCallback);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "~~~~~~~~~~ onDestroy");
        teardown(true);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "~~~~~~~~~~ onCreateOptionsMenu");
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

    /**
     * Callback for MediaRouter events
     */
    public class MyMediaRouterCallback extends MediaRouter.Callback {
    //private class MyMediaRouterCallback extends MediaRouter.Callback {


        @Override
        public void onRouteSelected(MediaRouter router, RouteInfo info) {
            Log.d(TAG, "~~~~~~~~~~ onRouteSelected");
            // Handle the user route selection.
            mSelectedDevice = CastDevice.getFromBundle(info.getExtras());

            launchReceiver();
        }

        @Override
        public void onRouteUnselected(MediaRouter router, RouteInfo info) {
            Log.d(TAG, "~~~~~~~~~~ onRouteUnselected: info=" + info);
            teardown(false);
            mSelectedDevice = null;
        }
    }

    /**
     * Start the receiver app
     */
    public void launchReceiver() {
    //private void launchReceiver() {
        Log.d(TAG, "~~~~~~~~~~ launchReceiver");
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
    private class ConnectionCallbacks implements
            GoogleApiClient.ConnectionCallbacks {

        @Override
        public void onConnected(Bundle connectionHint) {
            Log.d(TAG, "~~~~~~~~~~ onConnected");

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

                                                // set the initial instructions
                                                // on the receiver
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
            Log.d(TAG, "~~~~~~~~~~ onConnectionSuspended");
            mWaitingForReconnect = true;
        }

        public void initialMessage(){
            clearMessage();
            sendMessage("L&&" + UserIndex + "&&" + FriendIndex);
        }
    }

    /**
     * Google Play services callbacks
     */
    private class ConnectionFailedListener implements
            GoogleApiClient.OnConnectionFailedListener {

        @Override
        public void onConnectionFailed(ConnectionResult result) {
            Log.e(TAG, "~~~~~~~~~~ onConnectionFailed ");

            teardown(false);
        }
    }

    /**
     * Tear down the connection to the receiver
     */
    private void teardown(boolean selectDefaultRoute) {
        Log.d(TAG, "~~~~~~~~~~ teardown");
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
        Log.d(TAG, "~~~~~~~~~~ sendMessage OUT");
        if (mApiClient == null) Log.d(TAG, "mApiClient == null");
        if (mHelloWorldChannel == null) Log.d(TAG, "mHelloWorldChannel == null");

        if (mApiClient != null && mHelloWorldChannel != null) {
            try {
                Log.d(TAG, "sendMessage IN!!!!!!");
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
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
        }
    }

    /*
    public class MessageListener implements Cast.MessageReceivedCallback {
        @Override
        public void onMessageReceived(CastDevice castDevice, String namespace, String message) {
            Log.e(TAG, "~~~~~~~~~~ onMessageReceived ");
            Log.e(TAG, "~~~~~~~~~~ Message: "+message);
        }

    }
    */

    /**
     * Custom message channel
     */
    class HelloWorldChannel implements MessageReceivedCallback {

        /**
         * @return custom namespace
         */
        public String getNamespace() {
            Log.d(TAG, "~~~~~~~~~~ getNamespace");
            return getString(R.string.namespace);
        }

        
        // Receive message from the receiver app
        @Override
        public void onMessageReceived(CastDevice castDevice, String namespace,
                String message) {
            // Message: type && time && user index && content
            if(message.toLowerCase().contains("&&")){
                String[] parts = message.split("&&", 4);
                String time = parts[1];
                int index = Integer.parseInt(parts[2]);
                String content = parts[3];
                switch(parts[0].charAt(0)){
                case 'M':
                    if(index==UserIndex) setChatMessage(UserName, content, false, time);
                    else setChatMessage(FriendName, content, true, time);
                    break;
                case 'P':
                    String[] subparts = parts[0].split("-", 2);
                    if(Integer.parseInt(subparts[1])==UserIndex){
                        if(index==UserIndex) setChatMessage(UserName, content, false, time);
                        else setChatMessage(FriendName, content, true, time);
                    }
                    break;
                }
            }
        }
    }


    // Set Message
    public void setChatMessage(String name, String content, boolean incoming, String time){
        ChatMessage message_public = new ChatMessage();
        message_public.setUsername(name);
        message_public.setMessage(content);
        message_public.setIncomingMessage(incoming);
        message_public.setDate(time);
        adapterMessages.add(message_public);
        if(incoming) setVibrate(100);
    }


    // Vibrate
    public void setVibrate(int time){
        Vibrator myVibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
        myVibrator.vibrate(time);
    }


    // Set dialog to input user's name
    AlertDialog dialog;
    public void setDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Type Your Name");
        final EditText InputName = new EditText(this);
        InputName.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(InputName);
        builder.setPositiveButton(R.string.start_chatting, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                UserName = InputName.getText().toString();
            }
        });
        builder.setCancelable(false);
        dialog = builder.create();

        InputName.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() < 1 || s.length() > 20) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }
        });

        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    }

}