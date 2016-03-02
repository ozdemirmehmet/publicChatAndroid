package com.likapalab.mehmet.publicchat.gcm;

import java.io.IOException;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.likapalab.mehmet.publicchat.HttpRequestClass;
import com.likapalab.mehmet.publicchat.MainActivity;
import com.likapalab.mehmet.publicchat.SplashScreen;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.provider.Settings.Secure;
import android.view.animation.Animation;

public class RegisterApp extends AsyncTask<Void, Void, String> {

    SplashScreen a;
    Context ctx;
    GoogleCloudMessaging gcm;//Google Cloud referansı
    final String PROJECT_ID = "57822086143";
    String regid = null, language,androidId;
    private int appVersion;
    Animation animation;
    HttpRequestClass httpRequestClass;

    public RegisterApp(SplashScreen a, Animation animation, String language, String androidId, Context ctx, GoogleCloudMessaging gcm, int appVersion) { //SplashScreen den gelen değerleri aldık
        this.ctx = ctx;
        this.gcm = gcm;
        this.appVersion = appVersion;
        this.a = a;
        this.language = language;
        this.androidId = androidId;
        this.animation = animation;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected String doInBackground(Void... arg0) {
        String msg = "";

        try {
            if (gcm == null) {
                gcm = GoogleCloudMessaging.getInstance(ctx);//GCM objesi oluşturduk ve gcm referansına başladık
            }
            regid = gcm.register(PROJECT_ID);//gcm objesine PROJECT_ID mizi göndererek regid değerimizi aldık.Bu değerimizi hem sunucularımıza göndereceğiz Hemde Androidde saklıyacağız
            msg = "Registration ID=" + regid;

            saveRegistrationIdToBackend();//regId hem sunuculara gönderilecek hemde shared prefs ile cihaza kayıtlanacak

        } catch (IOException ex) {
            msg = "Error :" + ex.getMessage();

        }
        return msg;
    }

    private void storeRegistrationId(Context ctx, String regid) {//Androidde regid ve appversion saklı tutacak method
        //Burada SharedPreferences kullanarak kayıt yapmaktadır
        final SharedPreferences prefs = ctx.getSharedPreferences(SplashScreen.class.getSimpleName(),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("registration_id", regid);
        editor.putInt("appVersion", appVersion);
        editor.commit();
    }


    private void saveRegistrationIdToBackend() {//Sunucuya regid değerini gönderecek method
        //Post metodu ile sunucuya istekte bulunduk.
        httpRequestClass = new HttpRequestClass();

        String url = "http://publicchat.netne.net/register.php";/*"http://192.168.43.103:8080/Public%20Chat%20GCM/register.php";*/
        String parameters = "regId="+regid+"&androidId="+androidId+"&language="+language+"&appVersion="+appVersion;

        String response = httpRequestClass.httpRequest(url, "POST", parameters, 2000);
        if(response != null){
            response = response.substring(0,2);
        }
        if(response != null && response.equals("OK")){
            storeRegistrationId(ctx, regid);//Androidde regid saklı tutacak method
        }
    }


    @Override
    protected void onPostExecute(String result) {
        //doInBackground işlemi bittikten sonra çalışır
        super.onPostExecute(result);
        animation.cancel();
        Intent i = new Intent(ctx, MainActivity.class);//Anasayfaya Yönlendir
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        ctx.startActivity(i);
        a.finish();
    }
}