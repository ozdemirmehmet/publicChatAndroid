package com.likapalab.mehmet.publicchat;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;


import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.Locale;


public class MainActivity extends Activity {

    protected static Tracker mTracker;
    private InterstitialAd interstitialAd;
    EditText usernameText;
    Button connectButton,refreshButton;
    String username="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setBackgroundDrawableResource(R.drawable.splash_screen_background);

        RelativeLayout a = (RelativeLayout) findViewById(R.id.relev);

        AlphaAnimation animation = new AlphaAnimation(0, 1);
        animation.setDuration(1000);
        a.startAnimation(animation);

        ActionBar actionBar = getActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.actionBarBackColor)));

        //Locale locale = Locale.getDefault();
        //String displayLanguage = this.getIntent().getStringExtra("language"); //locale.getDisplayLanguage();

        /*if(!displayLanguage.equals("Türkçe")){
            Locale newLocale = new Locale("en");  //locale en yaptık. Artık değişkenler values-en paketinden alınacak
            Locale.setDefault(newLocale);
            Configuration config = new Configuration();
            config.locale = newLocale;
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());
        }*/

        if(isNetworkConnected()) {
            interstitialAd = new InterstitialAd(this);
            interstitialAd.setAdUnitId("ca-app-pub-6922992252721837/7588048906");
            interstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                    changeIntent();
                }

                @Override
                public void onAdFailedToLoad(int errorCode) {
                    super.onAdFailedToLoad(errorCode);
                }

                @Override
                public void onAdLeftApplication() {
                    super.onAdLeftApplication();
                }

                @Override
                public void onAdOpened() {
                    super.onAdOpened();
                }

                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                }
            });

            loadAd();

            AnalyticsApplication application = (AnalyticsApplication) getApplication();
            mTracker = application.getDefaultTracker();

            mTracker.setScreenName("MainActivity");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());

            usernameText = (EditText) findViewById(R.id.usernameText);
            connectButton = (Button) findViewById(R.id.connectButton);

            if ((this.getIntent().getStringExtra("username")) != null) {
                usernameText.setText(this.getIntent().getStringExtra("username"));
                connectButton.setEnabled(true);
            }

            usernameText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.toString().trim().isEmpty()) {
                        connectButton.setEnabled(false);
                    } else {
                        connectButton.setEnabled(true);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            connectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    username = usernameText.getText().toString().replaceAll(" ", "");

                    if (username.length() > 20) {
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.long_username_error), Toast.LENGTH_LONG).show();
                    }
                    else {
                        if (interstitialAd.isLoaded())
                            interstitialAd.show();
                        else {
                            changeIntent();
                        }
                    }
                }
            });
        }
        else{
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.no_internet_connection_error),Toast.LENGTH_LONG).show();
            refreshButton = (Button)findViewById(R.id.refresh_app_button);
            refreshButton.setVisibility(View.VISIBLE);
            refreshButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(getApplicationContext(),SplashScreen.class);
                    startActivity(i);
                    MainActivity.this.finish();
                }
            });
        }
    }

    public void changeIntent(){
        Intent i = new Intent(getApplicationContext(), PlaceActivity.class);
        i.putExtra("username", username);
        startActivity(i);
        MainActivity.this.finish();
        overridePendingTransition(R.anim.anim_left_in, R.anim.anim_left_out);
    }

    public void loadAd(){
        AdRequest adRequest = new AdRequest.Builder()//.build();
                .addTestDevice("FBD77822C26796509677AAAE70BE0E99")
.build();
        interstitialAd.loadAd(adRequest);
    }

    private boolean isNetworkConnected(){
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        return (cm.getActiveNetworkInfo() != null);
    }

}
