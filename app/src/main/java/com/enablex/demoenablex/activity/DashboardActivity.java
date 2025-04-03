package com.enablex.demoenablex.activity;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.core.app.ActivityCompat;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.enablex.demoenablex.ApplicationController;
import com.enablex.demoenablex.R;
import com.enablex.demoenablex.utilities.Utility;
import com.enablex.demoenablex.web_communication.WebCall;
import com.enablex.demoenablex.web_communication.WebConstants;
import com.enablex.demoenablex.web_communication.WebResponse;

import org.json.JSONException;
import org.json.JSONObject;

public class DashboardActivity extends AppCompatActivity implements View.OnClickListener, WebResponse {
    private EditText name;
    private EditText roomId;
    private Button joinRoom;
    private Button createRoom;
    private String token;
    private SharedPreferences sharedPreferences;
    private String room_Id;
    String[] PERMISSIONS = {
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO
    };
    int PERMISSION_ALL = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);
        getSupportActionBar().setTitle("QuickApp");
        sharedPreferences = ApplicationController.getSharedPrefs();

        setView();
        setClickListener();
        getSupportActionBar().setTitle("Quick App");
        setSharedPreference();
    }

    @Override
    public void onClick(View view) {
        int id=view.getId();

            if(id== R.id.createRoom) {

                new WebCall(this, this, null, WebConstants.getRoomId, WebConstants.getRoomIdCode, false).execute();

            }
            else if(id== R.id.joinRoom){
                room_Id = roomId.getText().toString();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!new Utility().hasPermissions(this, PERMISSIONS)) {
                        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
                    } else {
                        if (validations()) {
                            validateRoomIDWebCall();
                        }
                    }
                }


        }
    }

    private boolean validations() {
        if (name.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please Enter name", Toast.LENGTH_SHORT).show();
            return false;
        } else if (roomId.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please create Room Id.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void validateRoomIDWebCall() {
        new WebCall(this, this, null, WebConstants.validateRoomId + room_Id, WebConstants.validateRoomIdCode, true).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actions, menu);
        if (menu instanceof MenuBuilder) {
            MenuBuilder menuBuilder = (MenuBuilder) menu;
//            menuBuilder.setOptionalIconsVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id= item.getItemId();
            if(id== R.id.action_share){
                if (!roomId.getText().toString().equalsIgnoreCase("")) {
                    String shareBody = "Hi,\n" + name.getText().toString() + " has invited you to join room with Room Id " + roomId.getText().toString();
                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                    startActivity(sharingIntent);
                } else {
                    Toast.makeText(this, "Please create Room first.", Toast.LENGTH_SHORT).show();
                }

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onWebResponse(String response, int callCode) {
        switch (callCode) {
            case WebConstants.getRoomIdCode:
                onGetRoomIdSuccess(response);
                break;
            case WebConstants.getTokenURLCode:
                onGetTokenSuccess(response);
                break;
            case WebConstants.validateRoomIdCode:
                onVaidateRoomIdSuccess(response);
                break;
        }

    }

    private void onVaidateRoomIdSuccess(String response) {
        Log.e("responsevalidate", response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.optString("result").trim().equalsIgnoreCase("40001")) {
                Toast.makeText(this, jsonObject.optString("error"), Toast.LENGTH_SHORT).show();
            } else {
                savePreferences();
                getRoomTokenWebCall();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void onGetTokenSuccess(String response) {
        Log.e("responseToken", response);

        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.optString("result").equalsIgnoreCase("0")) {
                token = jsonObject.optString("token");
                Log.e("token", token);
                Intent intent = new Intent(DashboardActivity.this, VideoConferenceActivity.class);
                intent.putExtra("token", token);
                intent.putExtra("name", name.getText().toString());
                startActivity(intent);
            } else {
                Toast.makeText(this, jsonObject.optString("error"), Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void onGetRoomIdSuccess(String response) {
        Log.e("responseDashboard", response);

        try {
            JSONObject jsonObject = new JSONObject(response);
            room_Id = jsonObject.optJSONObject("room").optString("room_id");
        } catch (JSONException e) {

            e.printStackTrace();
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                roomId.setText(room_Id);
            }
        });
    }

    @Override
    public void onWebResponseError(String error, int callCode) {
        Log.e("errorDashboard", error);
    }

    private void setSharedPreference() {
        if (sharedPreferences != null) {
            if (!sharedPreferences.getString("name", "").isEmpty()) {
                name.setText(sharedPreferences.getString("name", ""));
            }
            if (!sharedPreferences.getString("room_id", "").isEmpty()) {
                roomId.setText(sharedPreferences.getString("room_id", ""));
            }
        }
    }

    private void setClickListener() {
        createRoom.setOnClickListener(this);
        joinRoom.setOnClickListener(this);
    }

    private void setView() {
        name = (EditText) findViewById(R.id.name);
        roomId = (EditText) findViewById(R.id.roomId);
        createRoom = (Button) findViewById(R.id.createRoom);
        joinRoom = (Button) findViewById(R.id.joinRoom);
    }

    private JSONObject jsonObjectToSend() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", "Test Dev Room");
            jsonObject.put("settings", getSettingsObject());
            jsonObject.put("data", getDataObject());
            jsonObject.put("sip", getSIPObject());
            jsonObject.put("owner_ref", "fadaADADAAee");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private JSONObject getSIPObject() {
        JSONObject jsonObject = new JSONObject();
        return jsonObject;
    }

    private JSONObject getDataObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", name.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private JSONObject getSettingsObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("description", "Testing");
            jsonObject.put("scheduled", false);
            jsonObject.put("scheduled_time", "");
            jsonObject.put("duration", 50);
            jsonObject.put("participants", 10);
            jsonObject.put("billing_code", 1234);
            jsonObject.put("auto_recording", false);
            jsonObject.put("active_talker", true);
            jsonObject.put("quality", "HD");
            jsonObject.put("wait_moderator", false);
            jsonObject.put("adhoc", false);
            jsonObject.put("mode", "group");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private void getRoomTokenWebCall() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", name.getText().toString());
            jsonObject.put("role", "participant");
            jsonObject.put("user_ref", "2236");
            jsonObject.put("roomId", room_Id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (!name.getText().toString().isEmpty() && !roomId.getText().toString().isEmpty()) {
            new WebCall(this, this, jsonObject, WebConstants.getTokenURL, WebConstants.getTokenURLCode, false).execute();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        ) {
                    if (validations()) {
                        validateRoomIDWebCall();
                    }
                } else {
                    Toast.makeText(this, "Please enable permissions to further proceed.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    private void savePreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", name.getText().toString());
        editor.putString("room_id", room_Id);
        editor.commit();

    }
}
