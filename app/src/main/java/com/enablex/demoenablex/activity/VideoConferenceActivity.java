package com.enablex.demoenablex.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.enablex.demoenablex.R;
import com.enablex.demoenablex.model.UserModel;
import com.enablex.demoenablex.utilities.OnDragTouchListener;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.StatsReport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import enxrtcandroid.Controller.EnxPlayerView;
import enxrtcandroid.Controller.EnxRoom;
import enxrtcandroid.Controller.EnxRoomObserver;
import enxrtcandroid.Controller.EnxRtc;
import enxrtcandroid.Controller.EnxStream;
import enxrtcandroid.Controller.EnxStreamObserver;

public class VideoConferenceActivity extends AppCompatActivity
        implements EnxRoomObserver, EnxStreamObserver, View.OnClickListener
{
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
    int size[] = {200, 200, 200, 200};
    RelativeLayout rl;
    ArrayList<UserModel> userArrayList;
    Gson gson;
    EnxStream localStream;
    EnxPlayerView enxPlayerViewRemote;
    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.RECORD_AUDIO
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_conference);
        getPreviousIntent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (!hasPermissions(this, PERMISSIONS))
            {
                ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
            }
            else
            {
                initialize();
            }
        }
    }

    private void initialize()
    {
        setUI();
        setClickListener();
        userArrayList = new ArrayList<>();
        gson = new Gson();
        getSupportActionBar().setTitle("QuickApp");
        enxRtc = new EnxRtc(this, this, this);
        localStream = enxRtc.joinRoom(token, getLocalStreamJsonObject());
        enxPlayerView = new EnxPlayerView(this, EnxPlayerView.ScalingType.SCALE_ASPECT_BALANCED, true);
        Log.e("localStream", localStream.toString());
        localStream.attachRenderer(enxPlayerView);
        moderator.addView(enxPlayerView);
    }

    private void setClickListener()
    {
        disconnect.setOnClickListener(this);
        mute.setOnClickListener(this);
        video.setOnClickListener(this);
        camera.setOnClickListener(this);
        volume.setOnClickListener(this);
        moderator.setOnTouchListener(new OnDragTouchListener(moderator));
    }

    private void setUI()
    {
        moderator = (FrameLayout) findViewById(R.id.moderator);
        participant = (FrameLayout) findViewById(R.id.participant);
        disconnect = (ImageView) findViewById(R.id.disconnect);
        mute = (ImageView) findViewById(R.id.mute);
        video = (ImageView) findViewById(R.id.video);
        camera = (ImageView) findViewById(R.id.camera);
        volume = (ImageView) findViewById(R.id.volume);
        rl = (RelativeLayout) findViewById(R.id.rl);
    }

    private JSONObject getLocalStreamJsonObject()
    {
        JSONObject jsonObject = new JSONObject();
        try
        {
            jsonObject.put("audio", "true");
            jsonObject.put("video", "true");
            jsonObject.put("data", "false");
            jsonObject.put("maxVideoBW", 400); //2048
            jsonObject.put("minVideoBW", 300);
            JSONObject videoSize = new JSONObject();
            videoSize.put("minWidth", 720);
            videoSize.put("minHeight", 480);
            videoSize.put("maxWidth", 1280);
            videoSize.put("maxHeight", 720);
            jsonObject.put("videoSize", videoSize);
            jsonObject.put("audioMuted", true);
            jsonObject.put("videoMuted", true);
            jsonObject.put("name", name);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private void getPreviousIntent()
    {
        if (getIntent() != null)
        {
            token = getIntent().getStringExtra("token");
            name = getIntent().getStringExtra("name");
        }
    }

    @Override
    public void onRoomConnected(EnxRoom enxRoom, JSONObject jsonObject)
    {
        enxRooms = enxRoom;
        if (enxRoom != null)
        {
            enxRooms.publish();
        }
    }

    @Override
    public void onRoomError(JSONObject jsonObject)
    {
    }

    @Override
    public void onReconnect()
    {
    }

    @Override
    public void onUserConnected(JSONObject jsonObject)
    {
        Log.e("userConnected", jsonObject.toString());
        UserModel userModel = gson.fromJson(jsonObject.toString(), UserModel.class);
        userArrayList.add(userModel);
    }

    @Override
    public void onUserDisConnected(JSONObject jsonObject)
    {
        Log.e("userConnected", jsonObject.toString());
        UserModel userModel = gson.fromJson(jsonObject.toString(), UserModel.class);
        for (UserModel userModel1 : userArrayList)
        {
            if (userModel1.getClientId().equalsIgnoreCase(userModel.getClientId()))
            {
                userArrayList.remove(userModel);
            }
        }
    }

    @Override
    public void onPublishedStream(EnxStream enxStream)
    {
    }

    @Override
    public void onUnPublishedStream(boolean b)
    {
    }

    @Override
    public void onStreamAdded(EnxStream enxStream)
    {
        if (enxStream != null)
        {
            enxRooms.subscribe(enxStream);
        }
    }

    @Override
    public void onSubscribedStream(EnxStream enxStream)
    {
    }

    @Override
    public void onRemoveSubscribedStream(String s)
    {

    }

    @Override
    public void onFloorRequested(JSONObject jsonObject)
    {

    }

    @Override
    public void onFloorRequestRecieved(JSONObject jsonObject)
    {

    }

    @Override
    public void onProcessFloorRequested(JSONObject jsonObject)
    {

    }

    @Override
    public void onGrantedFloorRequest(JSONObject jsonObject)
    {

    }

    @Override
    public void onDeniedFloorRequest(JSONObject jsonObject)
    {

    }

    @Override
    public void onReleasedFloorRequest(JSONObject jsonObject)
    {

    }

    @Override
    public void onLogUploaded(String s)
    {

    }

    @Override
    public void onStartRecordingEvent(JSONObject jsonObject)
    {
    }

    @Override
    public void onRoomRecordingOn(String s)
    {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStopRecordingEvent(JSONObject jsonObject)
    {
    }

    @Override
    public void onRoomRecordingOff(String s)
    {
    }

    @Override
    public void onMutedRoom(JSONObject jsonObject)
    {
    }

    @Override
    public void onRecievedMuteRoom(JSONObject jsonObject)
    {

    }

    @Override
    public void onUnMutedRoom(JSONObject jsonObject)
    {
    }

    @Override
    public void onRecievedUnMutedRoom(JSONObject jsonObject)
    {

    }

    @Override
    public void onRoomDisConnected(JSONObject jsonObject)
    {
        this.finish();
    }

    @Override
    public void onActiveTalkerList(JSONObject jsonObject)
    {
        Log.e("activeList", jsonObject.toString());
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Map<String, EnxStream> map = enxRooms.getRemoteStreams();
                    JSONArray jsonArray = jsonObject.getJSONArray("activeList");
                    if (jsonArray.length() == 0)
                    {
                        View temp = participant.getChildAt(0);
                        participant.removeView(temp);
                        return;
                    }
                    else
                    {
                        JSONObject jsonStreamid = jsonArray.getJSONObject(0);
                        String streamID = jsonStreamid.getString("streamId");
                        EnxStream stream = map.get(streamID);
                        if (enxPlayerViewRemote == null)
                        {
                            enxPlayerViewRemote = new EnxPlayerView(VideoConferenceActivity.this, EnxPlayerView.ScalingType.SCALE_ASPECT_BALANCED, false);
                            stream.attachRenderer(enxPlayerViewRemote);
                            participant.addView(enxPlayerViewRemote);
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onScreenSharedStarted(JSONObject jsonObject)
    {
    }

    @Override
    public void onScreenSharedStopped(JSONObject jsonObject)
    {

    }

    @Override
    public void onSetTalkerCount(JSONObject jsonObject)
    {

    }

    @Override
    public void onGetTalkerCount(JSONObject jsonObject)
    {

    }

    @Override
    public void onMaxTalkerCount(JSONObject jsonObject)
    {

    }

    @Override
    public void onBandWidthUpdated(JSONObject jsonObject)
    {

    }

    @Override
    public void onPeerConnectionStatsReady(StatsReport[] statsReports, int i)
    {

    }

    @Override
    public void onAudioEvent(JSONObject jsonObject)
    {
        try
        {
            if (jsonObject.optString("message").equalsIgnoreCase("success"))
            {
                if (!isAudioMuted)
                {
                    mute.setImageResource(R.drawable.unmute);
                    isAudioMuted = true;
                }
                else
                {
                    mute.setImageResource(R.drawable.mute);
                    isAudioMuted = false;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onVideoEvent(JSONObject jsonObject)
    {
        try
        {
            String message = jsonObject.getString("message");
            if (message.equalsIgnoreCase("Video on"))
            {
                video.setImageResource(R.drawable.ic_videocam);
                isVideoMuted = false;
            }
            else if (message.equalsIgnoreCase("Video off"))
            {
                video.setImageResource(R.drawable.ic_videocam_off);
                isVideoMuted = true;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onReceivedData(JSONObject jsonObject)
    {

    }

    @Override
    public void onRemoteStreamAudioMute(JSONObject jsonObject)
    {

    }

    @Override
    public void onRemoteStreamAudioUnMute(JSONObject jsonObject)
    {

    }

    @Override
    public void onHardMutedAudio(JSONObject jsonObject)
    {

    }

    @Override
    public void onHardUnMutedAudio(JSONObject jsonObject)
    {

    }

    @Override
    public void onRecievedHardMuteAudio(JSONObject jsonObject)
    {

    }

    @Override
    public void onRecievedHardUnMuteAudio(JSONObject jsonObject)
    {

    }

    @Override
    public void onRemoteStreamVideoMute(JSONObject jsonObject)
    {

    }

    @Override
    public void onRemoteStreamVideoUnMute(JSONObject jsonObject)
    {

    }

    @Override
    public void onHardMutedVideo(JSONObject jsonObject)
    {

    }

    @Override
    public void onHardUnMutedVideo(JSONObject jsonObject)
    {

    }

    @Override
    public void onRecievedHardMuteVideo(JSONObject jsonObject)
    {

    }

    @Override
    public void onRecievedHardUnMuteVideo(JSONObject jsonObject)
    {

    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.disconnect:
                if (enxRooms != null)
                {
                    enxRooms.unpublish();
                    enxRooms.disconnect();
                }
                break;
            case R.id.mute:
                if (localStream != null)
                {
                    if (!isAudioMuted)
                    {
                        localStream.muteSelfAudio(false);
                    }
                    else
                    {
                        localStream.muteSelfAudio(true);
                    }
                }
                break;
            case R.id.video:
                if (localStream != null)
                {
                    if (!isVideoMuted)
                    {
                        localStream.muteSelfVideo(false);
                    }
                    else
                    {
                        localStream.muteSelfVideo(true);
                    }
                }
                break;
            case R.id.camera:
                if (localStream != null)
                {
                    if (!isVideoMuted)
                    {
                        if (isFrontCamera)
                        {
                            localStream.switchCamera();
                            camera.setImageResource(R.drawable.rear_camera);
                            isFrontCamera = false;
                        }
                        else
                        {
                            localStream.switchCamera();
                            camera.setImageResource(R.drawable.front_camera);
                            isFrontCamera = true;
                        }
                    }
                }
                break;
            case R.id.volume:
                if (enxRooms != null)
                {
                    showRadioButtonDialog();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED
                        && grantResults[3] == PackageManager.PERMISSION_GRANTED)
                {
                    initialize();
                }
                else
                {
                    Toast.makeText(this, "Please enable permissions to further proceed.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public boolean hasPermissions(Context context, String... permissions)
    {
        if (context != null && permissions != null)
        {
            for (String permission : permissions)
            {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
                {
                    return false;
                }
            }
        }
        return true;
    }

    private void showRadioButtonDialog()
    {
        final Dialog dialog = new Dialog(VideoConferenceActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.radiogroup);
        List<String> stringList = new ArrayList<>();  // here is list

        List<String> deviceList = enxRooms.getDevices();
        for (int i = 0; i < deviceList.size(); i++)
        {
            stringList.add(deviceList.get(i));
        }
        RadioGroup rg = (RadioGroup) dialog.findViewById(R.id.radio_group);
        String selectedDevice = enxRooms.getSelectedDevice();
        if (selectedDevice != null)
        {
            for (int i = 0; i < stringList.size(); i++)
            {
                RadioButton rb = new RadioButton(VideoConferenceActivity.this); // dynamically creating RadioButton and adding to RadioGroup.
                rb.setText(stringList.get(i));
                rg.addView(rb);
                if (selectedDevice.equalsIgnoreCase(stringList.get(i)))
                {
                    rb.setChecked(true);
                }

            }
            dialog.show();
        }

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                int childCount = group.getChildCount();
                for (int x = 0; x < childCount; x++)
                {
                    RadioButton btn = (RadioButton) group.getChildAt(x);
                    if (btn.getId() == checkedId)
                    {
                        Log.e("selected RadioButton->", btn.getText().toString());
                        enxRooms.selectAudioDevice(btn.getText().toString());
                        dialog.dismiss();
                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed()
    {
        if (true)
        {

        }
        else
        {
            super.onBackPressed();
        }
    }

}
