package com.likapalab.mehmet.publicchat;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.github.nkzawa.socketio.client.IO;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;


import com.google.android.gms.ads.*;
/**
 * Created by Mehmet on 26.09.2015.
 */
public class MessageActivity extends Activity {

    AdView adView;

    private Socket mSocket;
    private MediaPlayer mediaPlayer;
    private ActionBar actionBar;

    int numberOfPerson = 0,myColor;
    String myUsername,myLocationName;
    boolean isAnimation = false;
    ListView messagesListView;
    TextView numberTv,tv1;
    EditText message;
    Button sendButton,backButton;
    ToggleButton soundButton;
    MessageAdapter messageAdapter;

    private final String chatServer = "http://45.55.193.128";//"http://ec2-52-10-206-171.us-west-2.compute.amazonaws.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        setBackground();

        //Banner reklamı tanımlamaları
        adView = (AdView)findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        mediaPlayer = MediaPlayer.create(this, R.raw.newmessage);

        actionBar = getActionBar();
        actionBar.setCustomView(R.layout.actionbar_view);
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.actionBarBackColor)));

        numberTv = (TextView)actionBar.getCustomView().findViewById(R.id.numberText);
        tv1 = (TextView)actionBar.getCustomView().findViewById(R.id.tvText1);

        soundButton = (ToggleButton)actionBar.getCustomView().findViewById(R.id.sound_button);
        soundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (soundButton.isChecked()) {
                    Toast.makeText(MessageActivity.this, getResources().getString(R.string.sound_closed), Toast.LENGTH_SHORT).show();
                    soundButton.setBackgroundResource(R.drawable.mute);
                    mediaPlayer.setVolume(0, 0);
                } else {
                    Toast.makeText(MessageActivity.this, getResources().getString(R.string.sound_opened), Toast.LENGTH_SHORT).show();
                    soundButton.setBackgroundResource(R.drawable.sound);
                    mediaPlayer.setVolume(1, 1);
                }
            }
        });

        backButton = (Button)actionBar.getCustomView().findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*final AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity.this,AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                builder.setCancelable(false);
                builder.setTitle(getResources().getString(R.string.attention));
                builder.setMessage(getResources().getString(R.string.go_new_location_message));
                builder.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(getApplicationContext(), PlaceActivity.class);
                        i.putExtra("username", myUsername);
                        startActivity(i);
                        Message.clearMessageList();
                        MessageActivity.this.finish();
                        overridePendingTransition(R.anim.anim_right_in,R.anim.anim_right_out);
                    }
                });
                builder.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();*/
                Intent i = new Intent(getApplicationContext(), PlaceActivity.class);
                i.putExtra("username", myUsername);
                startActivity(i);
                Message.clearMessageList();
                MessageActivity.this.finish();
                overridePendingTransition(R.anim.anim_right_in, R.anim.anim_right_out);
            }
        });

        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        myUsername = this.getIntent().getStringExtra("username");
        myLocationName = this.getIntent().getStringExtra("location");
        refreshTv(myLocationName, "");

        Random rnd = new Random();
        myColor = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));

        messagesListView = (ListView)findViewById(R.id.messagesList);
        message = (EditText)findViewById(R.id.messageText);
        sendButton = (Button)findViewById(R.id.sendButton);


        messageAdapter = new MessageAdapter(getApplicationContext(),R.layout.messages_item_template,Message.getMessages());
        messagesListView.setAdapter(messageAdapter);

        //Socket Bağlantısı burada sağlandı
        try{
            mSocket = IO.socket(chatServer);
            mSocket.connect();
        }catch (URISyntaxException e){
            e.printStackTrace();
        }


        mSocket.on(Socket.EVENT_CONNECT_ERROR,onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT,onConnectError);
        mSocket.on("who are you",iAm);
        mSocket.on("number of person",setPersonsNumber);
        mSocket.on("new message",onNewMessage);
        mSocket.on("user left",onUserLeft);
        mSocket.on("user joined",onUserJoined);
        //mSocket.on("");

        message.addTextChangedListener(new TextWatcher() {
            boolean onChanged = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().trim().isEmpty() && onChanged){
                    //Send button is passive
                    getButtonAnimation(R.drawable.send_button_passive);
                    sendButton.setEnabled(false);
                    onChanged = false;
                }
                else if(!s.toString().trim().isEmpty() && !onChanged){
                    //Send button is active
                    getButtonAnimation(R.drawable.send_button);
                    sendButton.setEnabled(true);
                    onChanged = true;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String message1 = message.getText().toString().trim();

                String currentDateTime = new SimpleDateFormat("HH:mm").format(new Date());
                JSONObject obj = new JSONObject();
                try {
                    obj.put("message", message1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mSocket.emit("new message", obj);
                message.setText("");
                Message message_item = new Message(myUsername, message1, currentDateTime, myColor, true);
                Message.addList(message_item);
                messageAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        mSocket.disconnect();
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT,onConnectError);
        mSocket.off("who are you",iAm);
        mSocket.off("number of person",setPersonsNumber);
        mSocket.off("new message", onNewMessage);
        mSocket.off("user left",onUserLeft);
        mSocket.off("user joined",onUserJoined);
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        setBackground();
    }

    public void setBackground(){
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            getWindow().setBackgroundDrawableResource(R.drawable.wallpaper);
        }
        else {
            getWindow().setBackgroundDrawableResource(R.drawable.wallpaper_land);
        }
    }

    @Override
    public void onBackPressed() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity.this,AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        builder.setCancelable(false);
        builder.setTitle(getResources().getString(R.string.exit_title));
        builder.setMessage(getResources().getString(R.string.exit_message));
        builder.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //tvAnimation(getResources().getString(R.string.no_exit_message),"",3000);
                Action.clearActions();
                Action action = new Action(getResources().getString(R.string.no_exit_message),"",3000);
                Action.addAction(action);
                if(!isAnimation){
                    animationControl();
                }
            }
        });

        builder.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onDestroy();
                MessageActivity.this.finish();
                Message.clearMessageList();
            }
        });
        builder.show();
    }


    private Emitter.Listener onConnectError = new Emitter.Listener(){

        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onDestroy();
                    AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                    builder.setCancelable(false);
                    builder.setTitle(getResources().getString(R.string.socket_error_title));
                    builder.setMessage(getResources().getString(R.string.socket_error_message));

                    builder.setPositiveButton(getResources().getString(R.string.okey), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent i = new Intent(getApplicationContext(), MainActivity.class);
                            i.putExtra("username", myUsername);
                            startActivity(i);
                            Message.clearMessageList();
                            MessageActivity.this.finish();
                            overridePendingTransition(R.anim.anim_right_in,R.anim.anim_right_out);
                        }
                    });
                    builder.show();
                }
            });
        }
    };

    private Emitter.Listener iAm = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

            JSONObject obj = new JSONObject();
            try {
                obj.put("username", myUsername);
                obj.put("usercolor", myColor);
                if(myLocationName.equals(getResources().getString(R.string.place_world)))
                    obj.put("location", "World");
                else
                    obj.put("location", myLocationName);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mSocket.emit("check in", obj);
        }
    };

    private Emitter.Listener setPersonsNumber = new Emitter.Listener(){

        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            try{
                numberOfPerson = data.getInt("numberofPerson");
            }catch (JSONException e){
                e.printStackTrace();
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    numberTv.setText("" + numberOfPerson);
                }
            });
        }
    };

    private  Emitter.Listener onNewMessage = new Emitter.Listener(){

        @Override
        public void call(Object... args) {
            String messageTime = new SimpleDateFormat("HH:mm").format(new Date());
            JSONObject data = (JSONObject) args[0];
            String message = "";
            String username = "";
            int userColor;
            try{
                message = data.getString("message");
                username = data.getString("username");
                userColor = data.getInt("usercolor");
                Message message_item = new Message(username,message,messageTime,userColor,false);
                Message.addList(message_item);
            } catch (Exception e){
                e.printStackTrace();
            }
            mediaPlayer.start();
            //Ekranı yinele(Mesajları yinele)
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    messageAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    private Emitter.Listener onUserLeft = new Emitter.Listener(){

        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            String username = "";
            try{
                username = data.getString("username");
            }catch (Exception e){
                e.printStackTrace();
            }
            numberOfPerson--;
            //tvAnimation(username,getResources().getString(R.string.left),2000);
            Action action = new Action(username,getResources().getString(R.string.left),2000);
            Action.addAction(action);
            if(!isAnimation){
                animationControl();
            }
        }
    };

    private Emitter.Listener onUserJoined = new Emitter.Listener(){

        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            String username = "";
            try{
                username = data.getString("username");
            }catch (Exception e){
                e.printStackTrace();
            }
            numberOfPerson++;
            //tvAnimation(username,getResources().getString(R.string.joined),2000);
            Action action = new Action(username,getResources().getString(R.string.joined),2000);
            Action.addAction(action);
            if(!isAnimation){
                animationControl();
            }
        }
    };

    /*private void tvAnimation(final String username, final String operation, final int duration){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                refreshTv(username, operation);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshTv(myLocationName, "");
                    }
                }, duration);
            }
        });
    }*/

    public void refreshTv(final String username, final String operation) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cleanTv();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (operation.equals(getResources().getString(R.string.left)))
                            tv1.setTextColor(getResources().getColor(R.color.red));
                        else if (operation.equals(getResources().getString(R.string.joined)))
                            tv1.setTextColor(getResources().getColor(R.color.green2));
                        else
                            tv1.setTextColor(Color.WHITE);
                        numberTv.setText("" + numberOfPerson);
                        tv1.setText(username + " " + operation);

                        TranslateAnimation moveDowntoUp = new TranslateAnimation(0, 0, 120, 0);
                        moveDowntoUp.setDuration(500);
                        moveDowntoUp.setFillAfter(true);
                        tv1.startAnimation(moveDowntoUp);
                    }
                }, 500);
            }
        });
    }

    public void cleanTv(){
        TranslateAnimation cleanAnim = new TranslateAnimation(0, 0, 0, -120);
        cleanAnim.setDuration(500);
        cleanAnim.setFillAfter(true);
        tv1.startAnimation(cleanAnim);
    }

    private void getButtonAnimation(final int resource){

        AlphaAnimation anim = new AlphaAnimation(1,0);
        anim.setDuration(200);
        sendButton.startAnimation(anim);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                sendButton.setBackgroundResource(resource);
                AlphaAnimation anim = new AlphaAnimation(0, 1);
                anim.setDuration(200);
                sendButton.startAnimation(anim);
            }
        }, 200);
    }

    private void animationControl() {
        isAnimation = true;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Action action = Action.getAction();

                refreshTv(action.getUsername(), action.getOperation());

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (Action.actions.size() > 0) {
                            animationControl();
                        } else {
                            refreshTv(myLocationName, "");
                            Handler handler1 = new Handler();
                            handler1.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    isAnimation = false;
                                }
                            }, 500);
                        }
                    }
                }, action.getDuration());
            }
        });
    }
}
