package com.likapalab.mehmet.publicchat;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.likapalab.mehmet.publicchat.gcm.RegisterApp;

import java.util.Locale;

/**
 * Created by Mehmet on 19.02.2016.
 */
public class SplashScreen extends Activity {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    GoogleCloudMessaging gcm;
    String regid;
    ImageView a;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        ActionBar actionBar = getActionBar();
        actionBar.hide();

        Locale locale = Locale.getDefault();
        final String displayLanguage = locale.getDisplayLanguage();
        if(!displayLanguage.equals("Türkçe")){
            Locale newLocale = new Locale("en");  //locale en yaptık. Artık değişkenler values-en paketinden alınacak
            Locale.setDefault(newLocale);
            Configuration config = new Configuration();
            config.locale = newLocale;
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());
        }

        a = (ImageView)findViewById(R.id.imageView);

        final Animation logoMoveAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_scale);
        a.startAnimation(logoMoveAnimation);

        if (checkPlayServices()) {//GOOGLE PLAY SERVİCE APK YÜKLÜMÜ
            gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
            regid = getRegistrationId(getApplicationContext()); //registration_id olup olmadığını kontrol ediyoruz

            if(regid.isEmpty()){//YENİ KAYIT
                //regid değerimiz boş gelmişse uygulama ya ilk kez acılıyor yada güncellenmiş demektir.Registration işlemleri tekrardan yapılacak.
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        new RegisterApp(SplashScreen.this, logoMoveAnimation, displayLanguage, getApplicationContext(), gcm, getAppVersion(getApplicationContext())).execute(); //RegisterApp clasını çalıştırıyoruz ve değerleri gönderiyoruz
                    }
                }, 500);
            }else {
                //regid değerimiz boş gelmemişse önceden registration işlemleri tamamlanmış ve güncelleme olmamış demektir.Yani uygulama direk açılacak
                Intent i = new Intent(getApplicationContext(), MainActivity.class);//Anasayfaya Yönlendir
                startActivity(i);
                SplashScreen.this.finish();
            }
        }
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
    }


    private boolean checkPlayServices() {
        //Google Play Servis APK yüklümü

        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),R.string.google_service_error,Toast.LENGTH_LONG).show();
                SplashScreen.this.finish();
            }
            return false;
        }
        return true;
    }

    private String getRegistrationId(Context context) { //registration_id geri döner
        //Bu method registration id ye bakar.
        //Bu uygulamada registration id nin önceden olabilmesi için uygulamanın önceden açılmış ve registration işlemlerini yapmış olması lazım
        //Uygulama önceden acıldıysa registration_id SharedPreferences yardımı ile kaydedilir.

        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");//registration_id değeri alındı
        if (registrationId.isEmpty()) {//eğer boşsa önceden kaydedilmemiş yani uygulama ilk kez çalışıyor.
            return "";
        }

        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(getApplicationContext());//yine SharedPreferences a kaydedilmiş version değerini aldık
        if (registeredVersion != currentVersion) {//versionlar uyuşmuyorsa güncelleme olmuş demektir. Yani tekrardan registration işlemleri yapılcak
            return "";
        }
        return registrationId;
    }

    private SharedPreferences getGCMPreferences(Context context) {
        return getSharedPreferences(SplashScreen.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    private static int getAppVersion(Context context) { //Versiyonu geri döner
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Paket versiyonu bulunamadı: " + e);
        }
    }

}
