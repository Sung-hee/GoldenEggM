package com.goldeggm.user.goldeggm;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class IntroActivity extends AppCompatActivity {

    private String JSON_STRING;
    static JSONArray jsonArray;
    static JSONObject jsonObject;
    private String hp;

    Intent intent;

    Handler handler = new Handler();
    Runnable r = new Runnable() {
        @SuppressLint("HardwareIds")
        @Override
        public void run() {

            TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

            if (ActivityCompat.checkSelfPermission(IntroActivity.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(IntroActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(IntroActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
                return;
            }

            intent = new Intent(IntroActivity.this, LoginActivity.class);

            if (tm != null) {
                hp = tm.getLine1Number() + tm.getDeviceId();
                intent.putExtra("userHp", hp);
            }

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
        handler.postDelayed(r, 1000); // 1초 뒤에 Runnable 객체 수행
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

}
