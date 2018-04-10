package com.goldeggm.user.goldeggm;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

public class IntroActivity extends AppCompatActivity {

    private String version;
    private String json_string;
    private String JSON_STRING;
    static JSONArray jsonArray;
    static JSONObject jsonObject;
    private String hp;

    //사용하는 함수
    String marketVersion, verSion;
    AlertDialog.Builder mDialog;

    Intent intent;

    Handler handler = new Handler();
    Runnable r = new Runnable() {
        @SuppressLint("HardwareIds")
        @Override
        public void run() {

            intent = new Intent(IntroActivity.this, LoginActivity.class);

            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(IntroActivity.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(IntroActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(IntroActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
                return;
            }
            @SuppressLint("HardwareIds") String tmSerial = telephonyManager != null ? telephonyManager.getSimSerialNumber() : null;
            @SuppressLint("HardwareIds") String tmDeviceId = telephonyManager != null ? telephonyManager.getDeviceId() : null;
            @SuppressLint("HardwareIds") String androidId = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
            if (tmSerial  == null) tmSerial   = "1";
            if (tmDeviceId== null) tmDeviceId = "1";
            if (androidId == null) androidId  = "1";
            UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDeviceId.hashCode() << 32) | tmSerial.hashCode());
            String uniqueId = (telephonyManager != null ? telephonyManager.getLine1Number() : null) + deviceUuid.toString();

            hp = uniqueId;
            intent.putExtra("userHp", hp);

            new ContactUser().execute();
        }
    };

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

    }

    @Override
    protected void onResume() {
        super.onResume();
        // 다시 화면에 들어어왔을 때 예약 걸어주기
//        handler.postDelayed(r, 1000); // 1초 뒤에 Runnable 객체 수행

        mDialog = new AlertDialog.Builder(this);
        new getMarketVersion().execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 화면을 벗어나면, handler 에 예약해놓은 작업을 취소하자
        handler.removeCallbacks(r); // 예약 취소
    }

    class ContactUser extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            String json_url = "http://13.125.147.26/phps/userContact?hp=" + hp;

            try {
                URL url = new URL(json_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();

                while ((JSON_STRING = bufferedReader.readLine()) != null) {
                    stringBuilder.append(JSON_STRING + "\n");
                }

                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();

                return stringBuilder.toString().trim();

            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                Log.v("ContactUser", "success");
                startActivity(intent); // 다음화면으로 넘어가기
                finish(); // Activity 화면 제거
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
    private class getMarketVersion extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            String json_url = "http://13.125.147.26/phps/versionCheck.json";

            try {
                URL url = new URL(json_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();

                while ((JSON_STRING = bufferedReader.readLine()) != null) {
                    stringBuilder.append(JSON_STRING + "\n");
                }

                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();

                return stringBuilder.toString().trim();

            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            json_string = result;

            try {
                jsonArray = new JSONArray(json_string);

                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    version = jsonObject.getString("version");

                    PackageInfo pi = null;

                    try {
                        pi = getPackageManager().getPackageInfo(getPackageName(), 0);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    verSion = pi.versionName;
                    marketVersion = version;

                    if (!verSion.equals(marketVersion)) {
                        mDialog.setMessage("업데이트 후 사용해주세요.")
                                .setCancelable(false)
                                .setPositiveButton("업데이트 바로가기",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog,
                                                                int id) {
                                                Intent marketLaunch = new Intent(
                                                        Intent.ACTION_VIEW);
                                                marketLaunch.setData(Uri
                                                        .parse("https://play.google.com/store/apps/details?id=com.goldeggm.user.goldeggm"));
                                                startActivity(marketLaunch);
                                                finish();
                                            }
                                        });
                        AlertDialog alert = mDialog.create();
                        alert.setTitle("안 내");
                        alert.show();
                    }
                    else {
                        handler.postDelayed(r, 1000); // 1초 뒤에 Runnable 객체 수행
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            super.onPostExecute(result);
        }
    }
}
