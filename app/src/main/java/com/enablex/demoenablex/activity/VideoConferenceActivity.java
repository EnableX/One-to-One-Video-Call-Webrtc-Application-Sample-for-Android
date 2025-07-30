package com.enablex.demoenablex.activity;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.enablex.demoenablex.R;
import com.enablex.demoenablex.utilities.OnDragTouchListener;
import com.enablex.demoenablex.utilities.Utility;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import enx_rtc_android.Controller.EnxActiveTalkerViewObserver;
import enx_rtc_android.Controller.EnxPlayerView;
import enx_rtc_android.Controller.EnxReconnectObserver;
import enx_rtc_android.Controller.EnxRoom;
import enx_rtc_android.Controller.EnxRoomObserver;
import enx_rtc_android.Controller.EnxRtc;
import enx_rtc_android.Controller.EnxStream;
import enx_rtc_android.Controller.EnxStreamObserver;


public class VideoConferenceActivity extends AppCompatActivity
        implements EnxRoomObserver, EnxStreamObserver, View.OnClickListener, EnxReconnectObserver, EnxActiveTalkerViewObserver {

    EnxRtc enxRtc;
    String token;
    String name;
    EnxPlayerView enxPlayerView;
    FrameLayout moderator;
    FrameLayout participant;
    ImageView disconnect;
    ImageView mute, video, camera, volume;
    EnxRoom enxRooms;
    boolean isVideoMuted = false;
    boolean isFrontCamera = true;
    boolean isAudioMuted = false;
    RelativeLayout rl;
    Gson gson;
    EnxStream localStream;
    EnxPlayerView enxPlayerViewRemote;
    ProgressDialog progressDialog;
    int PERMISSION_ALL = 1;
    private static final int PERMISSION_REQUEST_CODE = 200;
    private static final int REQUEST_CODE_MEDIA_PERMISSION = 201;
    RecyclerView mRecyclerView;
    boolean touch = false;
    String[] PERMISSIONS ;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private boolean isRequestedForSettingPage;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_conference);
        this.isRequestedForSettingPage = false;

        sharedPreferences = getSharedPreferences("enablex", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        getPreviousIntent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PERMISSIONS = new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH};
        } else {
            PERMISSIONS = new String[]{Manifest.permission.BLUETOOTH};
        }

        boolean status = sharedPreferences.getBoolean("isAppFirst", false);
        if (!status) {
            requestPermission();
        } else {
            if(!new Utility().hasPermissions(this,PERMISSIONS )) {//new String[]{Manifest.permission.BLUETOOTH, WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}
                showPermissionDialog(true);
            }else{
                initialize();
            }
        }
    }

    //Media permission  if required then use it
    private void checkAndRequestMediaPermissions() {
        String[] mediaPermissions;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mediaPermissions = new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO
            };
        } else {
            mediaPermissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        }

        if ( !new Utility().hasPermissions(this,mediaPermissions)) {
            ActivityCompat.requestPermissions(this, mediaPermissions, REQUEST_CODE_MEDIA_PERMISSION);
        }
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        if(isRequestedForSettingPage) {
            isRequestedForSettingPage = false;
            initialize();
        }
    }
    private void requestPermission() {
        if(!new Utility().hasPermissions(this, PERMISSIONS)){//new String[]{Manifest.permission.BLUETOOTH, WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}
            showPermissionDialog(false);
        }else{
            initialize();
        }
    }

    @Override
    public void onRoomConnected(EnxRoom enxRoom, JSONObject jsonObject) {
        //received when user connected with Enablex room
        enxRooms = enxRoom;
        if (enxRooms != null) {
            enxRooms.publish(localStream);
            enxRooms.setReconnectObserver(this);
            enxRoom.setActiveTalkerViewObserver(this);
            enxPlayerView = new EnxPlayerView(this, EnxPlayerView.ScalingType.SCALE_ASPECT_BALANCED, true);
            localStream.attachRenderer(enxPlayerView);
            moderator.addView(enxPlayerView);
        
        }
    }

    @Override
    public void onRoomError(JSONObject jsonObject) {
        //received when any error occurred while connecting to the Enablex room
        Toast.makeText(VideoConferenceActivity.this, jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onUserConnected(JSONObject jsonObject) {
        // received when a new remote participant joins the call
    }

    @Override
    public void onUserDisConnected(JSONObject jsonObject) {
        // received when a  remote participant left the call
        roomDisconnect();
    }

    @Override
    public void onPublishedStream(EnxStream enxStream) {
        //received when audio video published successfully to the other remote users
    }

    @Override
    public void onUnPublishedStream(EnxStream enxStream) {
        //received when audio video unpublished successfully to the other remote users
    }

    @Override
    public void onStreamAdded(EnxStream enxStream) {
        //received when a new stream added
        if (enxStream != null) {
            enxRooms.subscribe(enxStream);
        }
    }

    @Override
    public void onSubscribedStream(EnxStream enxStream) {
        //received when a remote stream subscribed successfully
    }

    @Override
    public void onUnSubscribedStream(EnxStream enxStream) {
        //received when a remote stream unsubscribed successfully
    }

    @Override
    public void onRoomDisConnected(JSONObject jsonObject) {
        //received when Enablex room successfully disconnected
        this.finish();
    }


    @Override
    public void onEventError(JSONObject jsonObject) {
        //received when any error occurred for any room event
        Toast.makeText(VideoConferenceActivity.this, jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEventInfo(JSONObject jsonObject) {
        // received for different events update
    }

    @Override
    public void onNotifyDeviceUpdate(String s) {
        // received when when new media device changed
    }

    @Override
    public void onAcknowledgedSendData(JSONObject jsonObject) {
        // received your chat data successfully sent to the other end
    }

    @Override
    public void onMessageReceived(JSONObject jsonObject) {
        // received when chat data received at room
    }

    @Override
    public void onUserDataReceived(JSONObject jsonObject) {
        // received when custom data received at room
    }

    @Override
    public void onUserStartTyping(JSONObject b) {

    }

    @Override
    public void onACKUpdateMessage(JSONObject jsonObject){

    }
    @Override
    public void  onMessageUpdate(JSONObject jsonObject){

    }
    @Override
    public void  onACKDeleteMessage(JSONObject jsonObject){

    }
    @Override
    public void onMessageDelete(JSONObject jsonObject){}
    @Override
    public void onACKSendMessage(JSONObject jsonObject){}
    @Override
    public void onConferencessExtended(JSONObject jsonObject) {

    }

    @Override
    public void onConferenceRemainingDuration(JSONObject jsonObject) {

    }

    @Override
    public void onAckDropUser(JSONObject jsonObject) {

    }

    @Override
    public void onAckDestroy(JSONObject jsonObject) {

    }

    @Override
    public void onAudioEvent(JSONObject jsonObject) {
        //received when audio mute/unmute happens
        try {
            String message = jsonObject.getString("msg");
            if (message.equalsIgnoreCase("Audio On")) {
                mute.setImageResource(R.drawable.unmute);
                isAudioMuted = false;
            } else if (message.equalsIgnoreCase("Audio Off")) {
                mute.setImageResource(R.drawable.mute);
                isAudioMuted = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onVideoEvent(JSONObject jsonObject) {
        //received when video mute/unmute happens
        try {
            String message = jsonObject.getString("msg");
            if (message.equalsIgnoreCase("Video On")) {
                video.setImageResource(R.drawable.ic_videocam);
                isVideoMuted = false;
            } else if (message.equalsIgnoreCase("Video Off")) {
                video.setImageResource(R.drawable.ic_videocam_off);
                isVideoMuted = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReceivedData(JSONObject jsonObject) {
        //received when chat data received at room level
    }

    @Override
    public void onRemoteStreamAudioMute(JSONObject jsonObject) {
        //received when any remote stream mute audio
    }

    @Override
    public void onRemoteStreamAudioUnMute(JSONObject jsonObject) {
        //received when any remote stream unmute audio
    }

    @Override
    public void onRemoteStreamVideoMute(JSONObject jsonObject) {
        //received when any remote stream mute video
    }

    @Override
    public void onRemoteStreamVideoUnMute(JSONObject jsonObject) {
        //received when any remote stream unmute audio
    }

    @Override
    public void onClick(View view) {
        int id= view.getId();

            if(id== R.id.disconnect) {
                roomDisconnect();
            }
            else if(id== R.id.mute) {
                if (localStream != null) {
                    if (!isAudioMuted) {
                        localStream.muteSelfAudio(true);
                    } else {
                        localStream.muteSelfAudio(false);
                    }
                }
            }
            else if(id== R.id.video) {
                if (localStream != null) {
                    if (!isVideoMuted) {
                        localStream.muteSelfVideo(true);
                    } else {
                        localStream.muteSelfVideo(false);
                    }
                }
            }
            else if(id==R.id.camera) {
                if (localStream != null) {
                    if (!isVideoMuted) {
                        if (isFrontCamera) {
                            localStream.switchCamera();
                            camera.setImageResource(R.drawable.rear_camera);
                            isFrontCamera = false;
                        } else {
                            localStream.switchCamera();
                            camera.setImageResource(R.drawable.front_camera);
                            isFrontCamera = true;
                        }
                    }
                }
            }
            else if(id== R.id.volume){
                if (enxRooms != null) {
                    showRadioButtonDialog();
                }

        }
    }

    private void showPermissionDialog(boolean isRedirectToSetting){
        try{
            AlertDialog.Builder builder = new AlertDialog.Builder(VideoConferenceActivity.this);
            builder.setTitle("Permission Needed");
            builder.setCancelable(false);
            String textPositiveButton = null;
            String title = null;
            if(isRedirectToSetting)
                textPositiveButton = "Open Setting";
            else
                textPositiveButton = "Continue";
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.S){
                if(!isRedirectToSetting){
                    title = "Allow Enablex to access File Storage and BlueTooth of your device. Please tap on Continue to proceed further and allow permission.";
                }
                else if(ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED){
                    title = "Allow Enablex to access  BlueTooth of your device. Tap Open Setting->Permissions, and turn  BlueTooth on.";
                }
                else if(ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED){
                    title = "Allow Enablex to access BlueTooth of your device. Tap Open Setting->Permissions, and turn BlueTooth on.";
                }

            }else{
                if(!isRedirectToSetting){
                    title = "Allow Enablex to access File Storage of your device.Please tap on Continue to proceed further and allow permission.";
                }

            }

            builder.setMessage(title);
            builder.setPositiveButton(textPositiveButton, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if(!isRedirectToSetting)
                        ActivityCompat.requestPermissions(VideoConferenceActivity.this, PERMISSIONS, PERMISSION_REQUEST_CODE);
                    else
                    {
                        isRequestedForSettingPage = true;
                        Utility.goToPermissionPage(VideoConferenceActivity.this);

                    }
                    dialogInterface.dismiss();
                }
            });
            builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    initialize();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }catch(Exception exception){
            exception.printStackTrace();
        }
    }
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    editor.putBoolean("isAppFirst", true);
                    editor.commit();
                    initialize();

                }

                break;
                //if use media permission
            case REQUEST_CODE_MEDIA_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Media permissions granted

                } else {
                    // Handle permission denial for media
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onBackPressed() {
//            super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (enxRooms != null) {
            enxRooms.stopVideoTracksOnApplicationBackground(true, true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (enxRooms != null) {
            enxRooms.startVideoTracksOnApplicationForeground(true, true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (enxPlayerViewRemote != null) {
            enxPlayerViewRemote.release();
        }
        if (enxPlayerView != null) {
            enxPlayerView.release();
        }
        if (localStream != null) {
            localStream.detachRenderer();
        }
        if (enxRooms != null) {
            enxRooms = null;
        }
        if (enxRtc != null) {
            enxRtc = null;
        }
    }

    private void initialize() {
        setUI();
        setClickListener();
        gson = new Gson();
        getSupportActionBar().setTitle("QuickApp");
        enxRtc = new EnxRtc(this, this, this);
        localStream = enxRtc.joinRoom(token, getLocalStreamJsonObject(), getReconnectInfo(), new JSONArray());
        progressDialog = new ProgressDialog(this);
    }

    private void setClickListener() {
        disconnect.setOnClickListener(this);
        mute.setOnClickListener(this);
        video.setOnClickListener(this);
        camera.setOnClickListener(this);
        volume.setOnClickListener(this);
        moderator.setOnTouchListener(new OnDragTouchListener(moderator));
    }

    private void setUI() {
        moderator = (FrameLayout) findViewById(R.id.moderator);
        participant = (FrameLayout) findViewById(R.id.participant);
        disconnect = (ImageView) findViewById(R.id.disconnect);
        mute = (ImageView) findViewById(R.id.mute);
        video = (ImageView) findViewById(R.id.video);
        camera = (ImageView) findViewById(R.id.camera);
        volume = (ImageView) findViewById(R.id.volume);
        rl = (RelativeLayout) findViewById(R.id.rl);
    }

    private JSONObject getLocalStreamJsonObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("audio", true);
            jsonObject.put("video", true);
            jsonObject.put("data", false);
            JSONObject videoSize = new JSONObject();
            videoSize.put("minWidth", 320);
            videoSize.put("minHeight", 180);
            videoSize.put("maxWidth", 1280);
            videoSize.put("maxHeight", 720);
            jsonObject.put("videoSize", videoSize);
            jsonObject.put("audioMuted", false);
            jsonObject.put("videoMuted", false);
            jsonObject.put("name", name);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private void getPreviousIntent() {
        if (getIntent() != null) {
            token = getIntent().getStringExtra("token");
            name = getIntent().getStringExtra("name");
        }
    }

    public JSONObject getReconnectInfo() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("allow_reconnect", true);
            jsonObject.put("number_of_attempts", 3);
            jsonObject.put("timeout_interval", 15);
            jsonObject.put("activeviews", "view");//view

            JSONObject object = new JSONObject();
            object.put("audiomute", true);
            object.put("videomute", true);
            object.put("bandwidth", true);
            object.put("screenshot", true);
            object.put("avatar", true);

            object.put("iconColor", getResources().getColor(R.color.colorPrimary));
            object.put("iconHeight", 30);
            object.put("iconWidth", 30);
            object.put("avatarHeight", 200);
            object.put("avatarWidth", 200);
            jsonObject.put("playerConfiguration", object);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void showRadioButtonDialog() {
        final Dialog dialog = new Dialog(VideoConferenceActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.radiogroup);
        List<String> stringList = new ArrayList<>();  // here is list

        List<String> deviceList = enxRooms.getDevices();
        for (int i = 0; i < deviceList.size(); i++) {
            stringList.add(deviceList.get(i));
        }
        RadioGroup rg = (RadioGroup) dialog.findViewById(R.id.radio_group);
        String selectedDevice = enxRooms.getSelectedDevice();
        if (selectedDevice != null) {
            for (int i = 0; i < stringList.size(); i++) {
                RadioButton rb = new RadioButton(VideoConferenceActivity.this); // dynamically creating RadioButton and adding to RadioGroup.
                rb.setText(stringList.get(i));
                rg.addView(rb);
                if (selectedDevice.equalsIgnoreCase(stringList.get(i))) {
                    rb.setChecked(true);
                }

            }
            dialog.show();
        }

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int childCount = group.getChildCount();
                for (int x = 0; x < childCount; x++) {
                    RadioButton btn = (RadioButton) group.getChildAt(x);
                    if (btn.getId() == checkedId) {
                        enxRooms.switchMediaDevice(btn.getText().toString());
                        dialog.dismiss();
                    }
                }
            }
        });
    }

    private void roomDisconnect() {
        if (enxRooms != null) {
            if (enxPlayerView != null) {
                enxPlayerView.release();
                enxPlayerView = null;
            }
            if (enxPlayerViewRemote != null) {
                enxPlayerViewRemote.release();
                enxPlayerViewRemote = null;
            }
            enxRooms.disconnect();
        } else {
            this.finish();
        }
    }

    @Override
    public void onReconnect(String message) {
        // received when room tries to reconnect due to low bandwidth or any connection interruption
        try {
            if (message.equalsIgnoreCase("Reconnecting")) {
                progressDialog.setMessage("Wait, Reconnecting");
                progressDialog.show();
            } else {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUserReconnectSuccess(EnxRoom enxRoom, JSONObject jsonObject) {
        // received when reconnect successfully completed
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        Toast.makeText(this, "Reconnect Success", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAckPinUsers(JSONObject jsonObject) {

    }

    @Override
    public void onAckUnpinUsers(JSONObject jsonObject) {

    }

    @Override
    public void onPinnedUsers(JSONObject jsonObject) {

    }

    @Override
    public void onRoomAwaited(EnxRoom enxRoom, JSONObject jsonObject) {

    }

    @Override
    public void onUserAwaited(JSONObject jsonObject) {

    }

    @Override
    public void onAckForApproveAwaitedUser(JSONObject jsonObject) {

    }

    @Override
    public void onAckForDenyAwaitedUser(JSONObject jsonObject) {

    }

    @Override
    public void onAckAddSpotlightUsers(JSONObject jsonObject) {

    }

    @Override
    public void onAckRemoveSpotlightUsers(JSONObject jsonObject) {

    }

    @Override
    public void onUpdateSpotlightUsers(JSONObject jsonObject) {

    }

    @Override
    public void onRoomBandwidthAlert(JSONObject jsonObject) {

    }

    @Override
    public void onStopAllSharingACK(JSONObject jsonObject) {

    }


    @Override
    public void onActiveTalkerView(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        if (recyclerView == null) {
            participant.removeAllViews();
            //dummyText.setVisibility(View.VISIBLE);

        } else {
            //dummyText.setVisibility(View.GONE);
            participant.removeAllViews();
            participant.addView(recyclerView);

        }

        if (touch) {
            return;
        }
        if (mRecyclerView != null) {
            touch = true;
            mRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
                @Override
                public boolean onInterceptTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {
                    if (motionEvent.getAction() == 1) {
                        //handleTouchListner();
                    }
                    return false;
                }

                @Override
                public void onTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {

                }

                @Override
                public void onRequestDisallowInterceptTouchEvent(boolean b) {

                }
            });
        }

    }

    @Override
    public void onActiveTalkerView(RecyclerView recyclerView,EnxRoom enxRoom){

    }

    @Override
    public void onAvailable(Integer integer) {

    }
}
